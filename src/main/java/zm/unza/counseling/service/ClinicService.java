package zm.unza.counseling.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import zm.unza.counseling.dto.request.ClinicReferralRequest;
import zm.unza.counseling.dto.request.ClinicVisitRequest;
import zm.unza.counseling.dto.response.ClinicReferralResponse;
import zm.unza.counseling.dto.response.ClinicVisitFrequencyResponse;
import zm.unza.counseling.dto.response.ClinicVisitResponse;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.ClinicReferral;
import zm.unza.counseling.entity.ClinicVisit;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.CaseRepository;
import zm.unza.counseling.repository.ClinicReferralRepository;
import zm.unza.counseling.repository.ClinicVisitRepository;
import zm.unza.counseling.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClinicService {

    private final ClinicReferralRepository referralRepo;
    private final ClinicVisitRepository visitRepo;
    private final UserRepository userRepo;
    private final CaseRepository caseRepo;

    public ClinicService(ClinicReferralRepository referralRepo,
                         ClinicVisitRepository visitRepo,
                         UserRepository userRepo,
                         CaseRepository caseRepo) {
        this.referralRepo = referralRepo;
        this.visitRepo = visitRepo;
        this.userRepo = userRepo;
        this.caseRepo = caseRepo;
    }

    // ── Referrals ─────────────────────────────────────────────────────────

    public ClinicReferralResponse createReferral(ClinicReferralRequest request, Long counselorId) {
        User client = userRepo.findById(request.getClientId())
                .orElseThrow(() -> new NoSuchElementException("Client not found: " + request.getClientId()));
        User counselor = userRepo.findById(counselorId)
                .orElseThrow(() -> new NoSuchElementException("Counselor not found: " + counselorId));

        ClinicReferral referral = new ClinicReferral();
        referral.setReferralNumber(generateReferralNumber());
        referral.setClient(client);
        referral.setCounselor(counselor);
        referral.setReason(request.getReason());
        referral.setClinicalNotes(request.getClinicalNotes());
        referral.setUrgency(request.getUrgency() != null ? request.getUrgency() : ClinicReferral.Urgency.ROUTINE);
        referral.setStatus(ClinicReferral.ReferralStatus.PENDING);
        referral.setClinicAppointmentDate(request.getClinicAppointmentDate());

        if (request.getCaseId() != null) {
            Case linkedCase = caseRepo.findById(request.getCaseId())
                    .orElseThrow(() -> new NoSuchElementException("Case not found: " + request.getCaseId()));
            referral.setLinkedCase(linkedCase);
        }

        return ClinicReferralResponse.from(referralRepo.save(referral));
    }

    public ClinicReferralResponse getReferralById(Long id) {
        return ClinicReferralResponse.from(findReferral(id));
    }

    public Page<ClinicReferralResponse> getAllReferrals(Pageable pageable) {
        return referralRepo.findAllOrderByCreatedAtDesc(pageable).map(ClinicReferralResponse::from);
    }

    public List<ClinicReferralResponse> getReferralsByClient(Long clientId) {
        return referralRepo.findByClientIdOrderByCreatedAtDesc(clientId)
                .stream().map(ClinicReferralResponse::from).collect(Collectors.toList());
    }

    public Page<ClinicReferralResponse> getReferralsByCase(Long caseId, Pageable pageable) {
        return referralRepo.findByLinkedCaseId(caseId, pageable).map(ClinicReferralResponse::from);
    }

    public ClinicReferralResponse updateReferralStatus(Long id,
                                                        ClinicReferral.ReferralStatus newStatus,
                                                        String clinicNotes,
                                                        String externalReferenceId) {
        ClinicReferral referral = findReferral(id);
        referral.setStatus(newStatus);
        if (clinicNotes != null) referral.setClinicNotes(clinicNotes);
        if (externalReferenceId != null) referral.setExternalReferenceId(externalReferenceId);
        if (newStatus == ClinicReferral.ReferralStatus.SENT && referral.getSentAt() == null) {
            referral.setSentAt(LocalDateTime.now());
        }
        if (newStatus == ClinicReferral.ReferralStatus.ACCEPTED
                || newStatus == ClinicReferral.ReferralStatus.DECLINED) {
            referral.setRespondedAt(LocalDateTime.now());
        }
        return ClinicReferralResponse.from(referralRepo.save(referral));
    }

    public void deleteReferral(Long id) {
        referralRepo.delete(findReferral(id));
    }

    // ── Clinic Visits ─────────────────────────────────────────────────────

    public ClinicVisitResponse recordVisit(ClinicVisitRequest request) {
        User client = userRepo.findById(request.getClientId())
                .orElseThrow(() -> new NoSuchElementException("Client not found: " + request.getClientId()));

        ClinicVisit visit = new ClinicVisit();
        visit.setClient(client);
        visit.setVisitDate(request.getVisitDate());
        visit.setVisitType(request.getVisitType() != null ? request.getVisitType() : ClinicVisit.VisitType.GENERAL);
        visit.setVisitPurpose(request.getVisitPurpose());
        visit.setNotes(request.getNotes());
        visit.setRecordedBy(request.getRecordedBy() != null ? request.getRecordedBy() : "MANUAL");
        visit.setCounselorNotified(false);

        if (request.getReferralId() != null) {
            ClinicReferral referral = findReferral(request.getReferralId());
            visit.setReferral(referral);
            // Auto-complete the referral if it was pending/sent
            if (referral.getStatus() == ClinicReferral.ReferralStatus.SENT
                    || referral.getStatus() == ClinicReferral.ReferralStatus.ACCEPTED) {
                referral.setStatus(ClinicReferral.ReferralStatus.COMPLETED);
                referral.setRespondedAt(LocalDateTime.now());
                referralRepo.save(referral);
            }
        }

        return ClinicVisitResponse.from(visitRepo.save(visit));
    }

    public List<ClinicVisitResponse> getVisitsByClient(Long clientId) {
        return visitRepo.findByClientIdOrderByVisitDateDesc(clientId)
                .stream().map(ClinicVisitResponse::from).collect(Collectors.toList());
    }

    public ClinicVisitFrequencyResponse getVisitFrequency(Long clientId) {
        User client = userRepo.findById(clientId)
                .orElseThrow(() -> new NoSuchElementException("Client not found: " + clientId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOf30  = now.minusDays(30);
        LocalDateTime startOf90  = now.minusDays(90);
        LocalDateTime startOfYear = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        long total       = visitRepo.countByClientId(clientId);
        long last30      = visitRepo.countByClientIdAndDateRange(clientId, startOf30, now);
        long last90      = visitRepo.countByClientIdAndDateRange(clientId, startOf90, now);
        long thisYear    = visitRepo.countByClientIdAndDateRange(clientId, startOfYear, now);
        boolean frequent = last90 >= 3;

        // Month breakdown
        List<Object[]> monthly = visitRepo.countByClientIdGroupedByMonth(clientId);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Long> byMonth = new LinkedHashMap<>();
        for (Object[] row : monthly) {
            // row[0] is a Timestamp from DATE_TRUNC
            String month = ((java.sql.Timestamp) row[0]).toLocalDateTime().format(fmt);
            byMonth.put(month, ((Number) row[1]).longValue());
        }

        // Recent 10 visits
        List<ClinicVisitResponse> recent = visitRepo
                .findByClientIdAndDateRange(clientId, now.minusYears(1), now)
                .stream().limit(10).map(ClinicVisitResponse::from).collect(Collectors.toList());

        ClinicVisitFrequencyResponse freq = new ClinicVisitFrequencyResponse();
        freq.setClientId(clientId);
        freq.setClientName(client.getFullName());
        freq.setClientEmail(client.getEmail());
        freq.setTotalVisits(total);
        freq.setVisitsLast30Days(last30);
        freq.setVisitsLast90Days(last90);
        freq.setVisitsThisYear(thisYear);
        freq.setFrequentVisitor(frequent);
        freq.setVisitsByMonth(byMonth);
        freq.setRecentVisits(recent);
        return freq;
    }

    public List<Map<String, Object>> getFrequentVisitors(int thresholdVisits, int withinDays) {
        LocalDateTime from = LocalDateTime.now().minusDays(withinDays);
        List<Object[]> rows = visitRepo.findFrequentVisitors(from, LocalDateTime.now(), thresholdVisits);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long cid = ((Number) row[0]).longValue();
            long cnt = ((Number) row[1]).longValue();
            userRepo.findById(cid).ifPresent(u -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("clientId", cid);
                entry.put("clientName", u.getFullName());
                entry.put("clientEmail", u.getEmail());
                entry.put("visitCount", cnt);
                entry.put("periodDays", withinDays);
                result.add(entry);
            });
        }
        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private ClinicReferral findReferral(Long id) {
        return referralRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Clinic referral not found: " + id));
    }

    private String generateReferralNumber() {
        return "REF-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 9000 + 1000);
    }
}
