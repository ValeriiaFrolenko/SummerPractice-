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
import managers.UIManager;
import org.json.JSONObject;
import puzzles.LaserLockPuzzle;
import puzzles.Puzzle;
import utils.GameLoader;
import utils.Vector2D;

import java.util.Random;

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

    public enum Type { NOTE, PICTURE, COMPUTER, ELECTRICAL_PANEL, WITH_MONEY, FINAL_PRIZE }

    public InteractiveObject(Vector2D position, JSONObject properties) {
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
        }
    }

    private String generateRandomCode() {
        Random random = new Random();
        int code = random.nextInt(9000) + 1000;
        return String.valueOf(code);
    }

    @Override
    public void interact(Player player) {
        UIManager uiManager = GameWindow.getInstance().getUIManager();
        if (uiManager == null) {
            System.err.println("UIManager не доступний");
            return;
        }

        switch (type) {
            case NOTE:
                uiManager.createWindow(UIManager.WindowType.NOTE, properties);
                break;
            case PICTURE:
                if (!isPictureMoved) {
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
                    GameManager.getInstance().addMoney(100);
                    isMoneyGiven = true;
                }
                break;
            case FINAL_PRIZE:
                if (!isMoneyGiven) {
                    GameManager.getInstance().addMoney(200);
                    isMoneyGiven = true;
                }
                GameManager.getInstance().setGameState(GameManager.GameState.VICTORY);
                uiManager.createWindow(UIManager.WindowType.VICTORY, properties);
                break;
        }
    }
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
        return data;
    }

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
    }

    @Override
    public void render(GraphicsContext gc) {
        if (sprite != null) {
            gc.setImageSmoothing(false);
            gc.drawImage(sprite, imageX, imageY, imageWidth, imageHeight);
        }

        gc.setStroke(Color.RED);
        gc.setLineWidth(1);
        gc.strokeRect(imageX, imageY, imageWidth, imageHeight);

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

    @Override
    public String getType() {
        return type.toString();
    }

    @Override
    public Vector2D getPosition() {
        return new Vector2D(imageX, imageY);
    }

    @Override
    public Vector2D getImagePosition() {
        return new Vector2D(imageX, imageY);
    }

    @Override
    public void setPosition(Vector2D position) {
        this.imageX = position.x;
        this.imageY = position.y;
        this.targetImageX = isPictureMoved ? imageY - 50 : imageY;
    }

    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.x;
        this.imageY = position.y;
        this.targetImageX = isPictureMoved ? imageY - 50 : imageY;
    }

    @Override
    public Bounds getBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    @Override
    public Bounds getImageBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    @Override
    public double getInteractionRange() {
        return 50.0;
    }

    @Override
    public String getInteractionPrompt() {
        return "Press E to interact with object";
    }

    @Override
    public int getRenderLayer() {
        return 0;
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}