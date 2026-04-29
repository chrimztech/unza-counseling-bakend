package zm.unza.counseling.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.ClientRepository;
import zm.unza.counseling.repository.UserRepository;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Bridges student user records into the Client subtype used by case/intake tables.
 */
@Service
public class ClientIdentityService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ClientIdentityService(ClientRepository clientRepository, UserRepository userRepository) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Client getOrCreateClient(Long userId) {
        return clientRepository.findById(userId)
                .orElseGet(() -> promoteUserToClient(userId));
    }

    @Transactional
    public List<Client> getAllClients() {
        syncRoleBackedClients();
        return clientRepository.findAll();
    }

    @Transactional
    public void syncRoleBackedClients() {
        LinkedHashMap<Long, User> distinctUsers = new LinkedHashMap<>();
        userRepository.findByRolesName(Role.ERole.ROLE_CLIENT)
                .forEach(user -> distinctUsers.put(user.getId(), user));
        userRepository.findByRolesName(Role.ERole.ROLE_STUDENT)
                .forEach(user -> distinctUsers.put(user.getId(), user));

        distinctUsers.keySet().forEach(this::getOrCreateClient);
    }

    private Client promoteUserToClient(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + userId));

        if (!user.isClient()) {
            throw new ValidationException("Selected user is not a client or student account");
        }

        entityManager.createNativeQuery("""
                UPDATE users
                SET user_type = 'CLIENT',
                    client_status = COALESCE(client_status, 'ACTIVE'),
                    risk_level = COALESCE(risk_level, 'LOW'),
                    risk_score = COALESCE(risk_score, 0),
                    total_sessions = COALESCE(total_sessions, 0),
                    registration_date = COALESCE(registration_date, CURRENT_DATE),
                    programme = COALESCE(programme, program),
                    faculty = COALESCE(faculty, department)
                WHERE id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        return clientRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + userId));
    }
}
