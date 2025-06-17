package managers;

import javafx.scene.text.Font;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Клас для управління шрифтами у додатку, реалізований за патерном Singleton.
 * Забезпечує завантаження, збереження та отримання шрифтів із файлів.
 */
public class FontManager {
    /** Єдиний екземпляр класу FontManager (патерн Singleton). */
    private static FontManager instance;

    /** Колекція для зберігання завантажених базових шрифтів. */
    private final Map<String, Font> loadedFonts;

    /**
     * Приватний конструктор для ініціалізації колекції шрифтів.
     * Використовується для забезпечення єдиного екземпляра класу.
     */
    private FontManager() {
        loadedFonts = new HashMap<>();
    }

    /**
     * Повертає єдиний екземпляр класу FontManager.
     * Якщо екземпляр ще не створено, створює його.
     *
     * @return єдиний екземпляр класу FontManager
     */
    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    /**
     * Завантажує шрифт із файлу та зберігає його у колекцію під вказаним ім'ям.
     *
     * @param fontPath відносний шлях до файлу шрифту в папці assets/fonts/
     * @param fontName ім'я, під яким шрифт буде збережено в колекції
     */
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
            } else {
                System.err.println("Не вдалося завантажити шрифт: " + fontPath);
            }
        } catch (Exception e) {
            System.err.println("Помилка завантаження шрифту " + fontPath + ": " + e.getMessage());
        }
    }

    /**
     * Повертає шрифт за вказаним ім'ям із заданим розміром.
     * Якщо шрифт не знайдено, повертається резервний шрифт Arial.
     *
     * @param fontName ім'я шрифту, який потрібно отримати
     * @param size розмір шрифту
     * @return об'єкт шрифту з указаним ім'ям і розміром або резервний шрифт Arial
     */
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

    /**
     * Ініціалізує набір стандартних шрифтів, завантажуючи їх із файлів.
     * Завантажує шрифти Hardpixel, DS-Digital та EpsilonCTT.
     */
    public void initializeFonts() {
        loadFont("Hardpixel.otf", "Hardpixel");
        loadFont("DS-DIGIT.TTF", "DS-Digital");
        loadFont("EpsilonCTT.ttf", "EpsilonCTT");
    }
}