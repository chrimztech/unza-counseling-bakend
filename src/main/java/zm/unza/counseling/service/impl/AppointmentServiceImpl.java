package zm.unza.counseling.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.CaseRepository;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.SessionRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.AppointmentService;
import zm.unza.counseling.service.AuditLogService;

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

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final SessionRepository sessionRepository;
    private final CaseRepository caseRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Value("${app.meeting.jitsi-domain:meet.jit.si}")
    private String jitsiDomain;

    @Value("${app.meeting.custom-domain:meet.unza.edu.zm}")
    private String customMeetingDomain;

    @Value("${app.meeting.default-provider:jitsi}")
    private String defaultMeetingProvider;

    @Override
    public Page<AppointmentDto> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(this::toAppointmentDto);
    }

    @Override
    public AppointmentDto getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .map(this::toAppointmentDto)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByClientId(Long clientId, Pageable pageable) {
        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isPresent()) {
            return appointmentRepository.findByClient(clientOpt.get(), pageable).map(this::toAppointmentDto);
        }

        User user = userRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("Client not found with id: " + clientId));
        log.info("No Client record found for user {}, querying appointments by student", user.getUsername());
        return appointmentRepository.findByStudent(user, pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByCounselorId(Long counselorId, Pageable pageable) {
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + counselorId));
        return appointmentRepository.findByCounselor(counselor, pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByStudentId(String studentId, Pageable pageable) {
        User student = findUserByStudentIdentifier(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found with identifier: " + studentId));
        return appointmentRepository.findByStudent(student, pageable).map(this::toAppointmentDto);
    }

    @Override
    public AppointmentDto createAppointment(CreateAppointmentRequest request) {
        User currentUser = resolveCurrentUser();
        User student = resolveStudentForRequest(request, currentUser);
        Client client = resolveClientForUser(student);
        Case caseEntity = resolveCaseForAppointment(request.getCaseId(), client);

        Appointment appointment = new Appointment();
        appointment.setTitle(request.getTitle());
        appointment.setStudent(student);
        appointment.setClient(client);
        appointment.setCaseEntity(caseEntity);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setType(Optional.ofNullable(request.getAppointmentType())
                .orElse(Appointment.AppointmentType.INITIAL_CONSULTATION));
        appointment.setDuration(request.getDuration() != null ? request.getDuration() : 60);
        appointment.setSessionMode(request.getSessionModeEnum());

        Map<String, Object> bookingDetails = sanitizeMap(request.getBookingDetails());
        appointment.setDescription(resolveAppointmentDescription(request.getDescription(), bookingDetails));
        appointment.setIntakeDataJson(writeJson(bookingDetails));

        User counselor = resolveCounselorForAppointment(request.getCounselorId(), caseEntity);
        if (counselor != null) {
            appointment.setCounselor(counselor);
            appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        } else {
            appointment.setStatus(Appointment.AppointmentStatus.UNASSIGNED);
        }

        if (appointment.getSessionMode() == Appointment.SessionMode.VIRTUAL) {
            if (counselor != null) {
                String meetingLink = request.getMeetingLink() != null && !request.getMeetingLink().isBlank()
                        ? request.getMeetingLink()
                        : generateMeetingLink(appointment);
                appointment.setMeetingLink(meetingLink);
                appointment.setMeetingProvider(defaultMeetingProvider);
            }
        } else if (request.getLocation() != null && !request.getLocation().isBlank()) {
            appointment.setLocation(request.getLocation());
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        touchCase(caseEntity);
        auditAppointment("APPOINTMENT_CREATED", savedAppointment, "Appointment created");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public AppointmentDto updateAppointment(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));

        if (request.getTitle() != null) {
            appointment.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            appointment.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }
        if (request.getAppointmentDate() != null) {
            appointment.setAppointmentDate(request.getAppointmentDate());
        }
        if (request.getCancellationReason() != null) {
            appointment.setCancellationReason(request.getCancellationReason());
        }
        if (request.getCaseId() != null) {
            Client client = resolveClientForUser(appointment.getStudent());
            appointment.setCaseEntity(resolveCaseForAppointment(request.getCaseId(), client));
        }
        if (request.getBookingDetails() != null && !request.getBookingDetails().isEmpty()) {
            appointment.setIntakeDataJson(writeJson(sanitizeMap(request.getBookingDetails())));
            if ((appointment.getDescription() == null || appointment.getDescription().isBlank())) {
                appointment.setDescription(resolveAppointmentDescription(appointment.getDescription(), request.getBookingDetails()));
            }
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        touchCase(savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_UPDATED", savedAppointment, "Appointment updated");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public AppointmentDto updateAppointmentStatus(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }
        if (request.getAppointmentDate() != null) {
            appointment.setAppointmentDate(request.getAppointmentDate());
        }
        if (request.getCancellationReason() != null) {
            appointment.setCancellationReason(request.getCancellationReason());
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        touchCase(savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_STATUS_UPDATED", savedAppointment, "Appointment status updated");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        User currentUser = resolveCurrentUser();

        appointment.setDeletedAt(LocalDateTime.now());
        appointment.setDeletedByUserId(currentUser != null ? currentUser.getId() : null);
        appointment.setDeletionReason("Soft deleted");
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        touchCase(appointment.getCaseEntity());
        auditAppointment("APPOINTMENT_DELETED", appointment, "Appointment soft deleted");
        log.info("Soft deleted appointment with id: {}", id);
    }

    @Override
    public Page<AppointmentDto> getUpcomingAppointments(Pageable pageable) {
        return appointmentRepository.findByAppointmentDateAfter(LocalDateTime.now(), pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getPastAppointments(Pageable pageable) {
        return appointmentRepository.findByAppointmentDateBefore(LocalDateTime.now(), pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getCancelledAppointments(Pageable pageable) {
        return appointmentRepository.findByStatus(Appointment.AppointmentStatus.CANCELLED, pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getConfirmedAppointments(Pageable pageable) {
        return appointmentRepository.findByStatus(Appointment.AppointmentStatus.CONFIRMED, pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getPendingAppointments(Pageable pageable) {
        return appointmentRepository.findByStatus(Appointment.AppointmentStatus.PENDING, pageable).map(this::toAppointmentDto);
    }

    @Override
    public AppointmentDto cancelAppointment(Long id) {
        return cancelAppointment(id, null);
    }

    @Override
    public AppointmentDto cancelAppointment(Long id, CancelRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        if (request != null && request.getReason() != null) {
            appointment.setCancellationReason(request.getReason());
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        touchCase(savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_CANCELLED", savedAppointment, "Appointment cancelled");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public AppointmentDto confirmAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));

        if (appointment.getCounselor() == null && appointment.getCaseEntity() != null && appointment.getCaseEntity().getCounselor() != null) {
            appointment.setCounselor(appointment.getCaseEntity().getCounselor());
        }
        if (appointment.getCounselor() == null) {
            throw new IllegalStateException("Appointment must be assigned to a counselor before it can be confirmed");
        }

        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        appointment = appointmentRepository.save(appointment);
        ensureSessionFromAppointment(appointment);

        touchCase(appointment.getCaseEntity());
        auditAppointment("APPOINTMENT_CONFIRMED", appointment, "Appointment confirmed");
        return toAppointmentDto(appointment);
    }

    @Override
    public AppointmentDto rescheduleAppointment(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        appointment.setAppointmentDate(request.getAppointmentDate());
        if (request.getTitle() != null) {
            appointment.setTitle(request.getTitle());
        }
        appointment.setStatus(Appointment.AppointmentStatus.RESCHEDULED);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        touchCase(savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_RESCHEDULED", savedAppointment, "Appointment rescheduled");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public AppointmentDto rescheduleAppointment(Long id, RescheduleRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        appointment.setAppointmentDate(request.getAppointmentDate());
        if (request.getTitle() != null) {
            appointment.setTitle(request.getTitle());
        }
        appointment.setStatus(Appointment.AppointmentStatus.RESCHEDULED);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        touchCase(savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_RESCHEDULED", savedAppointment, "Appointment rescheduled");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public boolean checkCounselorAvailability(Long counselorId, String dateTime) {
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + counselorId));

        LocalDateTime requestedTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
        int duration = 60;
        LocalDateTime endTime = requestedTime.plusMinutes(duration);

        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(counselor, requestedTime, endTime);
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
        return appointmentRepository.findByAppointmentDateBetween(startOfDay, endOfDay, pageable).map(this::toAppointmentDto);
    }

    @Override
    public Page<AppointmentDto> getUnassignedAppointments(Pageable pageable) {
        return appointmentRepository.findUnassignedAppointments(LocalDateTime.now(), pageable).map(this::toAppointmentDto);
    }

    @Override
    public AppointmentDto assignSessionToCounselor(AssignAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + request.getAppointmentId()));

        User counselor = userRepository.findById(request.getCounselorId())
                .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + request.getCounselorId()));

        appointment.setCounselor(counselor);
        if (appointment.getStatus() == Appointment.AppointmentStatus.UNASSIGNED) {
            appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        }
        if (appointment.getSessionMode() == Appointment.SessionMode.VIRTUAL && (appointment.getMeetingLink() == null || appointment.getMeetingLink().isBlank())) {
            appointment.setMeetingLink(generateMeetingLink(appointment));
            appointment.setMeetingProvider(defaultMeetingProvider);
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        touchCase(savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_ASSIGNED", savedAppointment, "Appointment assigned to counselor");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public AppointmentDto counselorTakeAppointment(Long appointmentId, Long counselorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + appointmentId));

        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + counselorId));

        if (appointment.getCounselor() != null) {
            throw new IllegalStateException("Appointment is already assigned to a counselor");
        }

        LocalDateTime appointmentTime = appointment.getAppointmentDate();
        LocalDateTime endTime = appointmentTime.plusMinutes(appointment.getDuration());
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(counselor, appointmentTime, endTime);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Counselor has conflicting appointments at this time");
        }

        appointment.setCounselor(counselor);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        if (appointment.getSessionMode() == Appointment.SessionMode.VIRTUAL && (appointment.getMeetingLink() == null || appointment.getMeetingLink().isBlank())) {
            appointment.setMeetingLink(generateMeetingLink(appointment));
            appointment.setMeetingProvider(defaultMeetingProvider);
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        touchCase(savedAppointment.getCaseEntity());
        auditAppointment("APPOINTMENT_TAKEN", savedAppointment, "Counselor self-assigned appointment");
        return toAppointmentDto(savedAppointment);
    }

    @Override
    public Long countUnassignedAppointments() {
        return appointmentRepository.countUnassignedAppointments();
    }

    private boolean checkAvailabilityInternal(Long counselorId, LocalDateTime dateTime) {
        User counselor = userRepository.findById(counselorId).orElse(null);
        if (counselor == null) {
            return false;
        }

        LocalDateTime endTime = dateTime.plusHours(1);
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(counselor, dateTime, endTime);
        return conflicts.isEmpty();
    }

    private AppointmentDto toAppointmentDto(Appointment appointment) {
        return AppointmentDto.from(appointment, readJsonMap(appointment.getIntakeDataJson()));
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
        if (currentUser != null && !currentUser.hasRole("ROLE_ADMIN") && !currentUser.hasRole("ROLE_COUNSELOR")) {
            return currentUser;
        }

        Optional<User> requestedUser = findUserByStudentIdentifier(request.getStudentId());
        if (requestedUser.isPresent()) {
            return requestedUser.get();
        }
        if (currentUser != null) {
            return currentUser;
        }
        throw new NoSuchElementException("Unable to resolve student for appointment");
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
            return userRepository.findByUsername(identifier);
        }
    }

    private Client resolveClientForUser(User user) {
        return clientRepository.findById(user.getId()).orElse(null);
    }

    private Case resolveCaseForAppointment(Long requestedCaseId, Client client) {
        if (requestedCaseId != null) {
            Case caseEntity = caseRepository.findById(requestedCaseId)
                    .orElseThrow(() -> new NoSuchElementException("Case not found with id: " + requestedCaseId));
            validateCaseOwnership(caseEntity, client);
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
        if (client == null) {
            return;
        }
        if (caseEntity.getClient() == null || !caseEntity.getClient().getId().equals(client.getId())) {
            throw new IllegalStateException("Selected case does not belong to the appointment client");
        }
    }

    private User resolveCounselorForAppointment(Long counselorId, Case caseEntity) {
        if (counselorId != null && counselorId > 0) {
            return userRepository.findById(counselorId)
                    .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + counselorId));
        }
        if (caseEntity != null && caseEntity.getCounselor() != null) {
            return caseEntity.getCounselor();
        }
        return null;
    }

    private String resolveAppointmentDescription(String explicitDescription, Map<String, Object> bookingDetails) {
        if (explicitDescription != null && !explicitDescription.isBlank()) {
            return explicitDescription;
        }
        Object presentingConcern = bookingDetails.get("presentingConcern");
        return presentingConcern != null ? String.valueOf(presentingConcern) : explicitDescription;
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

    private void ensureSessionFromAppointment(Appointment appointment) {
        if (sessionRepository.findFirstByAppointmentIdOrderBySessionDateDesc(appointment.getId()).isPresent()) {
            return;
        }
        createSessionFromAppointment(appointment);
    }

    private void createSessionFromAppointment(Appointment appointment) {
        Session session = new Session();
        session.setAppointment(appointment);
        session.setStudent(appointment.getStudent());
        session.setCounselor(appointment.getCounselor());
        session.setSessionDate(appointment.getAppointmentDate());
        session.setDurationMinutes(appointment.getDuration());
        session.setType(mapAppointmentTypeToSessionType(appointment.getType()));
        session.setStatus(Session.SessionStatus.SCHEDULED);
        session.setTitle(appointment.getTitle());
        session.setPresentingIssue(appointment.getDescription());
        sessionRepository.save(session);
        log.info("Created session from appointment: {}", appointment.getId());
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
        String roomName = generateRoomName(appointment);
        return String.format("https://%s/%s", jitsiDomain, roomName);
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
}
