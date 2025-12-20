package zm.unza.counseling.service.interfaces;

import zm.unza.counseling.dto.response.ClientResponse;
import zm.unza.counseling.entity.Client;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for client management
 * Provides abstraction layer for client operations
 */
public interface ClientService {
    
    /**
     * Create a new client
     * 
     * @param client client data
     * @return created client response
     */
    ClientResponse createClient(Client client);
    
    /**
     * Update existing client
     * 
     * @param id client ID
     * @param client updated data
     * @return updated client response
     */
    ClientResponse updateClient(Long id, Client client);
    
    /**
     * Get client by ID
     * 
     * @param id client ID
     * @return client response
     */
    Optional<ClientResponse> getClientById(Long id);
    
    /**
     * Get all clients with pagination
     * 
     * @param page page number
     * @param size page size
     * @return paginated list of clients
     */
    List<ClientResponse> getAllClients(int page, int size);
    
    /**
     * Get clients by counselor ID
     * 
     * @param counselorId counselor ID
     * @return list of clients
     */
    List<ClientResponse> getClientsByCounselorId(Long counselorId);
    
    /**
     * Get clients by status
     * 
     * @param status client status
     * @return list of clients
     */
    List<ClientResponse> getClientsByStatus(String status);
    
    /**
     * Search clients by criteria
     * 
     * @param searchTerm search term
     * @param page page number
     * @param size page size
     * @return paginated list of clients
     */
    List<ClientResponse> searchClients(String searchTerm, int page, int size);
    
    /**
     * Deactivate client
     * 
     * @param id client ID
     * @param reason deactivation reason
     */
    void deactivateClient(Long id, String reason);
    
    /**
     * Reactivate client
     * 
     * @param id client ID
     */
    void reactivateClient(Long id);
    
    /**
     * Get client statistics
     * 
     * @return client statistics
     */
    Object getClientStatistics();
    
    /**
     * Delete client
     * 
     * @param id client ID
     */
    void deleteClient(Long id);
    
    /**
     * Check if client exists
     * 
     * @param id client ID
     * @return true if exists
     */
    boolean existsById(Long id);
}