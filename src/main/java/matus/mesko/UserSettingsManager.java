package matus.mesko;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class UserSettingsManager {
    private static final String SETTINGS_FILE_NAME = "usersettings.yaml";
    private static final Path WRITABLE_SETTINGS_PATH = Path.of(System.getProperty("user.home"), ".pdf-utilities", SETTINGS_FILE_NAME);
    private final ObjectMapper objectMapper;
    private Map<String, Object> settings;

    public UserSettingsManager() {
        objectMapper = new ObjectMapper(new YAMLFactory());
        ensureWritableSettingsFile();
        loadSettings();
    }

    /**
     * Ensures that the writable settings file exists, creating it from resources if necessary.
     */
    private void ensureWritableSettingsFile() {
        try {
            if (!Files.exists(WRITABLE_SETTINGS_PATH.getParent())) {
                Files.createDirectories(WRITABLE_SETTINGS_PATH.getParent());
            }

            if (!Files.exists(WRITABLE_SETTINGS_PATH)) {
                InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(SETTINGS_FILE_NAME);
                if (resourceStream != null) {
                    Files.copy(resourceStream, WRITABLE_SETTINGS_PATH, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    throw new FileNotFoundException("Default settings file not found in resources: " + SETTINGS_FILE_NAME);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create writable settings file: " + e.getMessage(), e);
        }
    }

    /**
     * Loads the settings from the writable settings file.
     */
    private void loadSettings() {
        try {
            settings = objectMapper.readValue(WRITABLE_SETTINGS_PATH.toFile(), Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load settings: " + e.getMessage(), e);
        }
    }

    /**
     * Saves the updated settings to the writable settings file.
     */
    public void saveSettings() {
        try {
            objectMapper.writeValue(WRITABLE_SETTINGS_PATH.toFile(), settings);
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }

    public String getTheme() {
        return (String) settings.getOrDefault("theme", "dark");
    }

    public void setTheme(String theme) {
        settings.put("theme", theme);
        saveSettings();
    }
}
