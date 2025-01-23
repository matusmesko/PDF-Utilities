package matus.mesko;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

public class LangManager {
    private final Properties properties;

    public LangManager(String lang) {
        this.properties = loadLanguageProperties(lang);
    }

    private Properties loadLanguageProperties(String langCode) {
        Properties props = new Properties();
        String fileName = "/messages_" + langCode + ".properties";  // Resource path

        try {
            InputStreamReader reader = new InputStreamReader(
                    getClass().getResourceAsStream(fileName), StandardCharsets.UTF_8
            );
            props.load(reader);
        } catch (IOException | NullPointerException e) {
            System.err.println("Language file not found for '" + langCode + "', going back to English.");
            try {
                InputStreamReader reader = new InputStreamReader(
                        getClass().getResourceAsStream("/messages_en.properties"), StandardCharsets.UTF_8
                );
                props.load(reader);
            } catch (IOException | NullPointerException ex) {
                System.err.println("Default language file not found. Exiting application.");
                System.exit(1);
            }
        }
        return props;
    }

    public String getString(String key) {
        return properties.getProperty(key, "N/A");
    }


}
