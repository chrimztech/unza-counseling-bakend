package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zm.unza.counseling.entity.Admin;
import zm.unza.counseling.repository.AdminRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final AdminRepository adminRepository;

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }
}