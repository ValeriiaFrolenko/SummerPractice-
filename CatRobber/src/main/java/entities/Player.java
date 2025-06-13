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
    private boolean isAttacking; //прапорець, що вказує, чи перебуває об'єкт у стані атаки
    private String attackAnimationType; //тип анімації атаки, яка відтворюється
    private double attackAnimationDuration; // час відтворення анімації атаки

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
    private int detectionCount; //кількість разів, скільки гравець був помічений
    private int money; //поточна кількість грошей, які має гравець

    /** Встановлює напрямок руху
     * @param direction новий напрямок об'єкта
     **/
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * Додає вказану кількість грошей до поточного балансу
     * @param i кількість грошей для додавання
     */
    public void addMoney(int i) {
        money+=i;
    }

    /** Напрями та стани гравця **/
    public enum Direction { LEFT, RIGHT, UP, DOWN }
    public enum PlayerState { IDLE, RUN, HIT, CLIMB, INVISIBLE, SHOOT }

    /**
     * Конструктор класу Player, який ініціалізує об'єкт гравця з початковою позицією та параметрами, отриманими з JSON-даних
     * @param position початкова позиція гравця (координати Vector2D)
     * @param defaultData JSON-об'єкт, що містить параметри гравця (розміри, колізії, напрямок, кількість грошей тощо)
     */
    public Player(Vector2D position, JSONObject defaultData) {
        // Ініціалізація позиції та розмірів із JSON
        double jsonImageX = position.getX();
        double jsonImageY = position.getY();
        double jsonCollX = defaultData.optDouble("collX", jsonImageX);
        double jsonCollY = defaultData.optDouble("collY", jsonImageY - defaultData.optDouble("hightColl", 20.0));
        this.imageWidth = defaultData.getDouble("width");
        this.imageHeight = defaultData.getDouble("height");
        this.collWidth = defaultData.getDouble("widthColl");
        this.collHeight = defaultData.getDouble("hightColl");
        // Конвертація в верхній лівий кут
        this.imageX = jsonImageX;
        this.imageY = jsonImageY - imageHeight;
        this.collX = jsonCollX;
        this.collY = jsonCollY;
        this.detectionCount = defaultData.getInt("detectionCount");
        this.money = defaultData.getInt("money");
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
        this.isAttacking = false;
        this.attackAnimationDuration = 0.0;
        this.attackAnimationType = null;
        // Завантаження анімацій через GameLoader
        this.animations = new HashMap<>();
        this.spritePaths = new String[]{"player/idle.png", "player/run.png", "player/beating.png", "player/shoot.png"};
        GameLoader loader = new GameLoader();
        animations.put("idle", loader.splitSpriteSheet(spritePaths[0], 8));
        animations.put("run", loader.splitSpriteSheet(spritePaths[1], 10));
        animations.put("hit", loader.splitSpriteSheet(spritePaths[2], 13));
        animations.put("shoot", loader.splitSpriteSheet(spritePaths[3], 2));
    }

    // --- Ініціалізація та оновлення ---

    /**
     * Рухає гравця в заданому напрямку з урахуванням часу, що минув
     * Викликається з GameManager.handleInput()
     * @param direction напрямок руху (LEFT або RIGHT)
     * @param deltaTime час, що минув від останнього оновлення (в секундах)
     */
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

    /**
     * Зупиняє рух гравця
     * Викликається з GameManager.handleInput()
     */
    public void stopMovement() {
        setCanMove(false);
        setAnimationState("idle");
    }

    /**
     * Дозволяє рух гравця
     * Викликається з GameManager.checkCollisions()
     */
    public void allowMovement() {
        setCanMove(true);
    }

    /**
     * Оновлює анімацію гравця відповідно до часу, що минув
     * Викликається з GameManager.update()
     *
     * @param deltaTime час, що минув від останнього оновлення анімації (в секундах)
     */
    @Override
    public void updateAnimation(double deltaTime) {
        animationTime += deltaTime;
        Image[] frames = animations.getOrDefault(currentAnimation, animations.get("idle"));
        if (frames == null || frames.length == 0) return;

        if (isAttacking) {
            // Для анімації атаки відтворюємо всі кадри
            attackAnimationDuration -= deltaTime;
            double frameDuration = 0.2; // Тривалість одного кадру
            int frameCount = frames.length;
            animationFrame = (int) (animationTime / frameDuration) % frameCount;
            if (attackAnimationDuration <= 0) {
                // Завершення анімації атаки
                isAttacking = false;
                setAnimationState("idle"); // Повертаємося до idle після атаки
            }
        } else {
            // Звичайна анімація
            double frameDuration = 0.2;
            int frameCount = frames.length;
            animationFrame = (int) (animationTime / frameDuration) % frameCount;
        }
    }

    /**
     * Виконує атаку гравця — ближню або дальню
     * Якщо гравець зараз не атакує, починає анімацію атаки,
     * встановлюючи відповідний тип анімації та тривалість анімації залежно від кількості кадрів
     *
     * @param isRanged якщо true — виконується дальня атака (shoot), якщо false — ближня атака (hit)
     */
    public void attack(boolean isRanged) {
        if (!isAttacking) {
            isAttacking = true;
            setAnimationState(isRanged ? "shoot" : "hit");
            // Встановлюємо тривалість анімації залежно від кількості кадрів
            Image[] frames = animations.get(currentAnimation);
            if (frames != null) {
                attackAnimationDuration = frames.length * 0.2; // 0.2 сек на кадр
            }
        }
    }

    /**
     * Метод, що збільшує рівень виявлення
     */
    public void increaseDetection() {

    }

    /**
     * Телепортує гравця в нову кімнату через задані двері
     * Позиція гравця коригується на фіксовану відстань у напрямку поточного руху, якщо двері не є лазерними
     *
     * @param door двері, через які відбувається телепортація
     */
    public void teleportToRoom(Door door) {
        Direction currentDirection = this.getDirection();
        // Телепортуємо в тому напрямку, куди гравець рухається
       if (!door.isLaser()){
            adjustPlayerPosition(140.0, currentDirection);
        }// Телепортуємо в напрямку руху гравця
        System.out.println("Teleported to room: x=" + getPosition().x + ", y=" + getPosition().y);
    }

    /**
     * Телепортує гравця на інший поверх через задані двері
     * Напрямок телепортації визначається відповідно до напряму дверей ("up" або "down")
     *
     * @param door двері, через які відбувається телепортація на інший поверх
     */
    public void teleportToFloor(Door door) {
        String doorDirection = door.direction;
        Direction teleportDirection;

        // Для поверхів - той самий напрямок руху
        if (doorDirection.equals("up")) {
            teleportDirection = Direction.UP; // Якщо двері верхні, продовжуємо вгору
        } else if (doorDirection.equals("down")) {
            teleportDirection = Direction.DOWN; // Якщо двері нижні, продовжуємо вниз
        } else {
            return; // Некоректний напрямок
        }

        stopMovement(); // Зупиняємо рух і анімацію
        setAnimationState("idle"); // Скидаємо анімацію на idle
        adjustPlayerPosition(120, teleportDirection); // Телепортуємо в тому ж напрямку
        System.out.println("Teleported to floor: x=" + getPosition().x + ", y=" + getPosition().y);
    }

    /**
     * Коригує позицію гравця на певну відстань у вказаному напрямку
     * Позиція гравця та позиція його зображення зміщуються на однакову величину
     *
     * @param offset відстань, на яку потрібно змістити гравця
     * @param direction напрямок, у якому відбувається корекція позиції (LEFT, RIGHT, UP, DOWN)
     */
    public void adjustPlayerPosition(double offset, Direction direction) {
        Vector2D currentPosition = getPosition(); // Отримуємо поточну позицію гравця
        Vector2D currentImaginePosition = getImagePosition(); // Отримуємо уявну позицію
        double adjustmentX = 0; // Зміщення по X
        double adjustmentY = 0; // Зміщення по Y
        double backOffDistance = offset; // Відстань відступу
        if (direction == Player.Direction.LEFT) {
            adjustmentX = -backOffDistance;
        } else if (direction == Player.Direction.RIGHT) {
            adjustmentX = backOffDistance;
        } else if (direction == Direction.DOWN) {
            adjustmentY = backOffDistance;
        } else if (direction == Player.Direction.UP) {
            adjustmentY = -backOffDistance;
        }

        // Оновлюємо позицію гравця
        Vector2D newPosition = new Vector2D(currentPosition.x + adjustmentX, currentPosition.y + adjustmentY);
        setPosition(newPosition); // Встановлюємо нову позицію
        Vector2D newImaginePosition = new Vector2D(currentImaginePosition.x + adjustmentX, currentImaginePosition.y + adjustmentY);
        setImagePosition(newImaginePosition); // Встановлюємо нову позицію зображення
    }

    // --- Рендеринг ---

    /**
     * Відповідає за відображення гравця на канвасі
     * Викликається з GameManager.render()
     * Малює поточний кадр анімації гравця з урахуванням напрямку руху
     *
     * @param gc графічний контекст, на якому виконується малювання
     */
    @Override
    public void render(GraphicsContext gc) {
        Image frame = getCurrentFrame();
        if (frame != null && isVisible) {
            gc.setImageSmoothing(false);
            Bounds bounds = getImageBounds();
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
            }
            // Малюємо червону рамку для колізійної області
            Bounds collBounds = getBounds();
            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeRect(collBounds.getMinX(), collBounds.getMinY(), collBounds.getWidth(), collBounds.getHeight());
        }
    }

    /**
     * Метод, що повертає поточний кадр анімації
     */
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

    /**
     * Встановлює поточний стан анімації гравця
     * Якщо гравець не в стані атаки або ж намагаємося встановити анімацію атаки ("hit" або "shoot"), то змінює анімацію на вказану
     * @param state назва стану анімації, який потрібно встановити
     */
    @Override
    public void setAnimationState(String state) {
        if (!isAttacking || state.equals("hit") || state.equals("shoot")) {
            if (animations.containsKey(state) && !state.equals(currentAnimation)) {
                currentAnimation = state;
                animationFrame = 0;
                animationTime = 0;
            }
        }
    }

    /**
     * Взаємодія з гравцем (порожня, оскільки гравець не взаємодіє сам із собою)
     * @param player об'єкт гравця
     */
    @Override
    public void interact(Player player) {
        // Порожня реалізація
    }

    /**
     * Метод, що перевіряє можливість взаємодії (завжди false для гравця)
     */
    @Override
    public boolean canInteract(Player player) {
        return false;
    }

    // --- Серіалізація ---

    /**
     * Повертає JSON-об’єкт з даними гравця для збереження стану (позицію, розміри, напрямок, стан, поточну анімацію, можливість руху та тип)
     * Викликається з SaveManager.savePlayer()
     * @return JSONObject з параметрами гравця
     */
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

    /**
     * Відновлює стан гравця із JSON-об’єкта
     * Викликається з SaveManager.loadGame()
     * @param data JSONObject з параметрами для відновлення гравця
     */
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

    /**
     * Повертає тип об’єкта
     * @return тип об'єкту "Player"
     */
    @Override
    public String getType() {
        return "Player";
    }

    /**
     * Повертає позицію колізійної області (верхній лівий кут).
     * @return позиція гравця як Vector2D
     */
    @Override
    public Vector2D getPosition() {
        return new Vector2D(collX, collY);
    }

    /**
     * Повертає уявну позицію зображення (верхній лівий кут).
     * @return позиція зображення як Vector2D
     */
    @Override
    public Vector2D getImagePosition() {
        return new Vector2D(imageX, imageY);
    }

    /**
     * Встановлює позицію колізійної області.
     * @param position нова позиція колізії
     */
    @Override
    public void setPosition(Vector2D position) {
        this.collX = position.getX();
        this.collY = position.getY();
    }

    /**
     * Встановлює позицію зображення.
     * @param position нова позиція зображення
     */
    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
    }

    /**
     * Повертає межі колізійної області для обробки зіткнень.
     * @return Bounds з позицією і розмірами колізії
     */
    @Override
    public Bounds getBounds() {
        return new BoundingBox(collX, collY, collWidth, collHeight);
    }

    /**
     * Повертає межі зображення для рендерингу.
     * @return Bounds з позицією і розмірами зображення
     */
    @Override
    public Bounds getImageBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Повертає радіус або діапазон взаємодії гравця.
     * Гравець не взаємодіє сам із собою, тому повертає 0.
     * @return 0.0
     */
    @Override
    public double getInteractionRange() {
        return 0.0;
    }

    /**
     * Повертає підказку для користувача при взаємодії.
     * Гравець не має підказки, тому повертає null.
     * @return null
     */
    @Override
    public String getInteractionPrompt() {
        return null;
    }

    /**
     * Повертає шар, на якому рендериться гравець.
     * @return 2 - номер шару рендерингу
     */
    @Override
    public int getRenderLayer() {
        return 2; // Гравець рендериться на шарі 2
    }

    /**
     * Перевіряє, чи гравець видимий (стан не INVISIBLE).
     * @return true, якщо гравець видимий, інакше false
     */
    @Override
    public boolean isVisible() {
        return state != PlayerState.INVISIBLE;
    }

    /**
     * Повертає напрямок руху гравця.
     * @return поточний напрямок
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Перевіряє, чи гравець може рухатись.
     * @return true, якщо гравець може рухатись
     */
    public boolean isCanMove() {
        return canMove;
    }

    /**
     * Встановлює можливість руху гравця.
     * @param canMove true, якщо гравець може рухатись, інакше false
     */
    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    /**
     * Повертає стан атаки гравця.
     * @return true, якщо гравець виконує атаку
     */
    public boolean isAttacking() {
        return isAttacking;
    }
}