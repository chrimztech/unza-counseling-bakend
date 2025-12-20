package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.RegisterRequest;
import zm.unza.counseling.dto.response.ClientResponse;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.ClientRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

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
}
