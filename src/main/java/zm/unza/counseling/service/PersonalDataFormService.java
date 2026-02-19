package zm.unza.counseling.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.PersonalDataFormRequest;
import zm.unza.counseling.dto.response.PersonalDataFormResponse;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.FamilyMember;
import zm.unza.counseling.entity.PersonalDataForm;
import zm.unza.counseling.repository.CaseRepository;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.PersonalDataFormRepository;
import zm.unza.counseling.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Personal Data Form Service - Handles business logic for PersonalDataForm entities
 * This service manages the client intake form and links it to cases
 */
@Service
public class PersonalDataFormService {

    @Autowired
    private PersonalDataFormRepository personalDataFormRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CaseRepository caseRepository;

    /**
     * Creates a new personal data form (client intake form)
     * @param request the request DTO containing all form data
     * @return the created personal data form response
     */
    @Transactional
    public PersonalDataFormResponse createPersonalDataForm(PersonalDataFormRequest request) {
        // Get the client
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        // Create the form entity
        PersonalDataForm form = new PersonalDataForm();
        form.setClient(client);
        form.setClientFileNo(request.getClientFileNo());
        form.setComputerNo(request.getComputerNo());
        form.setOccupation(request.getOccupation());
        form.setContactAddress(request.getContactAddress());
        form.setMaritalStatus(request.getMaritalStatus());
        
        // Previous counselling
        form.setPreviousCounselling(request.getPreviousCounselling());
        form.setPreviousCounsellingOther(request.getPreviousCounsellingOther());
        
        // Referral source
        form.setReferralSource(request.getReferralSource());
        form.setReferralSourceOther(request.getReferralSourceOther());
        
        // Reasons for counselling (nested object)
        if (request.getReasonsForCounselling() != null) {
            PersonalDataForm.ReasonsForCounselling reasons = new PersonalDataForm.ReasonsForCounselling();
            PersonalDataFormRequest.ReasonsForCounsellingRequest reqReasons = request.getReasonsForCounselling();
            
            reasons.setPersonal(reqReasons.getPersonal());
            reasons.setPersonalOther(reqReasons.getPersonalOther());
            reasons.setHealth(reqReasons.getHealth());
            reasons.setHealthOther(reqReasons.getHealthOther());
            reasons.setEducational(reqReasons.getEducational());
            reasons.setEducationalOther(reqReasons.getEducationalOther());
            reasons.setCareer(reqReasons.getCareer());
            reasons.setCareerOther(reqReasons.getCareerOther());
            reasons.setFinancial(reqReasons.getFinancial());
            reasons.setFinancialOther(reqReasons.getFinancialOther());
            
            form.setReasonsForCounselling(reasons);
        }
        
        // Family history - save form first to get ID
        PersonalDataForm savedForm = personalDataFormRepository.save(form);
        
        // Now add family members
        if (request.getFamilyHistory() != null && !request.getFamilyHistory().isEmpty()) {
            List<FamilyMember> familyMembers = new ArrayList<>();
            for (PersonalDataFormRequest.FamilyMemberRequest fmReq : request.getFamilyHistory()) {
                FamilyMember fm = new FamilyMember();
                fm.setName(fmReq.getName());
                fm.setRelationship(fmReq.getRelationship());
                fm.setAge(fmReq.getAge());
                fm.setEducation(fmReq.getEducation());
                fm.setOccupation(fmReq.getOccupation());
                fm.setPersonalDataForm(savedForm);
                familyMembers.add(fm);
            }
            savedForm.setFamilyHistory(familyMembers);
        }
        
        // Health information
        savedForm.setHealthStatus(request.getHealthStatus());
        savedForm.setHealthCondition(request.getHealthCondition());
        savedForm.setTakingMedication(request.getTakingMedication());
        savedForm.setMedicationDetails(request.getMedicationDetails());
        
        // Additional info
        savedForm.setAdditionalInfo(request.getAdditionalInfo());
        
        // Link to case if caseId is provided
        if (request.getCaseId() != null) {
            Case caseEntity = caseRepository.findById(request.getCaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Case not found with id: " + request.getCaseId()));
            savedForm.setCaseEntity(caseEntity);
        }
        
        savedForm = personalDataFormRepository.save(savedForm);
        return PersonalDataFormResponse.fromEntity(savedForm);
    }

    /**
     * Creates a new personal data form for a specific client
     * @param clientId the ID of the client
     * @param request the request DTO
     * @return the created personal data form response
     */
    @Transactional
    public PersonalDataFormResponse createPersonalDataFormForClient(Long clientId, PersonalDataFormRequest request) {
        request.setClientId(clientId);
        return createPersonalDataForm(request);
    }

    /**
     * Gets a personal data form by client ID
     * @param clientId the ID of the client
     * @return the personal data form response
     */
    public PersonalDataFormResponse getPersonalDataFormByClientId(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        PersonalDataForm form = personalDataFormRepository.findByClient(client)
                .orElseThrow(() -> new ResourceNotFoundException("Personal data form not found for client id: " + clientId));

        return PersonalDataFormResponse.fromEntity(form);
    }

    /**
     * Gets a personal data form by client file number
     * @param clientFileNo the client file number
     * @return the personal data form response
     */
    public PersonalDataFormResponse getPersonalDataFormByClientFileNo(String clientFileNo) {
        PersonalDataForm form = personalDataFormRepository.findByClientFileNo(clientFileNo)
                .orElseThrow(() -> new ResourceNotFoundException("Personal data form not found for file number: " + clientFileNo));
        return PersonalDataFormResponse.fromEntity(form);
    }

    /**
     * Gets a personal data form by case ID
     * @param caseId the ID of the case
     * @return the personal data form response
     */
    public PersonalDataFormResponse getPersonalDataFormByCaseId(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with id: " + caseId));

        PersonalDataForm form = personalDataFormRepository.findByCaseEntity(caseEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Personal data form not found for case id: " + caseId));

        return PersonalDataFormResponse.fromEntity(form);
    }

    /**
     * Updates a personal data form
     * @param clientId the ID of the client
     * @param request the request DTO
     * @return the updated personal data form response
     */
    @Transactional
    public PersonalDataFormResponse updatePersonalDataForm(Long clientId, PersonalDataFormRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        PersonalDataForm form = personalDataFormRepository.findByClient(client)
                .orElseThrow(() -> new ResourceNotFoundException("Personal data form not found for client id: " + clientId));

        // Update basic info
        form.setClientFileNo(request.getClientFileNo());
        form.setComputerNo(request.getComputerNo());
        form.setOccupation(request.getOccupation());
        form.setContactAddress(request.getContactAddress());
        form.setMaritalStatus(request.getMaritalStatus());
        
        // Update previous counselling
        form.setPreviousCounselling(request.getPreviousCounselling());
        form.setPreviousCounsellingOther(request.getPreviousCounsellingOther());
        
        // Update referral source
        form.setReferralSource(request.getReferralSource());
        form.setReferralSourceOther(request.getReferralSourceOther());
        
        // Update reasons for counselling
        if (request.getReasonsForCounselling() != null) {
            PersonalDataForm.ReasonsForCounselling reasons = form.getReasonsForCounselling();
            if (reasons == null) {
                reasons = new PersonalDataForm.ReasonsForCounselling();
            }
            PersonalDataFormRequest.ReasonsForCounsellingRequest reqReasons = request.getReasonsForCounselling();
            
            reasons.setPersonal(reqReasons.getPersonal());
            reasons.setPersonalOther(reqReasons.getPersonalOther());
            reasons.setHealth(reqReasons.getHealth());
            reasons.setHealthOther(reqReasons.getHealthOther());
            reasons.setEducational(reqReasons.getEducational());
            reasons.setEducationalOther(reqReasons.getEducationalOther());
            reasons.setCareer(reqReasons.getCareer());
            reasons.setCareerOther(reqReasons.getCareerOther());
            reasons.setFinancial(reqReasons.getFinancial());
            reasons.setFinancialOther(reqReasons.getFinancialOther());
            
            form.setReasonsForCounselling(reasons);
        }
        
        // Update family history
        if (request.getFamilyHistory() != null) {
            form.getFamilyHistory().clear();
            for (PersonalDataFormRequest.FamilyMemberRequest fmReq : request.getFamilyHistory()) {
                FamilyMember fm = new FamilyMember();
                fm.setName(fmReq.getName());
                fm.setRelationship(fmReq.getRelationship());
                fm.setAge(fmReq.getAge());
                fm.setEducation(fmReq.getEducation());
                fm.setOccupation(fmReq.getOccupation());
                fm.setPersonalDataForm(form);
                form.getFamilyHistory().add(fm);
            }
        }
        
        // Update health information
        form.setHealthStatus(request.getHealthStatus());
        form.setHealthCondition(request.getHealthCondition());
        form.setTakingMedication(request.getTakingMedication());
        form.setMedicationDetails(request.getMedicationDetails());
        
        // Update additional info
        form.setAdditionalInfo(request.getAdditionalInfo());
        
        // Update case link if provided
        if (request.getCaseId() != null) {
            Case caseEntity = caseRepository.findById(request.getCaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Case not found with id: " + request.getCaseId()));
            form.setCaseEntity(caseEntity);
        }

        PersonalDataForm savedForm = personalDataFormRepository.save(form);
        return PersonalDataFormResponse.fromEntity(savedForm);
    }

    /**
     * Links a personal data form to a case
     * @param formId the ID of the personal data form
     * @param caseId the ID of the case
     * @return the updated personal data form response
     */
    @Transactional
    public PersonalDataFormResponse linkToCase(Long formId, Long caseId) {
        PersonalDataForm form = personalDataFormRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Personal data form not found with id: " + formId));
        
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with id: " + caseId));
        
        form.setCaseEntity(caseEntity);
        PersonalDataForm savedForm = personalDataFormRepository.save(form);
        
        return PersonalDataFormResponse.fromEntity(savedForm);
    }

    /**
     * Deletes a personal data form by client ID
     * @param clientId the ID of the client
     */
    @Transactional
    public void deletePersonalDataForm(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        PersonalDataForm form = personalDataFormRepository.findByClient(client)
                .orElseThrow(() -> new ResourceNotFoundException("Personal data form not found for client id: " + clientId));

        personalDataFormRepository.delete(form);
    }
}
