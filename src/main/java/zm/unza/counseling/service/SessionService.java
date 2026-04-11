package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.SessionDto;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Session;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.SessionRepository;
import zm.unza.counseling.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public Page<SessionDto> getAllSessions(Pageable pageable) {
        return sessionRepository.findAll(pageable).map(SessionDto::from);
    }

    public SessionDto getSessionById(Long id) {
        return sessionRepository.findById(id)
                .map(SessionDto::from)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
    }

    @Transactional
    public SessionDto createSession(SessionDto sessionDto) {
        Session session = new Session();
        
        if (sessionDto.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(sessionDto.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
            session.setAppointment(appointment);
            // Auto-populate from appointment if not provided
            if (sessionDto.getStudentId() == null) session.setStudent(appointment.getStudent());
            if (sessionDto.getCounselorId() == null) session.setCounselor(appointment.getCounselor());
            
            // Mark appointment as completed
            appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
        }

        if (session.getStudent() == null && sessionDto.getStudentId() != null) {
            User student = userRepository.findById(sessionDto.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
            session.setStudent(student);
        }

        if (session.getCounselor() == null && sessionDto.getCounselorId() != null) {
            User counselor = userRepository.findById(sessionDto.getCounselorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Counselor not found"));
            session.setCounselor(counselor);
        }

        session.setTitle(sessionDto.getTitle() != null ? sessionDto.getTitle() : "Counseling Session");
        session.setSessionDate(sessionDto.getSessionDate() != null ? sessionDto.getSessionDate() : LocalDateTime.now());
        session.setDurationMinutes(sessionDto.getDurationMinutes());
        
        if (sessionDto.getType() != null) {
            try {
                session.setType(Session.SessionType.valueOf(sessionDto.getType()));
            } catch (IllegalArgumentException e) {
                session.setType(Session.SessionType.INDIVIDUAL);
            }
        } else {
            session.setType(Session.SessionType.INDIVIDUAL);
        }
        
        if (sessionDto.getStatus() != null) {
            try {
                session.setStatus(Session.SessionStatus.valueOf(sessionDto.getStatus()));
            } catch (IllegalArgumentException e) {
                session.setStatus(Session.SessionStatus.COMPLETED);
            }
        } else {
            session.setStatus(Session.SessionStatus.COMPLETED);
        }
        
        session.setPresentingIssue(sessionDto.getPresentingIssue());
        
        if (sessionDto.getOutcome() != null) {
            try {
                session.setOutcome(Session.Outcome.valueOf(sessionDto.getOutcome()));
            } catch (IllegalArgumentException e) {
                // Handle invalid outcome
            }
        }
        
        session.setCreatedAt(LocalDateTime.now());
        session.setConfidential(true);

        Session savedSession = sessionRepository.save(session);
        return SessionDto.from(savedSession);
    }

    public void assignCounselor(Long sessionId, Long counselorId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new ResourceNotFoundException("Counselor not found"));
        session.setCounselor(counselor);
        sessionRepository.save(session);
    }
    
    @Transactional
    public SessionDto updateSession(Long id, SessionDto sessionDto) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        
        if (sessionDto.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(sessionDto.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
            session.setAppointment(appointment);
        }
        
        if (sessionDto.getStudentId() != null) {
            User student = userRepository.findById(sessionDto.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
            session.setStudent(student);
        }
        
        if (sessionDto.getCounselorId() != null) {
            User counselor = userRepository.findById(sessionDto.getCounselorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Counselor not found"));
            session.setCounselor(counselor);
        }
        
        session.setTitle(sessionDto.getTitle() != null ? sessionDto.getTitle() : session.getTitle());
        session.setSessionDate(sessionDto.getSessionDate() != null ? sessionDto.getSessionDate() : session.getSessionDate());
        session.setDurationMinutes(sessionDto.getDurationMinutes() != null ? sessionDto.getDurationMinutes() : session.getDurationMinutes());
        
        if (sessionDto.getType() != null) {
            try {
                session.setType(Session.SessionType.valueOf(sessionDto.getType()));
            } catch (IllegalArgumentException e) {
                // keep existing
            }
        }
        
        if (sessionDto.getStatus() != null) {
            try {
                session.setStatus(Session.SessionStatus.valueOf(sessionDto.getStatus()));
            } catch (IllegalArgumentException e) {
                // keep existing
            }
        }
        
        session.setPresentingIssue(sessionDto.getPresentingIssue() != null ? sessionDto.getPresentingIssue() : session.getPresentingIssue());
        
        if (sessionDto.getOutcome() != null) {
            try {
                session.setOutcome(Session.Outcome.valueOf(sessionDto.getOutcome()));
            } catch (IllegalArgumentException e) {
                // Handle invalid outcome
            }
        }
        
        Session updatedSession = sessionRepository.save(session);
        return SessionDto.from(updatedSession);
    }
    
    public void deleteSession(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        sessionRepository.delete(session);
    }
    
    public Page<SessionDto> getSessionsByClient(Long clientId, Pageable pageable) {
        return sessionRepository.findByStudentId(clientId, pageable).map(SessionDto::from);
    }
}