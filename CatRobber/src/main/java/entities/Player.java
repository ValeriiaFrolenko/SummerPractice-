package entities;

import interfaces.*;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.json.JSONObject;
import utils.GameLoader;
import utils.Vector2D;
import java.util.HashMap;
import java.util.Map;

// Представляє гравця, який може рухатися, атакувати, взаємодіяти з об’єктами
public class Player implements Animatable, GameObject, Interactable {
    // Поля
    private double imageX; // Верхній лівий кут зображення по X
    private double imageY; // Верхній лівий кут зображення по Y
    private double collX; // Верхній лівий кут колізійної області по X
    private double collY; // Верхній лівий кут колізійної області по Y
    private double imageWidth; // Ширина зображення, з JSON
    private double imageHeight; // Висота зображення, з JSON
    private double collWidth; // Ширина колізійної області, з JSON
    private double collHeight; // Висота колізійної області, з JSON
    private Direction direction; // Напрям (LEFT, RIGHT), з JSON
    private PlayerState state; // Стан (IDLE, RUN, HIT, CLIMB, INVISIBLE, SHOOT), з JSON
    private double speed; // Швидкість руху
    private boolean isVisible; // Видимість гравця
    private String currentAnimation; // Поточна анімація ("idle", "run", "hit", "shoot")
    private int animationFrame; // Поточний кадр анімації
    private double animationTime; // Час для анімації
    private Map<String, Image[]> animations; // Анімації, завантажені через GameLoader
    private String[] spritePaths; // Шляхи до спрайтів
    private boolean canMove; // Чи може гравець рухатися

    // Напрями та стани гравця
    public enum Direction { LEFT, RIGHT }
    public enum PlayerState { IDLE, RUN, HIT, CLIMB, INVISIBLE, SHOOT }

    // Конструктор: ініціалізує гравця з JSON-даними
    public Player(Vector2D position, JSONObject defaultData) {
        // Ініціалізація позиції та розмірів із JSON
        double jsonImageX = position.getX();
        double jsonImageY = position.getY();
        double jsonCollX = defaultData.optDouble("collX", jsonImageX);
        double jsonCollY = defaultData.optDouble("collY", jsonImageY - defaultData.optDouble("hightColl", 20.0));
        this.imageWidth = defaultData.getDouble("width");
        this.imageHeight = defaultData.getDouble("height");
        this.collWidth = defaultData.optDouble("widthColl", 20.0);
        this.collHeight = defaultData.optDouble("hightColl", 20.0);
        // Конвертація в верхній лівий кут
        this.imageX = jsonImageX;
        this.imageY = jsonImageY - imageHeight;
        this.collX = jsonCollX;
        this.collY = jsonCollY;
        // Ініціалізація інших параметрів
        this.canMove = defaultData.optBoolean("canMove", true);
        String directionStr = defaultData.optString("direction", "RIGHT").toUpperCase();
        try {
            this.direction = Direction.valueOf(directionStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Невірне значення direction: " + directionStr + ". Встановлюю RIGHT.");
            this.direction = Direction.RIGHT;
        }
        this.state = PlayerState.IDLE;
        this.speed = 70.0;
        this.isVisible = true;
        this.currentAnimation = "idle";
        this.animationFrame = 0;
        this.animationTime = 0;
        // Завантаження анімацій через GameLoader
        this.animations = new HashMap<>();
        this.spritePaths = new String[]{"player/idle.png", "player/run.png", "player/beating.png", "player/shoot.png"};
        GameLoader loader = new GameLoader();
        animations.put("idle", loader.splitSpriteSheet(spritePaths[0], 8));
        animations.put("run", loader.splitSpriteSheet(spritePaths[1], 10));
        animations.put("hit", loader.splitSpriteSheet(spritePaths[2], 13));
        animations.put("shoot", loader.splitSpriteSheet(spritePaths[3], 2));
        System.out.println("Player initialized with image position: (" + imageX + ", " + imageY + "), collision position: (" + collX + ", " + collY + ")");
    }

    // --- Ініціалізація та оновлення ---

    // Рухає гравця в заданому напрямку (викликається з GameManager.handleInput())
    public void move(Direction direction, double deltaTime) {
        if (canMove) {
            this.direction = direction;
            setAnimationState("run");
            double movement = speed * deltaTime;
            double deltaX = 0;
            if (direction == Direction.LEFT) {
                deltaX = -movement;
            } else if (direction == Direction.RIGHT) {
                deltaX = movement;
            }
            collX += deltaX;
            imageX += deltaX;
        }
    }

    // Зупиняє рух гравця (викликається з GameManager.handleInput())
    public void stopMovement() {
        setCanMove(false);
        setAnimationState("idle");
    }

    // Дозволяє рух гравця (викликається з GameManager.checkCollisions())
    public void allowMovement() {
        setCanMove(true);
    }

    // Оновлює анімацію гравця (викликається з GameManager.update())
    @Override
    public void updateAnimation(double deltaTime) {
        animationTime += deltaTime;
        Image[] frames = animations.getOrDefault(currentAnimation, animations.get("idle"));
        if (frames == null || frames.length == 0) return;
        double frameDuration = 0.2;
        int frameCount = frames.length;
        animationFrame = (int) (animationTime / frameDuration) % frameCount;
    }

    // Виконує атаку (ближню або дальню)
    public void attack(boolean isRanged) {
        setAnimationState(isRanged ? "shoot" : "hit");
    }

    // Піднімається по драбині
    public void climb(InteractiveObject ladder) {
        setAnimationState("climb");
    }

    // Збільшує рівень виявлення
    public void increaseDetection() {
        // TODO: Реалізувати логіку виявлення
        // Оновити рівень виявлення гравця (наприклад, для Police або SecurityCamera)
    }

    // Телепортує гравця до іншої кімнати
    public void teleportToRoom(int roomId) {
        // TODO: Реалізувати телепортацію
        // Змінити позицію гравця до координат кімнати roomId
    }

    // Телепортує гравця до іншого поверху
    public void teleportToFloor(int floorId) {
        // TODO: Реалізувати телепортацію
        // Завантажити новий рівень через GameManager.loadLevel(floorId)
    }

    // --- Рендеринг ---

    // Рендерить гравця на canvas (викликається з GameManager.render())
    @Override
    public void render(GraphicsContext gc) {
        Image frame = getCurrentFrame();
        if (frame != null && isVisible) {
            gc.setImageSmoothing(false);
            Bounds bounds = getImagineBounds();
            double renderX = bounds.getMinX();
            double renderY = bounds.getMinY();
            double renderWidth = imageWidth;
            double renderHeight = imageHeight;
            if (direction == Direction.LEFT) {
                gc.save();
                gc.translate(renderX + renderWidth, renderY);
                gc.scale(-1, 1);
                gc.drawImage(frame, 0, 0, renderWidth, renderHeight);
                gc.restore();
            } else {
                gc.drawImage(frame, renderX, renderY, renderWidth, renderHeight);
                System.out.println("Rendering object at (" + renderX + ", " + renderY + ")" + " WIDTH " + renderWidth + " height " + renderHeight);
            }
            // Малюємо червону рамку для колізійної області
            Bounds collBounds = getBounds();
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeRect(collBounds.getMinX(), collBounds.getMinY(), collBounds.getWidth(), collBounds.getHeight());
        }
    }

    // Повертає поточний кадр анімації
    @Override
    public Image getCurrentFrame() {
        Image[] frames = animations.getOrDefault(currentAnimation, animations.get("idle"));
        if (frames == null || frames.length == 0) {
            System.err.println("Немає кадрів для анімації: " + currentAnimation);
            return null;
        }
        return frames[animationFrame];
    }

    // --- Взаємодії ---

    // Встановлює стан анімації
    @Override
    public void setAnimationState(String state) {
        if (animations.containsKey(state) && !state.equals(currentAnimation)) {
            currentAnimation = state;
            animationFrame = 0;
            animationTime = 0;
        }
    }

    // Взаємодія з гравцем (порожня, оскільки гравець не взаємодіє сам із собою)
    @Override
    public void interact(Player player) {
        // Порожня реалізація
    }

    // Перевіряє можливість взаємодії (завжди false для гравця)
    @Override
    public boolean canInteract(Player player) {
        return false;
    }

    // --- Серіалізація ---

    // Повертає JSON для збереження (викликається з SaveManager.savePlayer())
    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("x", imageX);
        data.put("y", imageY + imageHeight); // Зберігаємо як нижній лівий кут
        data.put("collX", collX);
        data.put("collY", collY);
        data.put("width", imageWidth);
        data.put("height", imageHeight);
        data.put("widthColl", collWidth);
        data.put("hightColl", collHeight);
        data.put("direction", direction.toString());
        data.put("state", state.toString());
        data.put("currentAnimation", currentAnimation);
        data.put("canMove", canMove);
        data.put("type", "Player");
        return data;
    }

    // Відновлює стан із JSON (викликається з SaveManager.loadGame())
    @Override
    public void setFromData(JSONObject data) {
        double jsonImageX = data.optDouble("x", imageX);
        double jsonImageY = data.optDouble("y", imageY + imageHeight);
        double jsonCollX = data.optDouble("collX", collX);
        double jsonCollY = data.optDouble("collY", collY);
        this.imageX = jsonImageX;
        this.imageY = jsonImageY - imageHeight;
        this.collX = jsonCollX;
        this.collY = jsonCollY;
        this.imageWidth = data.optDouble("width", imageWidth);
        this.imageHeight = data.optDouble("height", imageHeight);
        this.collWidth = data.optDouble("widthColl", collWidth);
        this.collHeight = data.optDouble("hightColl", collHeight);
        this.canMove = data.optBoolean("canMove", canMove);
        String directionStr = data.optString("direction", direction.toString()).toUpperCase();
        try {
            this.direction = Direction.valueOf(directionStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Невірне значення direction: " + directionStr + ". Залишаю поточний.");
        }
    }

    // --- Геттери/Сеттери ---

    // Повертає тип об’єкта
    @Override
    public String getType() {
        return "Player";
    }

    // Повертає позицію (верхній лівий кут колізійної області)
    @Override
    public Vector2D getPosition() {
        return new Vector2D(collX, collY);
    }

    // Повертає уявну позицію (верхній лівий кут зображення)
    @Override
    public Vector2D getImaginePosition() {
        return new Vector2D(imageX, imageY);
    }

    // Встановлює позицію колізійної області
    @Override
    public void setPosition(Vector2D position) {
        this.collX = position.getX();
        this.collY = position.getY();
    }

    // Встановлює уявну позицію зображення
    @Override
    public void setImaginePosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
    }

    // Повертає межі колізійної області
    @Override
    public Bounds getBounds() {
        return new BoundingBox(collX, collY, collWidth, collHeight);
    }

    // Повертає межі зображення
    @Override
    public Bounds getImagineBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    // Повертає діапазон взаємодії
    @Override
    public double getInteractionRange() {
        return 0.0; // Гравець не взаємодіє сам із собою
    }

    // Повертає підказку для UI
    @Override
    public String getInteractionPrompt() {
        return null; // Гравець не має підказки
    }

    // Повертає шар рендерингу
    @Override
    public int getRenderLayer() {
        return 1; // Гравець рендериться на шарі 1
    }

    // Перевіряє видимість
    @Override
    public boolean isVisible() {
        return state != PlayerState.INVISIBLE;
    }

    // Повертає напрям
    public Direction getDirection() {
        return direction;
    }

    // Перевіряє можливість руху
    public boolean isCanMove() {
        return canMove;
    }

    // Встановлює можливість руху
    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }
}