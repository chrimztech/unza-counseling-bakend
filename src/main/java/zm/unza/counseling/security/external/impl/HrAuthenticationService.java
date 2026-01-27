package zm.unza.counseling.security.external.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;

/**
 * Human Resources (HR) Authentication Service
 * Handles authentication for staff against the university's HR system
 */
@Service("hrAuthenticationService")
@Profile("!development")
public class HrAuthenticationService implements ExternalAuthenticationService {

    @Value("${app.hr.api.url:https://hr.unza.zm}")
    private String hrApiUrl;

    @Value("${app.hr.api.loginEndpoint:/api/auth-login2}")
    private String hrLoginEndpoint;

    @Value("${app.hr.api.metaDataEndpoint:/api/get-meta-data-on-user}")
    private String hrMetaDataEndpoint;

    private final RestTemplate restTemplate;

    public HrAuthenticationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
        public ExternalAuthResponse authenticate(String username, String password) throws ExternalAuthenticationException {
            System.out.println("Attempting HR authentication for staff: " + username);
            
            try {
                // Step 1: Authenticate and get a token
                HttpHeaders loginHeaders = new HttpHeaders();
                loginHeaders.setContentType(MediaType.APPLICATION_JSON);
                loginHeaders.set("Accept", "application/json");
    
                Map<String, String> loginRequest = Map.of(
                    "username", username,
                    "password", password
                );
    
                HttpEntity<Map<String, String>> loginEntity = new HttpEntity<>(loginRequest, loginHeaders);
    
                String loginUrl = UriComponentsBuilder.fromHttpUrl(hrApiUrl + hrLoginEndpoint)
                        .build()
                        .toUriString();
    
                System.out.println("HR Login URL: " + loginUrl);
                System.out.println("HR Login Request: " + loginRequest);
    
                ResponseEntity<HrLoginResponse> loginResponse = restTemplate.postForEntity(loginUrl, loginEntity, HrLoginResponse.class);
    
                System.out.println("HR Login Response Status: " + loginResponse.getStatusCode());
                // System.out.println("HR Login Response Body: " + loginResponse.getBody());
    
                if (loginResponse.getStatusCode() != HttpStatus.OK || loginResponse.getBody() == null || loginResponse.getBody().getToken() == null) {
                    System.out.println("HR authentication failed for staff (login step): " + username);
                    return new ExternalAuthResponse(false, "Invalid HR credentials");
                }
    
                String token = loginResponse.getBody().getToken();
                String tokenType = loginResponse.getBody().getTokenType() != null ? loginResponse.getBody().getTokenType() : "Bearer";
    
                // Step 2: Use the token to get user metadata
                HttpHeaders metadataHeaders = new HttpHeaders();
                metadataHeaders.set("Authorization", tokenType + " " + token);
                metadataHeaders.set("Accept", "application/json");
                HttpEntity<String> metadataEntity = new HttpEntity<>(metadataHeaders);
    
                String metadataUrl = UriComponentsBuilder.fromHttpUrl(hrApiUrl + hrMetaDataEndpoint)
                        .build()
                        .toUriString();
    
                System.out.println("HR Metadata URL: " + metadataUrl);
                // System.out.println("HR Metadata Token: " + token);
    
                ResponseEntity<HrUserMetadataResponse> metadataResponse = restTemplate.exchange(metadataUrl, HttpMethod.POST, metadataEntity, HrUserMetadataResponse.class);
    
                System.out.println("HR Metadata Response Status: " + metadataResponse.getStatusCode());
                System.out.println("HR Metadata Response Body: " + metadataResponse.getBody());
    
                if (metadataResponse.getStatusCode() == HttpStatus.OK && metadataResponse.getBody() != null) {
                    HrUserMetadataResponse hrUser = metadataResponse.getBody();
                    User user = mapHrResponseToUser(hrUser);
                    System.out.println("HR authentication successful for staff: " + username);
                    return new ExternalAuthResponse(true, "Authentication successful", user, hrUser.getResponse().getData().getManNumber(), "HR");
                } else {
                    System.out.println("HR authentication failed for staff (metadata step): " + username);
                    return new ExternalAuthResponse(false, "Could not retrieve HR user details after login");
                }
    
            } catch (Exception e) {
                System.err.println("HR authentication error for staff: " + username + " - " + e.getMessage());
                e.printStackTrace();
                throw new ExternalAuthenticationException("HR authentication failed: " + e.getMessage(), e);
            }
        }

    @Override
    public boolean validateUserExists(String username) {
        try {
            // Production validation
            HttpHeaders headers = new HttpHeaders();
            // This endpoint might not exist in the new API spec. This is a best-effort guess.
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = UriComponentsBuilder.fromHttpUrl(hrApiUrl + "/staff/" + username)
                    .build()
                    .toUriString();
            // The response type is unknown, assuming it's HrUserMetadataResponse for now.
            ResponseEntity<HrUserMetadataResponse> response = restTemplate.getForEntity(url, HrUserMetadataResponse.class);
            return response.getStatusCode() == HttpStatus.OK && response.getBody() != null;
            
        } catch (Exception e) {
            System.out.println("HR validation error for staff: " + username + " - " + e.getMessage());
            return false;
        }
    }

    @Override
    public User getUserDetails(String username) throws ExternalAuthenticationException {
        try {
            // Production user details retrieval
            HttpHeaders headers = new HttpHeaders();
            // This requires a valid token. This method is problematic without a login context.
            // For now, we assume it's not used in a flow that doesn't first authenticate.
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = UriComponentsBuilder.fromHttpUrl(hrApiUrl + "/staff/" + username)
                    .build()
                    .toUriString();
            ResponseEntity<HrUserMetadataResponse> response = restTemplate.getForEntity(url, HrUserMetadataResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return mapHrResponseToUser(response.getBody());
            } else {
                throw new ExternalAuthenticationException("Staff member not found in HR system");
            }
            
        } catch (Exception e) {
            System.out.println("HR user details error for staff: " + username + " - " + e.getMessage());
            throw new ExternalAuthenticationException("Failed to get staff details: " + e.getMessage(), e);
        }
    }

    private User mapHrResponseToUser(HrUserMetadataResponse hrUser) throws ExternalAuthenticationException {
        if (hrUser == null || hrUser.getResponse() == null || hrUser.getResponse().getData() == null) {
            throw new ExternalAuthenticationException("Invalid HR user data received");
        }
        
        HrUserData data = hrUser.getResponse().getData();
        User user = new User();
        
        // Map fields from HR response to User entity
        user.setUsername(data.getManNumber()); // Use man_number as username
        user.setEmail(data.getEmail());
        user.setFirstName(data.getFirstName());
        user.setLastName(data.getLastName());
        user.setDepartment(data.getPosition()); // Use position as department
        user.setPhoneNumber(data.getPhone());
        user.setActive(true);
        user.setEmailVerified(true);
        user.setAuthenticationSource(zm.unza.counseling.security.AuthenticationSource.HR);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Set placeholder password for external authentication
        user.setPassword("EXTERNALLY_AUTHENTICATED_USER_NO_PASSWORD");
        
        // Don't assign roles here - let MultiSourceAuthService handle role assignment
        // based on the staff member's position and department
        user.setRoles(new HashSet<>());
        
        return user;
    }

    // Inner class for HR API Login response
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class HrLoginResponse {
        private String token;
        @JsonProperty("token_type")
        private String tokenType;
        private String message;
        @JsonProperty("expires_in")
        private Integer expiresIn;

        // Getters and setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Integer getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Integer expiresIn) { this.expiresIn = expiresIn; }
    }

    // Inner class for HR API User Metadata response
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class HrUserMetadataResponse {
        private HrApiResponse response;

        public HrApiResponse getResponse() { return response; }
        public void setResponse(HrApiResponse response) { this.response = response; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class HrApiResponse {
        private int status;
        private String message;
        private HrUserData data;

        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public HrUserData getData() { return data; }
        public void setData(HrUserData data) { this.data = data; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class HrUserData {
        @JsonProperty("man_number")
        private String manNumber;
        private String title;
        @JsonProperty("first_name")
        private String firstName;
        @JsonProperty("last_name")
        private String lastName;
        @JsonProperty("other_names")
        private String otherNames;
        private String email;
        private String phone;
        private String position;
        @JsonProperty("position_hierarchy")
        private String positionHierarchy;
        @JsonProperty("profile_image")
        private String profileImage;
        private int status;
        private HrAddress adress;

        // Getters and setters
        public String getManNumber() { return manNumber; }
        public void setManNumber(String manNumber) { this.manNumber = manNumber; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getOtherNames() { return otherNames; }
        public void setOtherNames(String otherNames) { this.otherNames = otherNames; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        public String getPositionHierarchy() { return positionHierarchy; }
        public void setPositionHierarchy(String positionHierarchy) { this.positionHierarchy = positionHierarchy; }
        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
        public HrAddress getAdress() { return adress; }
        public void setAdress(HrAddress adress) { this.adress = adress; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class HrAddress {
        private String area;
        private String street;
        @JsonProperty("house_number")
        private String houseNumber;

        public String getArea() { return area; }
        public void setArea(String area) { this.area = area; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public String getHouseNumber() { return houseNumber; }
        public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }
    }
}