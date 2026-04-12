package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ConversationDto;
import zm.unza.counseling.dto.response.UserResponse;
import zm.unza.counseling.entity.Appointment;
import zm.unza.counseling.entity.Message;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.repository.AppointmentRepository;
import zm.unza.counseling.repository.UserRepository;
import zm.unza.counseling.service.MessageService;
import zm.unza.counseling.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/v1/conversations", "/conversations", "/v1/contacts", "/contacts"})
@RequiredArgsConstructor
public class ConversationsController {

    private final MessageService messageService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ConversationDto>> getConversationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getConversations(userId));
    }

    @GetMapping
    public ResponseEntity<List<ConversationDto>> getConversations(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getConversations(user.getId()));
    }

    @GetMapping("/{partnerId}")
    public ResponseEntity<List<Message>> getConversationWithPartner(
            @PathVariable Long partnerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        return ResponseEntity.ok(messageService.getConversationWithPartner(user.getId(), partnerId));
    }

    @PutMapping("/{partnerId}/read")
    public ResponseEntity<Void> markConversationAsRead(
            @PathVariable Long partnerId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        messageService.markAllMessagesFromPartnerAsRead(user.getId(), partnerId);
        return ResponseEntity.ok().build();
    }

    // ============ CONTACTS ENDPOINTS ============

    /**
     * Get available contacts for messaging
     * Counselors see their assigned clients
     * Clients see their assigned counselors
     */
    @GetMapping("/contacts/available")
    public ResponseEntity<List<UserResponse>> getAvailableContacts(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        List<User> contacts = getAvailableContactsList(user);

        return ResponseEntity.ok(contacts.stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList()));
    }

    private List<User> getClientVisibleContacts(User user) {
        List<User> contacts = appointmentRepository.findByStudent(user).stream()
                .map(Appointment::getCounselor)
                .filter(c -> c != null)
                .distinct()
                .collect(Collectors.toList());

        if (!contacts.isEmpty()) {
            return contacts;
        }

        // If no appointments, show available counselors
        return userRepository.findByRolesName("ROLE_COUNSELOR").stream()
                .filter(c -> Boolean.TRUE.equals(c.getAvailableForAppointments()))
                .collect(Collectors.toList());
    }

    /**
     * Search contacts for messaging
     */
    @GetMapping("/contacts/search")
    public ResponseEntity<List<UserResponse>> searchContacts(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsernameOrEmail(userDetails.getUsername());
        List<User> contacts = getAvailableContactsList(user);

        String lowerQuery = query.toLowerCase();
        List<User> filtered = contacts.stream()
            .filter(c -> c.getFullName().toLowerCase().contains(lowerQuery) ||
                        c.getEmail().toLowerCase().contains(lowerQuery) ||
                        (c.getStudentId() != null && c.getStudentId().toLowerCase().contains(lowerQuery)) ||
                        (c.getProgram() != null && c.getProgram().toLowerCase().contains(lowerQuery)) ||
                        (c.getSpecialization() != null && c.getSpecialization().toLowerCase().contains(lowerQuery)))
            .collect(Collectors.toList());

        return ResponseEntity.ok(filtered.stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList()));
    }

    /**
     * Get list of users user can message
     */
    private List<User> getAvailableContactsList(User user) {
        if (user.isCounselor()) {
            return appointmentRepository.findByCounselor(user).stream()
                .map(Appointment::getStudent)
                .filter(s -> s != null)
                .distinct()
                .collect(Collectors.toList());
        } else {
            return getClientVisibleContacts(user);
        }
    }

    /**
     * Map User entity to UserResponse DTO
     */
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