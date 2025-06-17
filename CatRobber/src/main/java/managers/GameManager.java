package managers;

import entities.*;
import interfaces.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import main.GameWindow;
import org.json.JSONArray;
import org.json.JSONObject;
import puzzles.Puzzle;
import ui.ShopPane;
import ui.ShopItem;
import utils.GameLoader;
import utils.InputHandler;
import utils.Vector2D;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Клас для управління логікою гри, включаючи об'єкти, стан, колізії та взаємодії.
 * Реалізує патерн Singleton та інтерфейс Savable для збереження даних.
 */
public class GameManager implements Savable {
    /** Єдиний екземпляр класу GameManager (патерн Singleton). */
    private static GameManager instance;

    /** Менеджер рівнів для завантаження даних рівня. */
    private final LevelManager levelManager;

    /** Код для взаємодії з об'єктами (наприклад, двері). */
    private String code;

    /** Менеджер збереження гри. */
    private SaveManager saveManager;

    /** Дані поточного рівня у форматі JSON. */
    private JSONObject currentLevel;

    /** Список усіх ігрових об’єктів (гравець, поліцейські тощо). */
    private List<GameObject> gameObjects;

    /** Список об’єктів, які можна рендерити. */
    private List<Renderable> renderableObjects;

    /** Список об’єктів із анімаціями. */
    private List<Animatable> animatableObjects;

    /** Посилання на гравця. */
    private Player player;

    /** Список поліцейських NPC. */
    private List<Police> police;

    /** Список камер спостереження. */
    private List<SecurityCamera> cameras;

    /** Список інтерактивних об’єктів. */
    private List<Interactable> interactables;

    /** Список головоломок. */
    private List<Puzzle> puzzles;

    /** Карта колізій (межі кімнат). */
    private List<Bounds> collisionMap;

    /** Список кімнат для колізій і навігації. */
    private List<Room> rooms;

    /** Поточний стан гри (MENU, PLAYING, PAUSED, GAME_OVER, VICTORY). */
    private GameState gameState;

    /** Фонове зображення рівня. */
    private Image backgroundImage;

    /** Ширина canvas, отримана від GameWindow. */
    private double canvasWidth;

    /** Висота canvas, отримана від GameWindow. */
    private double canvasHeight;

    /** Найближчий інтерактивний об’єкт до гравця. */
    private Interactable closestInteractable;

    /** Прапорець глобальної тривоги. */
    private boolean isGlobalAlert;

    /** Таймер глобальної тривоги. */
    private double globalAlertTimer;

    /** Тривалість глобальної тривоги в секундах. */
    private static final double GLOBAL_ALERT_DURATION = 3.0;

    /** Список ID пройдених рівнів. */
    private List<Integer> completedLevels;

    /** Загальна кількість грошей гравця. */
    private int totalMoney;

    /** ID поточного рівня. */
    private int currentLevelId;

    /** Завантажувач ресурсів гри. */
    private GameLoader gameLoader = new GameLoader();

    /** Інвентар гравця (предмети та їх кількість). */
    private Map<ShopItem, Integer> inventory;

    /** Обробник вводу гравця. */
    private InputHandler inputHandler;

    /** Менеджер звуку. */
    private final SoundManager soundManager = SoundManager.getInstance();

    /** Тимчасові гроші, накопичені під час рівня. */
    private int temporaryMoney;

    /** Прапорець для відстеження стану атаки гравця. */
    private boolean wasHitting = false;

    /** Прапорець для відстеження стану руху гравця. */
    private boolean wasMoving = false;

    /**
     * Встановлює стан гри.
     *
     * @param gameState новий стан гри
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Повертає список дверей із інтерактивних об’єктів.
     *
     * @return список об’єктів типу Door
     */
    public List<Door> getDoors() {
        List<Door> doors = new ArrayList<>();
        for (Interactable interactable : interactables) {
            if (interactable instanceof Door) {
                doors.add((Door) interactable);
            }
        }
        return doors;
    }

    /**
     * Повертає поточний стан гри.
     *
     * @return поточний стан гри
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Перезапускає поточний рівень.
     */
    public void restartCurrentLevel() {
        loadLevel(currentLevelId, true);
    }

    /**
     * Повертає інвентар гравця.
     *
     * @return мапа предметів та їх кількості
     */
    public Map<ShopItem, Integer> getInventory() {
        return inventory;
    }

    /**
     * Перелік можливих станів гри.
     */
    public enum GameState {MENU, PLAYING, PAUSED, VICTORY, GAME_OVER}

    /**
     * Внутрішній клас для представлення кімнат у грі.
     */
    public static class Room {
        /** Унікальний ID кімнати. */
        private int id;

        /** Межі кімнати для колізій. */
        private BoundingBox bounds;

        /**
         * Конструктор для створення кімнати.
         *
         * @param id унікальний ID кімнати
         * @param bounds межі кімнати
         */
        public Room(int id, BoundingBox bounds) {
            this.id = id;
            this.bounds = bounds;
        }

        /**
         * Повертає ID кімнати.
         *
         * @return ID кімнати
         */
        public int getId() {
            return id;
        }

        /**
         * Повертає межі кімнати.
         *
         * @return межі кімнати
         */
        public BoundingBox getBounds() {
            return bounds;
        }
    }

    /**
     * Зупиняє гру та повертає до головного меню.
     */
    public void stopGameAndGoToMenu() {
        if (player != null) {
            player.stopMovement();
        }

        if (isGlobalAlert) {
            isGlobalAlert = false;
            globalAlertTimer = 0.0;
            for (Police police1 : police) {
                if (police1.getState() == Police.PoliceState.ALERT) {
                    police1.setState(Police.PoliceState.PATROL);
                    police1.setAnimationState("patrol");
                }
            }
        }

        saveProgress();
        saveGame();
        clearGameState();

        UIManager uiManager = GameWindow.getInstance().getUIManager();
        if (uiManager != null) {
            uiManager.hideInteractionPrompt();
            uiManager.forceHideInteractiveObjectUI();
            uiManager.hideMenuButton();
            uiManager.clearSceneForMenu();
            uiManager.showMenu();
            GameWindow.getInstance().hideTitleBar();
        } else {
            System.err.println("UIManager не доступний для переходу в меню");
        }
    }


    /**
     * Очищає поточний стан гри, видаляючи всі об’єкти та скидаючи тимчасові дані.
     */
    public void clearGameState() {
        gameObjects.clear();
        renderableObjects.clear();
        animatableObjects.clear();
        police.clear();
        cameras.clear();
        interactables.clear();
        puzzles.clear();
        collisionMap.clear();
        rooms.clear();
        player = null;
        backgroundImage = null;
        temporaryMoney = 0;
    }

    /**
     * Встановлює код для взаємодії.
     *
     * @param code новий код
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Повертає поточний код для взаємодії.
     *
     * @return код
     */
    public String getCode() {
        return code;
    }

    /**
     * Конструктор для ініціалізації менеджерів, списків і початкового стану гри.
     */
    public GameManager() {
        code = "0000";
        gameObjects = new ArrayList<>();
        renderableObjects = new ArrayList<>();
        animatableObjects = new ArrayList<>();
        police = new ArrayList<>();
        cameras = new ArrayList<>();
        interactables = new ArrayList<>();
        puzzles = new ArrayList<>();
        collisionMap = new ArrayList<>();
        rooms = new ArrayList<>();
        levelManager = new LevelManager();
        saveManager = new SaveManager();
        gameState = GameState.MENU;
        isGlobalAlert = false;
        globalAlertTimer = 0.0;
        completedLevels = new ArrayList<>();
        totalMoney = 0;
        currentLevelId = 1;
        inventory = new HashMap<>();
        backgroundImage = null;
        loadProgress();
    }

    /**
     * Повертає єдиний екземпляр класу GameManager (патерн Singleton).
     *
     * @return екземпляр GameManager
     */
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    /**
     * Реєструє обробники для взаємодій гравця (натискання клавіш E та ESC).
     *
     * @param inputHandler обробник вводу
     */
    public void registerInteractionCallback(InputHandler inputHandler) {
        inputHandler.registerCallback(KeyCode.E, () -> {
            UIManager uiManager = GameWindow.getInstance().getUIManager();
            if (closestInteractable != null) {
                if (closestInteractable instanceof Door) {
                    ((Door) closestInteractable).open();
                } else if (closestInteractable instanceof InteractiveObject interactiveObject) {
                    interactiveObject.interact(player);
                }
            }
        });

        inputHandler.registerCallback(KeyCode.ESCAPE, () -> {
            if (gameState == GameState.PLAYING) {
                stopGameAndGoToMenu();
            }
        });
    }

    /**
     * Обробляє ввід гравця, викликається з GameWindow.update().
     *
     * @param inputHandler обробник вводу
     * @param deltaTime час, що минув з останнього оновлення
     */
    public void handleInput(InputHandler inputHandler, double deltaTime) {
        this.inputHandler = inputHandler;
        if (player == null) return;

        checkInteractions();
        managePlayerMoving(inputHandler, deltaTime);
        managePlayerHit(inputHandler, deltaTime);
        managePlayerShooting();
        checkCollisions();
    }

    /**
     * Керує атакою гравця (натискання клавіші Q).
     *
     * @param inputHandler обробник вводу
     * @param deltaTime час, що минув з останнього оновлення
     */
    private void managePlayerHit(InputHandler inputHandler, double deltaTime) {
        boolean isHitting = inputHandler.isKeyPressed(KeyCode.Q);

        if (isHitting && !wasHitting) {
            soundManager.playSound(SoundManager.SoundType.HIT);
            player.attack(false);
            for (Interactable interactable : interactables) {
                if (interactable instanceof Police && interactable.canInteract(player)) {
                    soundManager.playSound(SoundManager.SoundType.HITTED);
                    interactable.interact(player);
                }
            }
        }
        wasHitting = isHitting;
    }

    /**
     * Керує рухом гравця за допомогою клавіш (WASD або стрілки).
     *
     * @param inputHandler обробник вводу
     * @param deltaTime час, що минув з останнього оновлення
     */
    private void managePlayerMoving(InputHandler inputHandler, double deltaTime) {
        if (gameState != GameState.PLAYING) {
            return;
        }
        boolean isMoving = false;
        if (inputHandler.isKeyPressed(KeyCode.LEFT) || inputHandler.isKeyPressed(KeyCode.A)) {
            player.setDirection(Player.Direction.LEFT);
            if (closestInteractable != null && closestInteractable instanceof Door) {
                closestInteractable.interact(player);
            }
            player.move(Player.Direction.LEFT, deltaTime);
            isMoving = true;
        } else if (inputHandler.isKeyPressed(KeyCode.RIGHT) || inputHandler.isKeyPressed(KeyCode.D)) {
            player.setDirection(Player.Direction.RIGHT);
            if (closestInteractable != null && closestInteractable instanceof Door) {
                closestInteractable.interact(player);
            }
            player.move(Player.Direction.RIGHT, deltaTime);
            isMoving = true;
        } else if (inputHandler.isKeyPressed(KeyCode.UP) || inputHandler.isKeyPressed(KeyCode.W)) {
            player.setDirection(Player.Direction.UP);
            if (closestInteractable != null && closestInteractable instanceof Door) {
                closestInteractable.interact(player);
            }
            isMoving = true;
        } else if (inputHandler.isKeyPressed(KeyCode.DOWN) || inputHandler.isKeyPressed(KeyCode.S)) {
            player.setDirection(Player.Direction.DOWN);
            if (closestInteractable != null && closestInteractable instanceof Door) {
                closestInteractable.interact(player);
            }
            isMoving = true;
        }
        if (!isMoving) {
            player.stopMovement();
        }
        if (isMoving && !wasMoving) {
            soundManager.startRunSound();
        } else if (!isMoving && wasMoving) {
            soundManager.stopRunSound();
        }
        wasMoving = isMoving;
    }

    /**
     * Встановлює список ігрових об’єктів і сортує їх за типами.
     *
     * @param objects список ігрових об’єктів
     */
    public void setGameObjects(List<GameObject> objects) {
        gameObjects.clear();
        renderableObjects.clear();
        animatableObjects.clear();
        police.clear();
        cameras.clear();
        interactables.clear();
        gameObjects.addAll(objects);
        int door = 0;
        for (GameObject obj : objects) {
            if (obj instanceof Renderable) renderableObjects.add((Renderable) obj);
            if (obj instanceof Animatable) animatableObjects.add((Animatable) obj);
            if (obj instanceof Player) {
                player = (Player) obj;
                syncPlayerInventory();
            }
            if (obj instanceof Police) police.add((Police) obj);
            if (obj instanceof SecurityCamera) cameras.add((SecurityCamera) obj);
            if (obj instanceof Interactable) interactables.add((Interactable) obj);
            if (obj instanceof Door) door++;
        }
        if (door == 0) {
            System.err.println("НЕМАЄ ДВЕРЕЙ");
        }
    }

    /**
     * Синхронізує інвентар гравця з даними GameManager.
     */
    private void syncPlayerInventory() {
        if (player != null) {
            player.clearInventory();
            for (Map.Entry<ShopItem, Integer> entry : inventory.entrySet()) {
                ShopItem item = entry.getKey();
                int quantity = entry.getValue();
                for (int i = 0; i < quantity; i++) {
                    player.buyItem(item);
                }
            }
        }
    }


    /**
     * Встановлює карту колізій на основі списку кімнат.
     *
     * @param rooms список кімнат
     */
    public void setCollisionMap(List<Room> rooms) {
        this.rooms.clear();
        this.rooms.addAll(rooms);
        this.collisionMap.clear();
        for (Room room : rooms) {
            this.collisionMap.add(room.getBounds());
        }
    }

    /**
     * Завантажує рівень гри.
     *
     * @param levelId ID рівня
     * @param isNewGame чи є це новою грою
     */
    public void loadLevel(int levelId, boolean isNewGame) {
        UIManager uiManager = UIManager.getInstance();
        uiManager.clearSceneForMenu();
        uiManager.hideMenu();
        if (uiManager.getCurrentWindow() != null) {
            uiManager.getCurrentWindow().hide();
            uiManager.setCurrentWindow(null);
        }
        gameState = GameState.PLAYING;
        levelManager.loadLevel(levelId, isNewGame);
        currentLevel = levelManager.getLevelData();
        loadBackgroundImage();
        GameWindow.getInstance().showTitleBar();
        uiManager.showMenuButton();
    }

    /**
     * Завантажує фонове зображення рівня.
     */
    private void loadBackgroundImage() {
        if (currentLevel == null) return;
        GameLoader gameLoader = new GameLoader();
        String backgroundPath = "background/level" + getCurrentLevelId() + "/rooms.png";
        backgroundImage = gameLoader.loadImage(backgroundPath);
        if (backgroundImage == null) {
            System.err.println("Не вдалося завантажити фонове зображення: " + backgroundPath);
        }
    }

    /**
     * Оновлює масштаб фонового зображення.
     *
     * @param canvasWidth ширина canvas
     * @param canvasHeight висота canvas
     */
    public void updateBackgroundScale(double canvasWidth, double canvasHeight) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
    }

    /**
     * Активує глобальну тривогу в грі.
     */
    public void alert() {
        if (!isGlobalAlert) {
            SoundManager.getInstance().playMusic("alert.mp3");
            isGlobalAlert = true;
            globalAlertTimer = GLOBAL_ALERT_DURATION;
            for (Police police1 : police) {
                police1.alert();
            }
            if (player != null) {
                player.increaseDetection();
                UIManager.getInstance().showSirenAlert();
            }
        }
    }

    /**
     * Оновлює логіку гри.
     *
     * @param deltaTime час, що минув з останнього оновлення
     */
    public void update(double deltaTime) {
        if (gameState != GameState.PLAYING) {
            return;
        }

        if (isGlobalAlert) {
            globalAlertTimer -= deltaTime;
            if (globalAlertTimer <= 0) {
                isGlobalAlert = false;
                SoundManager.getInstance().playMusic("game.mp3");
                for (Police police1 : police) {
                    if (police1.getState() == Police.PoliceState.ALERT) {
                        police1.setState(Police.PoliceState.PATROL);
                        police1.setAnimationState("patrol");
                    }
                }
            }
        }

        if (player != null) {
            player.updateAnimation(deltaTime);
        }
        for (Animatable animatable : animatableObjects) {
            animatable.updateAnimation(deltaTime);
        }
        for (Police police : police) {
            police.update(deltaTime, rooms, player);
            checkPoliceCollisions(police);
        }
        for (SecurityCamera camera : cameras) {
            camera.detectPlayer(player, police);
        }
        checkGameOver();
        checkCollisions();
        checkInteractions();
    }

    /**
     * Перевіряє умови завершення гри (програш).
     */
    private void checkGameOver() {
        if (player == null) {
            return;
        }

        UIManager uiManager = GameWindow.getInstance().getUIManager();
        if (player.getDetectionCount() >= 3) {
            setTemporaryMoney(0);
            uiManager.createWindow(UIManager.WindowType.GAME_OVER, new JSONObject());
        }
    }

    /**
     * Рендерить графіку гри.
     *
     * @param gc контекст для рендерингу
     */
    public void render(GraphicsContext gc) {
        if (gameState != GameState.PLAYING && gameState != GameState.PAUSED) {
            return;
        }
        gc.setImageSmoothing(false);
        renderBackground(gc);
        renderableObjects.sort(Comparator.comparingInt(Renderable::getRenderLayer));
        for (Renderable renderable : renderableObjects) {
            renderable.render(gc);
        }
    }

    /**
     * Рендерить фонове зображення.
     *
     * @param gc контекст для рендерингу
     */
    private void renderBackground(GraphicsContext gc) {
        if (backgroundImage == null) return;
        gc.drawImage(backgroundImage, 0, 0, 1280, 640);
    }

    /**
     * Перевіряє колізії гравця з об’єктами та кімнатами.
     */
    public void checkCollisions() {
        checkPlayerCollisions();
        checkPlayerCollisionsWithLaserDoor();
        checkPlayerCollisionsWithGrating();
    }

    /**
     * Перевіряє колізії гравця з гратами.
     */
    private void checkPlayerCollisionsWithGrating() {
        if (player == null) return;
        for (SecurityCamera camera : cameras) {
            camera.checkPlayerGateCollisions(player);
        }
    }

    /**
     * Перевіряє колізії гравця з лазерними дверима.
     */
    private void checkPlayerCollisionsWithLaserDoor() {
        if (player == null) return;
        Bounds laserBounds = null;
        Door door = null;
        for (Interactable interactable : interactables) {
            if (interactable instanceof Door && ((Door) interactable).isLaser()) {
                laserBounds = ((Door) interactable).getBounds();
                door = (Door) interactable;
            }
        }

        if (laserBounds != null && player.getBounds().intersects(laserBounds) && door.isLocked()) {
            if (player.getDirection().equals(Player.Direction.RIGHT)) {
                player.adjustPlayerPosition(1, Player.Direction.LEFT);
            } else {
                player.adjustPlayerPosition(1, Player.Direction.RIGHT);
            }
        }
    }

    /**
     * Перевіряє колізії поліцейського з кімнатами.
     *
     * @param police об’єкт поліцейського
     */
    private void checkPoliceCollisions(Police police) {
        if (this.police == null || this.police.isEmpty()) return;

        Bounds policeBounds = police.getBounds();
        double policeX = policeBounds.getMinX();
        double policeY = policeBounds.getMinY();
        double policeWidth = policeBounds.getWidth();
        double policeHeight = policeBounds.getHeight();

        boolean fullyInside = false;

        for (Room room : rooms) {
            Bounds roomBounds = room.getBounds();
            double roomX = roomBounds.getMinX();
            double roomY = roomBounds.getMinY();
            double roomWidth = roomBounds.getWidth();
            double roomHeight = roomBounds.getHeight();

            fullyInside = policeX >= roomX &&
                    policeY >= roomY &&
                    (policeX + policeWidth) <= (roomX + roomWidth) &&
                    (policeY + policeHeight) <= (roomY + roomHeight);

            if (fullyInside) {
                break;
            }
        }

        if (!fullyInside) {
            if (police.getDirection().equals(Police.PoliceDirection.LEFT)) {
                police.setDirection(Police.PoliceDirection.RIGHT);
            } else if (police.getDirection().equals(Police.PoliceDirection.RIGHT)) {
                police.setDirection(Police.PoliceDirection.LEFT);
            }
        }
    }

    /**
     * Перевіряє колізії гравця з кімнатами.
     */
    public void checkPlayerCollisions() {
        if (player == null) return;

        Bounds playerBounds = player.getBounds();
        double playerX = playerBounds.getMinX();
        double playerY = playerBounds.getMinY();
        double playerWidth = playerBounds.getWidth();
        double playerHeight = playerBounds.getHeight();

        boolean fullyInside = false;

        for (Room room : rooms) {
            Bounds roomBounds = room.getBounds();
            double roomX = roomBounds.getMinX();
            double roomY = roomBounds.getMinY();
            double roomWidth = roomBounds.getWidth();
            double roomHeight = roomBounds.getHeight();

            fullyInside = playerX >= roomX &&
                    playerY >= roomY &&
                    (playerX + playerWidth) <= (roomX + roomWidth) &&
                    (playerY + playerHeight) <= (roomY + roomHeight);

            if (fullyInside) {
                player.allowMovement();
                break;
            }
        }

        if (!fullyInside) {
            if (player.getDirection().equals(Player.Direction.LEFT)) {
                player.adjustPlayerPosition(1, Player.Direction.RIGHT);
            } else if (player.getDirection().equals(Player.Direction.RIGHT)) {
                player.adjustPlayerPosition(1, Player.Direction.LEFT);
            }
        }
    }

    /**
     * Перевіряє взаємодії гравця з інтерактивними об’єктами.
     */
    public void checkInteractions() {
        if (player == null) return;
        closestInteractable = null;
        UIManager uiManager = GameWindow.getInstance().getUIManager();
        if (uiManager == null) {
            System.err.println("UIManager не доступний");
            return;
        }
        for (Interactable interactable : interactables) {
            if (interactable.canInteract(player)) {
                closestInteractable = interactable;
                uiManager.showInteractionPrompt(interactable.getInteractionPrompt());
                break;
            }
        }
        if (closestInteractable == null) {
            uiManager.hideInteractionPrompt();
        }
    }

    /**
     * Повертає найближчий інтерактивний об’єкт.
     *
     * @return найближчий інтерактивний об’єкт або null
     */
    public Interactable getClosestInteractable() {
        return closestInteractable;
    }

    /**
     * Виконує покупку предмета в магазині.
     *
     * @param item предмет для покупки
     * @return true, якщо покупка успішна, інакше false
     */
    public boolean buyItem(ShopItem item) {
        if (totalMoney >= item.getPrice()) {
            SoundManager.getInstance().playSound(SoundManager.SoundType.COLLECT_MONEY);
            totalMoney -= item.getPrice();
            inventory.put(item, inventory.getOrDefault(item, 0) + 1);
            if (player != null) {
                player.buyItem(item);
            }
            saveGameManagerState();
            UIManager.getInstance().updateMoneyDisplay();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Оновлює інвентар GameManager на основі інвентаря гравця.
     */
    public void updateInventoryFromPlayer() {
        if (player != null) {
            inventory.clear();
            Map<ShopItem, Integer> playerInventory = player.getInventory();
            for (Map.Entry<ShopItem, Integer> entry : playerInventory.entrySet()) {
                inventory.put(entry.getKey(), entry.getValue());
            }
            saveProgress();
        }
    }

    /**
     * Завершує гру, встановлюючи стан GAME_OVER.
     */
    public void gameOver() {
        gameState = GameState.GAME_OVER;
    }

    /**
     * Зберігає стан гри через SaveManager.
     */
    public void saveGame() {
        saveManager.saveGame(gameState);
    }

    /**
     * Повертає дані для збереження гри (реалізація Savable).
     *
     * @return JSON-об’єкт із даними для збереження
     */
    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("gameState", gameState.toString());
        data.put("currentLevelId", currentLevelId);
        data.put("code", code);
        data.put("completedLevels", completedLevels);
        data.put("totalMoney", totalMoney);
        data.put("temporaryMoney", temporaryMoney);
        return data;
    }

    /**
     * Встановлює стан гри з даних збереження (реалізація Savable).
     *
     * @param data JSON-об’єкт із даними збереження
     */
    @Override
    public void setFromData(JSONObject data) {
        gameState = GameState.valueOf(data.getString("gameState"));
        currentLevelId = data.getInt("currentLevelId");
        code = data.getString("code");
        completedLevels = new ArrayList<>();
        for (Object level : data.getJSONArray("completedLevels")) {
            completedLevels.add((Integer) level);
        }
        totalMoney = data.getInt("totalMoney");
        temporaryMoney = data.optInt("temporaryMoney", totalMoney);
    }

    /**
     * Позначує рівень як пройдений і зберігає прогрес.
     *
     * @param levelId ID рівня
     */
    public void completeLevel(int levelId) {
        if (!completedLevels.contains(levelId)) {
            completedLevels.add(levelId);
            currentLevelId = levelId + 1;
            saveManager.saveGame(gameState);
        }
    }

    /**
     * Додає тимчасові гроші до загальних і обнуляє тимчасові.
     */
    public void addMoney(int amount) {
        totalMoney += temporaryMoney;
        temporaryMoney = 0;
        saveGameManagerState();
        UIManager.getInstance().updateMoneyDisplay();
    }

    /**
     * Додає тимчасові гроші під час рівня.
     *
     * @param amount кількість грошей
     */
    public void addTemporaryMoney(int amount) {
        temporaryMoney += amount;
        saveGameManagerState();
        UIManager.getInstance().updateMoneyDisplay();
    }

    /**
     * Встановлює тимчасові гроші.
     *
     * @param amount кількість грошей
     */
    public void setTemporaryMoney(int amount) {
        temporaryMoney = amount;
        saveGameManagerState();
        UIManager.getInstance().updateMoneyDisplay();
    }

    /**
     * Повертає загальну кількість грошей.
     *
     * @return загальна кількість грошей
     */
    public int getTotalMoney() {
        return totalMoney;
    }

    /**
     * Повертає тимчасові гроші.
     *
     * @return тимчасові гроші
     */
    public int getTemporaryMoney() {
        return temporaryMoney;
    }

    /**
     * Повертає список пройдених рівнів.
     *
     * @return копія списку пройдених рівнів
     */
    public List<Integer> getCompletedLevels() {
        return new ArrayList<>(completedLevels);
    }

    /**
     * Встановлює ID поточного рівня.
     *
     * @param levelId ID рівня
     */
    public void setCurrentLevelId(int levelId) {
        this.currentLevelId = levelId;
    }

    /**
     * Повертає фонове зображення рівня.
     *
     * @return фонове зображення
     */
    public Image getBackgroundImage() {
        return backgroundImage;
    }

    /**
     * Встановлює фонове зображення за шляхом.
     *
     * @param path шлях до зображення
     */
    public void setBackgroundImage(String path) {
        GameLoader loader = new GameLoader();
        backgroundImage = loader.loadImage(path);
    }

    /**
     * Завантажує прогрес гри з файлу.
     */
    public void loadProgress() {
        JSONObject progressData = gameLoader.loadJSON("data/saves/game_progress.json");
        if (progressData != null) {
            totalMoney = progressData.optInt("totalMoney", 0);
            currentLevelId = progressData.optInt("currentLevelId", 1);
            JSONArray completed = progressData.optJSONArray("completedLevels");
            if (completed != null) {
                for (int i = 0; i < completed.length(); i++) {
                    completedLevels.add(completed.getInt(i));
                }
            }
            setFromData(progressData);
            syncPlayerInventory();
        }
    }

    /**
     * Зберігає прогрес гри.
     */
    public void saveProgress() {
        saveManager.saveGame(gameState);
    }

    /**
     * Зберігає стан GameManager у файл game_progress.json.
     */
    public void saveGameManagerState() {
        JSONObject data = new JSONObject();
        data.put("gameState", GameState.PLAYING.toString());
        data.put("currentLevelId", currentLevelId);
        data.put("code", code);
        data.put("totalMoney", totalMoney);
        data.put("temporaryMoney", temporaryMoney);
        data.put("completedLevels", new JSONArray(completedLevels));

        JSONObject inventoryData = new JSONObject();
        for (Map.Entry<ShopItem, Integer> entry : inventory.entrySet()) {
            inventoryData.put(entry.getKey().getName(), entry.getValue());
        }
        data.put("inventory", inventoryData);

        try (FileWriter file = new FileWriter("data/saves/game_progress.json")) {
            file.write(data.toString(4));
            file.flush();
        } catch (IOException e) {
            System.err.println("Помилка при збереженні стану GameManager: " + e.getMessage());
        }
    }

    /**
     * Повертає ID поточного рівня.
     *
     * @return ID поточного рівня
     */
    public int getCurrentLevelId() {
        return levelManager.getCurrentLevelId();
    }

    /**
     * Повертає список ігрових об’єктів.
     *
     * @return список ігрових об’єктів
     */
    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    /**
     * Повертає гравця.
     *
     * @return об’єкт гравця
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Повертає список поліцейських.
     *
     * @return список поліцейських
     */
    public List<Police> getPolice() {
        return police;
    }

    /**
     * Повертає список камер спостереження.
     *
     * @return список камер
     */
    public List<SecurityCamera> getCameras() {
        return cameras;
    }

    /**
     * Повертає список інтерактивних об’єктів.
     *
     * @return список інтерактивних об’єктів
     */
    public List<Interactable> getInteractables() {
        return interactables;
    }

    /**
     * Повертає список головоломок.
     *
     * @return список головоломок
     */
    public List<Puzzle> getPuzzles() {
        return puzzles;
    }

    /**
     * Повертає кімнату за позицією.
     *
     * @param position позиція для перевірки
     * @return кімната або null, якщо не знайдено
     */
    public Room getRoomForPosition(Vector2D position) {
        for (Room room : rooms) {
            Bounds bounds = room.getBounds();
            if (bounds.contains(position.x, position.y)) {
                return room;
            }
        }
        return null;
    }

    /**
     * Керує стрільбою гравця (натискання клавіші F).
     */
    private void managePlayerShooting() {
        if (this.inputHandler == null || !inputHandler.isKeyPressed(KeyCode.F)) return;

        if (player != null && player.hasGun() && !player.isAttacking()) {
            ShopItem gunItem = player.getInventory().keySet().stream()
                    .filter(item -> item.getItemType() == ShopItem.ItemType.GUN)
                    .findFirst().orElse(null);

            if (gunItem != null && player.useItem(gunItem)) {
                UIManager.getInstance().updateAllBoostCounts();
                soundManager.playSound(SoundManager.SoundType.SHOOT);
                player.attack(true);
                Police target = findTargetPolice();
                if (target != null) {
                    target.takeHit(true);
                }
            }
        }
    }

    /**
     * Знаходить цільового поліцейського для стрільби.
     *
     * @return найближчий поліцейський у напрямку стрільби або null
     */
    private Police findTargetPolice() {
        GameManager.Room playerRoom = getRoomForPosition(player.getPosition());
        if (playerRoom == null) return null;

        return police.stream()
                .filter(p -> playerRoom.equals(getRoomForPosition(p.getPosition())))
                .filter(p -> (player.getDirection() == Player.Direction.RIGHT && p.getPosition().getX() > player.getPosition().getX()) ||
                        (player.getDirection() == Player.Direction.LEFT && p.getPosition().getX() < player.getPosition().getX()))
                .min(Comparator.comparingDouble(p -> player.getPosition().distance(p.getPosition())))
                .orElse(null);
    }
}