import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class LanguageManager {
    private static final Properties messages = new Properties();

    // Load configurations dynamically from the selected .message file
    public static void loadLanguage(String langCode) {
        String fileName = "language_" + langCode + ".message";
        try (FileInputStream fis = new FileInputStream(fileName);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            messages.load(isr);
        } catch (IOException e) {
            System.out.println("Warning: Could not load " + fileName + ". Using defaults.");
        }
    }

    // Fetch the translated string by key
    public static String getMessage(String key) {
        return messages.getProperty(key, key);
    }
}