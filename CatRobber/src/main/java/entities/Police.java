package entities;

import interfaces.*;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.json.JSONObject;
import utils.GameLoader;
import utils.Vector2D;
import java.util.HashMap;
import java.util.Map;

// Представляє поліцейського NPC, який патрулює або переслідує гравця
public class Police implements Animatable, GameObject {
    // Поля
    private Vector2D position; // Позиція поліцейського, з JSON
    private PoliceDirection direction; // Напрям руху (LEFT, RIGHT), з JSON
    private PoliceState state; // Стан (PATROL, CHASE, ALERT, STUNNED, IDLE), з JSON
    private String currentAnimation; // Поточна анімація ("idle", "patrol", "stunned", "alarm")
    private int animationFrame; // Поточний кадр анімації
    private double animationTime; // Час для анімації
    private Map<String, Image[]> animations; // Анімації, завантажені через GameLoader
    private String[] spritePaths; // Шляхи до спрайтів
    private double width; // Ширина зображення, з JSON
    private double height; // Висота зображення, з JSON
    private double collWidth; // Ширина колізійної області, з JSON
    private double collHeight; // Висота колізійної області, з JSON
    private double normalSpeed = 50.0; // Швидкість патрулювання
    private double chaseSpeed = 70.0; // Швидкість переслідування
    private double scaleX = 1.0; // Масштаб по X
    private double scaleY = 1.0; // Масштаб по Y

    // Напрями та стани поліцейського
    public enum PoliceDirection { LEFT, RIGHT }
    public enum PoliceState { PATROL, CHASE, ALERT, STUNNED, IDLE }

    // Конструктор: ініціалізує поліцейського з JSON-даними
    public Police(Vector2D position, JSONObject defaultData) {
        this.position = position;
        this.width = defaultData.optDouble("width", 32.0);
        this.height = defaultData.optDouble("height", 32.0);
        this.collWidth = defaultData.optDouble("widthColl", 20.0);
        this.collHeight = defaultData.optDouble("hightColl", 20.0);
        this.direction = PoliceDirection.valueOf(defaultData.optString("direction", "LEFT"));
        this.state = PoliceState.valueOf(defaultData.optString("state", "PATROL"));
        this.currentAnimation = defaultData.optString("animation", "patrol");
        this.animations = new HashMap<>();
        this.spritePaths = new String[]{"police/idle.png", "police/run.png", "police/lay.png", "police/alarm.png"};
        GameLoader loader = new GameLoader();
        animations.put("idle", loader.splitSpriteSheet(spritePaths[0], 12));
        animations.put("patrol", loader.splitSpriteSheet(spritePaths[1], 9));
        animations.put("stunned", loader.splitSpriteSheet(spritePaths[2], 1));
        animations.put("alarm", loader.splitSpriteSheet(spritePaths[3], 2));
        this.animationFrame = 0;
        this.animationTime = 0;
        // Зв’язок із GameLoader для завантаження спрайтів
    }

    // --- Ініціалізація та оновлення ---

    // Оновлює логіку поліцейського (викликається з GameManager.update())
    public void update(double deltaTime) {
        // TODO: Реалізувати логіку патрулювання та переслідування
        // Змінити position залежно від state (PATROL: рух у межах, CHASE: до гравця)
        // Зупинити рух, якщо досягнуто межу кімнати
    }

    // Оновлює анімацію поліцейського
    @Override
    public void updateAnimation(double deltaTime) {
        // TODO: Реалізувати оновлення анімації
        // Оновити animationTime, animationFrame залежно від deltaTime
        // Переключити кадри на основі currentAnimation
    }

    // Перевіряє, чи гравець у полі зору
    public void detectPlayer(Vector2D playerPosition) {
        // TODO: Реалізувати логіку виявлення гравця
        // Перевірити відстань до playerPosition, змінити state на CHASE або ALERT
    }

    // Зупиняє рух поліцейського
    public void stopMovement() {
        // TODO: Реалізувати зупинку руху
        // Встановити state = IDLE, скинути швидкість
    }

    // Активує стан тривоги
    public void alert() {
        // TODO: Реалізувати активацію тривоги
        // Встановити state = ALERT, змінити currentAnimation
    }

    // Обробляє отримання удару
    public void takeHit(boolean isRanged) {
        // TODO: Реалізувати обробку удару
        // Встановити state = STUNNED, змінити анімацію
    }

    // --- Рендеринг ---

    // Рендерить поліцейського на canvas (викликається з GameManager.render())
    @Override
    public void render(GraphicsContext gc) {
        // TODO: Реалізувати рендеринг
        // Отримати поточний кадр через getCurrentFrame()
        // Намалювати його на gc з урахуванням position, width, height, direction
    }

    // Повертає поточний кадр анімації
    @Override
    public Image getCurrentFrame() {
        // TODO: Реалізувати повернення кадру
        // Повернути animations.get(currentAnimation)[animationFrame] або null
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

    // Повертає JSON для збереження (викликається з SaveManager.savePolice())
    @Override
    public JSONObject getSerializableData() {
        // TODO: Реалізувати серіалізацію
        // Створити JSONObject з position, direction, state, currentAnimation тощо
        return null;
    }

    // Відновлює стан із JSON (викликається з SaveManager.loadGame())
    @Override
    public void setFromData(JSONObject data) {
        // TODO: Реалізувати десеріалізацію
        // Оновити position, direction, state, currentAnimation із data
    }

    // --- Геттери/Сеттери ---

    // Повертає тип об’єкта
    @Override
    public String getType() {
        return "Police";
    }

    // Повертає позицію поліцейського
    @Override
    public Vector2D getPosition() {
        return new Vector2D(position.x, position.y);
    }

    // Повертає уявну позицію (та ж, що й позиція)
    @Override
    public Vector2D getImagePosition() {
        return getPosition();
    }

    // Встановлює позицію поліцейського
    @Override
    public void setPosition(Vector2D position) {
        this.position = position;
    }

    // Встановлює уявну позицію
    @Override
    public void setImagePosition(Vector2D position) {
        this.position = position;
    }

    // Повертає межі для колізій
    @Override
    public Bounds getBounds() {
        return new BoundingBox(position.x, position.y, collWidth, collHeight);
    }

    // Повертає межі для рендерингу
    @Override
    public Bounds getImageBounds() {
        return new BoundingBox(position.x, position.y, width, height);
    }

    // Повертає шар рендерингу
    @Override
    public int getRenderLayer() {
        return 1; // Поліцейські рендеряться на шарі 1
    }

    // Перевіряє видимість
    @Override
    public boolean isVisible() {
        return true; // Поліцейський завжди видимий
    }

}