package zm.unza.counseling.security;

public enum UserPermission {
    CLIENT_READ("client:read"),
    CLIENT_WRITE("client:write"),
    COUNSELOR_READ("counselor:read"),
    COUNSELOR_WRITE("counselor:write"),
    ASSESSMENT_READ("assessment:read"),
    ASSESSMENT_WRITE("assessment:write"),
    APPOINTMENT_READ("appointment:read"),
    APPOINTMENT_WRITE("appointment:write");

    private final String permission;

    UserPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}