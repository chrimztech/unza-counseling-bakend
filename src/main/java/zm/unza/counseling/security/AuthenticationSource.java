package zm.unza.counseling.security;

/**
 * Authentication Source Enumeration
 * Defines the different sources where users can be authenticated from
 */
public enum AuthenticationSource {
    /**
     * Internal system authentication - for counselors and admins
     */
    INTERNAL("internal"),
    
    /**
     * Student Information System authentication - for students
     */
    SIS("sis"),
    
    /**
     * Human Resources system authentication - for staff
     */
    HR("hr");

    private final String value;

    AuthenticationSource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AuthenticationSource fromValue(String value) {
        for (AuthenticationSource source : values()) {
            if (source.getValue().equalsIgnoreCase(value)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown authentication source: " + value);
    }
}