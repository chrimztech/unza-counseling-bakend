package zm.unza.counseling.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.AppointmentDto;
import zm.unza.counseling.dto.CreateAppointmentRequest;
import zm.unza.counseling.dto.UpdateAppointmentRequest;
import zm.unza.counseling.dto.request.AssignAppointmentRequest;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.ClientRepository;
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
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("Client not found with id: " + clientId));
        return appointmentRepository.findByClient(client, pageable).map(AppointmentDto::from);
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
        
        // Get or create Client record
        Client client = clientRepository.findById(user.getId())
                .orElseGet(() -> {
                    // Create a new Client record for this user
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
                    log.info("Created Client record for user: {}", principalName);
                    return clientRepository.save(newClient);
                });
        
        Appointment appointment = new Appointment();
        appointment.setTitle(request.getTitle());
        appointment.setStudent(user);
        appointment.setClient(client);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setType(request.getAppointmentType());
        appointment.setDescription(request.getDescription());
        appointment.setDuration(request.getDuration() != null ? request.getDuration() : 60);
        
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
        return appointmentRepository.findByStatus(Appointment.AppointmentStatus.SCHEDULED, pageable).map(AppointmentDto::from);
    }

    @Override
    public AppointmentDto cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentDto confirmAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        return AppointmentDto.from(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentDto rescheduleAppointment(Long id, UpdateAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found with id: " + id));
        appointment.setAppointmentDate(request.getAppointmentDate());
        if (request.getTitle() != null) {
            appointment.setTitle(request.getTitle());
        }
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
    public Map<String, Object> getAppointmentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        
        // Total appointments
        stats.put("totalAppointments", appointmentRepository.count());
        
        // Today's appointments
        stats.put("todayAppointments", appointmentRepository.findByAppointmentDateBetween(startOfDay, endOfDay, Pageable.unpaged()).getTotalElements());
        
        // Monthly appointments
        stats.put("monthlyAppointments", appointmentRepository.findByAppointmentDateBetween(startOfMonth, endOfMonth, Pageable.unpaged()).getTotalElements());
        
        // Status counts
        stats.put("scheduled", appointmentRepository.findByStatus(Appointment.AppointmentStatus.SCHEDULED, Pageable.unpaged()).getTotalElements());
        stats.put("confirmed", appointmentRepository.findByStatus(Appointment.AppointmentStatus.CONFIRMED, Pageable.unpaged()).getTotalElements());
        stats.put("completed", appointmentRepository.findByStatus(Appointment.AppointmentStatus.COMPLETED, Pageable.unpaged()).getTotalElements());
        stats.put("cancelled", appointmentRepository.findByStatus(Appointment.AppointmentStatus.CANCELLED, Pageable.unpaged()).getTotalElements());
        stats.put("unassigned", appointmentRepository.countUnassignedAppointments());
        
        return stats;
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
}
