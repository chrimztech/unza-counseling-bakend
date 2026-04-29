package zm.unza.counseling.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.AppointmentDto;
import zm.unza.counseling.dto.AppointmentStats;
import zm.unza.counseling.dto.AvailabilitySlot;
import zm.unza.counseling.dto.CreateAppointmentRequest;
import zm.unza.counseling.dto.UpdateAppointmentRequest;
import zm.unza.counseling.dto.request.AssignAppointmentRequest;
import zm.unza.counseling.dto.request.CancelRequest;
import zm.unza.counseling.dto.request.RescheduleRequest;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.Session;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.CaseRepository;
import zm.unza.counseling.repository.SessionRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.AppointmentService;
import zm.unza.counseling.service.AuditLogService;
import zm.unza.counseling.service.ClientIdentityService;
import zm.unza.counseling.service.NotificationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of AppointmentService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final DateTimeFormatter NOTIFICATION_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy 'at' HH:mm");

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final CaseRepository caseRepository;
    private final AuditLogService auditLogService;
    private final ClientIdentityService clientIdentityService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Value("${app.meeting.jitsi-domain:meet.jit.si}")
    private String jitsiDomain;

    @Value("${app.meeting.custom-domain:meet.unza.edu.zm}")
    private String customMeetingDomain;

    @Value("${app.meeting.default-provider:jitsi}")
    private String defaultMeetingProvider;

    @Override
    public Page<AppointmentDto> getAllAppointments(Pageable pageable) {
        return getScopedAppointments(pageable).map(this::toAppointmentDto);
    }

    @Override
    public AppointmentDto getAppointmentById(Long id) {
        return toAppointmentDto(getAppointmentOrThrow(id));
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByClientId(Long clientId, Pageable pageable) {
        User user = userRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("Client not found with id: " + clientId));
        authorizeClientScopedRead(user);
        ensureAppointmentClientUser(user);
        return appointmentRepository.findByStudent(user, pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByCounselorId(Long counselorId, Pageable pageable) {
        User counselor = resolveCounselorUser(counselorId);
        validateCounselorScopedRead(counselor);
        return appointmentRepository.findByCounselor(counselor, pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByStudentId(String studentId, Pageable pageable) {
        User student = findUserByStudentIdentifier(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found with identifier: " + studentId));
        authorizeStudentScopedRead(student);
        ensureAppointmentClientUser(student);
        return appointmentRepository.findByStudent(student, pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByCaseId(Long caseId, Pageable pageable) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found with id: " + caseId));
        authorizeCaseRead(caseEntity);
        return appointmentRepository.findByCaseEntity(caseEntity, pageable).map(this::toAppointmentDto);
    }

    @Override
    public AppointmentDto createAppointment(CreateAppointmentRequest request) {
        User currentUser = resolveCurrentUser();
        User student = resolveStudentForRequest(request, currentUser);
        Client client = resolveClientForUser(student);
        Case caseEntity = resolveCaseForAppointment(request.getCaseId(), client);
        Appointment.AppointmentType appointmentType = resolveAppointmentType(request);
        Appointment.SessionMode sessionMode = resolveCreateSessionMode(request);
        int duration = normalizeDuration(request.getDuration());
        User counselor = resolveCounselorForAppointment(request.getCounselorId(), caseEntity);

        validateAvailability(counselor, request.getAppointmentDate(), duration, null);

        Appointment appointment = new Appointment();
        appointment.setTitle(normalizeRequired(request.getTitle(), "Appointment title is required"));
        appointment.setStudent(student);
        appointment.setClient(client);
        appointment.setCaseEntity(caseEntity);
        appointment.setAppointmentDate(requireAppointmentDate(request.getAppointmentDate()));
        appointment.setType(appointmentType);
        appointment.setDuration(duration);
        appointment.setSessionMode(sessionMode);

        Map<String, Object> bookingDetails = sanitizeMap(request.getBookingDetails());
        appointment.setDescription(resolveAppointmentDescription(request.getDescription(), bookingDetails));
        appointment.setIntakeDataJson(writeJson(bookingDetails));
        appointment.setCounselor(counselor);
        appointment.setStatus(counselor != null
                ? Appointment.AppointmentStatus.SCHEDULED
                : Appointment.AppointmentStatus.UNASSIGNED);
        applyVenueDetails(appointment, sessionMode, request.getMeetingLink(), request.getLocation());

        Appointment savedAppointment = appointmentRepository.save(appointment);
        syncSessionFromAppointment(savedAppointment);
        notifyStudentOnCreate(savedAppointment);
        touchCaseTransition(null, savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_CREATED", savedAppointment, "Appointment created");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public AppointmentDto updateAppointment(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = getAppointmentOrThrow(id);
        assertAppointmentUpdatable(appointment, "update");

        Case previousCase = appointment.getCaseEntity();
        Client client = resolveClientForUser(appointment.getStudent());

        Case targetCase = appointment.getCaseEntity();
        if (request.getCaseId() != null) {
            targetCase = request.getCaseId() > 0
                    ? resolveCaseForAppointment(request.getCaseId(), client)
                    : null;
        }

        User targetCounselor = resolveUpdatedCounselor(appointment, request, targetCase);
        LocalDateTime targetDate = request.getAppointmentDate() != null
                ? requireAppointmentDate(request.getAppointmentDate())
                : appointment.getAppointmentDate();
        int targetDuration = request.getDuration() != null
                ? normalizeDuration(request.getDuration())
                : resolveAppointmentDuration(appointment);

        validateAvailability(targetCounselor, targetDate, targetDuration, appointment.getId());

        if (request.getTitle() != null) {
            appointment.setTitle(normalizeRequired(request.getTitle(), "Appointment title is required"));
        }
        if (request.getDescription() != null) {
            appointment.setDescription(normalize(request.getDescription()));
        }
        appointment.setCaseEntity(targetCase);
        appointment.setCounselor(targetCounselor);
        appointment.setAppointmentDate(targetDate);
        appointment.setDuration(targetDuration);

        Appointment.SessionMode targetSessionMode = request.getSessionMode() != null
                ? resolveUpdateSessionMode(request)
                : appointment.getSessionMode();
        if (targetSessionMode == null) {
            targetSessionMode = Appointment.SessionMode.IN_PERSON;
        }
        appointment.setSessionMode(targetSessionMode);
        applyVenueDetails(appointment, targetSessionMode, request.getMeetingLink(), request.getLocation());

        if (request.getCancellationReason() != null) {
            appointment.setCancellationReason(normalize(request.getCancellationReason()));
        }
        if (request.getBookingDetails() != null && !request.getBookingDetails().isEmpty()) {
            Map<String, Object> bookingDetails = sanitizeMap(request.getBookingDetails());
            appointment.setIntakeDataJson(writeJson(bookingDetails));
            if (appointment.getDescription() == null || appointment.getDescription().isBlank()) {
                appointment.setDescription(resolveAppointmentDescription(appointment.getDescription(), bookingDetails));
            }
        }

        if (targetCounselor == null && appointment.getStatus() == Appointment.AppointmentStatus.UNASSIGNED) {
            appointment.setMeetingLink(null);
            appointment.setMeetingProvider(null);
        }

        if (request.getStatus() != null) {
            applyStatusChange(appointment, request.getStatus(), request.getCancellationReason());
        } else if (targetCounselor == null && appointment.getStatus() != Appointment.AppointmentStatus.CANCELLED) {
            appointment.setStatus(Appointment.AppointmentStatus.UNASSIGNED);
        } else if (targetCounselor != null && appointment.getStatus() == Appointment.AppointmentStatus.UNASSIGNED) {
            appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        }

        if (isActiveAppointmentStatus(appointment.getStatus())) {
            ensureLinkedCaseSupportsActiveAppointment(appointment);
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        syncSessionFromAppointment(savedAppointment);
        touchCaseTransition(previousCase, savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_UPDATED", savedAppointment, "Appointment updated");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public AppointmentDto updateAppointmentStatus(Long id, UpdateAppointmentRequest request) {
        if (request.getStatus() == null) {
            throw new ValidationException("Appointment status is required");
        }

        Appointment appointment = getAppointmentOrThrow(id);
        assertAppointmentUpdatable(appointment, "change status");

        Case previousCase = appointment.getCaseEntity();
        if (request.getAppointmentDate() != null) {
            appointment.setAppointmentDate(requireAppointmentDate(request.getAppointmentDate()));
            validateAvailability(
                    appointment.getCounselor(),
                    appointment.getAppointmentDate(),
                    resolveAppointmentDuration(appointment),
                    appointment.getId()
            );
        }
        if (request.getCancellationReason() != null) {
            appointment.setCancellationReason(normalize(request.getCancellationReason()));
        }

        applyStatusChange(appointment, request.getStatus(), request.getCancellationReason());
        Appointment savedAppointment = appointmentRepository.save(appointment);
        syncSessionFromAppointment(savedAppointment);
        touchCaseTransition(previousCase, savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_STATUS_UPDATED", savedAppointment, "Appointment status updated");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentOrThrow(id);
        assertAppointmentUpdatable(appointment, "delete");
        if (sessionRepository.findFirstByAppointmentIdOrderBySessionDateDesc(appointment.getId()).isPresent()) {
            throw new ValidationException("Appointments with session records cannot be deleted. Cancel or complete the appointment instead to preserve history.");
        }

        User currentUser = resolveCurrentUser();
        appointment.setDeletedAt(LocalDateTime.now());
        appointment.setDeletedByUserId(currentUser != null ? currentUser.getId() : null);
        appointment.setDeletionReason("Soft deleted");
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        syncSessionFromAppointment(appointment);
        touchCaseTransition(appointment.getCaseEntity(), appointment.getCaseEntity());
        auditAppointment("APPOINTMENT_DELETED", appointment, "Appointment soft deleted");
        log.info("Soft deleted appointment with id: {}", id);
    }

    @Override
    public Page<AppointmentDto> getUpcomingAppointments(Pageable pageable) {
        return getScopedAppointmentsAfter(LocalDateTime.now(), pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getPastAppointments(Pageable pageable) {
        return getScopedAppointmentsBefore(LocalDateTime.now(), pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getCancelledAppointments(Pageable pageable) {
        return getScopedAppointmentsByStatus(Appointment.AppointmentStatus.CANCELLED, pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getConfirmedAppointments(Pageable pageable) {
        return getScopedAppointmentsByStatus(Appointment.AppointmentStatus.CONFIRMED, pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getPendingAppointments(Pageable pageable) {
        return getScopedAppointmentsByStatus(Appointment.AppointmentStatus.PENDING, pageable).map(this::toAppointmentDto);
    }

    @Override
    public AppointmentDto cancelAppointment(Long id) {
        return cancelAppointment(id, null);
    }

    @Override
    public AppointmentDto cancelAppointment(Long id, CancelRequest request) {
        Appointment appointment = getAppointmentOrThrow(id);
        assertAppointmentUpdatable(appointment, "cancel");

        applyStatusChange(
                appointment,
                Appointment.AppointmentStatus.CANCELLED,
                request != null ? request.getReason() : null
        );

        Appointment savedAppointment = appointmentRepository.save(appointment);
        syncSessionFromAppointment(savedAppointment);
        notifyStudent(savedAppointment, "Appointment Cancelled", buildCancellationMessage(savedAppointment));
        touchCaseTransition(savedAppointment.getCaseEntity(), savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_CANCELLED", savedAppointment, "Appointment cancelled");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public AppointmentDto confirmAppointment(Long id) {
        Appointment appointment = getAppointmentOrThrow(id);
        assertAppointmentUpdatable(appointment, "confirm");

        applyStatusChange(appointment, Appointment.AppointmentStatus.CONFIRMED, null);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        syncSessionFromAppointment(savedAppointment);
        notifyStudent(savedAppointment,
                "Appointment Confirmed",
                String.format("Your appointment with %s on %s has been confirmed.",
                        describeCounselor(savedAppointment),
                        formatAppointmentDate(savedAppointment)));

        touchCaseTransition(savedAppointment.getCaseEntity(), savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_CONFIRMED", savedAppointment, "Appointment confirmed");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public AppointmentDto rescheduleAppointment(Long id, UpdateAppointmentRequest request) {
        return rescheduleAppointmentInternal(id, request.getAppointmentDate(), request.getTitle());
    }

    @Override
    public AppointmentDto rescheduleAppointment(Long id, RescheduleRequest request) {
        return rescheduleAppointmentInternal(id, request.getAppointmentDate(), request.getTitle());
    }

    @Override
    public boolean checkCounselorAvailability(Long counselorId, String dateTime) {
        User counselor = resolveCounselorUser(counselorId);
        LocalDateTime requestedTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
        int duration = 60;
        LocalDateTime endTime = requestedTime.plusMinutes(duration);

        List<Appointment> conflicts = findConflictingAppointments(counselor, requestedTime, endTime, null);
        return conflicts.isEmpty();
    }

    @Override
    public List<AvailabilitySlot> getCounselorAvailabilitySlots(Long counselorId, LocalDateTime dateTime) {
        List<AvailabilitySlot> slots = new ArrayList<>();
        LocalDateTime startOfDay = dateTime.toLocalDate().atStartOfDay();

        for (int hour = 8; hour < 17; hour++) {
            LocalDateTime slotStart = startOfDay.withHour(hour);
            LocalDateTime slotEnd = slotStart.plusHours(1);
            boolean available = checkAvailabilityInternal(counselorId, slotStart);
            slots.add(new AvailabilitySlot(slotStart, slotEnd, available));
        }

        return slots;
    }

    @Override
    public AppointmentStats getAppointmentStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        return AppointmentStats.builder()
                .totalAppointments(appointmentRepository.count())
                .todayAppointments(appointmentRepository.findByAppointmentDateBetween(startOfDay, endOfDay, Pageable.unpaged()).getTotalElements())
                .monthlyAppointments(appointmentRepository.findByAppointmentDateBetween(startOfMonth, endOfMonth, Pageable.unpaged()).getTotalElements())
                .scheduled(appointmentRepository.findByStatus(Appointment.AppointmentStatus.SCHEDULED, Pageable.unpaged()).getTotalElements())
                .confirmed(appointmentRepository.findByStatus(Appointment.AppointmentStatus.CONFIRMED, Pageable.unpaged()).getTotalElements())
                .completed(appointmentRepository.findByStatus(Appointment.AppointmentStatus.COMPLETED, Pageable.unpaged()).getTotalElements())
                .cancelled(appointmentRepository.findByStatus(Appointment.AppointmentStatus.CANCELLED, Pageable.unpaged()).getTotalElements())
                .pending(appointmentRepository.findByStatus(Appointment.AppointmentStatus.PENDING, Pageable.unpaged()).getTotalElements())
                .unassigned(appointmentRepository.countUnassignedAppointments())
                .build();
    }

    @Override
    public byte[] exportAppointments(String format, String startDate, String endDate) {
        LocalDateTime start = startDate != null
                ? LocalDateTime.parse(startDate + "T00:00:00", DateTimeFormatter.ISO_DATE_TIME)
                : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null
                ? LocalDateTime.parse(endDate + "T23:59:59", DateTimeFormatter.ISO_DATE_TIME)
                : LocalDateTime.now();

        Page<Appointment> appointments = appointmentRepository.findByAppointmentDateBetween(start, end, Pageable.unpaged());

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Student,Counselor,Case Number,Date,Time,Duration,Type,Status,Description\n");

        for (Appointment appointment : appointments.getContent()) {
            String counselorName = appointment.getCounselor() != null
                    ? appointment.getCounselor().getFirstName() + " " + appointment.getCounselor().getLastName()
                    : "Unassigned";
            csv.append(String.format("%d,\"%s\",\"%s %s\",\"%s\",\"%s\",%s,%s,%d,\"%s\",\"%s\",\"%s\"\n",
                    appointment.getId(),
                    appointment.getTitle(),
                    appointment.getStudent().getFirstName(),
                    appointment.getStudent().getLastName(),
                    counselorName,
                    appointment.getCaseEntity() != null ? appointment.getCaseEntity().getCaseNumber() : "",
                    appointment.getAppointmentDate().toLocalDate(),
                    appointment.getAppointmentDate().toLocalTime(),
                    appointment.getDuration(),
                    appointment.getType(),
                    appointment.getStatus(),
                    appointment.getDescription() != null ? appointment.getDescription().replace("\"", "\"\"") : ""
            ));
        }

        return csv.toString().getBytes();
    }

    @Override
    public Page<AppointmentDto> getTodaysAppointments(Pageable pageable) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return getScopedAppointmentsBetween(startOfDay, endOfDay, pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getUnassignedAppointments(Pageable pageable) {
        return appointmentRepository.findUnassignedAppointments(LocalDateTime.now(), pageable).map(this::toAppointmentDto);
    }

    @Override
    public AppointmentDto assignSessionToCounselor(AssignAppointmentRequest request) {
        Appointment appointment = getAppointmentOrThrow(request.getAppointmentId());
        assertAppointmentUpdatable(appointment, "assign");
        ensureLinkedCaseSupportsActiveAppointment(appointment);

        User counselor = resolveCounselorUser(request.getCounselorId());
        validateCaseCounselorCompatibility(appointment.getCaseEntity(), counselor);
        validateAvailability(counselor, appointment.getAppointmentDate(), resolveAppointmentDuration(appointment), appointment.getId());

        appointment.setCounselor(counselor);
        if (appointment.getStatus() == Appointment.AppointmentStatus.UNASSIGNED) {
            appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        }
        applyVenueDetails(appointment, appointment.getSessionMode(), appointment.getMeetingLink(), appointment.getLocation());

        Appointment savedAppointment = appointmentRepository.save(appointment);
        syncSessionFromAppointment(savedAppointment);
        notifyStudent(savedAppointment,
                "Counselor Assigned",
                String.format("Your appointment on %s has been assigned to %s.",
                        formatAppointmentDate(savedAppointment),
                        describeCounselor(savedAppointment)));
        touchCaseTransition(savedAppointment.getCaseEntity(), savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_ASSIGNED", savedAppointment, "Appointment assigned to counselor");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public AppointmentDto counselorTakeAppointment(Long appointmentId, Long counselorId) {
        Appointment appointment = getAppointmentOrThrow(appointmentId);
        assertAppointmentTakeableByCounselor(appointment);
        ensureLinkedCaseSupportsActiveAppointment(appointment);

        if (appointment.getCounselor() != null) {
            throw new ValidationException("Appointment is already assigned to a counselor");
        }

        User counselor = resolveCounselorUser(counselorId);
        ensureCounselorOwnsAction(counselor);
        validateCaseCounselorCompatibility(appointment.getCaseEntity(), counselor);
        validateAvailability(counselor, appointment.getAppointmentDate(), resolveAppointmentDuration(appointment), appointment.getId());

        appointment.setCounselor(counselor);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        applyVenueDetails(appointment, appointment.getSessionMode(), appointment.getMeetingLink(), appointment.getLocation());

        Appointment savedAppointment = appointmentRepository.save(appointment);
        syncSessionFromAppointment(savedAppointment);
        notifyStudent(savedAppointment,
                "Counselor Assigned",
                String.format("Your appointment on %s has been taken by %s.",
                        formatAppointmentDate(savedAppointment),
                        describeCounselor(savedAppointment)));
        touchCaseTransition(savedAppointment.getCaseEntity(), savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_TAKEN", savedAppointment, "Counselor self-assigned appointment");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public Long countUnassignedAppointments() {
        return appointmentRepository.countUnassignedAppointments();
    }

    private AppointmentDto rescheduleAppointmentInternal(Long id, LocalDateTime appointmentDate, String title) {
        Appointment appointment = getAppointmentOrThrow(id);
        assertAppointmentUpdatable(appointment, "reschedule");

        validateAvailability(
                appointment.getCounselor(),
                requireAppointmentDate(appointmentDate),
                resolveAppointmentDuration(appointment),
                appointment.getId()
        );

        appointment.setAppointmentDate(requireAppointmentDate(appointmentDate));
        if (title != null) {
            appointment.setTitle(normalizeRequired(title, "Appointment title is required"));
        }
        applyStatusChange(
                appointment,
                appointment.getCounselor() != null
                        ? Appointment.AppointmentStatus.RESCHEDULED
                        : Appointment.AppointmentStatus.UNASSIGNED,
                null
        );

        Appointment savedAppointment = appointmentRepository.save(appointment);
        syncSessionFromAppointment(savedAppointment);
        notifyStudent(savedAppointment,
                "Appointment Rescheduled",
                String.format("Your appointment has been rescheduled to %s.",
                        formatAppointmentDate(savedAppointment)));
        touchCaseTransition(savedAppointment.getCaseEntity(), savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_RESCHEDULED", savedAppointment, "Appointment rescheduled");
        return toAppointmentDto(savedAppointment);
    }

    private Page<Appointment> getScopedAppointments(Pageable pageable) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null || isAdministrator(currentUser)) {
            return appointmentRepository.findAll(pageable);
        }
        if (isCounselorUser(currentUser)) {
            return appointmentRepository.findByCounselor(currentUser, pageable);
        }
        ensureAppointmentClientUser(currentUser);
        return appointmentRepository.findByStudent(currentUser, pageable);
    }

    private Page<Appointment> getScopedAppointmentsAfter(LocalDateTime start, Pageable pageable) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null || isAdministrator(currentUser)) {
            return appointmentRepository.findByAppointmentDateAfter(start, pageable);
        }
        if (isCounselorUser(currentUser)) {
            return appointmentRepository.findByCounselorAndAppointmentDateAfter(currentUser, start, pageable);
        }
        ensureAppointmentClientUser(currentUser);
        return appointmentRepository.findByStudentAndAppointmentDateAfter(currentUser, start, pageable);
    }

    private Page<Appointment> getScopedAppointmentsBefore(LocalDateTime end, Pageable pageable) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null || isAdministrator(currentUser)) {
            return appointmentRepository.findByAppointmentDateBefore(end, pageable);
        }
        if (isCounselorUser(currentUser)) {
            return appointmentRepository.findByCounselorAndAppointmentDateBefore(currentUser, end, pageable);
        }
        ensureAppointmentClientUser(currentUser);
        return appointmentRepository.findByStudentAndAppointmentDateBefore(currentUser, end, pageable);
    }

    private Page<Appointment> getScopedAppointmentsBetween(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null || isAdministrator(currentUser)) {
            return appointmentRepository.findByAppointmentDateBetween(start, end, pageable);
        }
        if (isCounselorUser(currentUser)) {
            return appointmentRepository.findByCounselorAndAppointmentDateBetween(currentUser, start, end, pageable);
        }
        ensureAppointmentClientUser(currentUser);
        return appointmentRepository.findByStudentAndAppointmentDateBetween(currentUser, start, end, pageable);
    }

    private Page<Appointment> getScopedAppointmentsByStatus(Appointment.AppointmentStatus status, Pageable pageable) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null || isAdministrator(currentUser)) {
            return appointmentRepository.findByStatus(status, pageable);
        }
        if (isCounselorUser(currentUser)) {
            return appointmentRepository.findByCounselorAndStatus(currentUser, status, pageable);
        }
        ensureAppointmentClientUser(currentUser);
        return appointmentRepository.findByStudentAndStatus(currentUser, status, pageable);
    }

    private Appointment getAppointmentOrThrow(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        authorizeAppointmentRead(appointment);
        return appointment;
    }

    private boolean checkAvailabilityInternal(Long counselorId, LocalDateTime dateTime) {
        User counselor;
        try {
            counselor = resolveCounselorUser(counselorId);
        } catch (RuntimeException exception) {
            return false;
        }

        LocalDateTime endTime = dateTime.plusHours(1);
        List<Appointment> conflicts = findConflictingAppointments(counselor, dateTime, endTime, null);
        return conflicts.isEmpty();
    }

    private List<Appointment> findConflictingAppointments(
            User counselor,
            LocalDateTime start,
            LocalDateTime end,
            Long excludeAppointmentId
    ) {
        if (counselor == null) {
            return Collections.emptyList();
        }

        return appointmentRepository.findByCounselor(counselor).stream()
                .filter(appointment -> appointment.getStatus() == Appointment.AppointmentStatus.SCHEDULED
                        || appointment.getStatus() == Appointment.AppointmentStatus.CONFIRMED
                        || appointment.getStatus() == Appointment.AppointmentStatus.RESCHEDULED
                        || appointment.getStatus() == Appointment.AppointmentStatus.IN_PROGRESS
                        || appointment.getStatus() == Appointment.AppointmentStatus.PENDING)
                .filter(appointment -> excludeAppointmentId == null || !appointment.getId().equals(excludeAppointmentId))
                .filter(appointment -> overlaps(appointment, start, end))
                .toList();
    }

    private boolean overlaps(Appointment appointment, LocalDateTime start, LocalDateTime end) {
        LocalDateTime appointmentStart = appointment.getAppointmentDate();
        LocalDateTime appointmentEnd = appointmentStart.plusMinutes(resolveAppointmentDuration(appointment));
        return appointmentStart.isBefore(end) && appointmentEnd.isAfter(start);
    }

    private int resolveAppointmentDuration(Appointment appointment) {
        return appointment.getDuration() != null ? appointment.getDuration() : 60;
    }

    private AppointmentDto toAppointmentDto(Appointment appointment) {
        AppointmentDto dto = AppointmentDto.from(appointment, readJsonMap(appointment.getIntakeDataJson()));
        sessionRepository.findFirstByAppointmentIdOrderBySessionDateDesc(appointment.getId())
                .map(Session::getId)
                .ifPresent(dto::setSessionId);
        return dto;
    }

    private User resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            return null;
        }

        String principalName = authentication.getName();
        return userRepository.findByUsername(principalName)
                .or(() -> userRepository.findByEmail(principalName))
                .orElse(null);
    }

    private User resolveStudentForRequest(CreateAppointmentRequest request, User currentUser) {
        if (currentUser != null && !isAdministrator(currentUser) && !isCounselorUser(currentUser)) {
            ensureAppointmentClientUser(currentUser);
            return currentUser;
        }

        if (request.getStudentId() == null || String.valueOf(request.getStudentId()).isBlank()) {
            throw new ValidationException("Student identifier is required when creating an appointment on behalf of a client");
        }

        User student = findUserByStudentIdentifier(String.valueOf(request.getStudentId()))
                .orElseThrow(() -> new NoSuchElementException("Student not found with identifier: " + request.getStudentId()));
        ensureAppointmentClientUser(student);
        return student;
    }

    private Optional<User> findUserByStudentIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        Optional<User> byStudentId = userRepository.findByStudentId(identifier);
        if (byStudentId.isPresent()) {
            return byStudentId;
        }

        try {
            return userRepository.findById(Long.parseLong(identifier));
        } catch (NumberFormatException ignored) {
            return userRepository.findByUsername(identifier)
                    .or(() -> userRepository.findByEmail(identifier));
        }
    }

    private Client resolveClientForUser(User user) {
        return user != null ? clientIdentityService.getOrCreateClient(user.getId()) : null;
    }

    private Case resolveCaseForAppointment(Long requestedCaseId, Client client) {
        if (requestedCaseId != null) {
            if (client == null) {
                throw new ValidationException("Only registered clients can link appointments to case files");
            }

            Case caseEntity = caseRepository.findById(requestedCaseId)
                    .orElseThrow(() -> new NoSuchElementException("Case not found with id: " + requestedCaseId));
            validateCaseOwnership(caseEntity, client);
            ensureCaseCanAcceptAppointments(caseEntity);
            return caseEntity;
        }

        if (client == null) {
            return null;
        }

        List<Case.CaseStatus> activeStatuses = List.of(
                Case.CaseStatus.OPEN,
                Case.CaseStatus.IN_PROGRESS,
                Case.CaseStatus.ON_HOLD
        );
        List<Case> activeCases = caseRepository.findRecentCasesByClientAndStatuses(client, activeStatuses);
        return activeCases.isEmpty() ? null : activeCases.get(0);
    }

    private void validateCaseOwnership(Case caseEntity, Client client) {
        if (caseEntity.getClient() == null || !caseEntity.getClient().getId().equals(client.getId())) {
            throw new ValidationException("Selected case does not belong to the appointment client");
        }
    }

    private void ensureCaseCanAcceptAppointments(Case caseEntity) {
        if (caseEntity.getStatus() == Case.CaseStatus.CLOSED
                || caseEntity.getStatus() == Case.CaseStatus.RESOLVED
                || caseEntity.getStatus() == Case.CaseStatus.REFERRED) {
            throw new ValidationException("Appointments can only be linked to active case files");
        }
    }

    private User resolveCounselorForAppointment(Long counselorId, Case caseEntity) {
        if (counselorId != null && counselorId > 0) {
            User counselor = resolveCounselorUser(counselorId);
            validateCaseCounselorCompatibility(caseEntity, counselor);
            return counselor;
        }
        if (caseEntity != null && caseEntity.getCounselor() != null) {
            return caseEntity.getCounselor();
        }
        return null;
    }

    private User resolveUpdatedCounselor(Appointment appointment, UpdateAppointmentRequest request, Case targetCase) {
        User targetCounselor = appointment.getCounselor();
        if (request.getCounselorId() != null) {
            targetCounselor = request.getCounselorId() > 0
                    ? resolveCounselorUser(request.getCounselorId())
                    : null;
        }

        if (targetCase != null && targetCase.getCounselor() != null) {
            if (request.getCounselorId() == null || request.getCounselorId() <= 0) {
                targetCounselor = targetCase.getCounselor();
            } else {
                validateCaseCounselorCompatibility(targetCase, targetCounselor);
            }
        }

        return targetCounselor;
    }

    private User resolveCounselorUser(Long counselorId) {
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + counselorId));
        if (!isCounselorUser(counselor)) {
            throw new ValidationException("Selected user is not configured as a counselor");
        }
        if (!Boolean.TRUE.equals(counselor.getActive())) {
            throw new ValidationException("Selected counselor account is inactive");
        }
        if (Boolean.FALSE.equals(counselor.getAvailableForAppointments())) {
            throw new ValidationException("Selected counselor is currently unavailable for appointments");
        }
        return counselor;
    }

    private void validateCounselorScopedRead(User counselor) {
        User currentUser = resolveCurrentUser();
        if (currentUser != null && isCounselorUser(currentUser) && !isAdministrator(currentUser)
                && !currentUser.getId().equals(counselor.getId())) {
            throw new ValidationException("Counselors can only view their own appointment lists");
        }
    }

    private void ensureCounselorOwnsAction(User counselor) {
        User currentUser = resolveCurrentUser();
        if (currentUser != null && isCounselorUser(currentUser) && !isAdministrator(currentUser)
                && !currentUser.getId().equals(counselor.getId())) {
            throw new ValidationException("Counselors can only take appointments for themselves");
        }
    }

    private String resolveAppointmentDescription(String explicitDescription, Map<String, Object> bookingDetails) {
        String normalizedDescription = normalize(explicitDescription);
        if (normalizedDescription != null) {
            return normalizedDescription;
        }

        Object presentingConcern = bookingDetails.get("presentingConcern");
        return presentingConcern != null ? String.valueOf(presentingConcern) : null;
    }

    private Appointment.AppointmentType resolveAppointmentType(CreateAppointmentRequest request) {
        Appointment.AppointmentType type = request.getAppointmentType();
        if (type == null) {
            throw new ValidationException("Invalid appointment type");
        }
        return type;
    }

    private Appointment.SessionMode resolveCreateSessionMode(CreateAppointmentRequest request) {
        if (request.getSessionMode() != null && !request.getSessionMode().isBlank()) {
            try {
                return Appointment.SessionMode.valueOf(request.getSessionMode());
            } catch (IllegalArgumentException exception) {
                throw new ValidationException("Invalid session mode");
            }
        }
        return request.getSessionModeEnum();
    }

    private Appointment.SessionMode resolveUpdateSessionMode(UpdateAppointmentRequest request) {
        if (request.getSessionMode() != null && !request.getSessionMode().isBlank()) {
            Appointment.SessionMode mode = request.getSessionModeEnum();
            if (mode == null) {
                throw new ValidationException("Invalid session mode");
            }
            return mode;
        }
        return null;
    }

    private void applyVenueDetails(
            Appointment appointment,
            Appointment.SessionMode sessionMode,
            String requestedMeetingLink,
            String requestedLocation
    ) {
        if (sessionMode == Appointment.SessionMode.VIRTUAL) {
            appointment.setLocation(null);
            String meetingLink = normalize(requestedMeetingLink);
            if (meetingLink == null) {
                meetingLink = normalize(appointment.getMeetingLink());
            }
            if (meetingLink == null && appointment.getCounselor() != null) {
                meetingLink = generateMeetingLink(appointment);
            }
            appointment.setMeetingLink(meetingLink);
            appointment.setMeetingProvider(meetingLink != null ? defaultMeetingProvider : null);
            return;
        }

        appointment.setMeetingLink(null);
        appointment.setMeetingProvider(null);
        if (requestedLocation != null) {
            appointment.setLocation(normalize(requestedLocation));
        }
    }

    private void validateAvailability(User counselor, LocalDateTime appointmentDate, int duration, Long excludeAppointmentId) {
        if (counselor == null || appointmentDate == null) {
            return;
        }

        LocalDateTime appointmentEnd = appointmentDate.plusMinutes(duration);
        List<Appointment> conflicts = findConflictingAppointments(counselor, appointmentDate, appointmentEnd, excludeAppointmentId);
        if (!conflicts.isEmpty()) {
            throw new ValidationException("Counselor has conflicting appointments at this time");
        }
    }

    private void validateCaseCounselorCompatibility(Case caseEntity, User counselor) {
        if (caseEntity == null || caseEntity.getCounselor() == null || counselor == null) {
            return;
        }
        if (!caseEntity.getCounselor().getId().equals(counselor.getId())) {
            throw new ValidationException("Linked appointments must use the counselor assigned to the case file");
        }
    }

    private void applyStatusChange(
            Appointment appointment,
            Appointment.AppointmentStatus status,
            String cancellationReason
    ) {
        if (status == Appointment.AppointmentStatus.SCHEDULED
                || status == Appointment.AppointmentStatus.CONFIRMED
                || status == Appointment.AppointmentStatus.IN_PROGRESS
                || status == Appointment.AppointmentStatus.RESCHEDULED
                || status == Appointment.AppointmentStatus.PENDING
                || status == Appointment.AppointmentStatus.UNASSIGNED) {
            ensureLinkedCaseSupportsActiveAppointment(appointment);
        }

        switch (status) {
            case CONFIRMED -> {
                if (appointment.getCounselor() == null && appointment.getCaseEntity() != null && appointment.getCaseEntity().getCounselor() != null) {
                    appointment.setCounselor(appointment.getCaseEntity().getCounselor());
                }
                if (appointment.getCounselor() == null) {
                    throw new ValidationException("Appointment must be assigned to a counselor before it can be confirmed");
                }
                validateCaseCounselorCompatibility(appointment.getCaseEntity(), appointment.getCounselor());
                validateAvailability(appointment.getCounselor(), appointment.getAppointmentDate(), resolveAppointmentDuration(appointment), appointment.getId());
                appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
            }
            case CANCELLED -> {
                appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
                if (cancellationReason != null) {
                    appointment.setCancellationReason(normalize(cancellationReason));
                }
            }
            case UNASSIGNED -> {
                appointment.setCounselor(null);
                appointment.setStatus(Appointment.AppointmentStatus.UNASSIGNED);
                appointment.setMeetingLink(null);
                appointment.setMeetingProvider(null);
            }
            case IN_PROGRESS -> {
                if (appointment.getCounselor() == null) {
                    throw new ValidationException("Appointment must be assigned to a counselor before it can start");
                }
                validateAvailability(appointment.getCounselor(), appointment.getAppointmentDate(), resolveAppointmentDuration(appointment), appointment.getId());
                appointment.setStatus(Appointment.AppointmentStatus.IN_PROGRESS);
            }
            case SCHEDULED -> {
                if (appointment.getCounselor() == null) {
                    appointment.setStatus(Appointment.AppointmentStatus.UNASSIGNED);
                } else {
                    validateAvailability(appointment.getCounselor(), appointment.getAppointmentDate(), resolveAppointmentDuration(appointment), appointment.getId());
                    appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
                }
            }
            case COMPLETED -> {
                if (appointment.getCounselor() == null) {
                    throw new ValidationException("Appointment must be assigned to a counselor before it can be completed");
                }
                appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
            }
            case PENDING -> appointment.setStatus(Appointment.AppointmentStatus.PENDING);
            case RESCHEDULED -> {
                if (appointment.getCounselor() == null) {
                    appointment.setStatus(Appointment.AppointmentStatus.UNASSIGNED);
                } else {
                    validateAvailability(appointment.getCounselor(), appointment.getAppointmentDate(), resolveAppointmentDuration(appointment), appointment.getId());
                    appointment.setStatus(Appointment.AppointmentStatus.RESCHEDULED);
                }
            }
            case NO_SHOW, MISSED -> appointment.setStatus(status);
        }
    }

    private void assertAppointmentUpdatable(Appointment appointment, String action) {
        authorizeAppointmentManage(appointment);
        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
            throw new ValidationException("Completed appointments cannot be changed via " + action);
        }
    }

    private void assertAppointmentTakeableByCounselor(Appointment appointment) {
        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
            throw new ValidationException("Completed appointments cannot be taken by another counselor");
        }
    }

    private void syncSessionFromAppointment(Appointment appointment) {
        Optional<Session> existingSession = sessionRepository.findFirstByAppointmentIdOrderBySessionDateDesc(appointment.getId());
        if (existingSession.isEmpty()) {
            if (shouldEnsureSession(appointment)) {
                createSessionFromAppointment(appointment);
            }
            return;
        }

        Session session = existingSession.get();
        updateSessionFromAppointment(session, appointment);
        sessionRepository.save(session);
    }

    private boolean shouldEnsureSession(Appointment appointment) {
        return appointment.getStatus() == Appointment.AppointmentStatus.CONFIRMED
                || appointment.getStatus() == Appointment.AppointmentStatus.IN_PROGRESS
                || appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED;
    }

    private void updateSessionFromAppointment(Session session, Appointment appointment) {
        boolean lockedSession = session.getStatus() == Session.SessionStatus.IN_PROGRESS
                || session.getStatus() == Session.SessionStatus.COMPLETED;

        if (!lockedSession) {
            session.setStudent(appointment.getStudent());
            session.setClient(appointment.getClient());
            session.setCounselor(appointment.getCounselor());
            session.setSessionDate(appointment.getAppointmentDate());
            session.setMeetingLink(appointment.getMeetingLink());
            session.setDurationMinutes(resolveAppointmentDuration(appointment));
            session.setType(mapAppointmentTypeToSessionType(appointment.getType()));
            session.setTitle(appointment.getTitle());
            session.setPresentingIssue(appointment.getDescription());
        } else {
            if (session.getCounselor() == null && appointment.getCounselor() != null) {
                session.setCounselor(appointment.getCounselor());
            }
            if (session.getClient() == null && appointment.getClient() != null) {
                session.setClient(appointment.getClient());
            }
        }

        session.setStatus(mapAppointmentStatusToSessionStatus(appointment.getStatus(), session.getStatus()));
    }

    private Session.SessionStatus mapAppointmentStatusToSessionStatus(
            Appointment.AppointmentStatus appointmentStatus,
            Session.SessionStatus currentStatus
    ) {
        if (currentStatus == Session.SessionStatus.COMPLETED && appointmentStatus != Appointment.AppointmentStatus.CANCELLED) {
            return currentStatus;
        }

        return switch (appointmentStatus) {
            case CONFIRMED, SCHEDULED, RESCHEDULED, PENDING, UNASSIGNED -> Session.SessionStatus.SCHEDULED;
            case IN_PROGRESS -> Session.SessionStatus.IN_PROGRESS;
            case COMPLETED -> Session.SessionStatus.COMPLETED;
            case CANCELLED -> Session.SessionStatus.CANCELLED;
            case NO_SHOW, MISSED -> Session.SessionStatus.NO_SHOW;
        };
    }

    private void touchCaseTransition(Case previousCase, Case currentCase) {
        if (previousCase != null) {
            touchCase(previousCase);
        }
        if (currentCase != null && (previousCase == null || !previousCase.getId().equals(currentCase.getId()))) {
            touchCase(currentCase);
        }
    }

    private void touchCase(Case caseEntity) {
        if (caseEntity == null) {
            return;
        }
        caseEntity.setLastActivityAt(LocalDateTime.now());
        if (caseEntity.getStatus() == Case.CaseStatus.OPEN) {
            caseEntity.setStatus(Case.CaseStatus.IN_PROGRESS);
        }
        caseRepository.save(caseEntity);
    }

    private void createSessionFromAppointment(Appointment appointment) {
        Session session = new Session();
        session.setAppointment(appointment);
        session.setStudent(appointment.getStudent());
        session.setClient(appointment.getClient());
        session.setCounselor(appointment.getCounselor());
        session.setSessionDate(appointment.getAppointmentDate());
        session.setMeetingLink(appointment.getMeetingLink());
        session.setDurationMinutes(resolveAppointmentDuration(appointment));
        session.setType(mapAppointmentTypeToSessionType(appointment.getType()));
        session.setStatus(mapAppointmentStatusToSessionStatus(appointment.getStatus(), Session.SessionStatus.SCHEDULED));
        session.setTitle(appointment.getTitle());
        session.setPresentingIssue(appointment.getDescription());
        sessionRepository.save(session);
        log.info("Created session from appointment: {}", appointment.getId());
    }

    private void notifyStudentOnCreate(Appointment appointment) {
        if (appointment.getStatus() == Appointment.AppointmentStatus.UNASSIGNED) {
            notifyStudent(appointment,
                    "Appointment Request Received",
                    String.format("Your appointment request for %s has been received and is awaiting counselor assignment.",
                            formatAppointmentDate(appointment)));
            return;
        }

        notifyStudent(appointment,
                "Appointment Scheduled",
                String.format("Your appointment with %s has been scheduled for %s.",
                        describeCounselor(appointment),
                        formatAppointmentDate(appointment)));
    }

    private void notifyStudent(Appointment appointment, String title, String message) {
        if (appointment.getStudent() == null || appointment.getStudent().getId() == null) {
            return;
        }

        try {
            notificationService.sendSystemNotification(appointment.getStudent().getId(), title, message, "MEDIUM");
        } catch (Exception ex) {
            log.warn("Failed to create notification for appointment {}", appointment.getId(), ex);
        }
    }

    private String buildCancellationMessage(Appointment appointment) {
        String reason = appointment.getCancellationReason();
        if (reason != null && !reason.isBlank()) {
            return String.format("Your appointment scheduled for %s has been cancelled. Reason: %s",
                    formatAppointmentDate(appointment),
                    reason);
        }

        return String.format("Your appointment scheduled for %s has been cancelled.",
                formatAppointmentDate(appointment));
    }

    private String formatAppointmentDate(Appointment appointment) {
        if (appointment.getAppointmentDate() == null) {
            return "the scheduled time";
        }

        return appointment.getAppointmentDate().format(NOTIFICATION_DATE_FORMAT);
    }

    private String describeCounselor(Appointment appointment) {
        User counselor = appointment.getCounselor();
        if (counselor == null) {
            return "your counselor";
        }

        String firstName = counselor.getFirstName();
        String lastName = counselor.getLastName();
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        if (firstName != null && !firstName.isBlank()) {
            return firstName;
        }
        if (counselor.getUsername() != null && !counselor.getUsername().isBlank()) {
            return counselor.getUsername();
        }
        if (counselor.getEmail() != null && !counselor.getEmail().isBlank()) {
            return counselor.getEmail();
        }

        return "your counselor";
    }

    private Session.SessionType mapAppointmentTypeToSessionType(Appointment.AppointmentType appointmentType) {
        return switch (appointmentType) {
            case INITIAL_CONSULTATION -> Session.SessionType.CONSULTATION;
            case FOLLOW_UP -> Session.SessionType.FOLLOW_UP;
            case GROUP_SESSION -> Session.SessionType.GROUP;
            case ASSESSMENT -> Session.SessionType.ASSESSMENT;
            case CRISIS_INTERVENTION -> Session.SessionType.CRISIS;
        };
    }

    private String generateMeetingLink(Appointment appointment) {
        String domain = customMeetingDomain != null && !customMeetingDomain.isBlank()
                ? customMeetingDomain
                : jitsiDomain;
        String roomName = generateRoomName(appointment);
        return String.format("https://%s/%s", domain, roomName);
    }

    private String generateRoomName(Appointment appointment) {
        String randomPart = UUID.randomUUID().toString().substring(0, 8);
        Long counselorId = appointment.getCounselor() != null ? appointment.getCounselor().getId() : 0L;
        return String.format("unza-counseling-%d-%d-%d-%s",
                counselorId,
                appointment.getStudent().getId(),
                System.currentTimeMillis(),
                randomPart
        );
    }

    private void auditAppointment(String action, Appointment appointment, String details) {
        User actor = resolveCurrentUser();
        String userId = actor != null ? String.valueOf(actor.getId()) : "system";
        auditLogService.logAction(
                action,
                "APPOINTMENT",
                String.valueOf(appointment.getId()),
                details,
                userId,
                "system",
                true
        );
    }

    private Map<String, Object> sanitizeMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        return new LinkedHashMap<>(source);
    }

    private String writeJson(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize appointment booking details", exception);
            return null;
        }
    }

    private Map<String, Object> readJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to parse appointment booking details", exception);
            return Collections.emptyMap();
        }
    }

    private boolean isAdministrator(User user) {
        return user != null && (user.hasRole("ROLE_ADMIN") || user.hasRole("ROLE_SUPER_ADMIN"));
    }

    private boolean isCounselorUser(User user) {
        return user != null && user.isCounselor();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new ValidationException(message);
        }
        return normalized;
    }

    private LocalDateTime requireAppointmentDate(LocalDateTime appointmentDate) {
        if (appointmentDate == null) {
            throw new ValidationException("Appointment date is required");
        }
        return appointmentDate;
    }

    private int normalizeDuration(Integer duration) {
        int resolvedDuration = duration != null ? duration : 60;
        if (resolvedDuration <= 0) {
            throw new ValidationException("Appointment duration must be greater than zero");
        }
        return resolvedDuration;
    }

    private void ensureAppointmentClientUser(User user) {
        if (user == null || (!user.isStudent() && resolveClientForUser(user) == null)) {
            throw new ValidationException("Appointments can only be created and retrieved for client or student accounts");
        }
    }

    private void ensureLinkedCaseSupportsActiveAppointment(Appointment appointment) {
        if (appointment.getCaseEntity() != null) {
            ensureCaseCanAcceptAppointments(appointment.getCaseEntity());
        }
    }

    private boolean isActiveAppointmentStatus(Appointment.AppointmentStatus status) {
        return status == Appointment.AppointmentStatus.SCHEDULED
                || status == Appointment.AppointmentStatus.CONFIRMED
                || status == Appointment.AppointmentStatus.IN_PROGRESS
                || status == Appointment.AppointmentStatus.RESCHEDULED
                || status == Appointment.AppointmentStatus.PENDING
                || status == Appointment.AppointmentStatus.UNASSIGNED;
    }

    private void authorizeAppointmentRead(Appointment appointment) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null || isAdministrator(currentUser)) {
            return;
        }
        if (isCounselorUser(currentUser)) {
            if (appointment.getCounselor() == null && appointment.getStatus() == Appointment.AppointmentStatus.UNASSIGNED) {
                return;
            }
            boolean matchesAssignedCounselor = appointment.getCounselor() != null
                    && currentUser.getId().equals(appointment.getCounselor().getId());
            boolean matchesCaseCounselor = appointment.getCaseEntity() != null
                    && appointment.getCaseEntity().getCounselor() != null
                    && currentUser.getId().equals(appointment.getCaseEntity().getCounselor().getId());
            if (matchesAssignedCounselor || matchesCaseCounselor) {
                return;
            }
            throw new AccessDeniedException("You do not have access to this appointment");
        }
        if (appointment.getStudent() != null && currentUser.getId().equals(appointment.getStudent().getId())) {
            return;
        }
        throw new AccessDeniedException("You do not have access to this appointment");
    }

    private void authorizeAppointmentManage(Appointment appointment) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null || isAdministrator(currentUser)) {
            return;
        }
        if (isCounselorUser(currentUser)) {
            boolean matchesAssignedCounselor = appointment.getCounselor() != null
                    && currentUser.getId().equals(appointment.getCounselor().getId());
            boolean matchesCaseCounselor = appointment.getCaseEntity() != null
                    && appointment.getCaseEntity().getCounselor() != null
                    && currentUser.getId().equals(appointment.getCaseEntity().getCounselor().getId());
            if (matchesAssignedCounselor || matchesCaseCounselor) {
                return;
            }
            throw new AccessDeniedException("You do not have permission to modify this appointment");
        }
        if (appointment.getStudent() != null && currentUser.getId().equals(appointment.getStudent().getId())) {
            return;
        }
        throw new AccessDeniedException("You do not have permission to modify this appointment");
    }

    private void authorizeClientScopedRead(User clientUser) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null || isAdministrator(currentUser) || isCounselorUser(currentUser)) {
            return;
        }
        if (!currentUser.getId().equals(clientUser.getId())) {
            throw new AccessDeniedException("You do not have access to this client appointment list");
        }
    }

    private void authorizeStudentScopedRead(User student) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null || isAdministrator(currentUser) || isCounselorUser(currentUser)) {
            return;
        }
        if (!currentUser.getId().equals(student.getId())) {
            throw new AccessDeniedException("You do not have access to this student appointment list");
        }
    }

    private void authorizeCaseRead(Case caseEntity) {
        User currentUser = resolveCurrentUser();
        if (currentUser == null || isAdministrator(currentUser)) {
            return;
        }
        if (isCounselorUser(currentUser)) {
            if (caseEntity.getCounselor() != null && currentUser.getId().equals(caseEntity.getCounselor().getId())) {
                return;
            }
            boolean hasLinkedAppointment = appointmentRepository.findByCaseEntity(caseEntity).stream()
                    .anyMatch(appointment -> appointment.getCounselor() != null
                            && currentUser.getId().equals(appointment.getCounselor().getId()));
            if (hasLinkedAppointment) {
                return;
            }
            throw new AccessDeniedException("You do not have access to this case appointment history");
        }
        if (caseEntity.getClient() != null && currentUser.getId().equals(caseEntity.getClient().getId())) {
            return;
        }
        throw new AccessDeniedException("You do not have access to this case appointment history");
    }
}
