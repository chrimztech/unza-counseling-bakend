package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.response.UserResponse;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/contacts", "/v1/contacts", "/contacts"})
@RequiredArgsConstructor
@Slf4j
public class ContactsController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAvailableContacts(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
            
            List<User> contacts = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .filter(u -> u.isCounselor() || u.isClient())
                .collect(Collectors.toList());

            List<UserResponse> responses = contacts.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            log.error("Error getting available contacts", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to get contacts: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchContacts(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
            String lowerQuery = query.toLowerCase();

            List<User> contacts = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .filter(u -> {
                    String name = u.getFirstName() + " " + u.getLastName();
                    return name.toLowerCase().contains(lowerQuery) ||
                           u.getEmail().toLowerCase().contains(lowerQuery);
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                contacts.stream().map(this::mapToUserResponse).collect(Collectors.toList())));
        } catch (Exception e) {
            log.error("Error searching contacts", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to search contacts: " + e.getMessage()));
        }
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setActive(user.getActive());
        response.setRoles(user.getRoles());
        response.setDepartment(user.getDepartment());
        response.setProfilePicture(user.getProfilePicture());
        response.setSpecialization(user.getSpecialization());
        response.setStudentId(user.getStudentId());
        response.setProgram(user.getProgram());
        return response;
    }
}