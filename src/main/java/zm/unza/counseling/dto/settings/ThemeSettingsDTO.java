package zm.unza.counseling.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for appearance/theme settings including dark mode preference
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThemeSettingsDTO {
    
    /**
     * Theme mode: LIGHT, DARK, or SYSTEM
     */
    private String themeMode;
    
    /**
     * Primary color hex code
     */
    private String primaryColor;
    
    /**
     * Enable compact mode
     */
    private Boolean compactMode;
    
    /**
     * Enable reduced motion
     */
    private Boolean reducedMotion;
    
    /**
     * Enable high contrast
     */
    private Boolean highContrast;
    
    /**
     * Font size preference: SMALL, MEDIUM, LARGE
     */
    private String fontSize;
}