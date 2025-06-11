package managers;

import javafx.scene.text.Font;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FontManager {
    private static FontManager instance; // Singleton-екземпляр
    private final Map<String, Font> loadedFonts; // Зберігає базові завантажені шрифти

    // Приватний конструктор
    private FontManager() {
        loadedFonts = new HashMap<>();
    }

    // Повертає єдиний екземпляр FontManager
    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    // Завантажує шрифт із файлу та зберігає його як базовий
    public void loadFont(String fontPath, String fontName) {
        try {
            // Формуємо повний шлях до файлу
            File file = new File("assets/fonts/" + fontPath);
            if (!file.exists()) {
                System.err.println("Файл шрифту не знайдено: " + file.getAbsolutePath());
                return;
            }
            // Завантажуємо шрифт через URL з базовим розміром
            Font font = Font.loadFont(file.toURI().toURL().toString(), 12);
            if (font != null) {
                loadedFonts.put(fontName, font);
                System.out.println("Шрифт завантажено: " + font.getName() + " з " + file.getAbsolutePath());
            } else {
                System.err.println("Не вдалося завантажити шрифт: " + fontPath);
            }
        } catch (Exception e) {
            System.err.println("Помилка завантаження шрифту " + fontPath + ": " + e.getMessage());
        }
    }

    // Повертає шрифт за ім'ям з потрібним розміром
    public Font getFont(String fontName, double size) {
        Font baseFont = loadedFonts.get(fontName);
        if (baseFont != null) {
            // Створюємо новий екземпляр шрифту з потрібним розміром
            return Font.font(baseFont.getName(), size);
        } else {
            System.err.println("Шрифт " + fontName + " не знайдено, використовується Arial");
            return Font.font("Arial", size); // Резервний шрифт
        }
    }

    // Ініціалізація всіх потрібних шрифтів
    public void initializeFonts() {
        loadFont("Hardpixel.otf", "Hardpixel");
        loadFont("DS-DIGIT.TTF", "DS-Digital");
        loadFont("EpsilonCTT.ttf", "EpsilonCTT");
        System.out.println("Доступні шрифти: " + Font.getFamilies());
        System.out.println("Завантажені користувацькі шрифти: " + loadedFonts.keySet());
    }
}