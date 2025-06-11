package entities;

import interfaces.*;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import main.GameWindow;
import managers.GameManager;
import managers.UIManager;
import org.json.JSONObject;
import puzzles.LaserLockPuzzle;
import utils.GameLoader;
import utils.Vector2D;

public class InteractiveObject implements GameObject, Interactable {
    private double imageX;
    private double imageY;
    private double imageWidth;
    private double imageHeight;
    private Image sprite;
    private String spritePath;
    private Type type;
    private JSONObject properties;

    public enum Type { NOTE, PICTURE, COMPUTER, LADDER, ELECTRICAL_PANEL, WITH_MONEY, FINAL_PRICE }

    public InteractiveObject(Vector2D position, JSONObject properties) {
        this.properties = properties;
        this.imageX = position.x;
        this.imageY = position.y;
        this.type = Type.valueOf(properties.optString("type", "NOTE"));
        this.spritePath = properties.optString("spritePath", "interactables/default.png");
        this.imageWidth = properties.optDouble("width", 32.0);
        this.imageHeight = properties.optDouble("height", 32.0);
        GameLoader loader = new GameLoader();
        this.sprite = loader.loadImage(spritePath);
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
                uiManager.createWindow(UIManager.WindowType.PICTURE, properties);
                break;
            case COMPUTER:
                uiManager.createWindow(UIManager.WindowType.COMPUTER, properties);
                break;
            case LADDER:
                // TODO: Реалізувати взаємодію для драбини
                break;
            case ELECTRICAL_PANEL:
                // Знаходимо лазерні двері серед інтерактивних об’єктів
                Door laserDoor = null;
                for (Interactable obj : GameManager.getInstance().getInteractables()) {
                    if (obj instanceof Door && ((Door) obj).isLaser()) {
                        laserDoor = (Door) obj;
                        break;
                    }
                }
                if (laserDoor != null) {
                    LaserLockPuzzle puzzle = new LaserLockPuzzle(new JSONObject().put("solution", "2"));
                    puzzle.setLinkedDoor(laserDoor, (solved, door) -> {
                        if (solved) {
                            door.unlock();
                            uiManager.hidePuzzleUI(); // Закриваємо UI головоломки
                        }
                    });
                    uiManager.showPuzzleUI(puzzle.getUI());
                    System.out.println("LaserLockPuzzle opened for door: " + laserDoor.getSharedId());
                } else {
                    System.err.println("No laser door found among interactables");
                }
                break;
            case WITH_MONEY:
                player.addMoney(100);
                break;
            case FINAL_PRICE:
                player.addMoney(500);
                uiManager.createWindow(UIManager.WindowType.VICTORY, properties);
                break;
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
    public void render(GraphicsContext gc) {
        gc.setFill(Color.BLUE);
        gc.fillRect(imageX, imageY, imageWidth, imageHeight);
        if (sprite != null) {
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
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("type", type.toString());
        data.put("x", imageX);
        data.put("y", imageY);
        data.put("width", imageWidth);
        data.put("height", imageHeight);
        data.put("spritePath", spritePath);
        return data;
    }

    @Override
    public void setFromData(JSONObject data) {
        this.imageX = data.optDouble("x", imageX);
        this.imageY = data.optDouble("y", imageY);
        this.imageWidth = data.optDouble("width", imageWidth);
        this.imageHeight = data.optDouble("height", imageHeight);
        this.spritePath = data.optString("spritePath", spritePath);
        try {
            this.type = Type.valueOf(data.optString("type", type.toString()));
        } catch (IllegalArgumentException e) {
            System.err.println("Невірне значення типу: " + data.optString("type") + ". Залишаю поточний.");
        }
        GameLoader loader = new GameLoader();
        this.sprite = loader.loadImage(spritePath);
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
    }

    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.x;
        this.imageY = position.y;
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
        return 1;
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}
