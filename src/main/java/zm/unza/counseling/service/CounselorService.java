package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.CreateCounselorRequest;
import zm.unza.counseling.entity.Counselor;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.CounselorRepository;
import zm.unza.counseling.repository.RoleRepository;
import zm.unza.counseling.security.AuthenticationSource;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CounselorService {
    
    private final CounselorRepository counselorRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Counselor> getAllCounselors() {
        return counselorRepository.findAll();
    }

    public Counselor getCounselorById(Long id) {
        return counselorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Counselor not found with id: " + id));
    }

    @Transactional
    public Counselor createCounselor(CreateCounselorRequest request) {
        Counselor counselor = new Counselor();
        counselor.setEmail(request.getEmail());
        counselor.setUsername(request.getEmail());
        counselor.setPassword(passwordEncoder.encode(request.getPassword()));
        counselor.setFirstName(request.getFirstName());
        counselor.setLastName(request.getLastName());
        counselor.setPhoneNumber(request.getPhoneNumber());
        counselor.setSpecialization(request.getSpecialization());
        counselor.setBio(request.getBio());
        counselor.setOfficeLocation(request.getOfficeLocation());
        counselor.setDepartment(request.getDepartment());
        counselor.setGender(User.Gender.OTHER); // Required field
        counselor.setActive(true);
        counselor.setEmailVerified(true);
        counselor.setAuthenticationSource(AuthenticationSource.INTERNAL);
        counselor.setCreatedAt(LocalDateTime.now());
        counselor.setUpdatedAt(LocalDateTime.now());

        // Set counselor role
        Role counselorRole = roleRepository.findByName(Role.ERole.ROLE_COUNSELOR)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(Role.ERole.ROLE_COUNSELOR);
                    newRole.setDescription("Counselor role for counseling staff");
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(counselorRole);
        counselor.setRoles(roles);

        return counselorRepository.save(counselor);
    }

    @Transactional
    public void deleteCounselor(Long id) {
        if (!counselorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Counselor not found with id: " + id);
        }
        counselorRepository.deleteById(id);
    }
}
