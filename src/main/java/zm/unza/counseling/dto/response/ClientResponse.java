package zm.unza.counseling.dto.response;

import lombok.Data;
import zm.unza.counseling.entity.Client;

@Data
public class ClientResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String studentId;
    private Client.ClientStatus status;
    private Client.RiskLevel riskLevel;

    public static ClientResponse from(Client client) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setFirstName(client.getFirstName());
        response.setLastName(client.getLastName());
        response.setEmail(client.getEmail());
        response.setStudentId(client.getStudentId());
        response.setStatus(client.getClientStatus());
        response.setRiskLevel(client.getRiskLevel());
        return response;
    }
}