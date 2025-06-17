package entities;

import interfaces.*;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import managers.GameManager;
import managers.SoundManager;
import org.json.JSONObject;
import ui.ShopItem;
import utils.GameLoader;
import utils.Vector2D;

import java.util.HashMap;
import java.util.Map;

/**
 * Представляє ігрового персонажа (гравця), який може рухатися, атакувати,
 * взаємодіяти з об’єктами та керувати своїм інвентарем.
 */
public class Player implements Animatable, GameObject, Interactable {
    private boolean isAttacking;
    private String attackAnimationType;
    private double attackAnimationDuration;

    // Поля
    private double imageX;
    private double imageY;
    private double collX;
    private double collY;
    private double imageWidth;
    private double imageHeight;
    private double collWidth;
    private double collHeight;
    private Direction direction;
    private PlayerState state;
    private double speed;
    private boolean isVisible;
    private String currentAnimation;
    private int animationFrame;
    private double animationTime;
    private Map<String, Image[]> animations;
    private String[] spritePaths;
    private boolean canMove;
    private int detectionCount;
    private Map<ShopItem, Integer> inventory; // Інвентар: предмет -> кількість
    private Map<ShopItem, Boolean> itemUsage; // Стан використання: предмет -> чи використовується
    private JSONObject mapData; // Дані карти для синхронізації

    private final SoundManager soundManager = SoundManager.getInstance();


    private double baseSpeed; // Для збереження початкової швидкості
    private boolean isSpeedBoosted;
    private double speedBoostTimer;


    private boolean isInvisible;
    private double invisibilityTimer;


    private boolean hasUniversalKey;

    // Напрями та стани гравця
    public enum Direction { LEFT, RIGHT, UP, DOWN }
    public enum PlayerState { IDLE, RUN, HIT, CLIMB, INVISIBLE, SHOOT }

    /**
     * Конструктор для створення об'єкта гравця.
     * Ініціалізує позицію, розміри, анімації та інвентар на основі JSON-даних.
     * @param position Початкова позиція гравця у вигляді вектора.
     * @param defaultData JSONObject, що містить усі параметри для ініціалізації гравця.
     */
    // Конструктор: ініціалізує гравця з JSON-даними
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
        this.detectionCount = defaultData.optInt("detectionCount", 0);
        this.inventory = new HashMap<>();
        this.itemUsage = new HashMap<>();
        this.mapData = defaultData; // Зберігаємо дані карти
        // Ініціалізація інвентарю з mapData
        initializeInventory();
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
        this.baseSpeed = this.speed; // Зберігаємо базову швидкість
        this.isSpeedBoosted = false;
        this.speedBoostTimer = 0.0;
        this.isVisible = true;



        this.isInvisible = false;
        this.invisibilityTimer = 0.0;


        this.hasUniversalKey = false;
        this.isVisible = true;

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


    /**
     * Ініціалізує інвентар гравця на основі даних, що зберігаються в `mapData`.
     * Використовується для відновлення стану інвентарю при завантаженні.
     */
    // Ініціалізує інвентар із mapData
    private void initializeInventory() {
        // Перевіряємо наявність предметів у mapData
        for (String key : new String[]{"invisibility", "key", "speedBoost", "gun"}) {
            int quantity = mapData.optInt(key, 0);
            if (quantity > 0) {
                // Шукаємо існуючий ShopItem у inventory
                ShopItem item = findItemByMapKey(key);
                if (item != null) {
                    inventory.put(item, quantity);
                    itemUsage.put(item, false); // За замовчуванням не використовується
                }
            }
        }
    }


    /**
     * Знаходить об'єкт ShopItem в інвентарі за його строковим ключем, що використовується в `mapData`.
     * @param mapKey Ключ предмета (наприклад, "invisibility", "key").
     * @return Знайдений об'єкт ShopItem або null, якщо не знайдено.
     */
    // Знаходить ShopItem за ключем mapData
    private ShopItem findItemByMapKey(String mapKey) {
        for (ShopItem item : inventory.keySet()) {
            String itemMapKey = getMapKeyForItem(item);
            if (mapKey.equals(itemMapKey)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Додає предмет до інвентарю гравця.
     * @param item Предмет, який потрібно додати.
     * @return true, якщо додавання успішне.
     */
    public boolean buyItem(ShopItem item) {
        inventory.put(item, inventory.getOrDefault(item, 0) + 1);
        itemUsage.putIfAbsent(item, false);
        updateMapData(item);
        if (inventory.get(item) == 0) {
            inventory.remove(item);
            itemUsage.remove(item);
        }
        updateMapData(item);
        System.out.println("Гравець отримав: " + item.getName());
        return true;
    }


    /**
     * Оновлює кількість конкретного предмета в `mapData` для подальшого збереження.
     * @param item Предмет, дані якого потрібно оновити.
     */
    private void updateMapData(ShopItem item) {
        String mapKey = getMapKeyForItem(item);
        if (mapKey != null) {
            int currentQuantity = inventory.getOrDefault(item, 0);
            mapData.put(mapKey, currentQuantity);
        }
    }


    /**
     * Повертає строковий ключ для предмета, що використовується для збереження в `mapData`.
     * @param item Предмет, для якого потрібно отримати ключ.
     * @return Строковий ключ або null.
     */
    private String getMapKeyForItem(ShopItem item) {
        switch (item.getName()) {
            case "Невидимість": return "invisibility";
            case "Універсальний ключ": return "key";
            case "Буст швидкості": return "speedBoost";
            case "Пістолет": return "gun";
            default: return null;
        }
    }


    /**
     * Повертає копію інвентарю гравця.
     * @return Копія `Map`, де ключ - ShopItem, а значення - кількість.
     */
    public Map<ShopItem, Integer> getInventory() {
        return new HashMap<>(inventory); // Повертаємо копію інвентаря
    }

    /**
     * Повністю очищає інвентар гравця та скидає всі пов'язані з ним стани.
     */
    public void clearInventory() {
        inventory.clear();
        itemUsage.clear();
        hasUniversalKey = false;
        isInvisible = false;
        isSpeedBoosted = false;
        speed = baseSpeed;
    }


    /**
     * Встановлює напрямок погляду гравця.
     * @param direction Новий напрямок.
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }


    /**
     * Рухає гравця в заданому напрямку, розбиваючи рух на менші кроки при високій швидкості.
     * @param direction Напрямок руху.
     * @param deltaTime Час, що минув з останнього кадру.
     */
    public void move(Direction direction, double deltaTime) {
        if (canMove) {
            this.direction = direction;
            setAnimationState("run");

            // Обчислюємо базове переміщення
            double movement = speed * deltaTime;

            // Визначаємо кількість мікрокроків (залежить від швидкості)
            double speedRatio = speed / Math.max(baseSpeed, 1.0); // Відношення поточної швидкості до базової
            int steps = (int) Math.ceil(speedRatio); // Кількість кроків (округлюємо вгору)
            double stepMovement = movement / steps; // Переміщення за один крок

            double deltaX = 0;
            if (direction == Direction.LEFT) {
                deltaX = -stepMovement;
            } else if (direction == Direction.RIGHT) {
                deltaX = stepMovement;
            }

            // Виконуємо рух по кроках
            for (int i = 0; i < steps; i++) {
                // Оновлюємо позицію
                collX += deltaX;
                imageX += deltaX;

                // Викликаємо перевірку колізій після кожного кроку
                GameManager.getInstance().checkPlayerCollisions();
            }
        }
    }


    /**
     * Зупиняє рух гравця і встановлює анімацію бездіяльності.
     */    public void stopMovement() {
        setCanMove(false);
        setAnimationState("idle");
    }

    // Дозволяє рух гравця
    public void allowMovement() {
        setCanMove(true);
    }

    /**
     * Оновлює стан анімації гравця та таймери активних ефектів (прискорення, невидимість).
     * @param deltaTime Час, що минув з останнього кадру.
     */
    @Override
    public void updateAnimation(double deltaTime) {
        //  Оновлюємо час анімації ОДИН РАЗ на початку
        animationTime += deltaTime;

        if (isSpeedBoosted) {
            speedBoostTimer -= deltaTime;
            if (speedBoostTimer <= 0) {
                isSpeedBoosted = false;
                this.speed = baseSpeed; // Повертаємо швидкість до базової
                System.out.println("Дія прискорення закінчилась. Швидкість повернуто до: " + this.speed);
            }
        }


        if (isInvisible) {
            invisibilityTimer -= deltaTime;
            if (invisibilityTimer <= 0) {
                isInvisible = false;
                this.state = PlayerState.IDLE; // Повертаємо до звичайного стану
                System.out.println("Дія невидимості закінчилась.");
            }
        }


        Image[] frames = animations.getOrDefault(currentAnimation, animations.get("idle"));
        if (frames == null || frames.length == 0) return;

        //  Розраховуємо кадр анімації, використовуючи вже оновлений animationTime
        if (isAttacking) {
            attackAnimationDuration -= deltaTime;
            double frameDuration = 0.2; // Можна винести цю змінну за межі if/else
            int frameCount = frames.length;
            animationFrame = (int) (animationTime / frameDuration) % frameCount;
            if (attackAnimationDuration <= 0) {
                isAttacking = false;
                setAnimationState("idle");
            }
        } else {
            double frameDuration = 0.2;
            int frameCount = frames.length;
            animationFrame = (int) (animationTime / frameDuration) % frameCount;
        }
    }

    /**
     * Ініціює атаку гравця.
     * @param isRanged true для дальньої атаки (постріл), false для ближньої (удар).
     */    public void attack(boolean isRanged) {

        if (!isAttacking) {
            isAttacking = true;
            setAnimationState(isRanged ? "shoot" : "hit");
            Image[] frames = animations.get(currentAnimation);
            if (frames != null) {
                attackAnimationDuration = frames.length * 0.2;
            }
        }
    }

    /**
     * Збільшує лічильник виявлення гравця. Викликається, коли гравця помічає ворог.
     */    public void increaseDetection() {
        this.detectionCount++;
        System.out.println("Гравець виявлений! Рівень виявлення: " + this.detectionCount);
    }


    /**
     * Телепортує гравця в іншу кімнату через двері.
     * @param door Двері, через які відбувається телепортація.
     */
    public void teleportToRoom(Door door) {
        Direction currentDirection = this.getDirection();
        if (!door.isLaser()) {
            adjustPlayerPosition(140.0, currentDirection);
        }
        System.out.println("Teleported to room: x=" + getPosition().x + ", y=" + getPosition().y);
    }


    /**
     * Телепортує гравця на інший поверх.
     * @param door Двері (сходи), через які відбувається телепортація.
     * @param levelId ID поточного рівня для визначення зміщення.
     */
    public void teleportToFloor(Door door, int levelId) {
        String doorDirection = door.direction;
        Direction teleportDirection;
        if (doorDirection.equals("up")) {
            teleportDirection = Direction.UP;
        } else if (doorDirection.equals("down")) {
            teleportDirection = Direction.DOWN;
        } else {
            return;
        }
        stopMovement();
        setAnimationState("idle");
        if (levelId==1){
            adjustPlayerPosition(120, teleportDirection);
        } else {
            adjustPlayerPosition(113, teleportDirection);
        }
        System.out.println("Teleported to floor: x=" + getPosition().x + ", y=" + getPosition().y);
    }


    /**
     * Зміщує позицію гравця на задану відстань у вказаному напрямку.
     * Використовується для телепортації та корекції позиції при колізіях.
     * @param offset Відстань зміщення.
     * @param direction Напрямок зміщення.
     */
    public void adjustPlayerPosition(double offset, Direction direction) {
        Vector2D currentPosition = getPosition();
        Vector2D currentImaginePosition = getImagePosition();
        double adjustmentX = 0;
        double adjustmentY = 0;
        double backOffDistance = offset;
        if (direction == Direction.LEFT) {
            adjustmentX = -backOffDistance;
        } else if (direction == Direction.RIGHT) {
            adjustmentX = backOffDistance;
        } else if (direction == Direction.DOWN) {
            adjustmentY = backOffDistance;
        } else if (direction == Direction.UP) {
            adjustmentY = -backOffDistance;
        }
        Vector2D newPosition = new Vector2D(currentPosition.x + adjustmentX, currentPosition.y + adjustmentY);
        setPosition(newPosition);
        Vector2D newImaginePosition = new Vector2D(currentImaginePosition.x + adjustmentX, currentImaginePosition.y + adjustmentY);
        setImagePosition(newImaginePosition);
    }

    /**
     * Рендерить поточний кадр анімації гравця на екрані.
     * Враховує напрямок погляду для віддзеркалення спрайту.
     * @param gc Графічний контекст для рендерингу.
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

             }
    }



    /**
     * Повертає поточне зображення (кадр) для поточної анімації.
     * @return Об'єкт Image, що представляє поточний кадр.
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

    /**
     * Встановлює поточний стан анімації гравця.
     * @param state Назва стану анімації (наприклад, "run", "idle").
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
     * Метод взаємодії. Для гравця він порожній, оскільки гравець є ініціатором взаємодій.
     * @param player Об'єкт гравця.
     */
    @Override
    public void interact(Player player) {
        // Порожня реалізація
    }


    /**
     * Визначає, чи може інший об'єкт взаємодіяти з гравцем.
     * @param player Об'єкт гравця.
     * @return Завжди false, оскільки ніхто не "взаємодіє" з гравцем у такий спосіб.
     */
    @Override
    public boolean canInteract(Player player) {
        return false;
    }



    /**
     * Серіалізує стан гравця у формат JSONObject для збереження.
     * @return JSONObject зі станом гравця.
     */

    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("x", imageX);
        data.put("y", imageY + imageHeight);
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
        data.put("detectionCount", detectionCount);
        // Синхронізація інвентарю з mapData
        for (Map.Entry<ShopItem, Integer> entry : inventory.entrySet()) {
            String mapKey = getMapKeyForItem(entry.getKey());
            if (mapKey != null) {
                data.put(mapKey, entry.getValue());
            }
        }
        // Серіалізація стану використання
        JSONObject usageData = new JSONObject();
        for (Map.Entry<ShopItem, Boolean> entry : itemUsage.entrySet()) {
            usageData.put(entry.getKey().getName(), entry.getValue());
        }
        data.put("itemUsage", usageData);
        data.put("type", "Player");
        data.put("type", "Player");
        return data;
    }


    /**
     * Відновлює стан гравця з JSONObject.
     * @param data JSONObject зі збереженим станом гравця.
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
        this.canMove = data.optBoolean("canMove", true);
        this.detectionCount = data.optInt("detectionCount", 0);
        this.mapData = data;
        // Оновлення інвентарю
        initializeInventory();
        // Відновлення itemUsage
        JSONObject usageData = data.optJSONObject("itemUsage");
        if (usageData != null) {
            for (ShopItem item : inventory.keySet()) {
                String name = item.getName();
                if (usageData.has(name)) {
                    itemUsage.put(item, usageData.getBoolean(name));
                }
            }
        }
        String directionStr = data.optString("direction", direction.toString()).toUpperCase();
        try {
            this.direction = Direction.valueOf(directionStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Невірне значення direction: " + directionStr + ". Залишаю поточний.");
        }
    }

    /**
     * Повертає тип об'єкта.
     * @return Рядок "Player".
     */
    @Override
    public String getType() {
        return "Player";
    }

    /**
     * Повертає позицію колізійного прямокутника гравця.
     * @return Vector2D з координатами.
     */
    @Override
    public Vector2D getPosition() {
        return new Vector2D(collX, collY);
    }


    /**
     * Повертає позицію візуального спрайту гравця.
     * @return Vector2D з координатами.
     */
    @Override
    public Vector2D getImagePosition() {
        return new Vector2D(imageX, imageY);
    }


    /**
     * Встановлює позицію колізійного прямокутника гравця.
     * @param position Нова позиція.
     */

    @Override
    public void setPosition(Vector2D position) {
        this.collX = position.getX();
        this.collY = position.getY();
    }


    /**
     * Встановлює позицію візуального спрайту гравця.
     * @param position Нова позиція.
     */
    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
    }

    /**
     * Повертає межі колізійного прямокутника гравця.
     * @return Об'єкт Bounds.
     */
    @Override
    public Bounds getBounds() {
        return new BoundingBox(collX, collY, collWidth, collHeight);
    }

    /**
     * Повертає межі візуального спрайту гравця.
     * @return Об'єкт Bounds.
     */
    @Override
    public Bounds getImageBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Повертає радіус взаємодії. Для гравця він дорівнює 0.
     * @return 0.0
     */
    @Override
    public double getInteractionRange() {
        return 0.0;
    }

    /**
     * Повертає текст підказки для взаємодії. Для гравця він відсутній.
     * @return null
     */
    @Override
    public String getInteractionPrompt() {
        return null;
    }

    /**
     * Повертає шар рендерингу для гравця.
     * @return 2 (гравець рендериться поверх більшості об'єктів).
     */
    @Override
    public int getRenderLayer() {
        return 2;
    }


    /**
     * Визначає, чи є гравець видимим.
     * @return true, якщо гравець не в стані INVISIBLE.
     */
    @Override
    public boolean isVisible() {
        return state != PlayerState.INVISIBLE;
    }

    /**
     * Повертає поточний напрямок гравця.
     * @return Напрямок (Direction).
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Встановлює, чи може гравець рухатися.
     * @param canMove true, щоб дозволити рух, false - щоб заборонити.
     */

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }
    /**
     * Перевіряє, чи гравець зараз атакує.
     * @return true, якщо виконується анімація атаки.
     */

    public boolean isAttacking() {
        return isAttacking;
    }



    /**
     * Використовує один предмет з інвентарю.
     * @param item Предмет для використання.
     * @return true, якщо предмет був успішно використаний, інакше false.
     */
    public boolean useItem(ShopItem item) {
        int currentQuantity = inventory.getOrDefault(item, 0);
        if (currentQuantity > 0) {
            inventory.put(item, currentQuantity - 1);
            updateMapData(item); // Оновлюємо дані для збереження
            System.out.println("Використано предмет: " + item.getName() + ", залишилось: " + (currentQuantity - 1));
            return true;
        }
        System.out.println("Неможливо використати предмет: " + item.getName() + ", немає в наявності.");
        return false;
    }




    /**
     * Активує тимчасове прискорення для гравця.
     * @param duration Тривалість ефекту в секундах.
     */
    public void applySpeedBoost(double duration) {
        if (!isSpeedBoosted) { // Застосовуємо, тільки якщо буст не активний
            isSpeedBoosted = true;
            this.speed *= 5; // Подвоюємо швидкість
            System.out.println("Прискорення активовано! Нова швидкість: " + this.speed);
        } else {
            System.out.println("Прискорення поновлено!");
        }
        this.speedBoostTimer = duration; // Встановлюємо або оновлюємо таймер
    }




    /**
     * Активує тимчасову невидимість для гравця.
     * @param duration Тривалість ефекту в секундах.
     */
    public void applyInvisibility(double duration) {
        if (!isInvisible) { // Застосовуємо, тільки якщо невидимість не активна
            this.state = PlayerState.INVISIBLE; // Змінюємо стан
            System.out.println("Невидимість активовано!");
        } else {
            System.out.println("Невидимість поновлено!");
        }
        this.isInvisible = true;
        this.invisibilityTimer = duration; // Встановлюємо або оновлюємо таймер
    }


    public boolean isInvisible() {
        return isInvisible;
    }



    /**
     * Дає гравцеві універсальний ключ.
     */
    public void giveUniversalKey() {
        this.hasUniversalKey = true;
        System.out.println("Гравець отримав універсальний ключ!");
    }

    /**
     * Перевіряє, чи є у гравця універсальний ключ.
     * @return true, якщо ключ є, інакше false.
     */
    public boolean hasUniversalKey() {
        return this.hasUniversalKey;
    }

    /**
     * Використовує універсальний ключ. Ключ зникає після використання.
     */
    public void useUniversalKey() {
        if (this.hasUniversalKey) {
            this.hasUniversalKey = false;
            System.out.println("Універсальний ключ використано.");
        }
    }


    /**
     * Перевіряє, чи є у гравця пістолет в інвентарі.
     * @return true, якщо пістолет є, інакше false.
     */

    public boolean hasGun() {
        for (Map.Entry<ShopItem, Integer> entry : inventory.entrySet()) {
            if (entry.getKey().getItemType() == ShopItem.ItemType.GUN && entry.getValue() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Повертає поточну кількість разів, коли гравця було виявлено.
     * @return кількість виявлень.
     */
    public int getDetectionCount() {
        return this.detectionCount;
    }

}