package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.SessionNoteDto;
import zm.unza.counseling.dto.request.SessionNoteRequest;
import zm.unza.counseling.entity.Session;
import zm.unza.counseling.entity.SessionNote;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.SessionNoteRepository;
import zm.unza.counseling.repository.SessionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionNoteService {

    private final SessionNoteRepository sessionNoteRepository;
    private final SessionRepository sessionRepository;

    public Page<SessionNoteDto> getAllNotes(Pageable pageable) {
        return sessionNoteRepository.findAll(pageable).map(SessionNoteDto::from);
    }

    public List<SessionNoteDto> getNotesBySession(Long sessionId) {
        return sessionNoteRepository.findBySessionId(sessionId).stream()
                .map(SessionNoteDto::from)
                .toList();
    }

    @Transactional
    public SessionNoteDto createNote(SessionNoteRequest request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + request.getSessionId()));

        SessionNote note = new SessionNote();
        note.setSession(session);
        note.setContent(request.getContent());
        note.setPrivateNotes(request.getPrivateNotes());
        note.setNextSteps(request.getNextSteps());

        return SessionNoteDto.from(sessionNoteRepository.save(note));
    }

    @Transactional
    public SessionNoteDto updateNote(Long noteId, SessionNoteRequest request) {
        SessionNote note = sessionNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Session note not found with id: " + noteId));

        if (request.getContent() != null) note.setContent(request.getContent());
        if (request.getPrivateNotes() != null) note.setPrivateNotes(request.getPrivateNotes());
        if (request.getNextSteps() != null) note.setNextSteps(request.getNextSteps());

        return SessionNoteDto.from(sessionNoteRepository.save(note));
    }
}
