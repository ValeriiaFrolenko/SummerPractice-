package entities;

import interfaces.*;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import managers.GameManager;
import org.json.JSONObject;
import utils.GameLoader;
import utils.Vector2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Представляє поліцейського NPC, який патрулює або переслідує гравця
public class Police implements Animatable, GameObject, Interactable {
    // Поля
    private double imageX; // Верхній лівий кут зображення по X
    private double imageY; // Верхній лівий кут зображення по Y
    private double collX; // Верхній лівий кут колізійної області по X
    private double collY; // Верхній лівий кут колізійної області по Y
    private double imageWidth; // Ширина зображення, з JSON
    private double imageHeight; // Висота зображення, з JSON
    private double collWidth; // Ширина колізійної області, з JSON
    private double collHeight; // Висота колізійної області, з JSON
    private PoliceDirection direction; // Напрям руху (LEFT, RIGHT), з JSON
    private PoliceState state; // Стан (PATROL, CHASE, ALERT, STUNNED, IDLE), з JSON
    private String currentAnimation; // Поточна анімація ("idle", "patrol", "stunned", "alarm")
    private int animationFrame; // Поточний кадр анімації
    private double animationTime; // Час для анімації
    private Map<String, Image[]> animations; // Анімації, завантажені через GameLoader
    private String[] spritePaths; // Шляхи до спрайтів
    private double normalSpeed = 40.0; // Швидкість патрулювання
    private double chaseSpeed = 70.0; // Швидкість переслідування
    private double stunDuration = 0.0; // Тривалість стану STUNNED
    private static final double MAX_STUN_DURATION = 3.0; // Максимальна тривалість оглушення (в секундах)
    private boolean canSeePlayer;
    private boolean inSameRoom;
    private double alarmDuration = 3;
    private double frameDuration = 0.2;

    @Override
    public void interact(Player player) {
        takeHit(false);
    }

    @Override
    public boolean canInteract(Player player) {
        Bounds playerBounds = player.getBounds();
        Bounds policeBounds = this.getBounds();
        Bounds newBounds = new BoundingBox(playerBounds.getMinX(), playerBounds.getMinY(), playerBounds.getWidth(), playerBounds.getHeight());
        // Перевіряємо перекриття bounds'ів
        boolean hasOverlap = newBounds.intersects(policeBounds);

        if (!hasOverlap) {
            return false;
        }
        if (canSeePlayer && inSameRoom) {
            return false;
        }
        return true;
    }

    @Override
    public double getInteractionRange() {
        return 0;
    }

    @Override
    public String getInteractionPrompt() {
        return "Press Q to hit";
    }

    // Напрями та стани поліцейського
    public enum PoliceDirection { LEFT, RIGHT }
    public enum PoliceState { PATROL, CHASE, ALERT, STUNNED, IDLE }

    // Конструктор: ініціалізує поліцейського з JSON-даними
    public Police(Vector2D position, JSONObject defaultData) {
        this.imageHeight = defaultData.getDouble("height");
        this.imageX = position.x;
        this.imageY = position.y - imageHeight;
        this.collX = defaultData.getDouble("collX");
        this.collY = defaultData.getDouble("collY");
        this.imageWidth = defaultData.getDouble("width");
        this.collWidth = defaultData.getDouble("widthColl");
        this.collHeight = defaultData.getDouble("hightColl");
        this.direction = PoliceDirection.valueOf(defaultData.optString("direction", "LEFT"));
        this.state = PoliceState.valueOf(defaultData.optString("state", "PATROL"));
        this.currentAnimation = defaultData.optString("currentAnimation", "patrol");
        this.animations = new HashMap<>();
        this.spritePaths = new String[]{"police/idle.png", "police/run.png", "police/lay.png", "police/alarm.png", "police/question.png"};
        GameLoader loader = new GameLoader();
        animations.put("idle", loader.splitSpriteSheet(spritePaths[0], 12));
        animations.put("patrol", loader.splitSpriteSheet(spritePaths[1], 9));
        animations.put("stunned", loader.splitSpriteSheet(spritePaths[2], 1));
        animations.put("alarm", loader.splitSpriteSheet(spritePaths[3], 2));
        animations.put("question", loader.splitSpriteSheet(spritePaths[4], 1));

        this.animationFrame = 0;
        this.animationTime = 0;
    }

    // --- Ініціалізація та оновлення ---

    // Перевіряє, чи гравець у тій самій кімнаті
    private boolean isPlayerInSameRoom(List<GameManager.Room> rooms, Player player) {
        Bounds playerBounds = player.getBounds();
        Bounds policeBounds = getBounds();
        for (GameManager.Room room : rooms) {
            if (room.getBounds().contains(policeBounds.getCenterX(), policeBounds.getCenterY()) &&
                    room.getBounds().contains(playerBounds.getCenterX(), playerBounds.getCenterY())) {
                return true;
            }
        }
        return false;
    }

    // Оновлює логіку поліцейського (викликається з GameManager.update())
    public void update(double deltaTime, List<GameManager.Room> rooms, Player player) {
        System.out.println(state);
        // Оновлення стану оглушення
        if (state == PoliceState.STUNNED) {
            stunDuration -= deltaTime;
            if (stunDuration <= 0) {
                inSameRoom = isPlayerInSameRoom(rooms, player);
                if (inSameRoom) {
                    state = PoliceState.CHASE;
                    setAnimationState("patrol");
                    // Визначаємо напрямок до гравця
                    double playerX = player.getPosition().x;
                    double policeX = getPosition().x;
                    if (playerX < policeX) {
                        direction = PoliceDirection.LEFT;
                    } else {
                        direction = PoliceDirection.RIGHT;
                    }
                } else {
                    state = PoliceState.PATROL;
                    setAnimationState("patrol");
                }
            }
            return;
        }
        if (state == PoliceState.ALERT) {
            alarmDuration -= deltaTime;
            if (alarmDuration <= 0) {
                state = PoliceState.PATROL;
                setAnimationState("patrol");
            }
            return;
        }

        // Перевірка, чи гравець у тій самій кімнаті
        inSameRoom = isPlayerInSameRoom(rooms, player);

        // Якщо гравець у тій самій кімнаті, перевіряємо виявлення
        if (inSameRoom) {
            canSeePlayer = false;
            double playerX = player.getPosition().x;
            double policeX = getPosition().x;
            double playerCenterX = player.getBounds().getCenterX();
            double policeMinX = getBounds().getMinX();
            double policeMaxX = getBounds().getMaxX();
            double policeMidX = policeMinX + getBounds().getWidth() / 2.0;
            double policeWidth = getBounds().getWidth();

            // Перевірка позиції гравця (лівіше для LEFT, правіше для RIGHT)
            if (direction == PoliceDirection.LEFT && playerX < policeX ||
                    direction == PoliceDirection.RIGHT && playerX > policeX) {
                canSeePlayer = true;
            }

            // Перевірка перетину меж
            if (player.getBounds().intersects(getBounds())) {
                // Повний перетин: гравець повністю в межах поліцейського
                if (player.getBounds().getMinX() >= policeMinX && player.getBounds().getMaxX() <= policeMaxX) {
                    canSeePlayer = true;
                } else {
                    // Часткове перетин: перевіряємо передню половину
                    if (direction == PoliceDirection.LEFT && playerCenterX <= policeMidX) {
                        canSeePlayer = true; // Гравець у передній половині (ліва для LEFT)
                    } else if (direction == PoliceDirection.RIGHT && playerCenterX >= policeMidX) {
                        canSeePlayer = true; // Гравець у передній половині (права для RIGHT)
                    } else {
                        // Задня половина: перевіряємо, чи перетин більше половини
                        double overlapWidth = Math.min(player.getBounds().getMaxX(), policeMaxX) - Math.max(player.getBounds().getMinX(), policeMinX);
                        if (overlapWidth > policeWidth / 2.0) {
                            canSeePlayer = true; // Перетин ззаду більше половини
                        }
                    }
                }
            }

            // Якщо гравець виявлений, переслідуємо
            if (canSeePlayer) {
                player.increaseDetection();
                state = PoliceState.CHASE;
                setAnimationState("patrol"); // Використовуємо анімацію бігу для переслідування
                // Визначаємо напрямок до гравця
                if (playerX < policeX) {
                    direction = PoliceDirection.LEFT;
                } else {
                    direction = PoliceDirection.RIGHT;
                }
                // Рухаємося до гравця зі швидкістю переслідування
                patrol(deltaTime, chaseSpeed);
            } else {
                // Якщо гравець не виявлений, патрулюємо
                state = PoliceState.PATROL;
                setAnimationState("patrol");
                patrol(deltaTime, normalSpeed);
            }
        } else {
            // Якщо гравця немає в кімнаті, патрулюємо
            state = PoliceState.PATROL;
            setAnimationState("patrol");
            patrol(deltaTime, normalSpeed);
        }
    }

    // Патрулює зі вказаною швидкістю
    public void patrol(double deltaTime, double speed) {
        setAnimationState("patrol");
        double movement = speed * deltaTime;
        double deltaX = 0;
        frameDuration = 0.2;
        if (direction == PoliceDirection.LEFT) {
            deltaX = -movement;
        } else if (direction == PoliceDirection.RIGHT) {
            deltaX = movement;
        }
        collX += deltaX;
        imageX += deltaX;
    }

    // Оновлює анімацію поліцейського
    @Override
    public void updateAnimation(double deltaTime) {
        System.out.println(frameDuration);

        animationTime += deltaTime;
        Image[] frames = animations.getOrDefault(currentAnimation, animations.get("idle"));
        if (frames == null || frames.length == 0) return;
        int frameCount = frames.length;
        animationFrame = (int) (animationTime / frameDuration) % frameCount;
    }

    // Зупиняє рух поліцейського
    public void stopMovement() {
        state = PoliceState.IDLE;
        setAnimationState("idle");
    }

    // Активує стан тривоги
    public void alert() {
        if (state != PoliceState.ALERT) { // Активуємо лише якщо не в ALERT
            state = PoliceState.ALERT;
            setAnimationState("alarm");
            alarmDuration = 3.0;
            frameDuration = 1.5;
            animationTime = 0;
            animationFrame = 0;
        }
    }

    // Обробляє отримання удару
    public void takeHit(boolean isRanged) {
        state = PoliceState.STUNNED;
        setAnimationState("stunned");
        stunDuration = MAX_STUN_DURATION; // Встановлюємо тривалість оглушення
    }

    // --- Рендеринг ---

    // Рендерить поліцейського на canvas (викликається з GameManager.render())
    @Override
    public void render(GraphicsContext gc) {
        Image frame = getCurrentFrame();
        if (frame != null) {
            gc.setImageSmoothing(false);
            Bounds bounds = getImageBounds();
            double renderX = bounds.getMinX();
            double renderY = bounds.getMinY();
            double renderWidth = imageWidth;
            double renderHeight = imageHeight;

            // Рендеримо основне зображення поліцейського
            if (direction == PoliceDirection.LEFT) {
                gc.save();
                gc.translate(renderX + renderWidth, renderY);
                gc.scale(-1, 1);
                gc.drawImage(frame, 0, 0, renderWidth, renderHeight);
                gc.restore();
            } else {
                gc.drawImage(frame, renderX, renderY, renderWidth, renderHeight);
            }

            // Рендеримо знак питання, якщо поліцейський у стані ALERT
            if (state == PoliceState.ALERT) {
                Image questionFrame = animations.get("question")[0]; // Беремо перший кадр question
                double questionWidth = imageWidth * 0.2; // Зменшуємо розмір знака питання
                double questionHeight = imageHeight * 0.2;
                double questionX = collX + (collWidth - questionWidth) / 2; // Центруємо над головою
                double questionY = collY - 25; // Розміщуємо над головою з відступом
                    gc.save();
                    gc.translate(questionX + questionWidth, questionY);
                    gc.scale(-1, 1);
                    gc.drawImage(questionFrame, 0, 0, questionWidth, questionHeight);
                    gc.restore();
                    gc.drawImage(questionFrame, questionX, questionY, questionWidth, questionHeight);
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

    // --- Серіалізація ---

    // Повертає JSON для збереження (викликається з SaveManager.savePolice())
    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("type", getType());
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
        data.put("stunDuration", stunDuration);
        return data;
    }

    // Відновлює стан із JSON (викликається з SaveManager.loadGame())
    @Override
    public void setFromData(JSONObject data) {
        this.imageX = data.optDouble("x", imageX);
        this.imageY = data.optDouble("y", imageY + imageHeight) - imageHeight; // Конвертуємо назад у верхній лівий кут
        this.collX = data.optDouble("collX", collX);
        this.collY = data.optDouble("collY", collY);
        this.imageWidth = data.optDouble("width", imageWidth);
        this.imageHeight = data.optDouble("height", imageHeight);
        this.collWidth = data.optDouble("widthColl", collWidth);
        this.collHeight = data.optDouble("hightColl", collHeight);
        this.stunDuration = data.optDouble("stunDuration", stunDuration);
        try {
            this.direction = PoliceDirection.valueOf(data.optString("direction", direction.toString()));
        } catch (IllegalArgumentException e) {
            System.err.println("Невірне значення direction: " + data.optString("direction") + ". Залишаю поточний.");
        }
        try {
            this.state = PoliceState.valueOf(data.optString("state", state.toString()));
        } catch (IllegalArgumentException e) {
            System.err.println("Невірне значення state: " + data.optString("state") + ". Залишаю поточний.");
        }
        this.currentAnimation = data.optString("currentAnimation", currentAnimation);
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
        return new Vector2D(collX, collY);
    }

    // Повертає уявну позицію (та ж, що й позиція)
    @Override
    public Vector2D getImagePosition() {
        return new Vector2D(imageX, imageY);
    }

    // Встановлює позицію поліцейського
    @Override
    public void setPosition(Vector2D position) {
        this.collX = position.x;
        this.collY = position.y;
    }

    // Встановлює уявну позицію
    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.x;
        this.imageY = position.y;
    }

    // Повертає межі для колізій
    @Override
    public Bounds getBounds() {
        return new BoundingBox(collX, collY, collWidth, collHeight);
    }

    // Повертає межі для рендерингу
    @Override
    public Bounds getImageBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
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

    public PoliceDirection getDirection() {
        return direction;
    }

    public void setDirection(PoliceDirection direction) {
        this.direction = direction;
    }

    public PoliceState getState() {
        return state;
    }

    public void setState(PoliceState state) {
        this.state = state;
    }
}