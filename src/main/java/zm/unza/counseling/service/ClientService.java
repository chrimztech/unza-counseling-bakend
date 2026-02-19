package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.CreateCaseRequest;
import zm.unza.counseling.dto.request.CreateClientRequest;
import zm.unza.counseling.dto.request.RegisterRequest;
import zm.unza.counseling.dto.response.CaseResponse;
import zm.unza.counseling.dto.response.ClientResponse;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.ClientRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final CaseService caseService;

    public Client createClient(RegisterRequest request) {
        log.info("Creating new client: {}", request.getEmail());

        Client client = new Client();
        client.setEmail(request.getEmail());
        client.setPassword(passwordEncoder.encode(request.getPassword()));
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setPhoneNumber(request.getPhoneNumber());
        client.setStudentId(request.getStudentId());
        client.setActive(true);
        client.setClientStatus(Client.ClientStatus.ACTIVE);
        client.setRegistrationDate(LocalDateTime.now());

        return clientRepository.save(client);
    }

    /**
     * Create a new client with optional automatic case creation
     * @param request the client creation request
     * @return ClientResponse with client and case details
     */
    public ClientWithCaseResponse createClientWithCase(CreateClientRequest request) {
        log.info("Creating new client with case option: {}", request.getEmail());

        // Create the client
        Client client = new Client();
        client.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            client.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setPhoneNumber(request.getPhoneNumber());
        client.setStudentId(request.getStudentId());
        client.setProgramme(request.getProgramme());
        client.setFaculty(request.getFaculty());
        client.setYearOfStudy(request.getYearOfStudy());
        client.setGpa(request.getGpa());
        client.setActive(true);
        client.setClientStatus(Client.ClientStatus.ACTIVE);
        client.setRegistrationDate(LocalDateTime.now());

        Client savedClient = clientRepository.save(client);

        // Auto-create case if requested (default is true)
        CaseResponse caseResponse = null;
        if (request.getCreateCase() == null || request.getCreateCase()) {
            CreateCaseRequest caseRequest = new CreateCaseRequest();
            caseRequest.setClientId(savedClient.getId());
            caseRequest.setSubject(request.getCaseSubject() != null ? 
                request.getCaseSubject() : "Initial Counselling Request");
            caseRequest.setDescription(request.getCaseDescription() != null ? 
                request.getCaseDescription() : extractDescriptionFromReasons(request.getReasonsForCounselling()));
            caseRequest.setPriority(Case.CasePriority.MEDIUM);

            caseResponse = caseService.createCase(caseRequest);
            log.info("Auto-created case {} for client {}", caseResponse.getCaseNumber(), savedClient.getId());
        }

        return new ClientWithCaseResponse(savedClient, caseResponse);
    }

    /**
     * Extract a description from the reasons for counselling
     */
    private String extractDescriptionFromReasons(CreateClientRequest.ReasonsForCounsellingRequest reasons) {
        if (reasons == null) {
            return "Client seeking counselling services.";
        }

        StringBuilder description = new StringBuilder("Client is seeking counselling for: ");

        if (reasons.getPersonal() != null && !reasons.getPersonal().isEmpty()) {
            description.append("\nPersonal reasons: ").append(String.join(", ", reasons.getPersonal()));
            if (reasons.getPersonalOther() != null) {
                description.append(" (").append(reasons.getPersonalOther()).append(")");
            }
        }
        if (reasons.getHealth() != null && !reasons.getHealth().isEmpty()) {
            description.append("\nHealth reasons: ").append(String.join(", ", reasons.getHealth()));
            if (reasons.getHealthOther() != null) {
                description.append(" (").append(reasons.getHealthOther()).append(")");
            }
        }
        if (reasons.getEducational() != null && !reasons.getEducational().isEmpty()) {
            description.append("\nEducational reasons: ").append(String.join(", ", reasons.getEducational()));
            if (reasons.getEducationalOther() != null) {
                description.append(" (").append(reasons.getEducationalOther()).append(")");
            }
        }
        if (reasons.getCareer() != null && !reasons.getCareer().isEmpty()) {
            description.append("\nCareer reasons: ").append(String.join(", ", reasons.getCareer()));
            if (reasons.getCareerOther() != null) {
                description.append(" (").append(reasons.getCareerOther()).append(")");
            }
        }
        if (reasons.getFinancial() != null && !reasons.getFinancial().isEmpty()) {
            description.append("\nFinancial reasons: ").append(String.join(", ", reasons.getFinancial()));
            if (reasons.getFinancialOther() != null) {
                description.append(" (").append(reasons.getFinancialOther()).append(")");
            }
        }

        return description.toString();
    }

    /**
     * Response class that includes both client and case information
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ClientWithCaseResponse {
        private Client client;
        private CaseResponse createdCase;
    }

    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with ID: " + id));
    }

    public Client getClientByStudentId(String studentId) {
        return clientRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with student ID: " + studentId));
    }

    public Page<Client> getAllClients(Pageable pageable) {
        return clientRepository.findAll(pageable);
    }

    public Page<Client> searchClients(String searchTerm, Pageable pageable) {
        return clientRepository.searchClients(searchTerm, pageable);
    }

    public Page<Client> getClientsByStatus(Client.ClientStatus status, Pageable pageable) {
        return clientRepository.findByClientStatus(status, pageable);
    }

    public Page<Client> getClientsByRiskLevel(Client.RiskLevel riskLevel, Pageable pageable) {
        return clientRepository.findByRiskLevel(riskLevel, pageable);
    }

    public Client updateClient(Long id, Client updates) {
        Client client = getClientById(id);

        if (updates.getFirstName() != null) client.setFirstName(updates.getFirstName());
        if (updates.getLastName() != null) client.setLastName(updates.getLastName());
        if (updates.getPhoneNumber() != null) client.setPhoneNumber(updates.getPhoneNumber());
        if (updates.getProgramme() != null) client.setProgramme(updates.getProgramme());
        if (updates.getFaculty() != null) client.setFaculty(updates.getFaculty());
        if (updates.getYearOfStudy() != null) client.setYearOfStudy(updates.getYearOfStudy());
        if (updates.getGpa() != null) client.setGpa(updates.getGpa());

        return clientRepository.save(client);
    }

    public void updateRiskLevel(Long clientId, Client.RiskLevel riskLevel, Integer riskScore) {
        Client client = getClientById(clientId);
        client.setRiskLevel(riskLevel);
        client.setRiskScore(riskScore);
        clientRepository.save(client);

        log.info("Updated risk level for client {} to {}", clientId, riskLevel);
    }

    public void incrementSessionCount(Long clientId) {
        Client client = getClientById(clientId);
        client.setTotalSessions(client.getTotalSessions() + 1);
        client.setLastSessionDate(LocalDateTime.now());
        clientRepository.save(client);
    }

    public Long getActiveClientCount() {
        return clientRepository.countByClientStatus(Client.ClientStatus.ACTIVE);
    }

    public Long getHighRiskClientCount() {
        return clientRepository.countByRiskLevels(
                List.of(Client.RiskLevel.HIGH, Client.RiskLevel.CRITICAL)
        );
    }

    public Client updateClientRiskLevel(Long clientId, Client.RiskLevel riskLevel) {
        Client client = getClientById(clientId);
        client.setRiskLevel(riskLevel);
        return clientRepository.save(client);
    }
}
