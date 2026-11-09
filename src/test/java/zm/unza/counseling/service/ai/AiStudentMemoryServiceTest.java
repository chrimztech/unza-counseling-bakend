package zm.unza.counseling.service.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import zm.unza.counseling.entity.*;
import zm.unza.counseling.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AiStudentMemoryServiceTest {

    private AiUserMemoryRepository memoryRepo;
    private ChatMessageRepository chatRepo;
    private SelfAssessmentRepository selfRepo;
    private AiGatewayService gateway;
    private AiStudentMemoryService service;

    @BeforeEach
    void setUp() {
        memoryRepo = mock(AiUserMemoryRepository.class);
        chatRepo = mock(ChatMessageRepository.class);
        AiAssistantSettingsRepository settingsRepo = mock(AiAssistantSettingsRepository.class);
        when(settingsRepo.findById(AiAssistantSettings.SINGLETON_ID)).thenReturn(Optional.of(new AiAssistantSettings()));
        selfRepo = mock(SelfAssessmentRepository.class);
        gateway = mock(AiGatewayService.class);
        service = new AiStudentMemoryService(memoryRepo, chatRepo, settingsRepo, selfRepo,
                mock(AppointmentRepository.class), mock(ClientRepository.class),
                mock(MentalHealthAcademicAnalysisRepository.class), gateway);
    }

    private static List<ChatMessage> messages(long fromId, long toId) {
        List<ChatMessage> out = new ArrayList<>();
        for (long i = fromId; i <= toId; i++) {
            ChatMessage m = new ChatMessage();
            m.setId(i);
            m.setUserId(7L);
            m.setMessage("student message " + i);
            m.setResponse("assistant reply " + i);
            m.setCreatedAt(LocalDateTime.now().minusDays(30 - i));
            out.add(m);
        }
        return out;
    }

    @Test
    void sendsEveryUnsummarisedExchangeVerbatim() {
        when(memoryRepo.findById(7L)).thenReturn(Optional.empty());
        when(chatRepo.findByUserIdAndIdGreaterThanOrderByIdAsc(7L, 0L)).thenReturn(messages(1, 14));

        List<AiMessage> history = service.verbatimHistory(7L);

        assertEquals(28, history.size(), "14 exchanges = 28 turns, none dropped");
        assertEquals("student message 1", history.get(0).content());
        assertEquals("assistant reply 14", history.get(27).content());
    }

    @Test
    void foldsOldestIntoSummaryAndKeepsNewestTenVerbatim() {
        when(memoryRepo.findById(7L)).thenReturn(Optional.empty());
        when(chatRepo.findByUserIdAndIdGreaterThanOrderByIdAsc(7L, 0L)).thenReturn(messages(1, 20));
        when(gateway.chat(anyString(), anyList(), anyInt()))
                .thenReturn(new AiResult("- Worried about fees (Nov)", 1L, "p", "m", 5));

        service.maybeFoldAsync(7L);   // called directly: runs synchronously in the test

        ArgumentCaptor<AiUserMemory> saved = ArgumentCaptor.forClass(AiUserMemory.class);
        verify(memoryRepo).save(saved.capture());
        assertEquals(10L, saved.getValue().getSummarizedThroughId(), "messages 1-10 folded, 11-20 stay verbatim");
        assertEquals(10, saved.getValue().getSummarizedCount());
        assertEquals("- Worried about fees (Nov)", saved.getValue().getSummary());

        // The transcript sent for summarising contains the folded messages only.
        ArgumentCaptor<List<AiMessage>> sent = ArgumentCaptor.forClass(List.class);
        verify(gateway).chat(anyString(), sent.capture(), anyInt());
        String transcript = sent.getValue().get(0).content();
        assertTrue(transcript.contains("student message 10"));
        assertFalse(transcript.contains("student message 11"));
    }

    @Test
    void doesNotFoldUntilThresholdIsPassed() {
        when(memoryRepo.findById(7L)).thenReturn(Optional.empty());
        when(chatRepo.findByUserIdAndIdGreaterThanOrderByIdAsc(7L, 0L)).thenReturn(messages(1, 15));

        service.maybeFoldAsync(7L);

        verifyNoInteractions(gateway);
        verify(memoryRepo, never()).save(any());
    }

    @Test
    void personalContextCombinesMemoryAndRecords() {
        AiUserMemory memory = new AiUserMemory();
        memory.setUserId(7L);
        memory.setSummary("- Final-year student worried about K8,000 fees");
        when(memoryRepo.findById(7L)).thenReturn(Optional.of(memory));
        SelfAssessment a = new SelfAssessment();
        a.setPhq9Score(12);
        a.setGad7Score(16);
        a.setAssessmentDate(LocalDateTime.of(2026, 11, 1, 10, 0));
        when(selfRepo.findTopBySubmittedByUserIdOrderByAssessmentDateDesc(7L)).thenReturn(Optional.of(a));

        User user = new User();
        user.setId(7L);
        user.setFirstName("Mwila");

        String ctx = service.personalContext(user);
        System.out.println(ctx);

        assertNotNull(ctx);
        assertTrue(ctx.contains("K8,000 fees"));
        assertTrue(ctx.contains("First name: Mwila"));
        assertTrue(ctx.contains("PHQ-9 (depression) 12/27, moderate"));
        assertTrue(ctx.contains("GAD-7 (anxiety) 16/21, severe"));
        assertTrue(ctx.contains("No upcoming counselling appointment"));
    }

    @Test
    void anonymousUsersGetNoRecords() {
        when(memoryRepo.findById(9L)).thenReturn(Optional.empty());
        User anon = new User();
        anon.setId(9L);
        anon.setFirstName("Should not appear");
        anon.setAnonymous(true);

        assertNull(service.personalContext(anon));
        verifyNoInteractions(selfRepo);
    }
}
