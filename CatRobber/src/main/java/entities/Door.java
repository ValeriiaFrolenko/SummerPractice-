package entities;

import interfaces.*;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.json.JSONObject;
import puzzles.*;
import utils.GameLoader;
import utils.Vector2D;
import managers.GameManager;
import managers.UIManager;
import main.GameWindow;
import java.util.HashMap;
import java.util.Map;

public class Door implements GameObject, Interactable {
    private double imageX, imageY, imageWidth, imageHeight; //координати, ширина та висота дверей
    private boolean isOpen, isLocked, isLaser; //чи відкриті, заблоковані, лазерні двері
    private boolean isRoomLink, isFloorLink; //двері ведуть до іншої кімнати чи на інший поверх
    String direction; // напрямок дверей (left, right, up, down)
    private Map<String, Image> sprites; //зображення дверей
    private String[] spritePaths; // шляхи до файлів зі спрайтами
    private final String path = "background/doors/"; //дефолтний шлях до будь-якої картинки з дверми
    private int sharedId; //ID, який об’єднує двері
    private LockType lockType; //тип замка: CODE_LOCK, PICK_LOCK, LASER_LOCK, NONE

    /**
     * Конструктор, що приймає координати та JSON з інформацією про двері
     * @param vector2D координати дверей
     * @param defaultData //дані про двері
     */
    public Door(Vector2D vector2D, JSONObject defaultData) {
        double jsonImageX = vector2D.getX();
        double jsonImageY = vector2D.getY();
        this.imageWidth = defaultData.getDouble("width");
        this.imageHeight = defaultData.getDouble("height");
        this.imageX = jsonImageX;
        this.imageY = jsonImageY - imageHeight;
        this.sharedId = defaultData.getInt("sharedId");
        this.isOpen = defaultData.optBoolean("isOpen", false);
        this.isLocked = defaultData.optBoolean("isLocked", false);
        this.isLaser = defaultData.optBoolean("isLaser", false);
        this.isRoomLink = defaultData.optBoolean("isRoomLink", false);
        this.isFloorLink = defaultData.optBoolean("isFloorLink", false);
        this.direction = defaultData.optString("direction", "right");
        this.lockType = LockType.valueOf(defaultData.getString("lockType"));
        this.spritePaths = new String[]{
                path + "close.png", path + "closeLeft.png", path + "closeRight.png",
                path + "leftLock.png", path + "openLeft.png", path + "openRight.png",
                path + "rightLock.png", path + "stairsUp.png", path + "stairsDown.png",
                path + "withLock.png", path + "laserLocked.png", path + "laserUnlocked.png"
        };
        GameLoader loader = new GameLoader();
        this.sprites = new HashMap<>();
        //встановлює ключ заначення: put(текстовий індифікатор, об'єкт(зображення))
        sprites.put("stairsClosed", loader.loadImage(spritePaths[0]));
        sprites.put("closedLeft", loader.loadImage(spritePaths[1]));
        sprites.put("closedRight", loader.loadImage(spritePaths[2]));
        sprites.put("lockedLeft", loader.loadImage(spritePaths[3]));
        sprites.put("openLeft", loader.loadImage(spritePaths[4]));
        sprites.put("openRight", loader.loadImage(spritePaths[5]));
        sprites.put("lockedRight", loader.loadImage(spritePaths[6]));
        sprites.put("stairsUp", loader.loadImage(spritePaths[7]));
        sprites.put("stairsDown", loader.loadImage(spritePaths[8]));
        sprites.put("stairsLocked", loader.loadImage(spritePaths[9]));
        sprites.put("laserLocked", loader.loadImage(spritePaths[10]));
        sprites.put("laserUnlocked", loader.loadImage(spritePaths[11]));
    }

    /**
     * Перелік типів замків
     * CODE_LOCK - кодовий замок
     * PICK_LOCK - звичайний замок (з відмичкою)
     * LASER_LOCK - щиток з лазерними дверми
     * NONE - ніякий
     */
    public enum LockType { CODE_LOCK, PICK_LOCK, LASER_LOCK, NONE }

    /**
     * Метод, який викликається при взаємодії гравця з дверми
     * Викликається при натисканні стрілок: телепортує гравця, якщо двері відкриті та не заблоковані
     * @param player гравець
     */
    @Override
    public void interact(Player player) {
        if (!isLocked && isOpen) {
            Player.Direction playerDirection = player.getDirection();
            if (isRoomLink &&
                    ((playerDirection == Player.Direction.LEFT && direction.equals("right")) ||
                            (playerDirection == Player.Direction.RIGHT && direction.equals("left")))) {
                player.teleportToRoom(this);
                System.out.println("Teleporting to room: " + sharedId);
            } else if (isFloorLink &&
                    ((playerDirection == Player.Direction.UP && direction.equals("up")) ||
                            (playerDirection == Player.Direction.DOWN && direction.equals("down")))) {
                player.teleportToFloor(this, GameManager.getInstance().getCurrentLevelId());
                System.out.println("Teleporting to floor: " + sharedId);
            }
        }
    }


    /**
     * Метод, що відкриває двері або запускає процес взаємодії (відображення підказки чи головоломки)
     * залежно від їхнього стану (заблоковані чи ні) та типу замка
     */
    public void open() {
        UIManager uiManager = GameWindow.getInstance().getUIManager();
        if (uiManager == null) return;

        Player player = GameManager.getInstance().getPlayer();

        if (isLocked) {
            // Перевіряємо, чи є ключ "в руках", але НЕ ВИТРАЧАЄМО його тут
            if (lockType == LockType.PICK_LOCK && player != null && player.hasUniversalKey()) {
                player.useUniversalKey(); // Цей метод тепер просто "скидає" ключ з рук (hasUniversalKey = false)
                // Ми більше не викликаємо updateAllBoostCounts тут, бо лічильник вже оновився при активації
                unlock(); // Відмикаємо двері
                System.out.println("Двері відкрито за допомогою активованого ключа.");
                return;
            }
            Puzzle puzzle = null;
            switch (lockType) {
                case CODE_LOCK:
                    puzzle = new CodeLockPuzzle(new JSONObject().put("solution", "1234"));
                    break;
                case PICK_LOCK:
                    puzzle = new LockPickPuzzle(new JSONObject().put("solution", "lockpick"));
                    break;
                case LASER_LOCK:
                    return;
                case NONE:
                    return;
            }
            if (puzzle != null) {
                puzzle.setLinkedDoor(this, (solved, door) -> {
                    if (solved) {
                        door.unlock();
                        uiManager.hidePuzzleUI();
                        System.out.println("Puzzle solved, door unlocked: " + sharedId);
                    }
                });
                uiManager.showPuzzleUI(puzzle.getUI());
            }
        } else {
            openLinkedDoors();
            System.out.println("Door opened: " + sharedId);
        }
    }

    public void openLinkedDoors() {
        for (Interactable interactable : GameManager.getInstance().getInteractables()) {
            if (interactable instanceof Door otherDoor && otherDoor.getSharedId() == this.sharedId) {
                otherDoor.isOpen = true;
                System.out.println("Linked door opened: " + otherDoor.getSharedId());
            }
        }
    }

    public void unlock() {
        this.isLocked = false;
        this.isOpen = true;
        for (Interactable interactable : GameManager.getInstance().getInteractables()) {
            if (interactable instanceof Door otherDoor && otherDoor.getSharedId() == this.sharedId) {
                otherDoor.isLocked = false;
                otherDoor.isOpen = true;
                System.out.println("Linked door unlocked: " + otherDoor.getSharedId());
            }
        }
    }

    @Override
    public boolean canInteract(Player player) {
        Bounds playerBounds = player.getBounds();
        Bounds doorBounds = this.getBounds();
        double offset = 5;
        Bounds bounds = new BoundingBox(playerBounds.getMinX() - offset, playerBounds.getMinY(),
                playerBounds.getWidth() + offset * 2, playerBounds.getHeight());
        boolean hasOverlap = bounds.intersects(doorBounds);
        return hasOverlap;
    }


    /**
     * Метод, що повертає відстань, на якій гравець може взаємодіяти з дверми
     * @return 0, оскільки двері вимагають безпосереднього наближення для взаємодії
     */
    @Override
    public double getInteractionRange() {
        return 0;
    }

    /**
     * Метод, що відповідає за візуалізацію дверей на екрані
     * @param gc графічний компонент, у якому виконується рендеринг
     */
    @Override
    public void render(GraphicsContext gc) {
        Image sprite = null; //змінна в яку буде завантажено спрайт
        boolean shouldMirror = false; //флаг для відзеркалювання

        if (isFloorLink) {
            if (isLocked) {
                sprite = sprites.get("stairsLocked");
            } else {
                if (isOpen) {
                    if (direction.equals("up")) {
                        sprite = sprites.get("stairsUp");
                    } else if (direction.equals("down")) {
                        sprite = sprites.get("stairsDown");
                    }
                } else {
                    sprite = sprites.get("stairsClosed");
                }
            }
        } else {
            if (isLocked) {
                if (isLaser) {
                    sprite = sprites.get("laserLocked");
                    // Відзеркалюємо лазерні двері якщо вони направо
                    shouldMirror = direction.equals("right");
                } else {
                    if (direction.equals("left")) {
                        sprite = sprites.get("lockedLeft");
                    } else if (direction.equals("right")) {
                        sprite = sprites.get("lockedRight");
                    }
                }
            } else {
                if (isOpen) {
                    if (isLaser) {
                        sprite = sprites.get("laserUnlocked");
                        // Відзеркалюємо лазерні двері якщо вони направо
                        shouldMirror = direction.equals("right");
                    } else {
                        if (direction.equals("left")) {
                            sprite = sprites.get("openLeft");
                        } else if (direction.equals("right")) {
                            sprite = sprites.get("openRight");
                        }
                    }
                } else {
                    if (isLaser) {
                        sprite = sprites.get("laserUnlocked");
                        // Відзеркалюємо лазерні двері якщо вони направо
                        shouldMirror = direction.equals("right");
                    } else {
                        if (direction.equals("left")) {
                            sprite = sprites.get("closedLeft");
                        } else if (direction.equals("right")) {
                            sprite = sprites.get("closedRight");
                        }
                    }
                }
            }
        }

        if (sprite != null) {
            gc.setImageSmoothing(false); //вимикаємо згладжування
            Bounds bounds = getImageBounds(); //отримаємо координати, де треба малювати
            double renderX = bounds.getMinX();
            double renderY = bounds.getMinY();
            double renderWidth = imageWidth;
            double renderHeight = imageHeight;

            if (shouldMirror) {
                // Зберігаємо поточний стан трансформації
                gc.save();

                // Переміщуємо початок координат до центру зображення
                gc.translate(renderX + renderWidth / 2, renderY + renderHeight / 2);

                // Відзеркалюємо по горизонталі
                gc.scale(-1, 1);

                // Малюємо зображення відносно нового центру
                gc.drawImage(sprite, -renderWidth / 2, -renderHeight / 2, renderWidth, renderHeight);

                // Відновлюємо стан трансформації
                gc.restore();
            } else {
                // Звичайне малювання без відзеркалювання
                gc.drawImage(sprite, renderX, renderY, renderWidth, renderHeight);
            }
        }
    }

    /**
     * Метод, що повертає серіалізовані дані дверей у вигляді JSON-об'єкта.
     * @return JSON-об'єкт, що містить інформацію про положення, розміри, стан, напрямок дверей, тип замка
     */
    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("type", getType());
        data.put("x", imageX);
        data.put("y", imageY + imageHeight);
        data.put("width", imageWidth);
        data.put("height", imageHeight);
        data.put("sharedId", sharedId);
        data.put("isOpen", isOpen);
        data.put("isLocked", isLocked);
        data.put("isLaser", isLaser);
        data.put("isRoomLink", isRoomLink);
        data.put("isFloorLink", isFloorLink);
        data.put("direction", direction);
        data.put("lockType", lockType.toString());
        return data;
    }

    /**
     * Метод, що ініціалізує або оновлює стан дверей на основі переданих серіалізованих даних
     * @param data JSON-об'єкт, що містить збережений стан дверей
     */
    @Override
    public void setFromData(JSONObject data) {
        this.imageX = data.optDouble("x", imageX);
        this.imageY = data.optDouble("y", imageY + imageHeight) - imageHeight;
        this.imageWidth = data.optDouble("width", imageWidth);
        this.imageHeight = data.optDouble("height", imageHeight);
        this.sharedId = data.optInt("sharedId", sharedId);
        this.isOpen = data.optBoolean("isOpen", isOpen);
        this.isLocked = data.optBoolean("isLocked", isLocked);
        this.isLaser = data.optBoolean("isLaser", isLaser);
        this.isRoomLink = data.optBoolean("isRoomLink", isRoomLink);
        this.isFloorLink = data.optBoolean("isFloorLink", isFloorLink);
        this.direction = data.optString("direction", direction);
        try {
            this.lockType = LockType.valueOf(data.optString("lockType", lockType.toString()));
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid lock type: " + data.optString("lockType"));
        }
    }

    /**
     * Метод, що повертає тип об'єкта
     * @return рядок, що представляє тип — "Door"
     */
    @Override
    public String getType() {
        return "Door";
    }

    /**
     * Метод, що повертає позицію об'єкта у вигляді вектора
     * @return об'єкт Vector2D, що містить координати дверей
     */
    @Override
    public Vector2D getPosition() {
        return new Vector2D(imageX, imageY);
    }

    /**
     * Метод, що повертає позицію зображення об'єкта
     * @return об'єкт Vector2D з координатами зображення дверей
     */
    @Override
    public Vector2D getImagePosition() {
        return new Vector2D(imageX, imageY);
    }

    /**
     * Метод, що встановлює нову позицію об'єкта
     * @param position нові координати у вигляді Vector2D
     */
    @Override
    public void setPosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
    }

    /**
     * Метод, що встановлює нову позицію зображення об'єкта.
     * @param position нові координати у вигляді Vector2D
     */
    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
    }

    /**
     * Метод, що повертає межі (розмір і позицію) об'єкта
     * @return об'єкт Bounds, що описує прямокутник дверей
     */
    @Override
    public Bounds getBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Метод, що повертає межі зображення об'єкта
     * @return об'єкт Bounds, що відповідає зображенню дверей
     */
    @Override
    public Bounds getImageBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Метод, що повертає текст підказки для взаємодії з дверима
     * @return підказка у вигляді рядка або null, якщо взаємодія не потрібна
     */
    @Override
    public String getInteractionPrompt() {
        if (isLaser && isLocked) {
            return "Laser door is locked";
        }
        if (isLocked) {
            return "Door is locked. Press E to unlock the door";
        }
        if (!isOpen) {
            return "Press E to open the door";
        }
        return null;
    }

    /**
     * Метод, що повертає номер шару, на якому рендериться об'єкт
     * Чим менше число, тим раніше об'єкт буде відображено
     * @return 0 — стандартний шар рендеру
     */
    @Override
    public int getRenderLayer() {
        return 0;
    }

    /**
     * Метод, що визначає, чи об'єкт видимий на екрані
     * @return true — двері завжди видимі
     */
    @Override
    public boolean isVisible() {
        return true;
    }

    /**
     * Метод, що перевіряє, чи двері відкриті
     * @return true, якщо двері відкриті
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Метод, що повертає спільний ідентифікатор дверей (використовується для синхронізації)
     * @return ціле число — sharedId
     */
    public int getSharedId() {
        return sharedId;
    }

    /**
     * Метод, що перевіряє, чи є двері лазерними
     * @return true, якщо це лазерні двері
     */
    public boolean isLaser() {
        return isLaser;
    }

    /**
     * Метод, що перевіряє, чи двері заблоковані.
     * @return true, якщо двері заблоковані
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * Метод, що повертає тип замка дверей.
     * @return значення перерахування LockType, що описує тип замка
     */
    public LockType getLockType() {
        return lockType;
    }

}
