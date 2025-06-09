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
    private double imageX;
    private double imageY;
    private double imageWidth;
    private double imageHeight;
    private boolean isOpen;
    private boolean isLocked;
    private boolean isLaser;
    private boolean isRoomLink;
    private boolean isFloorLink;
    String direction;
    private Map<String, Image> sprites;
    private String[] spritePaths;
    private final String path = "background/doors/";
    private int sharedId;
    private LockTipe lockTipe;

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
        this.lockTipe = LockTipe.valueOf(defaultData.getString("lockType"));
        this.spritePaths = new String[]{
                path + "close.png", path + "closeLeft.png", path + "closeRight.png",
                path + "leftLock.png", path + "openLeft.png", path + "openRight.png",
                path + "rightLock.png", path + "stairsUp.png", path + "stairsDown.png",
                path + "withLock.png", path + "laserLocked.png", path + "laserUnlocked.png"
        };
        GameLoader loader = new GameLoader();
        this.sprites = new HashMap<>();
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

    public enum LockTipe { CODE_LOCK, PICK_LOCK, LASER_LOCK, NONE }

    @Override
    public void interact(Player player) {
        // Викликається при натисканні стрілок: телепортує гравця, якщо двері відкриті та не заблоковані
        if (!isLocked && isOpen) {
            Player.Direction direction = player.getDirection();
            if ((direction.equals(Player.Direction.LEFT) || direction.equals(Player.Direction.RIGHT)) && isRoomLink) {
                player.teleportToRoom(this);
                System.out.println("Teleporting to room: " + sharedId);
            } else if (isFloorLink) {
                player.teleportToFloor(this);
                System.out.println("Teleporting to floor: " + sharedId);
            }
        }
    }

    public void open(Player player) {
        UIManager uiManager = GameWindow.getInstance().getUIManager();
        if (uiManager == null) {
            System.err.println("UIManager не доступний");
            return;
        }

        if (isLocked) {
            if (isLaser) {
                // Лазерні двері лише показують підказку
                uiManager.showInteractionPrompt(getInteractionPrompt());
                System.out.println("Laser door interaction: locked");
                return;
            }
            // Для інших заблокованих дверей викликаємо головоломку
            Puzzle puzzle = null;
            switch (lockTipe) {
                case CODE_LOCK:
                    puzzle = new CodeLockPuzzle(new JSONObject().put("solution", "1234"));
                    break;
                case PICK_LOCK:
                    puzzle = new LockPickPuzzle(new JSONObject().put("solution", "lockpick"));
                    break;
                case LASER_LOCK:
                    return; // Лазерні двері не викликають головоломку
                case NONE:
                    return;
            }
            if (puzzle != null) {
                puzzle.setLinkedDoor(this, (solved, door) -> {
                    if (solved) {
                        door.unlock(); // Розблоковуємо всі зв’язані двері
                        uiManager.hidePuzzleUI();
                        System.out.println("Puzzle solved, door unlocked: " + sharedId);
                    }
                });
                uiManager.showPuzzleUI(puzzle.getUI());
            }
        } else {
            // Якщо двері не заблоковані, відкриваємо всіх зв’язаних
            openLinkedDoors();
            System.out.println("Door opened: " + sharedId);
        }
    }

    public void openLinkedDoors() {
        // Відкриваємо всі двері з однаковим sharedId
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
        // Розблоковуємо всі зв’язані двері
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
        Bounds bounds = new BoundingBox(playerBounds.getMinX()-offset, playerBounds.getMinY(), playerBounds.getWidth() + offset*2, playerBounds.getHeight());
        boolean hasOverlap = bounds.intersects(doorBounds);

        if (!hasOverlap) {
            return false;
        }

        Player.Direction playerDirection = player.getDirection();
        String doorDirection = this.direction;

        if (isLaser) {
            if (playerDirection == Player.Direction.RIGHT || playerDirection == Player.Direction.LEFT) {
                return true; // Лазерні двері дозволяють взаємодію для показу підказки
            }
        }

        if (isRoomLink) {
            if (playerDirection == Player.Direction.RIGHT && doorDirection.equals("left")) {
                return true;
            } else if (playerDirection == Player.Direction.LEFT && doorDirection.equals("right")) {
                return true;
            }
        }

        if (isFloorLink) {
            if (playerDirection == Player.Direction.UP && doorDirection.equals("up")) {
                return true;
            } else if (playerDirection == Player.Direction.DOWN && doorDirection.equals("down")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public double getInteractionRange() {
        return 0;
    }

    @Override
    public void render(GraphicsContext gc) {
        Image sprite = null;
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
            gc.setImageSmoothing(false);
            Bounds bounds = getImageBounds();
            double renderX = bounds.getMinX();
            double renderY = bounds.getMinY();
            double renderWidth = imageWidth;
            double renderHeight = imageHeight;
            gc.drawImage(sprite, renderX, renderY, renderWidth, renderHeight);
        }
    }

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
        data.put("lockType", lockTipe.toString());
        return data;
    }

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
            this.lockTipe = LockTipe.valueOf(data.optString("lockType", lockTipe.toString()));
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid lock type: " + data.optString("lockType"));
        }
    }

    @Override
    public String getType() {
        return "Door";
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
        this.imageX = position.getX();
        this.imageY = position.getY();
    }

    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
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

    @Override
    public int getRenderLayer() {
        return 0;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public int getSharedId() {
        return sharedId;
    }

    public boolean isLaser() {
        return isLaser;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public LockTipe getLockType() {
        return lockTipe;
    }
}
