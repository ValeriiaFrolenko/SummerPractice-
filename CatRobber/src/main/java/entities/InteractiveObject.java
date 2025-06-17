package entities;

import interfaces.*;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import main.GameWindow;
import managers.GameManager;
import managers.SoundManager;
import managers.UIManager;
import org.json.JSONObject;
import puzzles.LaserLockPuzzle;
import puzzles.Puzzle;
import utils.GameLoader;
import utils.Vector2D;

import java.util.Random;


/**
 * Представляє інтерактивний об'єкт у грі, з яким гравець може взаємодіяти:
 * наприклад, записки, картини, комп'ютери, електрощити тощо.
 * Реалізує інтерфейси {@link GameObject} та {@link Interactable}.
 */
public class InteractiveObject implements GameObject, Interactable {
    private final String path = "interactiveObjects/";
    private double imageX, imageY, imageWidth, imageHeight;
    private Image sprite;
    private String spritePath;
    private Type type;
    private JSONObject properties;
    private boolean isMoneyGiven = false;
    private boolean isPictureMoved = false;
    private double targetImageX;
    private final SoundManager soundManager = SoundManager.getInstance();

    /**
     * Типи інтерактивних об'єктів.
     */
    public enum Type {
        NOTE,              // Записка з кодом
        PICTURE,           // Картина, яку можна посунути
        COMPUTER,          // Комп'ютер для взаємодії
        ELECTRICAL_PANEL,  // Панель з головоломкою (лазер)
        WITH_MONEY,        // Об'єкт із грошима
        FINAL_PRIZE        // Кінцева нагорода
    }


    /**
     * Створює інтерактивний об'єкт на основі позиції та властивостей.
     *
     * @param position   Початкова позиція об'єкта
     * @param properties JSON-об'єкт з властивостями (включно з ім'ям файлу, типом тощо)
     */
    public InteractiveObject(Vector2D position, JSONObject properties) {
        if (type == Type.WITH_MONEY||type == Type.FINAL_PRIZE) {
            this.isMoneyGiven = properties.getBoolean("isMoneyGiven");
        }
        this.properties = properties;
        this.imageWidth = properties.optDouble("width", 32.0);
        this.imageHeight = properties.optDouble("height", 32.0);
        this.imageX = position.x;
        this.imageY = position.y - imageHeight;
        this.targetImageX = imageY;
        this.type = Type.valueOf(properties.optString("typeObj", "NOTE"));
        this.spritePath = path + properties.getString("fileName");
        GameLoader loader = new GameLoader();
        this.sprite = loader.loadImage(spritePath);

        // Генеруємо код для нотатки при створенні об'єкта
        if ((type == Type.NOTE||type == Type.PICTURE||type == Type.COMPUTER) && !properties.has("code")) {
            String code = generateRandomCode();
            properties.put("code", code);
            GameManager.getInstance().setCode(code); // Зберігаємо в GameManager
            GameManager.getInstance().saveGameManagerState();
        }
    }

    /**
     * Генерує випадковий 4-значний код для об'єктів типу NOTE, PICTURE або COMPUTER.
     *
     * @return Рядок з 4 цифрами
     */
    private String generateRandomCode() {
        Random random = new Random();
        int code = random.nextInt(9000) + 1000;
        return String.valueOf(code);
    }


    /**
     * Визначає логіку взаємодії гравця з об'єктом, залежно від типу.
     *
     * @param player Об'єкт гравця, що ініціює взаємодію
     */
    @Override
    public void interact(Player player) {
        UIManager uiManager = GameWindow.getInstance().getUIManager();
        if (uiManager == null) {
            System.err.println("UIManager не доступний");
            return;
        }

        switch (type) {
            case NOTE:
                soundManager.playSound(SoundManager.SoundType.TAKE_NOTE);
                uiManager.createWindow(UIManager.WindowType.NOTE, properties);
                break;
            case PICTURE:
                if (!isPictureMoved) {
                    soundManager.playSound(SoundManager.SoundType.MOVE_PICTURE);
                    animatePicture(uiManager);
                } else {
                    uiManager.createWindow(UIManager.WindowType.PICTURE, properties);
                }
                break;
            case COMPUTER:
                uiManager.createWindow(UIManager.WindowType.COMPUTER, properties);
                break;
            case ELECTRICAL_PANEL:
                Door laserDoor = null;
                for (Interactable obj : GameManager.getInstance().getInteractables()) {
                    if (obj instanceof Door && ((Door) obj).isLaser()) {
                        laserDoor = (Door) obj;
                        break;
                    }
                }
                if (laserDoor != null) {
                    Puzzle puzzle = null;
                    for (Puzzle puzzleLock : GameManager.getInstance().getPuzzles()) {
                        if (puzzleLock instanceof LaserLockPuzzle) {
                            puzzle = puzzleLock;
                        }
                    }
                    if (puzzle != null) {
                        puzzle.setLinkedDoor(laserDoor, (solved, door) -> {
                            if (solved) {
                                door.unlock();
                                uiManager.hidePuzzleUI();
                            }
                        });
                        uiManager.showPuzzleUI(puzzle.getUI());
                        System.out.println("LaserLockPuzzle opened for door: " + laserDoor.getSharedId());
                    }
                } else {
                    System.err.println("No laser door found among interactables");
                }
                break;
            case WITH_MONEY:
                if (!isMoneyGiven) {
                    soundManager.playSound(SoundManager.SoundType.COLLECT_MONEY);
                    GameManager.getInstance().addTemporaryMoney(100);
                    UIManager.getInstance().updateMoneyDisplay(); // Оновлюємо moneyLabel
                    isMoneyGiven = true;
                }
                break;
            case FINAL_PRIZE:
                if (!isMoneyGiven) {
                    GameManager.getInstance().addTemporaryMoney(200);
                    UIManager.getInstance().updateMoneyDisplay(); // Оновлюємо moneyLabel
                    isMoneyGiven = true;
                }
                GameManager.getInstance().setGameState(GameManager.GameState.VICTORY);
                uiManager.createWindow(UIManager.WindowType.VICTORY, properties);
                break;
        }
    }

    /**
     * Анімує зсув картини вліво на 50 пікселів і відкриває відповідне вікно.
     *
     * @param uiManager Менеджер UI для створення вікон
     */
    private void animatePicture(UIManager uiManager) {
        targetImageX = imageX - 50; // Цільова позиція X (зміщення вліво)
        TranslateTransition transition = new TranslateTransition(Duration.millis(500));
        transition.setFromX(0);
        transition.setToX(targetImageX - imageX); // Відносне зміщення вліво
        transition.setOnFinished(event -> {
            imageX = targetImageX; // Оновлюємо позицію X після анімації
            isPictureMoved = true;
            uiManager.createWindow(UIManager.WindowType.PICTURE, properties);
        });
        transition.play();
    }


    /**
     * Повертає об'єкт збережених даних, які можна серіалізувати у файл.
     *
     * @return JSON-об'єкт з усіма властивостями об'єкта
     */
    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("typeObj", type.toString());
        data.put("x", imageX);
        data.put("y", imageY + imageHeight);
        data.put("width", imageWidth);
        data.put("height", imageHeight);
        data.put("fileName", properties.getString("fileName"));
        data.put("type", "InteractiveObject");
        data.put("isPictureMoved", isPictureMoved);
        if ((type == Type.NOTE||type == Type.PICTURE||type == Type.COMPUTER) && properties.has("code")) {
            data.put("code", properties.getString("code")); // Зберігаємо код нотатки
        }
        if (type == Type.WITH_MONEY ||type == Type.FINAL_PRIZE) {
            data.put("isMoneyGiven", isMoneyGiven);
        }
        return data;
    }


    /**
     * Встановлює стан об'єкта з JSON-даних, які були збережені раніше.
     *
     * @param data JSON-дані, що описують стан об'єкта
     */
    @Override
    public void setFromData(JSONObject data) {
        this.imageX = data.optDouble("x", imageX);
        this.imageY = data.optDouble("y", imageY) - imageHeight;
        this.imageWidth = data.optDouble("width", imageWidth);
        this.imageHeight = data.optDouble("height", imageHeight);
        this.spritePath = data.optString("fileName", spritePath);
        this.isPictureMoved = data.optBoolean("isPictureMoved", false);
        if (isPictureMoved) {
            this.targetImageX = imageY - 50;
            this.imageY = targetImageX;
        } else {
            this.targetImageX = imageY;
        }
        try {
            this.type = Type.valueOf(data.optString("typeObj", type.toString()));
        } catch (IllegalArgumentException e) {
            System.err.println("Невірне значення типу: " + data.optString("typeObj") + ". Залишаю поточний.");
        }
        GameLoader loader = new GameLoader();
        this.sprite = loader.loadImage(spritePath);
        // Відновлюємо код нотатки
        if (data.has("code")) {
            properties.put("code", data.getString("code"));
            GameManager.getInstance().setCode(data.getString("code"));
        }
        if (type == Type.WITH_MONEY ||type == Type.FINAL_PRIZE) {
            this.isMoneyGiven = data.getBoolean("isMoneyGiven");
        }
    }



    /**
     * Малює об'єкт на вказаному графічному контексті.
     *
     * @param gc GraphicsContext для малювання
     */
    @Override
    public void render(GraphicsContext gc) {
        if (sprite != null) {
            gc.setImageSmoothing(false);
            gc.drawImage(sprite, imageX, imageY, imageWidth, imageHeight);
        }

        if (GameManager.getInstance().getClosestInteractable() == this) {
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(4);
            double cornerLength = 10;
            gc.strokeLine(imageX - 2, imageY - 2, imageX - 2 + cornerLength, imageY - 2);
            gc.strokeLine(imageX - 2, imageY - 2, imageX - 2, imageY - 2 + cornerLength);
            gc.strokeLine(imageX + imageWidth + 2 - cornerLength, imageY - 2, imageX + imageWidth + 2, imageY - 2);
            gc.strokeLine(imageX + imageWidth + 2, imageY - 2, imageX + imageWidth + 2, imageY - 2 + cornerLength);
            gc.strokeLine(imageX - 2, imageY + imageHeight + 2 - cornerLength, imageX - 2, imageY + imageHeight + 2);
            gc.strokeLine(imageX - 2, imageY + imageHeight + 2, imageX - 2 + cornerLength, imageY + imageHeight + 2);
            gc.strokeLine(imageX + imageWidth + 2 - cornerLength, imageY + imageHeight + 2, imageX + imageWidth + 2, imageY + imageHeight + 2);
            gc.strokeLine(imageX + imageWidth + 2, imageY + imageHeight + 2 - cornerLength, imageX + imageWidth + 2, imageY + imageHeight + 2);
        }
    }



    /**
     * Визначає, чи може гравець взаємодіяти з об'єктом (перевірка колізій).
     *
     * @param player Об'єкт гравця
     * @return true, якщо взаємодія можлива
     */
    @Override
    public boolean canInteract(Player player) {
        Bounds playerBounds = player.getBounds();
        Bounds objectBounds = this.getBounds();
        double interactionRange = getInteractionRange();
        Bounds extendedBounds = new BoundingBox(
                playerBounds.getMinX(),
                playerBounds.getMinY(),
                playerBounds.getWidth(),
                playerBounds.getHeight()
        );
        return extendedBounds.intersects(objectBounds);
    }

    /**
     * Повертає тип об'єкта у вигляді рядка.
     *
     * @return Назва типу
     */

    @Override
    public String getType() {
        return type.toString();
    }


    /**
     * Повертає позицію об'єкта.
     *
     * @return Вектор позиції
     */
    @Override
    public Vector2D getPosition() {
        return new Vector2D(imageX, imageY);
    }


    /**
     * Повертає позицію для рендеру зображення.
     *
     * @return Вектор позиції зображення
     */

    @Override
    public Vector2D getImagePosition() {
        return new Vector2D(imageX, imageY);
    }


    /**
     * Встановлює нову позицію об'єкта.
     *
     * @param position нова позиція
     */

    @Override
    public void setPosition(Vector2D position) {
        this.imageX = position.x;
        this.imageY = position.y;
        this.targetImageX = isPictureMoved ? imageY - 50 : imageY;
    }

    /**
     * Встановлює нову позицію для зображення об'єкта.
     *
     * @param position нова позиція
     */

    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.x;
        this.imageY = position.y;
        this.targetImageX = isPictureMoved ? imageY - 50 : imageY;
    }


    /**
     * Повертає межі об'єкта для логіки колізій.
     *
     * @return Об'єкт Bounds
     */
    @Override
    public Bounds getBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Повертає межі зображення об'єкта.
     *
     * @return Об'єкт Bounds
     */
    @Override
    public Bounds getImageBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Повертає відстань, на якій гравець може взаємодіяти з об'єктом.
     *
     * @return Довжина у пікселях
     */
    @Override
    public double getInteractionRange() {
        return 50.0;
    }


    /**
     * Повертає текст-підказку для гравця щодо взаємодії.
     *
     * @return Рядок-підказка
     */
    @Override
    public String getInteractionPrompt() {
        return "Натисніть Е, щоб взаємодіяти з об'єктом";
    }


    /**
     * Повертає шар рендерингу для правильного порядку малювання.
     *
     * @return 0 — базовий шар
     */
    @Override
    public int getRenderLayer() {
        return 0;
    }


    /**
     * Повертає, чи об'єкт видимий для рендеру.
     *
     * @return true, якщо об'єкт видно
     */
    @Override
    public boolean isVisible() {
        return true;
    }
}