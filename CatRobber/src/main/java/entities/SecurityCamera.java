package entities;

import interfaces.GameObject;
import interfaces.Renderable;
import interfaces.Positioned;
import interfaces.Savable;
import javafx.geometry.Bounds;
import utils.Vector2D;
import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;
import org.json.JSONObject;

// Камера спостереження, реалізує необхідні інтерфейси
public class SecurityCamera implements GameObject{
    // Поля
    private Vector2D position; // Позиція, з defaultData/saveData (x, y)
    private CameraDirection direction; // Напрямок (LEFT, RIGHT, DOWN), з defaultData/saveData
    private int currentFrame; // Поточний кадр (0-2), з defaultData/saveData
    private Polygon fieldOfView; // Поле зору, ініціалізується з direction
    private double animationTime; // Таймер анімації, з defaultData/saveData
    private Image[] normalFrames; // Кадри нормального стану, з spritePaths
    private Image[] alertFrames; // Кадри тривоги, з spritePaths
    private String[] spritePaths; // Шляхи до спрайтів, з defaultData/saveData

    @Override
    public String getType() {
        return "Camera";
    }

    // Енум
    public enum CameraDirection { LEFT, RIGHT, DOWN }

    // Конструктор
    // Отримує defaultData з /data/defaults/cameras_level_X.json через GameLoader
    public SecurityCamera(JSONObject defaultData) {
        setFromData(defaultData); // Ініціалізація з дефолтних даних
    }

    // Ініціалізація або відновлення стану
    // Отримує defaultData (/data/defaults/) або saveData (/data/saves/), парсить усі поля
    // Створює normalFrames, alertFrames з spritePaths через GameLoader.loadImage, ініціалізує fieldOfView
    @Override
    public void setFromData(JSONObject data) {}

    // Методи Renderable
    // Малює камеру, отримує GraphicsContext від GameManager
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

    // Оновлює кадр (змінює currentFrame між 0-2)
    // Викликається в GameManager.update, оновлює animationTime
    public void updateFrame() {}

    // Виявляє гравця
    // Отримує playerPosition від GameManager, перевіряє fieldOfView, передає тривогу в GameManager
    public void detectPlayer(Vector2D playerPosition) {}
}