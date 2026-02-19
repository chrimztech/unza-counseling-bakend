package zm.unza.counseling.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.Session;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.SessionRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.AppointmentService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementation of AppointmentService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Value("${app.meeting.jitsi-domain:meet.jit.si}")
    private String jitsiDomain;
    
    @Value("${app.meeting.custom-domain:meet.unza.edu.zm}")
    private String customMeetingDomain;
    
    @Value("${app.meeting.default-provider:jitsi}")
    private String defaultMeetingProvider;

    @Override
    public Page<AppointmentDto> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(AppointmentDto::from);
    }

    @Override
    public AppointmentDto getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .map(AppointmentDto::from)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByClientId(Long clientId, Pageable pageable) {
        // Try to find Client record first
        Optional<Client> clientOpt = clientRepository.findById(clientId);
        
        if (clientOpt.isPresent()) {
            // Client record exists, query by client
            return appointmentRepository.findByClient(clientOpt.get(), pageable).map(AppointmentDto::from);
        } else {
            // No Client record - find User and query by student instead
            // This avoids duplicate email constraint violation from auto-creating Client
            User user = userRepository.findById(clientId)
                    .orElseThrow(() -> new NoSuchElementException("Client not found with id: " + clientId));
            log.info("No Client record found for user {}, querying appointments by student", user.getUsername());
            return appointmentRepository.findByStudent(user, pageable).map(AppointmentDto::from);
        }
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByCounselorId(Long counselorId, Pageable pageable) {
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + counselorId));
        return appointmentRepository.findByCounselor(counselor, pageable).map(AppointmentDto::from);
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByStudentId(String studentId, Pageable pageable) {
        User student = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found with studentId: " + studentId));
        return appointmentRepository.findByStudent(student, pageable).map(AppointmentDto::from);
    }

    @Override
    public AppointmentDto createAppointment(CreateAppointmentRequest request) {
        // Get the authenticated user's identifier from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication.getName();
        
        // Find the user by username or email
        User user = userRepository.findByUsername(principalName)
                .orElseGet(() -> userRepository.findByEmail(principalName)
                        .orElseThrow(() -> new NoSuchElementException("User not found: " + principalName)));
        
        // Get existing Client record or use null (appointments can be linked via student field)
        // Don't auto-create Client to avoid duplicate email constraint violation
        Client client = clientRepository.findById(user.getId()).orElse(null);
        if (client == null) {
            log.info("No Client record found for user {}, appointment will be linked via student field", principalName);
        }
        
        Appointment appointment = new Appointment();
        appointment.setTitle(request.getTitle());
        appointment.setStudent(user);
        appointment.setClient(client);
        appointment.setAppointmentDate(request.getAppointmentDate());
        // Set appointment type with default fallback
        Appointment.AppointmentType appointmentType = request.getAppointmentType();
        if (appointmentType == null) {
            appointmentType = Appointment.AppointmentType.INITIAL_CONSULTATION;
            log.warn("No valid appointment type provided, defaulting to INITIAL_CONSULTATION");
        }
        appointment.setType(appointmentType);
        appointment.setDescription(request.getDescription());
        appointment.setDuration(request.getDuration() != null ? request.getDuration() : 60);
        
        // Set session mode and generate meeting link for virtual sessions
        Appointment.SessionMode sessionMode = request.getSessionModeEnum();
        appointment.setSessionMode(sessionMode);
        
        if (sessionMode == Appointment.SessionMode.VIRTUAL) {
            // Generate meeting link for virtual sessions
            String meetingLink = generateMeetingLink(appointment);
            appointment.setMeetingLink(meetingLink);
            appointment.setMeetingProvider(defaultMeetingProvider);
            log.info("Generated meeting link for virtual appointment: {}", meetingLink);
        } else {
            // For in-person sessions, set location if provided
            if (request.getLocation() != null) {
                appointment.setLocation(request.getLocation());
            }
        }
        
        // Handle optional counselor (for unassigned appointments)
        if (request.getCounselorId() != null) {
            User counselor = userRepository.findById(request.getCounselorId())
                    .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + request.getCounselorId()));
            appointment.setCounselor(counselor);
            appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        } else {
            // No counselor assigned yet - unassigned status
            appointment.setStatus(Appointment.AppointmentStatus.UNASSIGNED);
        }
        
        return AppointmentDto.from(appointmentRepository.save(appointment));
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
        
        return AppointmentDto.from(appointmentRepository.save(appointment));
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
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    @Override
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        appointmentRepository.delete(appointment);
        log.info("Deleted appointment with id: {}", id);
    }

    @Override
    public Page<AppointmentDto> getUpcomingAppointments(Pageable pageable) {
        return appointmentRepository.findByAppointmentDateAfter(LocalDateTime.now(), pageable).map(AppointmentDto::from);
    }

    @Override
    public Page<AppointmentDto> getPastAppointments(Pageable pageable) {
        return appointmentRepository.findByAppointmentDateBefore(LocalDateTime.now(), pageable).map(AppointmentDto::from);
    }

    @Override
    public Page<AppointmentDto> getCancelledAppointments(Pageable pageable) {
        return appointmentRepository.findByStatus(Appointment.AppointmentStatus.CANCELLED, pageable).map(AppointmentDto::from);
    }

    @Override
    public Page<AppointmentDto> getConfirmedAppointments(Pageable pageable) {
        return appointmentRepository.findByStatus(Appointment.AppointmentStatus.CONFIRMED, pageable).map(AppointmentDto::from);
    }

    @Override
    public Page<AppointmentDto> getPendingAppointments(Pageable pageable) {
        return appointmentRepository.findByStatus(Appointment.AppointmentStatus.PENDING, pageable).map(AppointmentDto::from);
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
        log.info("Cancelled appointment with id: {}", id);
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentDto confirmAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        appointment = appointmentRepository.save(appointment);
        
        // Create a session from the confirmed appointment
        createSessionFromAppointment(appointment);
        
        log.info("Confirmed appointment with id: {}", id);
        return AppointmentDto.from(appointment);
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
        log.info("Rescheduled appointment with id: {} to {}", id, request.getAppointmentDate());
        return AppointmentDto.from(appointmentRepository.save(appointment));
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
        log.info("Rescheduled appointment with id: {} to {}", id, request.getAppointmentDate());
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    @Override
    public boolean checkCounselorAvailability(Long counselorId, String dateTime) {
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + counselorId));
        
        LocalDateTime requestedTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE_TIME);
        int duration = 60; // default duration
        LocalDateTime endTime = requestedTime.plusMinutes(duration);
        
        // Check for conflicting appointments
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(counselor, requestedTime, endTime);
        return conflicts.isEmpty();
    }

    @Override
    public List<AvailabilitySlot> getCounselorAvailabilitySlots(Long counselorId, LocalDateTime dateTime) {
        List<AvailabilitySlot> slots = new ArrayList<>();
        LocalDateTime startOfDay = dateTime.toLocalDate().atStartOfDay();
        
        // Generate slots from 8 AM to 5 PM
        for (int hour = 8; hour < 17; hour++) {
            LocalDateTime slotStart = startOfDay.withHour(hour);
            LocalDateTime slotEnd = slotStart.plusHours(1);
            
            boolean available = checkAvailabilityInternal(counselorId, slotStart);
            
            slots.add(new AvailabilitySlot(slotStart, slotEnd, available));
        }
        
        return slots;
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
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate + "T00:00:00", DateTimeFormatter.ISO_DATE_TIME) : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate + "T23:59:59", DateTimeFormatter.ISO_DATE_TIME) : LocalDateTime.now();
        
        Page<Appointment> appointments = appointmentRepository.findByAppointmentDateBetween(start, end, Pageable.unpaged());
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Student,Counselor,Date,Time,Duration,Type,Status,Description\n");
        
        for (Appointment apt : appointments.getContent()) {
            String counselorName = apt.getCounselor() != null ? 
                apt.getCounselor().getFirstName() + " " + apt.getCounselor().getLastName() : "Unassigned";
            csv.append(String.format("%d,\"%s\",\"%s %s\",\"%s\",%s,%s,%d,\"%s\",\"%s\",\"%s\"\n",
                    apt.getId(),
                    apt.getTitle(),
                    apt.getStudent().getFirstName(),
                    apt.getStudent().getLastName(),
                    counselorName,
                    apt.getAppointmentDate().toLocalDate(),
                    apt.getAppointmentDate().toLocalTime(),
                    apt.getDuration(),
                    apt.getType(),
                    apt.getStatus(),
                    apt.getDescription() != null ? apt.getDescription().replace("\"", "\"\"") : ""
            ));
        }
        
        return csv.toString().getBytes();
    }

    @Override
    public Page<AppointmentDto> getTodaysAppointments(Pageable pageable) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return appointmentRepository.findByAppointmentDateBetween(startOfDay, endOfDay, pageable).map(AppointmentDto::from);
    }

    @Override
    public Page<AppointmentDto> getUnassignedAppointments(Pageable pageable) {
        return appointmentRepository.findUnassignedAppointments(LocalDateTime.now(), pageable).map(AppointmentDto::from);
    }

    @Override
    public AppointmentDto assignSessionToCounselor(AssignAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + request.getAppointmentId()));
        
        User counselor = userRepository.findById(request.getCounselorId())
                .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + request.getCounselorId()));
        
        // Validate appointment is unassigned
        if (appointment.getCounselor() != null) {
            throw new IllegalStateException("Appointment is already assigned to a counselor");
        }
        
        appointment.setCounselor(counselor);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        
        log.info("Admin assigned appointment {} to counselor {}", appointment.getId(), counselor.getId());
        
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentDto counselorTakeAppointment(Long appointmentId, Long counselorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + appointmentId));
        
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new NoSuchElementException("Counselor not found with id: " + counselorId));
        
        // Validate appointment is unassigned
        if (appointment.getCounselor() != null) {
            throw new IllegalStateException("Appointment is already assigned to a counselor");
        }
        
        // Check counselor availability
        LocalDateTime appointmentTime = appointment.getAppointmentDate();
        LocalDateTime endTime = appointmentTime.plusMinutes(appointment.getDuration());
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(counselor, appointmentTime, endTime);
        
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Counselor has conflicting appointments at this time");
        }
        
        appointment.setCounselor(counselor);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        
        log.info("Counselor {} took appointment {}", counselor.getId(), appointment.getId());
        
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    @Override
    public Long countUnassignedAppointments() {
        return appointmentRepository.countUnassignedAppointments();
    }
    
    /**
     * Generate a unique meeting link using Jitsi Meet
     * Jitsi is free and doesn't require API keys
     */
    private String generateMeetingLink(Appointment appointment) {
        String roomName = generateRoomName(appointment);
        return String.format("https://%s/%s", jitsiDomain, roomName);
    }
    
    /**
     * Generate a unique room name for the meeting
     */
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
    
    /**
     * Create a counseling session from a confirmed appointment
     */
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
    
    /**
     * Map appointment type to session type
     */
    private Session.SessionType mapAppointmentTypeToSessionType(Appointment.AppointmentType appointmentType) {
        switch (appointmentType) {
            case INITIAL_CONSULTATION:
                return Session.SessionType.CONSULTATION;
            case FOLLOW_UP:
                return Session.SessionType.FOLLOW_UP;
            case GROUP_SESSION:
                return Session.SessionType.GROUP;
            case ASSESSMENT:
                return Session.SessionType.ASSESSMENT;
            case CRISIS_INTERVENTION:
                return Session.SessionType.CRISIS;
            default:
                return Session.SessionType.INDIVIDUAL;
        }
    }
    
    /**
     * Create a Client entity from a User entity
     */
    private Client createClientFromUser(User user) {
        Client newClient = new Client();
        newClient.setId(user.getId());
        newClient.setUsername(user.getUsername());
        newClient.setEmail(user.getEmail());
        newClient.setPassword(user.getPassword());
        newClient.setFirstName(user.getFirstName());
        newClient.setLastName(user.getLastName());
        newClient.setStudentId(user.getStudentId());
        newClient.setPhoneNumber(user.getPhoneNumber());
        newClient.setProfilePicture(user.getProfilePicture());
        newClient.setBio(user.getBio());
        newClient.setGender(user.getGender());
        newClient.setDateOfBirth(user.getDateOfBirth());
        newClient.setDepartment(user.getDepartment());
        newClient.setProgram(user.getProgram());
        newClient.setYearOfStudy(user.getYearOfStudy());
        newClient.setActive(user.getActive());
        newClient.setEmailVerified(user.getEmailVerified());
        newClient.setLastLogin(user.getLastLogin());
        newClient.setRoles(user.getRoles());
        newClient.setCreatedAt(user.getCreatedAt());
        newClient.setUpdatedAt(user.getUpdatedAt());
        newClient.setAvailableForAppointments(user.getAvailableForAppointments());
        newClient.setHasSignedConsent(user.getHasSignedConsent());
        // Client-specific fields
        newClient.setClientStatus(Client.ClientStatus.ACTIVE);
        newClient.setRiskLevel(Client.RiskLevel.LOW);
        newClient.setRiskScore(0);
        newClient.setTotalSessions(0);
        return newClient;
    }
}
