package zm.unza.counseling.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.ClientIntakeFormRequest;
import zm.unza.counseling.dto.response.ClientIntakeFormResponse;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.ClientIntakeForm;
import zm.unza.counseling.entity.Counselor;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.CaseRepository;
import zm.unza.counseling.repository.ClientIntakeFormRepository;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.CounselorRepository;

/**
 * Handles persistence for the counselor-completed client intake form.
 */
@Service
public class ClientIntakeFormService {

    private final ClientIntakeFormRepository clientIntakeFormRepository;
    private final ClientRepository clientRepository;
    private final CaseRepository caseRepository;
    private final CounselorRepository counselorRepository;

    public ClientIntakeFormService(
            ClientIntakeFormRepository clientIntakeFormRepository,
            ClientRepository clientRepository,
            CaseRepository caseRepository,
            CounselorRepository counselorRepository
    ) {
        this.clientIntakeFormRepository = clientIntakeFormRepository;
        this.clientRepository = clientRepository;
        this.caseRepository = caseRepository;
        this.counselorRepository = counselorRepository;
    }

    @Transactional
    public ClientIntakeFormResponse createForClient(Long clientId, ClientIntakeFormRequest request) {
        if (clientIntakeFormRepository.existsByClientId(clientId)) {
            throw new ValidationException("Client intake form already exists for client id: " + clientId);
        }

        ClientIntakeForm form = new ClientIntakeForm();
        form.setClient(getClient(clientId));
        copyRequestToEntity(request, form);
        return ClientIntakeFormResponse.fromEntity(clientIntakeFormRepository.save(form));
    }

    public ClientIntakeFormResponse getByClientId(Long clientId) {
        ClientIntakeForm form = clientIntakeFormRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client intake form not found for client id: " + clientId
                ));
        return ClientIntakeFormResponse.fromEntity(form);
    }

    public ClientIntakeFormResponse getById(Long id) {
        ClientIntakeForm form = clientIntakeFormRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client intake form not found with id: " + id
                ));
        return ClientIntakeFormResponse.fromEntity(form);
    }

    public ClientIntakeFormResponse getByCaseId(Long caseId) {
        Case caseEntity = getCase(caseId);
        ClientIntakeForm form = clientIntakeFormRepository.findByCaseEntity(caseEntity)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client intake form not found for case id: " + caseId
                ));
        return ClientIntakeFormResponse.fromEntity(form);
    }

    @Transactional
    public ClientIntakeFormResponse update(Long id, ClientIntakeFormRequest request) {
        ClientIntakeForm form = clientIntakeFormRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client intake form not found with id: " + id
                ));
        copyRequestToEntity(request, form);
        return ClientIntakeFormResponse.fromEntity(clientIntakeFormRepository.save(form));
    }

    @Transactional
    public void delete(Long id) {
        ClientIntakeForm form = clientIntakeFormRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client intake form not found with id: " + id
                ));
        clientIntakeFormRepository.delete(form);
    }

    private void copyRequestToEntity(ClientIntakeFormRequest request, ClientIntakeForm form) {
        form.setCaseEntity(request.getCaseId() != null ? getCase(request.getCaseId()) : null);
        form.setCounselor(request.getCounselorId() != null ? getCounselor(request.getCounselorId()) : null);
        form.setClientFileNo(request.getClientFileNo());
        form.setSex(request.getSex());
        form.setAge(request.getAge());
        form.setMaritalStatus(request.getMaritalStatus());
        form.setComputerNo(request.getComputerNo());
        form.setYearOfStudy(request.getYearOfStudy());
        form.setSchool(request.getSchool());
        form.setHallRoomNo(request.getHallRoomNo());
        form.setContactPhoneNo(request.getContactPhoneNo());
        form.setPresentingConcern(request.getPresentingConcern());
        form.setProblemConceptualization(request.getProblemConceptualization());
        form.setTentativeGoalsDirections(request.getTentativeGoalsDirections());
        form.setCopingStrategies(request.getCopingStrategies());
        form.setActionTaken(request.getActionTaken());
        form.setTimeTakenCounselling(request.getTimeTakenCounselling());
        form.setNextContactAppointmentDate(request.getNextContactAppointmentDate());
        form.setNumberOfContactSessions(request.getNumberOfContactSessions());
        form.setCounsellorName(request.getCounsellorName());
        form.setFormDate(request.getFormDate());
    }

    private Client getClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with id: " + clientId
                ));
    }

    private Case getCase(Long caseId) {
        return caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Case not found with id: " + caseId
                ));
    }

    private Counselor getCounselor(Long counselorId) {
        return counselorRepository.findById(counselorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Counselor not found with id: " + counselorId
                ));
    }
}
