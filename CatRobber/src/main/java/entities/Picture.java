package entities;

import interfaces.*;
import javafx.geometry.Bounds;
import utils.Vector2D;
import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import org.json.JSONObject;

// Картина з одним текстом, реалізує необхідні інтерфейси
public class Picture implements GameObject, Interactable {
    // Поля
    private Vector2D position; // Позиція, з defaultData/saveData (x, y)
    private String text; // Текст картини, з defaultData/saveData
    private Image sprite; // Спрайт, з spritePath
    private String spritePath; // Шлях до спрайта, з defaultData/saveData

    // Конструктор
    // Отримує defaultData з /data/defaults/interactable_objects_level_X.json через GameLoader
    public Picture(JSONObject defaultData) {
        setFromData(defaultData); // Ініціалізація з дефолтних даних
    }

    // Ініціалізація або відновлення стану
    // Отримує defaultData (/data/defaults/) або saveData (/data/saves/), парсить усі поля
    // Створює sprite з spritePath через GameLoader.loadImage
    @Override
    public void setFromData(JSONObject data) {}

    // Методи Renderable
    // Малює картину, отримує GraphicsContext від GameManager
    @Override
    public void render(GraphicsContext gc) {}

    // Повертає шар рендерингу (1, як у гравця, малюється поверх фону, під UI)
    // Передає в GameManager для сортування
    @Override
    public int getRenderLayer() { return 1; }

    // Перевіряє видимість (завжди true), передає в GameManager
    @Override
    public boolean isVisible() { return true; }

    // Методи Positioned
    // Повертає позицію, передає в GameManager для колізій
    @Override
    public Vector2D getPosition() { return position; }

    // Встановлює позицію, отримує від GameManager (зазвичай статична)
    @Override
    public void setPosition(Vector2D position) {}

    // Повертає межі для колізій, передає в GameManager
    @Override
    public Bounds getBounds() { return null; }

    // Методи Savable
    // Повертає JSON-стан, передає в SaveManager
    @Override
    public JSONObject getSerializableData() { return null; }

    // Методи Interactable
    // Показує текст, передає text в UIManager
    @Override
    public void interact(Player player) {}

    // Перевіряє можливість взаємодії (залежить від відстані), передає в GameManager
    @Override
    public boolean canInteract(Player player) { return false; }

    // Повертає дистанцію взаємодії, передає в GameManager
    @Override
    public double getInteractionRange() { return 0.0; }

    // Повертає підказку ("Натисни E"), передає в UIManager
    @Override
    public String getInteractionPrompt() { return null; }

    @Override
    public String getType() {
        return "Picture";
    }
}