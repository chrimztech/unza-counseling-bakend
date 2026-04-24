package zm.unza.counseling.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.PersonalDataFormRequest;
import zm.unza.counseling.dto.response.PersonalDataFormResponse;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.FamilyMember;
import zm.unza.counseling.entity.PersonalDataForm;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.CaseRepository;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.PersonalDataFormRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence for the digitized UNZA personal data form.
 */
@Service
public class PersonalDataFormService {

    private final PersonalDataFormRepository personalDataFormRepository;
    private final ClientRepository clientRepository;
    private final CaseRepository caseRepository;

    public PersonalDataFormService(
            PersonalDataFormRepository personalDataFormRepository,
            ClientRepository clientRepository,
            CaseRepository caseRepository
    ) {
        this.personalDataFormRepository = personalDataFormRepository;
        this.clientRepository = clientRepository;
        this.caseRepository = caseRepository;
    }

    @Transactional
    public PersonalDataFormResponse createPersonalDataForm(PersonalDataFormRequest request) {
        Client client = getClient(request.getClientId());

        PersonalDataForm form = new PersonalDataForm();
        form.setClient(client);
        copyRequestToForm(request, form);

        if (request.getCaseId() != null) {
            form.setCaseEntity(getCase(request.getCaseId()));
        }

        PersonalDataForm savedForm = personalDataFormRepository.save(form);
        return PersonalDataFormResponse.fromEntity(savedForm);
    }

    @Transactional
    public PersonalDataFormResponse createPersonalDataFormForClient(Long clientId, PersonalDataFormRequest request) {
        request.setClientId(clientId);
        return createPersonalDataForm(request);
    }

    public PersonalDataFormResponse getPersonalDataFormByClientId(Long clientId) {
        Client client = getClient(clientId);
        PersonalDataForm form = personalDataFormRepository.findByClient(client)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personal data form not found for client id: " + clientId
                ));
        return PersonalDataFormResponse.fromEntity(form);
    }

    public PersonalDataFormResponse getPersonalDataFormByClientFileNo(String clientFileNo) {
        PersonalDataForm form = personalDataFormRepository.findByClientFileNo(clientFileNo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personal data form not found for file number: " + clientFileNo
                ));
        return PersonalDataFormResponse.fromEntity(form);
    }

    public PersonalDataFormResponse getPersonalDataFormByCaseId(Long caseId) {
        Case caseEntity = getCase(caseId);
        PersonalDataForm form = personalDataFormRepository.findByCaseEntity(caseEntity)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personal data form not found for case id: " + caseId
                ));
        return PersonalDataFormResponse.fromEntity(form);
    }

    @Transactional
    public PersonalDataFormResponse updatePersonalDataForm(Long clientId, PersonalDataFormRequest request) {
        Client client = getClient(clientId);
        PersonalDataForm form = personalDataFormRepository.findByClient(client)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personal data form not found for client id: " + clientId
                ));

        copyRequestToForm(request, form);
        form.setClient(client);
        form.setCaseEntity(request.getCaseId() != null ? getCase(request.getCaseId()) : null);

        PersonalDataForm savedForm = personalDataFormRepository.save(form);
        return PersonalDataFormResponse.fromEntity(savedForm);
    }

    @Transactional
    public PersonalDataFormResponse linkToCase(Long formId, Long caseId) {
        PersonalDataForm form = personalDataFormRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personal data form not found with id: " + formId
                ));
        form.setCaseEntity(getCase(caseId));
        return PersonalDataFormResponse.fromEntity(personalDataFormRepository.save(form));
    }

    @Transactional
    public void deletePersonalDataForm(Long clientId) {
        Client client = getClient(clientId);
        PersonalDataForm form = personalDataFormRepository.findByClient(client)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Personal data form not found for client id: " + clientId
                ));
        personalDataFormRepository.delete(form);
    }

    private void copyRequestToForm(PersonalDataFormRequest request, PersonalDataForm form) {
        form.setClientFileNo(request.getClientFileNo());
        form.setDateOfInterview(request.getDateOfInterview());
        form.setSex(request.getSex());
        form.setYearOfBirth(request.getYearOfBirth());
        form.setAge(request.getAge());
        form.setSchool(request.getSchool());
        form.setComputerNo(request.getComputerNo());
        form.setYearOfStudy(request.getYearOfStudy());
        form.setStudentCategory(request.getStudentCategory());
        form.setOccupation(request.getOccupation());
        form.setContactAddress(request.getContactAddress());
        form.setPhoneNumber(request.getPhoneNumber());
        form.setMaritalStatus(request.getMaritalStatus());
        form.setPreviousCounselling(safeList(request.getPreviousCounselling()));
        form.setPreviousCounsellingOther(request.getPreviousCounsellingOther());
        form.setReferralSource(safeList(request.getReferralSource()));
        form.setReferralSourceOther(request.getReferralSourceOther());
        form.setReferralPointFrom(request.getReferralPointFrom());
        form.setReferralPointTo(request.getReferralPointTo());
        form.setReasonsForCounselling(mapReasons(request.getReasonsForCounselling()));
        form.setFamilyHistory(mapFamilyHistory(request.getFamilyHistory(), form));
        form.setHealthStatus(request.getHealthStatus());
        form.setHealthCondition(request.getHealthCondition());
        form.setTakingMedication(request.getTakingMedication());
        form.setMedicationDetails(request.getMedicationDetails());
        form.setAdditionalInfo(request.getAdditionalInfo());
    }

    private PersonalDataForm.ReasonsForCounselling mapReasons(
            PersonalDataFormRequest.ReasonsForCounsellingRequest request
    ) {
        if (request == null) {
            return null;
        }

        PersonalDataForm.ReasonsForCounselling reasons = new PersonalDataForm.ReasonsForCounselling();
        reasons.setPersonal(safeList(request.getPersonal()));
        reasons.setPersonalOther(request.getPersonalOther());
        reasons.setHealth(safeList(request.getHealth()));
        reasons.setHealthOther(request.getHealthOther());
        reasons.setEducational(safeList(request.getEducational()));
        reasons.setEducationalOther(request.getEducationalOther());
        reasons.setCareer(safeList(request.getCareer()));
        reasons.setCareerOther(request.getCareerOther());
        reasons.setFinancial(safeList(request.getFinancial()));
        reasons.setFinancialOther(request.getFinancialOther());
        return reasons;
    }

    private List<FamilyMember> mapFamilyHistory(
            List<PersonalDataFormRequest.FamilyMemberRequest> requests,
            PersonalDataForm form
    ) {
        List<FamilyMember> familyMembers = new ArrayList<>();
        if (requests == null) {
            return familyMembers;
        }

        for (PersonalDataFormRequest.FamilyMemberRequest familyMemberRequest : requests) {
            FamilyMember familyMember = new FamilyMember();
            familyMember.setName(familyMemberRequest.getName());
            familyMember.setRelationship(familyMemberRequest.getRelationship());
            familyMember.setAge(familyMemberRequest.getAge());
            familyMember.setEducation(familyMemberRequest.getEducation());
            familyMember.setOccupation(familyMemberRequest.getOccupation());
            familyMember.setContactPhone(familyMemberRequest.getContactPhone());
            familyMember.setContactAddress(familyMemberRequest.getContactAddress());
            familyMember.setPersonalDataForm(form);
            familyMembers.add(familyMember);
        }
        return familyMembers;
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

    private <T> List<T> safeList(List<T> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }
}
