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
import puzzles.Puzzle;
import utils.GameLoader;
import utils.Vector2D;

public class InteractiveObject implements GameObject, Interactable {
    private final String path = "interactiveObjects/"; //базовий шлях до папки зі спрайтами
    private double imageX, imageY, imageWidth, imageHeight; //координати, висота та ширина об'єктів
    private Image sprite; //об'єкт зображення
    private String spritePath; //шлях до файлу із зображенням
    private Type type; //тип інтерективного об'єкта
    private JSONObject properties; //додаткові властивості об’єкта у форматі JSON

    /** Перелік можливих типів інтерактивних об'єктів **/

    public enum Type { NOTE, PICTURE, COMPUTER, ELECTRICAL_PANEL, WITH_MONEY, FINAL_PRIZE}

    /**
     * Конструктор, що створює новий інтерактивний об'єкт на основі заданої позиції та властивостей
     * @param position початкова позиція об'єкта у вигляді вектора Vector2D
     * @param properties об'єкт JSON, що містить властивості інтерактивного об'єкта
     */
    public InteractiveObject(Vector2D position, JSONObject properties) {
        this.properties = properties;
        this.imageWidth = properties.optDouble("width", 32.0);
        this.imageHeight = properties.optDouble("height", 32.0);
        this.imageX = position.x;
        this.imageY = position.y - imageHeight;
        this.type = Type.valueOf(properties.optString("typeObj", "NOTE"));
        this.spritePath = path + properties.getString("fileName");
        this.imageWidth = properties.optDouble("width", 32.0);
        this.imageHeight = properties.optDouble("height", 32.0);
        GameLoader loader = new GameLoader();
        this.sprite = loader.loadImage(spritePath);
    }

    /**
     * Обробляє взаємодію гравця з інтерактивним об'єктом
     * @param player гравець, який взаємодіє з об'єктом
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
                uiManager.createWindow(UIManager.WindowType.NOTE, properties);
                break;
            case PICTURE:
                uiManager.createWindow(UIManager.WindowType.PICTURE, properties);
                break;
            case COMPUTER:
                uiManager.createWindow(UIManager.WindowType.COMPUTER, properties);
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
                    Puzzle puzzle = null;
                    for(Puzzle puzzleLock : GameManager.getInstance().getPuzzles()){
                        if(puzzleLock instanceof LaserLockPuzzle){
                            puzzle = puzzleLock;
                        }
                    }
                    if(puzzle != null) {
                        puzzle.setLinkedDoor(laserDoor, (solved, door) -> {
                            if (solved) {
                                door.unlock();
                                uiManager.hidePuzzleUI(); // Закриваємо UI головоломки
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
                GameManager.getInstance().addMoney(100);
                break;
            case FINAL_PRIZE:
                GameManager.getInstance().addMoney(500);
                uiManager.createWindow(UIManager.WindowType.VICTORY, properties);
                break;
        }
    }

    /**
     * Перевіряє, чи може гравець взаємодіяти з об'єктом
     * @param player гравець, для якого перевіряється можливість взаємодії
     * @return true - якщо гравець знаходиться в зоні взаємодії з об'єктом, інакше - false
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
     * Метод, що відображає інтерактивний об'єкт на графічному контексті
     * Малює спрайт об'єкта з заданими координатами та розмірами
     * Якщо цей об'єкт є найближчим для взаємодії з гравцем,
     * навколо нього додається додаткове виділення у вигляді білих "кутів"
     * @param gc графічний контекст, на якому виконується малювання
     */
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

    /**
     * Повертає JSON-об’єкт, що містить серіалізовані дані інтерактивного об’єкта
     * @return JSONObject, що містить серіалізовані поля об’єкта
     */
    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("typeObj", type.toString());
        data.put("x", imageX);
        data.put("y", imageY+imageHeight);
        data.put("width", imageWidth);
        data.put("height", imageHeight);
        data.put("fileName", properties.getString("fileName"));
        data.put("Type", "interactiveObject");

        return data;
    }

    /**
     * Відновлює стан інтерактивного об’єкта з JSON-даних
     * @param data JSONObject, що містить серіалізовані поля об’єкта
     */
    @Override
    public void setFromData(JSONObject data) {
        this.imageX = data.optDouble("x", imageX);
        this.imageY = data.optDouble("y", imageY) - imageHeight;
        this.imageWidth = data.optDouble("width", imageWidth);
        this.imageHeight = data.optDouble("height", imageHeight);
        this.spritePath = data.optString("fileName", spritePath);
        try {
            this.type = Type.valueOf(data.optString("typeObj", type.toString()));
        } catch (IllegalArgumentException e) {
            System.err.println("Невірне значення типу: " + data.optString("typeObj") + ". Залишаю поточний.");
        }
        GameLoader loader = new GameLoader();
        this.sprite = loader.loadImage(spritePath);
    }

    /**
     * Повертає тип інтерактивного об'єкта у вигляді рядка
     * @return рядок, що представляє тип об'єкта
     */
    @Override
    public String getType() {
        return type.toString();
    }

    /**
     * Повертає позицію об'єкта у вигляді вектора
     * @return позиція (x, y) об'єкта
     */
    @Override
    public Vector2D getPosition() {
        return new Vector2D(imageX, imageY);
    }

    /**
     * Повертає позицію зображення об'єкта
     * @return позиція (x, y) зображення об'єкта
     */
    @Override
    public Vector2D getImagePosition() {
        return new Vector2D(imageX, imageY);
    }

    /**
     * Встановлює позицію об'єкта
     * @param position нова позиція у вигляді вектора (x, y)
     */
    @Override
    public void setPosition(Vector2D position) {
        this.imageX = position.x;
        this.imageY = position.y;
    }

    /**
     * Встановлює позицію зображення об'єкта
     * @param position нова позиція зображення у вигляді вектора (x, y)
     */
    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.x;
        this.imageY = position.y;
    }

    /**
     * Повертає межі об'єкта у вигляді BoundingBox
     * @return об'єкт типу Bounds, що описує межі об'єкта
     */
    @Override
    public Bounds getBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Повертає межі зображення об'єкта
     * @return об'єкт типу Bounds, що описує межі зображення об'єкта
     */
    @Override
    public Bounds getImageBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Повертає радіус взаємодії з об'єктом
     * @return відстань (у пікселях) для взаємодії з об'єктом
     */
    @Override
    public double getInteractionRange() {
        return 50.0;
    }

    /**
     * Повертає підказку, яка відображається гравцю для взаємодії з об'єктом
     * @return текст підказки
     */
    @Override
    public String getInteractionPrompt() {
        return "Press E to interact with object";
    }

    /**
     * Повертає рівень відмалювання (шар), на якому розташований об'єкт
     * Визначає порядок відображення об'єктів
     * @return 1 - номер шару відмалювання
     */
    @Override
    public int getRenderLayer() {
        return 1;
    }

    /**
     * Визначає, чи є об'єкт видимим
     * @return true, якщо об'єкт видно, інакше false
     */
    @Override
    public boolean isVisible() {
        return true;
    }

}
