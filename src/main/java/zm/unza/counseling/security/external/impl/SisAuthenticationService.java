package zm.unza.counseling.security.external.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import org.springframework.context.annotation.Profile;

/**
 * Student Information System (SIS) Authentication Service
 * Handles authentication for students against the university's actual SIS systems
 */
@Service("sisAuthenticationService")
@RequiredArgsConstructor
@Slf4j
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
                
                // DEBUG: Log the raw response
                System.out.println("=== SIS Raw Response ===");
                System.out.println(jsonResponse.toString());
                
                // Check for new API format (success: true, data: { user: { ... } })
                if (jsonResponse.has("success") && jsonResponse.get("success").asBoolean() && jsonResponse.has("data")) {
                    JsonNode dataNode = jsonResponse.get("data");
                    JsonNode userNode = dataNode.get("user");
                    
                    System.out.println("=== New API Format Detected ===");
                    System.out.println("Data Node: " + dataNode.toString());
                    System.out.println("User Node: " + (userNode != null ? userNode.toString() : "null"));
                    
                    // Also check dataNode directly for user info (some APIs put user data at data level)
                    if (userNode == null) {
                        userNode = dataNode;
                        System.out.println("No 'user' node found, using dataNode as userNode");
                    }
                    
                    if (userNode != null) {
                        // Extract user data from the new format
                        String email = getJsonText(userNode, "email");
                        // Fix email typo: replace comma with dot
                        if (email != null && email.contains(",")) {
                            email = email.replace(",", ".");
                        }
                        
                        System.out.println("=== EXTRACTING NAMES FROM SIS RESPONSE ===");
                        System.out.println("userNode has first_name: " + userNode.has("first_name"));
                        System.out.println("userNode has last_name: " + userNode.has("last_name"));
                        
                        // Try multiple field names for first name
                        String firstName = getJsonText(userNode, "first_name");
                        System.out.println("getJsonText(userNode, 'first_name') = '" + firstName + "'");
                        if (firstName == null || firstName.isEmpty()) {
                            firstName = getJsonText(userNode, "firstname");
                            System.out.println("getJsonText(userNode, 'firstname') = '" + firstName + "'");
                        }
                        if (firstName == null || firstName.isEmpty()) {
                            firstName = getJsonText(userNode, "given_name");
                        }
                        if (firstName == null || firstName.isEmpty()) {
                            firstName = getJsonText(userNode, "fname");
                        }
                        
                        // Try multiple field names for last name
                        String lastName = getJsonText(userNode, "last_name");
                        System.out.println("getJsonText(userNode, 'last_name') = '" + lastName + "'");
                        if (lastName == null || lastName.isEmpty()) {
                            lastName = getJsonText(userNode, "lastname");
                            System.out.println("getJsonText(userNode, 'lastname') = '" + lastName + "'");
                        }
                        if (lastName == null || lastName.isEmpty()) {
                            lastName = getJsonText(userNode, "surname");
                        }
                        if (lastName == null || lastName.isEmpty()) {
                            lastName = getJsonText(userNode, "family_name");
                        }
                        if (lastName == null || lastName.isEmpty()) {
                            lastName = getJsonText(userNode, "lname");
                        }
                        
                        System.out.println("=== FINAL EXTRACTED NAMES ===");
                        System.out.println("firstName: '" + firstName + "'");
                        System.out.println("lastName: '" + lastName + "'");
                        
                        // Try to get full name and split if first/last are empty
                        String fullName = getJsonText(userNode, "full_name");
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(userNode, "fullname");
                        }
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(userNode, "name");
                        }
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(userNode, "names");
                        }
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(userNode, "student_name");
                        }
                        // Also check dataNode for names
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(dataNode, "names");
                        }
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(dataNode, "full_name");
                        }
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(dataNode, "student_name");
                        }
                        
                        if (fullName != null && !fullName.isEmpty() && 
                            (firstName == null || firstName.isEmpty()) && 
                            (lastName == null || lastName.isEmpty())) {
                            // Handle "SURNAME FIRSTNAME" format (common in some systems)
                            String[] nameParts = fullName.trim().split("\\s+", 2);
                            if (nameParts.length >= 2) {
                                // Check if first part is all caps (likely surname)
                                if (nameParts[0].equals(nameParts[0].toUpperCase()) && 
                                    !nameParts[1].equals(nameParts[1].toUpperCase())) {
                                    // SURNAME FIRSTNAME format
                                    firstName = nameParts[1];
                                    lastName = nameParts[0];
                                } else {
                                    // FIRSTNAME SURNAME format
                                    firstName = nameParts[0];
                                    lastName = nameParts[1];
                                }
                            } else if (nameParts.length == 1) {
                                lastName = nameParts[0];
                            }
                        }
                        
                        String studentId = getJsonText(userNode, "student_id");
                        if (studentId == null || studentId.isEmpty()) {
                            studentId = getJsonText(userNode, "computer_no");
                        }
                        if (studentId == null || studentId.isEmpty()) {
                            studentId = getJsonText(userNode, "studentId");
                        }
                        
                        System.out.println("Extracted - Email: " + email + ", FirstName: " + firstName + ", LastName: " + lastName + ", StudentId: " + studentId);
                        
                        User user = mapSisUserData(userNode, dataNode, username, email, firstName, lastName, studentId);
                        
                        return new ExternalAuthResponse(true, "Authentication successful", user, 
                                                      studentId != null ? studentId : username, "SIS_" + instance.toUpperCase());
                    }
                }

                // Fallback to old API format
                JsonNode responseNode = jsonResponse.get("response");
                
                System.out.println("=== Old API Format (response) ===");
                System.out.println("Response Node: " + (responseNode != null ? responseNode.toString() : "null"));
                
                if (responseNode != null && 
                    responseNode.get("status").asInt() == 200 &&
                    responseNode.has("data")) {
                    
                    JsonNode dataNode = responseNode.get("data");
                    JsonNode userNode = dataNode.get("user");
                    
                    // Also check dataNode directly for user info (some APIs put user data at data level)
                    if (userNode == null) {
                        userNode = dataNode;
                        System.out.println("Old format: No 'user' node found, using dataNode as userNode");
                    }
                    
                    if (userNode != null) {
                        // Old format uses computer_no as student identifier
                        String computerNo = getJsonText(userNode, "computer_no");
                        if (computerNo == null || computerNo.isEmpty()) {
                            computerNo = getJsonText(userNode, "student_id");
                        }
                        
                        // Try multiple field names for first name
                        String firstName = getJsonText(userNode, "first_name");
                        if (firstName == null || firstName.isEmpty()) {
                            firstName = getJsonText(userNode, "firstname");
                        }
                        if (firstName == null || firstName.isEmpty()) {
                            firstName = getJsonText(userNode, "given_name");
                        }
                        if (firstName == null || firstName.isEmpty()) {
                            firstName = getJsonText(userNode, "fname");
                        }
                        
                        // Try multiple field names for last name
                        String lastName = getJsonText(userNode, "last_name");
                        if (lastName == null || lastName.isEmpty()) {
                            lastName = getJsonText(userNode, "lastname");
                        }
                        if (lastName == null || lastName.isEmpty()) {
                            lastName = getJsonText(userNode, "surname");
                        }
                        if (lastName == null || lastName.isEmpty()) {
                            lastName = getJsonText(userNode, "family_name");
                        }
                        if (lastName == null || lastName.isEmpty()) {
                            lastName = getJsonText(userNode, "lname");
                        }
                        
                        // Try to get full name and split if first/last are empty
                        String fullName = getJsonText(userNode, "full_name");
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(userNode, "fullname");
                        }
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(userNode, "name");
                        }
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(userNode, "names");
                        }
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(userNode, "student_name");
                        }
                        // Also check dataNode for names
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(dataNode, "names");
                        }
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(dataNode, "full_name");
                        }
                        if (fullName == null || fullName.isEmpty()) {
                            fullName = getJsonText(dataNode, "student_name");
                        }
                        
                        if (fullName != null && !fullName.isEmpty() && 
                            (firstName == null || firstName.isEmpty()) && 
                            (lastName == null || lastName.isEmpty())) {
                            // Handle "SURNAME FIRSTNAME" format (common in some systems)
                            String[] nameParts = fullName.trim().split("\\s+", 2);
                            if (nameParts.length >= 2) {
                                // Check if first part is all caps (likely surname)
                                if (nameParts[0].equals(nameParts[0].toUpperCase()) && 
                                    !nameParts[1].equals(nameParts[1].toUpperCase())) {
                                    // SURNAME FIRSTNAME format
                                    firstName = nameParts[1];
                                    lastName = nameParts[0];
                                } else {
                                    // FIRSTNAME SURNAME format
                                    firstName = nameParts[0];
                                    lastName = nameParts[1];
                                }
                            } else if (nameParts.length == 1) {
                                lastName = nameParts[0];
                            }
                        }
                        
                        String email = getJsonText(userNode, "email");
                        // Fix email typo: replace comma with dot
                        if (email != null && email.contains(",")) {
                            email = email.replace(",", ".");
                        }
                        
                        System.out.println("Old Format - ComputerNo: " + computerNo + ", FirstName: " + firstName + ", LastName: " + lastName);
                        
                        User user = mapSisUserData(userNode, dataNode, username, email, firstName, lastName, computerNo);
                        
                        return new ExternalAuthResponse(true, "Authentication successful", user, 
                                                      computerNo, "SIS_" + instance.toUpperCase());
                    }
                }
                
                String errorMessage = "Authentication failed";
                if (jsonResponse.has("message")) {
                    errorMessage = jsonResponse.get("message").asText();
                } else if (responseNode != null && responseNode.has("message")) {
                    errorMessage = responseNode.get("message").asText();
                }
                
                System.out.println("=== Authentication Failed ===" + errorMessage);
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

    // Helper method to safely extract text from JSON node (handles both string and numeric values)
    private String getJsonText(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) return "";
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) return "";
        // asText() works for both string and numeric values
        String value = fieldNode.asText();
        return "null".equalsIgnoreCase(value) ? "" : value;
    }
    
    private User mapSisUserData(JsonNode userNode, JsonNode dataNode, String loginUsername, 
                                String email, String firstName, String lastName, String studentId) {
        User user = new User();
        
        System.out.println("=== mapSisUserData CALLED ===");
        System.out.println("Parameters received:");
        System.out.println("  loginUsername: " + loginUsername);
        System.out.println("  email: " + email);
        System.out.println("  firstName: '" + firstName + "'");
        System.out.println("  lastName: '" + lastName + "'");
        System.out.println("  studentId: " + studentId);
        
        // Set username from studentId or login username
        String username = (studentId != null && !studentId.isEmpty()) ? studentId : loginUsername;
        if (username.isEmpty()) {
            username = "user_" + System.currentTimeMillis();
        }
        user.setUsername(username);
        
        // Set email (fallback to username if email is empty)
        user.setEmail((email != null && !email.isEmpty()) ? email : username);
        
        // Debug: Log all available fields in userNode and dataNode
        System.out.println("=== DEBUG: All fields in userNode ===");
        if (userNode != null) {
            userNode.fields().forEachRemaining(entry -> {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue().asText());
            });
        }
        System.out.println("=== DEBUG: All fields in dataNode ===");
        if (dataNode != null) {
            dataNode.fields().forEachRemaining(entry -> {
                System.out.println("  " + entry.getKey() + ": " + (entry.getValue().isContainerNode() ? "[object]" : entry.getValue().asText()));
            });
        }
        
        // Try multiple field names for first name - check both userNode and dataNode
        String resolvedFirstName = firstName;
        System.out.println("Initial resolvedFirstName from parameter: '" + resolvedFirstName + "'");
        
        // Extended list of firstName field variations
        String[] firstNameFields = {"first_name", "firstname", "given_name", "fname", "givenname", 
                                    "First_Name", "FirstName", "FIRST_NAME", "forename", "fore_name"};
        
        // First check userNode
        if (resolvedFirstName == null || resolvedFirstName.isEmpty()) {
            for (String field : firstNameFields) {
                resolvedFirstName = getJsonText(userNode, field);
                if (resolvedFirstName != null && !resolvedFirstName.isEmpty()) {
                    System.out.println("Found firstName in userNode." + field + ": " + resolvedFirstName);
                    break;
                }
            }
        }
        
        // Then check dataNode if still empty
        if (resolvedFirstName == null || resolvedFirstName.isEmpty()) {
            for (String field : firstNameFields) {
                resolvedFirstName = getJsonText(dataNode, field);
                if (resolvedFirstName != null && !resolvedFirstName.isEmpty()) {
                    System.out.println("Found firstName in dataNode." + field + ": " + resolvedFirstName);
                    break;
                }
            }
        }
        
        System.out.println("Final resolvedFirstName: '" + resolvedFirstName + "'");
        
        // Try multiple field names for last name - check both userNode and dataNode
        String resolvedLastName = lastName;
        System.out.println("Initial resolvedLastName from parameter: '" + resolvedLastName + "'");
        
        // Extended list of lastName field variations
        String[] lastNameFields = {"last_name", "lastname", "surname", "family_name", "lname", "familyname",
                                   "Last_Name", "LastName", "LAST_NAME", "Surname", "SURNAME"};
        
        // First check userNode
        if (resolvedLastName == null || resolvedLastName.isEmpty()) {
            for (String field : lastNameFields) {
                resolvedLastName = getJsonText(userNode, field);
                if (resolvedLastName != null && !resolvedLastName.isEmpty()) {
                    System.out.println("Found lastName in userNode." + field + ": " + resolvedLastName);
                    break;
                }
            }
        }
        
        // Then check dataNode if still empty
        if (resolvedLastName == null || resolvedLastName.isEmpty()) {
            for (String field : lastNameFields) {
                resolvedLastName = getJsonText(dataNode, field);
                if (resolvedLastName != null && !resolvedLastName.isEmpty()) {
                    System.out.println("Found lastName in dataNode." + field + ": " + resolvedLastName);
                    break;
                }
            }
        }
        
        System.out.println("Final resolvedLastName: '" + resolvedLastName + "'");
        
        // Try to get full name and split it if first/last names are empty
        String fullName = null;
        // Extended list of fullName field variations
        String[] fullNameFields = {"full_name", "fullname", "name", "student_name", "names", 
                                   "Full_Name", "FullName", "FULL_NAME", "Name", "NAME",
                                   "student_fullname", "student_full_name", "studentName"};
        
        // Check userNode for full name
        for (String field : fullNameFields) {
            fullName = getJsonText(userNode, field);
            if (fullName != null && !fullName.isEmpty()) {
                System.out.println("Found fullName in userNode." + field + ": " + fullName);
                break;
            }
        }
        
        // Check dataNode for full name if still empty
        if (fullName == null || fullName.isEmpty()) {
            for (String field : fullNameFields) {
                fullName = getJsonText(dataNode, field);
                if (fullName != null && !fullName.isEmpty()) {
                    System.out.println("Found fullName in dataNode." + field + ": " + fullName);
                    break;
                }
            }
        }
        
        // If we have a full name but no first/last, try to split
        if (fullName != null && !fullName.isEmpty()) {
            if ((resolvedFirstName == null || resolvedFirstName.isEmpty()) && 
                (resolvedLastName == null || resolvedLastName.isEmpty())) {
                // Handle "SURNAME FIRSTNAME" format (common in some systems)
                // Also handle "FIRSTNAME SURNAME" format
                String[] nameParts = fullName.trim().split("\\s+", 2);
                if (nameParts.length >= 1) {
                    // Check if the name might be in "SURNAME FIRSTNAME" format
                    // If the first part is all caps, it's likely a surname
                    if (nameParts.length == 2 && nameParts[0].equals(nameParts[0].toUpperCase()) && 
                        !nameParts[1].equals(nameParts[1].toUpperCase())) {
                        // SURNAME FIRSTNAME format
                        resolvedFirstName = nameParts[1];
                        resolvedLastName = nameParts[0];
                        System.out.println("Detected SURNAME FIRSTNAME format: firstName=" + resolvedFirstName + ", lastName=" + resolvedLastName);
                    } else {
                        // Default: FIRSTNAME SURNAME format
                        resolvedFirstName = nameParts[0];
                        resolvedLastName = nameParts[1];
                        System.out.println("Detected FIRSTNAME SURNAME format: firstName=" + resolvedFirstName + ", lastName=" + resolvedLastName);
                    }
                } else if (nameParts.length == 1) {
                    // Only one name part - use as lastName (surname) with empty firstName
                    resolvedLastName = nameParts[0];
                    System.out.println("Single name part found, using as lastName: " + resolvedLastName);
                }
            }
        }
        
        // Set names with defaults if still empty
        user.setFirstName(resolvedFirstName != null && !resolvedFirstName.isEmpty() ? resolvedFirstName : "Student");
        user.setLastName(resolvedLastName != null && !resolvedLastName.isEmpty() ? resolvedLastName : username);
        
        // Set student ID
        user.setStudentId(studentId != null ? studentId : "");
        
        // Extract phone number
        user.setPhoneNumber(getJsonText(userNode, "phone"));
        
        // Academic information from dataNode
        if (dataNode != null) {
            String[] yearFields = {"yr_of_study", "year_of_study", "year", "level"};
            for (String fieldName : yearFields) {
                if (dataNode.has(fieldName)) {
                    try {
                        user.setYearOfStudy(Integer.parseInt(getJsonText(dataNode, fieldName)));
                        break;
                    } catch (NumberFormatException e) {
                        // Handle non-numeric year of study
                    }
                }
            }
            
            // Program from userNode
            String[] programFields = {"major", "program", "programme", "degree", "course"};
            for (String fieldName : programFields) {
                if (userNode.has(fieldName) && !getJsonText(userNode, fieldName).isEmpty()) {
                    user.setProgram(getJsonText(userNode, fieldName));
                    break;
                }
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
        
        System.out.println("=== Mapped User ===");
        System.out.println("Username: " + user.getUsername());
        System.out.println("Email: " + user.getEmail());
        System.out.println("FirstName: " + user.getFirstName());
        System.out.println("LastName: " + user.getLastName());
        System.out.println("StudentId: " + user.getStudentId());
        
        return user;
    }
}