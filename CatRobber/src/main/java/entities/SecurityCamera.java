package entities;

import interfaces.*;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.json.JSONObject;
import utils.GameLoader;
import utils.Vector2D;

// Представляє камеру спостереження з полем зору, яка може виявляти гравця
public class SecurityCamera implements GameObject, Animatable {
    // Поля
    private Vector2D position; // Позиція камери (верхній лівий кут), з JSON
    private CameraDirection direction; // Напрям камери (LEFT, RIGHT, DOWN), з JSON
    private String currentAnimation; // Поточна анімація ("normal", "alert"), з JSON
    private Polygon fieldOfView; // Полігон поля зору для виявлення гравця
    private double animationTime; // Час для анімації
    private int animationFrame; // Поточний кадр анімації
    private boolean isAlert; // Чи в стані тривоги
    private double alertTimer; // Таймер тривоги
    private Image[] frames; // Кадри анімації, завантажені через GameLoader
    private String spritePath; // Шлях до спрайт-листа
    private double width; // Ширина зображення, з JSON
    private double height; // Висота зображення, з JSON
    private double collWidth; // Ширина колізійної області, з JSON
    private double collHeight; // Висота колізійної області, з JSON
    private double scaleX = 1.0; // Масштаб по X
    private double scaleY = 1.0; // Масштаб по Y

    // Напрями камери
    public enum CameraDirection { LEFT, RIGHT, DOWN }

    // Конструктор: ініціалізує камеру з JSON-даними
    public SecurityCamera(JSONObject defaultData) {
        // Ініціалізація позиції та розмірів із JSON
        this.position = new Vector2D(defaultData.getDouble("x"), defaultData.getDouble("y"));
        this.width = defaultData.optDouble("width", 32.0);
        this.height = defaultData.optDouble("height", 32.0);
        this.collWidth = defaultData.optDouble("widthColl", 20.0);
        this.collHeight = defaultData.optDouble("hightColl", 20.0);
        // Ініціалізація напрямку, анімації та стану тривоги
        this.direction = CameraDirection.valueOf(defaultData.optString("direction", "DOWN"));
        this.currentAnimation = defaultData.optString("animation", "normal");
        this.isAlert = defaultData.optBoolean("isAlert", false);
        // Завантаження спрайтів через GameLoader
        this.spritePath = "camera/camera.png";
        GameLoader loader = new GameLoader();
        this.frames = loader.splitSpriteSheet(spritePath, defaultData.optInt("frameCount", 2));
        // Ініціалізація поля зору та анімації
        this.fieldOfView = new Polygon();
        this.animationTime = 0;
        this.animationFrame = 0;
        this.alertTimer = 0;
    }

    // --- Ініціалізація та оновлення ---

    // Оновлює анімацію камери
    @Override
    public void updateAnimation(double deltaTime) {
        // TODO: Реалізувати оновлення анімації
        // Оновити animationTime, animationFrame залежно від deltaTime
        // Переключити кадри з frames на основі currentAnimation
    }

    // Оновлює кадр анімації (викликається з GameManager.update())
    public void updateFrame() {
        // TODO: Реалізувати оновлення кадру
        // Змінити animationFrame для наступного кадру
    }

    // Перевіряє, чи гравець у полі зору камери
    public void detectPlayer(Vector2D playerPosition) {
        // TODO: Реалізувати логіку виявлення гравця
        // Перевірити, чи playerPosition у межах fieldOfView
        // Якщо виявлено, встановити isAlert = true, оновити alertTimer
    }

    // --- Рендеринг ---

    // Рендерить камеру на canvas (викликається з GameManager.render())
    @Override
    public void render(GraphicsContext gc) {
        // TODO: Реалізувати рендеринг
        // Отримати поточний кадр через getCurrentFrame()
        // Намалювати його на gc з урахуванням position, width, height, direction
        // Якщо isAlert, можливо, змінити колір або додати ефект
    }

    // Повертає поточний кадр анімації
    @Override
    public Image getCurrentFrame() {
        // TODO: Реалізувати повернення кадру
        // Повернути frames[animationFrame] або null, якщо frames порожній
        return null;
    }

    // --- Взаємодії ---

    // Встановлює стан анімації
    @Override
    public void setAnimationState(String state) {
        // TODO: Реалізувати зміну стану анімації
        // Оновити currentAnimation, скинути animationFrame і animationTime
    }

    // --- Серіалізація ---

    // Повертає JSON для збереження (викликається з SaveManager.saveCameras())
    @Override
    public JSONObject getSerializableData() {
        // TODO: Реалізувати серіалізацію
        // Створити JSONObject з position, direction, currentAnimation, isAlert тощо
        return null;
    }

    // Відновлює стан із JSON (викликається з SaveManager.loadGame())
    @Override
    public void setFromData(JSONObject data) {
        // TODO: Реалізувати десеріалізацію
        // Оновити position, direction, currentAnimation, isAlert із data
    }

    // --- Геттери/Сеттери ---

    // Повертає тип об’єкта для ідентифікації
    @Override
    public String getType() {
        return "Camera";
    }

    // Повертає позицію камери
    @Override
    public Vector2D getPosition() {
        return new Vector2D(position.x, position.y);
    }

    // Повертає уявну позицію (та ж, що й позиція)
    @Override
    public Vector2D getImaginePosition() {
        return getPosition();
    }

    // Встановлює позицію камери
    @Override
    public void setPosition(Vector2D position) {
        this.position = position;
    }

    // Встановлює уявну позицію (та ж, що й позиція)
    @Override
    public void setImaginePosition(Vector2D position) {
        this.position = position;
    }

    // Повертає межі для колізій
    @Override
    public Bounds getBounds() {
        return new BoundingBox(position.x, position.y, collWidth, collHeight);
    }

    // Повертає межі для рендерингу
    @Override
    public Bounds getImagineBounds() {
        return new BoundingBox(position.x, position.y, width, height);
    }

    // Повертає шар рендерингу
    @Override
    public int getRenderLayer() {
        return 1; // Камери рендеряться на шарі 1
    }

    // Перевіряє видимість камери
    @Override
    public boolean isVisible() {
        return true; // Камера завжди видима
    }

}