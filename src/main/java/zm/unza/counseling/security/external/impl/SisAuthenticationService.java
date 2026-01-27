package zm.unza.counseling.security.external.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.security.external.ExternalAuthenticationException;
import zm.unza.counseling.security.external.ExternalAuthenticationService;
import zm.unza.counseling.security.external.ExternalAuthResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Student Information System (SIS) Authentication Service
 * Handles authentication for students against the university's actual SIS systems
 */
@Service("sisAuthenticationService")
@RequiredArgsConstructor
public class SisAuthenticationService implements ExternalAuthenticationService {

    @Value("${app.sis.api.baseUrls.undergraduate:https://devoap.unza.zm}")
    private String undergraduateUrl;
    
    @Value("${app.sis.api.baseUrls.postgraduate:https://pgonline.unza.zm}")
    private String postgraduateUrl;
    
    @Value("${app.sis.api.baseUrls.distance:https://online.unza.zm}")
    private String distanceUrl;
    
    @Value("${app.sis.api.baseUrls.gsb:https://gsbonline.unza.zm}")
    private String gsbUrl;
    
    @Value("${app.sis.api.baseUrls.zou:https://zouonline.unza.zm}")
    private String zouUrl;
    
    @Value("${app.sis.api.baseUrls.ecampus:https://ecampusonline.unza.zm}")
    private String ecampusUrl;
    
    @Value("${app.sis.api.loginEndpoint:/api/v1/customers/login}")
    private String loginEndpoint;
    
    @Value("${app.sis.api.tokenEndpoint:/StudentApp/get_student_token.json}")
    private String tokenEndpoint;
    
    @Value("${app.sis.api.instanceKey:undergraduate}")
    private String defaultInstanceKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;



    @Override
    public ExternalAuthResponse authenticate(String username, String password) throws ExternalAuthenticationException {
        System.out.println("Attempting SIS authentication for student: " + username);
        
        try {
            // Try multiple instances in case the user doesn't specify
            String[] instances = getInstanceKeys();
            
            for (String instance : instances) {
                try {
                    ExternalAuthResponse response = attemptAuthentication(username, password, instance);
                    if (response.isAuthenticated()) {
                        System.out.println("SIS authentication successful for student: " + username + " via " + instance);
                        return response;
                    }
                } catch (ExternalAuthenticationException e) {
                    System.out.println("SIS authentication failed for instance " + instance + ": " + e.getMessage());
                    // Continue to try next instance
                }
            }
            
            // If all instances failed
            System.out.println("SIS authentication failed for student: " + username + " across all instances");
            return new ExternalAuthResponse(false, "Invalid credentials or student not found in any SIS instance");
            
        } catch (Exception e) {
            System.out.println("SIS authentication error for student: " + username + " - " + e.getMessage());
            throw new ExternalAuthenticationException("SIS authentication failed: " + e.getMessage(), e);
        }
    }

    private ExternalAuthResponse attemptAuthentication(String username, String password, String instance) throws ExternalAuthenticationException {
        String baseUrl = getBaseUrlForInstance(instance);
        
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new ExternalAuthenticationException("Unknown SIS instance: " + instance);
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        
        String loginUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + loginEndpoint)
                .build()
                .toUriString();
        
        System.out.println("Trying SIS login at: " + loginUrl);
        
        ResponseEntity<String> response = restTemplate.postForEntity(loginUrl, entity, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            try {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                
                // Check for new API format (success: true, data: { ... })
                if (jsonResponse.has("success") && jsonResponse.get("success").asBoolean() && jsonResponse.has("data")) {
                    JsonNode dataNode = jsonResponse.get("data");
                    JsonNode userNode = dataNode.get("user");
                    
                    if (userNode != null) {
                        User user = mapSisResponseToUser(userNode, dataNode, instance, baseUrl);
                        String externalId = userNode.has("username") ? userNode.get("username").asText() : 
                                          (userNode.has("student_id") ? userNode.get("student_id").asText() : username);
                        
                        return new ExternalAuthResponse(true, "Authentication successful", user, 
                                                      externalId, "SIS_" + instance.toUpperCase());
                    }
                }

                // Fallback to old API format
                JsonNode responseNode = jsonResponse.get("response");
                
                if (responseNode != null && 
                    responseNode.get("status").asInt() == 200 &&
                    responseNode.has("data")) {
                    
                    JsonNode dataNode = responseNode.get("data");
                    JsonNode userNode = dataNode.get("user");
                    
                    if (userNode != null) {
                        User user = mapSisResponseToUser(userNode, dataNode, instance, baseUrl);
                        String externalId = userNode.has("computer_no") ? userNode.get("computer_no").asText() : 
                                          (userNode.has("id") ? userNode.get("id").asText() : username);

                        return new ExternalAuthResponse(true, "Authentication successful", user, 
                                                      externalId, "SIS_" + instance.toUpperCase());
                    }
                }
                
                String errorMessage = "Authentication failed";
                if (jsonResponse.has("message")) {
                    errorMessage = jsonResponse.get("message").asText();
                } else if (responseNode != null && responseNode.has("message")) {
                    errorMessage = responseNode.get("message").asText();
                }
                
                return new ExternalAuthResponse(false, errorMessage);
                
            } catch (Exception e) {
                throw new ExternalAuthenticationException("Failed to parse SIS response: " + e.getMessage(), e);
            }
        }
        
        throw new ExternalAuthenticationException("HTTP error: " + response.getStatusCode());
    }

    @Override
    public boolean validateUserExists(String username) {
        try {
            // Try to authenticate with a dummy password to check if user exists
            // This is a workaround since the SIS doesn't have a separate validation endpoint
            authenticate(username, "dummy_password");
            return false; // If we get here, authentication failed
        } catch (ExternalAuthenticationException e) {
            // Check if it's a "user not found" type error vs "invalid credentials"
            String message = e.getMessage().toLowerCase();
            return !message.contains("not found") && !message.contains("does not exist");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public User getUserDetails(String username) throws ExternalAuthenticationException {
        // First authenticate to get the token and user data
        ExternalAuthResponse response = authenticate(username, ""); // Will use cached logic from authenticate
        
        if (response.isAuthenticated() && response.getUser() != null) {
            return response.getUser();
        }
        
        throw new ExternalAuthenticationException("Failed to get user details for: " + username);
    }

    private String[] getInstanceKeys() {
        return new String[]{defaultInstanceKey, "undergraduate", "postgraduate", "distance", "gsb", "zou", "ecampus"};
    }

    private String getBaseUrlForInstance(String instance) {
        switch (instance.toLowerCase()) {
            case "undergraduate":
                return undergraduateUrl;
            case "postgraduate":
                return postgraduateUrl;
            case "distance":
            case "distance education":
                return distanceUrl;
            case "gsb":
            case "graduate school of business":
                return gsbUrl;
            case "zou":
                return zouUrl;
            case "ecampus":
                return ecampusUrl;
            default:
                return undergraduateUrl; // Default fallback
        }
    }

    private User mapSisResponseToUser(JsonNode userNode, JsonNode dataNode, String instance, String baseUrl) {
        User user = new User();
        
        // Basic user information
        // Handle both new (username, student_id) and old (computer_no) formats
        String username = "";
        if (userNode.has("username")) {
            username = userNode.get("username").asText();
        } else if (userNode.has("computer_no")) {
            username = userNode.get("computer_no").asText();
        }
        user.setUsername(username);

        user.setEmail(userNode.has("email") ? userNode.get("email").asText() : "");
        user.setFirstName(userNode.has("first_name") ? userNode.get("first_name").asText() : "");
        user.setLastName(userNode.has("last_name") ? userNode.get("last_name").asText() : "");
        
        String studentId = "";
        if (userNode.has("student_id")) {
            studentId = userNode.get("student_id").asText();
        } else if (userNode.has("computer_no")) {
            studentId = userNode.get("computer_no").asText();
        } else if (userNode.has("id")) {
            studentId = userNode.get("id").asText();
        }
        user.setStudentId(studentId);
        
        user.setPhoneNumber(userNode.has("phone") ? userNode.get("phone").asText() : "");
        
        // Academic information
        if (dataNode.has("yr_of_study")) {
            try {
                user.setYearOfStudy(Integer.parseInt(dataNode.get("yr_of_study").asText()));
            } catch (NumberFormatException e) {
                // Handle non-numeric year of study
            }
        }
        
        if (userNode.has("major")) {
            user.setProgram(userNode.get("major").asText());
        }
        
        // Extract program information if available
        if (dataNode.has("student_program_info")) {
            JsonNode programInfo = dataNode.get("student_program_info");
            
            if (programInfo.has("Program") && programInfo.get("Program").has("program_description")) {
                user.setProgram(programInfo.get("Program").get("program_description").asText());
            }
            
            if (programInfo.has("School") && programInfo.get("School").has("school_description")) {
                user.setDepartment(programInfo.get("School").get("school_description").asText());
            }
            
            if (programInfo.has("Campus") && programInfo.get("Campus").has("campus_description")) {
                user.setDepartment(user.getDepartment() + " - " + programInfo.get("Campus").get("campus_description").asText());
            }
        }
        
        // System fields
        user.setActive(true);
        user.setEmailVerified(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Set authentication source
        user.setAuthenticationSource(zm.unza.counseling.security.AuthenticationSource.SIS);
        
        // Set role as student
        HashSet<Role> roles = new HashSet<>();
        Role studentRole = new Role();
        studentRole.setName(Role.ERole.ROLE_STUDENT);
        roles.add(studentRole);
        user.setRoles(roles);
        
        return user;
    }
}