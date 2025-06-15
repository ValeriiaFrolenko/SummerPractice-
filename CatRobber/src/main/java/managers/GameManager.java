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

import java.util.*;

// Клас GameManager керує логікою гри, включаючи об'єкти, стан, колізії, взаємодії
public class GameManager implements Savable {
    // Поля
    private static GameManager instance; // Singleton-екземпляр GameManager
    private final LevelManager levelManager; // Менеджер рівнів (завантаження даних рівня)
    private String noteCode;
    private SaveManager saveManager; // Менеджер збереження гри, пов’язаний із SaveManager
    private JSONObject currentLevel; // Дані поточного рівня у форматі JSON, отримані від LevelManager
    private List<GameObject> gameObjects; // Список усіх ігрових об’єктів (Player, Police тощо)
    private List<Renderable> renderableObjects; // Список об’єктів, які можна рендерити (реалізують Renderable)
    private List<Animatable> animatableObjects; // Список об’єктів з анімаціями (реалізують Animatable)
    private Player player; // Посилання на гравця (тип Player), витягується з gameObjects
    private List<Police> police; // Список поліцейських NPC
    private List<SecurityCamera> cameras; // Список камер спостереження
    private List<Interactable> interactables; // Список інтерактивних об’єктів
    private List<Puzzle> puzzles; // Список головоломок
    private List<Bounds> collisionMap; // Карта колізій (межі кімнат)
    private List<Room> rooms; // Список кімнат (використовується для колізій і навігації)
    private GameState gameState; // Поточний стан гри (MENU, PLAYING, PAUSED, GAME_OVER)
    private Image backgroundImage; // Фонове зображення рівня, завантажується через GameLoader
    private double canvasWidth; // Ширина canvas, отримується від GameWindow
    private double canvasHeight; // Висота canvas, отримується від GameWindow
    private Interactable closestInteractable;
    private boolean isGlobalAlert; // Прапорець глобальної тривоги
    private double globalAlertTimer; // Таймер глобальної тривоги
    private static final double GLOBAL_ALERT_DURATION = 3.0; // Тривалість глобальної тривоги (секунди)
    private List<Integer> completedLevels; // Список пройдених рівнів
    private int totalMoney; // Загальна кількість грошей
    private int currentLevelId; // Поточний рівень
    private GameLoader gameLoader = new GameLoader(); // Додано поле
    private Map<ShopItem, Integer> inventory;

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public List<Door> getDoors() {
        List<Door> doors = new ArrayList<>();
        for (Interactable interactable: interactables){
            if (interactable instanceof Door){
                doors.add((Door) interactable);
            }
        }
        return doors;
    }

    // Перелік станів гри
    public enum GameState {MENU, PLAYING, PAUSED, GAME_OVER}

    // Внутрішній клас Room для представлення кімнат
    public static class Room {
        private int id; // Унікальний ID кімнати
        private BoundingBox bounds; // Межі кімнати (для колізій)

        public Room(int id, BoundingBox bounds) {
            this.id = id; // Ініціалізуємо ID
            this.bounds = bounds; // Ініціалізуємо межі
        }

        public int getId() {
            return id; // Повертаємо ID для використання (наприклад, у логіці)
        }

        public BoundingBox getBounds() {
            return bounds; // Повертаємо межі для перевірки колізій
        }
    }

    public void setNoteCode(String code) {
        noteCode  = code;
    }

    public String getNoteCode() {
        return noteCode;
    }

    // Конструктор, ініціалізує менеджери та списки
    public GameManager() {
        noteCode = String.valueOf(0000);
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

    // Повертає єдиний екземпляр GameManager (патерн Singleton)
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager(); // Створюємо новий екземпляр, якщо не існує
        }
        return instance; // Повертаємо екземпляр для використання в GameWindow
    }


    public void registerInteractionCallback(InputHandler inputHandler) {
        inputHandler.registerCallback(KeyCode.E, () -> {
            UIManager uiManager = GameWindow.getInstance().getUIManager();
            if (closestInteractable != null) {
                if (closestInteractable instanceof Door) {
                    ((Door) closestInteractable).open();
                } else if (closestInteractable instanceof InteractiveObject interactiveObject) {
                    interactiveObject.interact(player);
                }
            } else {
                System.out.println("E pressed, blocked: window=" + uiManager.getCurrentWindow() + ", interactable=" + closestInteractable);
            }
        });
    }

    // Обробляє ввід гравця, викликається з GameWindow.update()
    public void handleInput(InputHandler inputHandler, double deltaTime) {
        if (player == null) return;
        checkInteractions(); // Перевіряємо взаємодії перед обробкою вводу
        managePlayerMoving(inputHandler, deltaTime);
        managePlayerHit(inputHandler, deltaTime);
        checkCollisions();
    }

    private void managePlayerHit(InputHandler inputHandler, double deltaTime) {
        if (inputHandler.isKeyPressed(KeyCode.Q)) {
            player.attack(false);
            for (Interactable interactable : interactables) {
                if (interactable instanceof Police && interactable.canInteract(player)) {
                    interactable.interact(player);
                }
            }
        }
    }

    // Рух гравця
    private void managePlayerMoving(InputHandler inputHandler, double deltaTime) {
        if(gameState != GameState.PLAYING){
            return;
        }
        boolean isMoving = false;
        if (inputHandler.isKeyPressed(KeyCode.LEFT) || inputHandler.isKeyPressed(KeyCode.A)) {
            player.setDirection(Player.Direction.LEFT);
            if (closestInteractable != null && closestInteractable instanceof Door) {
                closestInteractable.interact(player); // Телепортація при натисканні стрілки
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
    }


    // Встановлює список ігрових об’єктів, сортує їх за типами
    public void setGameObjects(List<GameObject> objects) {
        // Clear all lists before loading new objects
        gameObjects.clear();
        renderableObjects.clear();
        animatableObjects.clear();
        police.clear();
        cameras.clear();
        interactables.clear();
        // Add new objects
        gameObjects.addAll(objects);
        int door =0;
        for (GameObject obj : objects) {
            if (obj instanceof Renderable) renderableObjects.add((Renderable) obj);
            if (obj instanceof Animatable) animatableObjects.add((Animatable) obj);
            if (obj instanceof Player) {
                player = (Player) obj;
                syncPlayerInventory(); // Синхронізуємо інвентар при ініціалізації гравця
            }            if (obj instanceof Police) police.add((Police) obj);
            if (obj instanceof SecurityCamera) cameras.add((SecurityCamera) obj);
            if (obj instanceof Interactable) interactables.add((Interactable) obj);
            if (obj instanceof Door) door++;
        }
        if (door==0){
            System.out.println("НЕМАЄ ДВЕРЕЙ");
        }
    }

    private void syncPlayerInventory() {
        if (player != null) {
            for (Map.Entry<ShopItem, Integer> entry : inventory.entrySet()) {
                ShopItem item = entry.getKey();
                int quantity = entry.getValue();
                for (int i = 0; i < quantity; i++) {
                    player.buyItem(item);
                }
            }
        }
    }

    private ShopItem findShopItemByName(String name) {
        for (ShopItem item : ShopPane.getItems()) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    // Встановлює карту колізій на основі списку кімнат
    public void setCollisionMap(List<Room> rooms) {
        this.rooms.clear(); // Очищаємо список кімнат
        this.rooms.addAll(rooms); // Додаємо нові кімнати
        this.collisionMap.clear(); // Очищаємо карту колізій
        for (Room room : rooms) {
            this.collisionMap.add(room.getBounds()); // Додаємо межі кімнат до карти колізій
        }
    }

    public void loadLevel(int levelId, boolean isNewGame) {
        UIManager.getInstance().hideMenu();
        if (UIManager.getInstance().getCurrentWindow() != null) {
            UIManager.getInstance().getCurrentWindow().hide();
            UIManager.getInstance().setCurrentWindow(null);
        }
        UIManager.getInstance().hideCurrentWindowToGame();
        UIManager.getInstance().getMenuPane().getChildren().clear();
        UIManager.getInstance().getMenuPane().setVisible(false);
        UIManager.getInstance().getMenuPane().setMouseTransparent(true);
        UIManager.getInstance().getOverlayPane().getChildren().clear();
        UIManager.getInstance().getOverlayPane().setVisible(false);
        UIManager.getInstance().getOverlayPane().setMouseTransparent(true);
        gameState = GameState.PLAYING;
        levelManager.loadLevel(levelId, isNewGame);
        currentLevel = levelManager.getLevelData();
        loadBackgroundImage();
    }

    // Завантажує фонове зображення рівня
    private void loadBackgroundImage() {
        if (currentLevel == null) return; // Виходимо, якщо рівень не завантажено
        GameLoader gameLoader = new GameLoader(); // Створюємо GameLoader для завантаження ресурсів
        String backgroundPath = "background/level" + getCurrentLevelId() + "/rooms.png"; // Формуємо шлях до фону
        backgroundImage = gameLoader.loadImage(backgroundPath); // Завантажуємо зображення
        if (backgroundImage == null) {
            System.err.println("Не вдалося завантажити фонове зображення: " + backgroundPath); // Логуємо помилку
        }
    }

    // Оновлює масштаб фону, викликається з GameWindow
    public void updateBackgroundScale(double canvasWidth, double canvasHeight) {
        this.canvasWidth = canvasWidth; // Зберігаємо ширину canvas
        this.canvasHeight = canvasHeight; // Зберігаємо висоту canvas
    }

    // Активує глобальну тривогу
    public void alert() {
        if (!isGlobalAlert) { // Активуємо лише якщо тривога ще не увімкнена
            isGlobalAlert = true;
            globalAlertTimer = GLOBAL_ALERT_DURATION;
            for (Police police1 : police) {
                police1.alert();
            }
            if (player != null) {
                player.increaseDetection(); // Збільшуємо лічильник виявлення один раз
            }
        }
    }

    // Оновлює логіку гри, викликається з GameWindow.update()
    public void update(double deltaTime) {
        if (gameState != GameState.PLAYING) {
            return;
        }

        // Оновлення глобальної тривоги
        if (isGlobalAlert) {
            globalAlertTimer -= deltaTime;
            if (globalAlertTimer <= 0) {
                isGlobalAlert = false;
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
        checkCollisions();
        checkInteractions();
    }

    // Рендерить гру, викликається з GameWindow.render()
    public void render(GraphicsContext gc) {
        gc.setImageSmoothing(false); // Вимикаємо згладжування для піксельної графіки
        renderBackground(gc); // Рендеримо фон
        renderableObjects.sort(Comparator.comparingInt(Renderable::getRenderLayer)); // Сортуємо об’єкти за шаром рендерингу
        for (Renderable renderable : renderableObjects) {
            renderable.render(gc); // Рендеримо кожен об’єкт
        }
    }

    // Рендерить фонове зображення
    private void renderBackground(GraphicsContext gc) {
        if (backgroundImage == null) return; // Виходимо, якщо фон не завантажено
        gc.drawImage(backgroundImage, 0, 0, 1280, 640); // Малюємо фон із фіксованими розмірами
    }

    // Малює контури кімнат (для тестування), викликається з GameWindow.render()
    public void drawRoomOutlines(GraphicsContext gc) {
        gc.setStroke(Color.RED); // Встановлюємо червоний колір для контурів
        gc.setLineWidth(2); // Встановлюємо товщину лінії

        for (Room room : rooms) {
            Bounds bounds = room.getBounds(); // Отримуємо межі кімнати
            gc.strokeRect(
                    bounds.getMinX(),
                    bounds.getMinY(),
                    bounds.getWidth(),
                    bounds.getHeight()
            ); // Малюємо контур
        }
    }

    // Перевіряє колізії гравця з кімнатами
    public void checkCollisions() {
        checkPlayerCollisions();
        // checkPlayerCollisionsWithLaserDoor();
    }

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

        if (laserBounds != null && player.getBounds().intersects(laserBounds)) {
            if (door.isLocked()) {
                player.adjustPlayerPosition(1, Player.Direction.LEFT);
            }
        }
    }

    private void checkPoliceCollisions(Police police) {
        if (this.police == null || this.police.isEmpty()) return;

            Bounds policeBounds = police.getBounds(); // Отримуємо межі гравця
            double policeX = policeBounds.getMinX();
            double policeY = policeBounds.getMinY();
            double policeWidth = policeBounds.getWidth();
            double policeHeight = policeBounds.getHeight();

            boolean fullyInside = false; // Прапорець, чи гравець повністю в межах кімнати

            for (Room room : rooms) {
                Bounds roomBounds = room.getBounds(); // Отримуємо межі кімнати
                double roomX = roomBounds.getMinX();
                double roomY = roomBounds.getMinY();
                double roomWidth = roomBounds.getWidth();
                double roomHeight = roomBounds.getHeight();

                // Перевіряємо, чи гравець повністю в межах кімнати
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

    private void checkPlayerCollisions() {
        if (player == null) return; // Виходимо, якщо гравець не ініціалізований

        Bounds playerBounds = player.getBounds(); // Отримуємо межі гравця
        double playerX = playerBounds.getMinX();
        double playerY = playerBounds.getMinY();
        double playerWidth = playerBounds.getWidth();
        double playerHeight = playerBounds.getHeight();

        boolean fullyInside = false; // Прапорець, чи гравець повністю в межах кімнати

        for (Room room : rooms) {
            Bounds roomBounds = room.getBounds(); // Отримуємо межі кімнати
            double roomX = roomBounds.getMinX();
            double roomY = roomBounds.getMinY();
            double roomWidth = roomBounds.getWidth();
            double roomHeight = roomBounds.getHeight();

            // Перевіряємо, чи гравець повністю в межах кімнати
            fullyInside = playerX >= roomX &&
                    playerY >= roomY &&
                    (playerX + playerWidth) <= (roomX + roomWidth) &&
                    (playerY + playerHeight) <= (roomY + roomHeight);

            if (fullyInside) {
                player.allowMovement(); // Дозволяємо рух, якщо гравець у кімнаті
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

    // Перевіряє взаємодії гравця з об’єктами
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
                // Показуємо підказку лише якщо немає відкритого вікна
                //if (uiManager.getCurrentWindow() == null) {
                    uiManager.showInteractionPrompt(interactable.getInteractionPrompt());
                //}
                break;
            }
        }
        if (closestInteractable == null) {
            uiManager.hideInteractionPrompt();
        }
    }

    public Interactable getClosestInteractable() {
        return closestInteractable;
    }

    public boolean buyItem(ShopItem item) {
        if (totalMoney >= item.getPrice()) {
            totalMoney -= item.getPrice();
            inventory.put(item, inventory.getOrDefault(item, 0) + 1);
            if (player != null) {
                player.buyItem(item);
            }
            saveProgress();
            saveGame();
            return true;
        } else {
            System.out.println("Недостатньо грошей для покупки: " + item.getName());
            return false;
        }
    }

    // Завершує гру, змінюючи стан
    public void gameOver() {
        gameState = GameState.GAME_OVER; // Встановлюємо стан GAME_OVER
    }

    // Зберігає гру через SaveManager

    public void saveGame() {
        saveManager.saveGame(gameState);
    }



    // Реалізує інтерфейс Savable: повертає дані для збереження

    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("gameState", gameState.toString());
        data.put("currentLevelId", currentLevelId);
        data.put("noteCode", noteCode);
        data.put("completedLevels", completedLevels);
        data.put("totalMoney", totalMoney);
        return data;
    }

    @Override
    public void setFromData(JSONObject data) {
        gameState = GameState.valueOf(data.getString("gameState"));
        currentLevelId = data.getInt("currentLevelId");
        noteCode = data.getString("noteCode");
        completedLevels = new ArrayList<>();
        for (Object level : data.getJSONArray("completedLevels")) {
            completedLevels.add((Integer) level);
        }
        totalMoney = data.getInt("totalMoney");
        saveManager.saveGame(gameState);
    }

    public void completeLevel(int levelId) {
        if (!completedLevels.contains(levelId)) {
            completedLevels.add(levelId);
            currentLevelId = levelId + 1; // Наступний рівень
            saveManager.saveGame(gameState); // Зберігаємо стан гри
        }
}

    public void addMoney(int amount) {
        totalMoney += amount;
        saveProgress();
        saveGame(); // Зберігаємо стан гри
    }

    public int getTotalMoney() {
        return totalMoney;
    }

    public List<Integer> getCompletedLevels() {
        return new ArrayList<>(completedLevels);
    }


    public void setCurrentLevelId(int levelId) {
        this.currentLevelId = levelId;
        saveProgress();
    }

    public Image getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String path) {
        GameLoader loader = new GameLoader();
        backgroundImage = loader.loadImage(path);
    }

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
        }
    }


    public void saveProgress() {
        saveManager.saveGame(gameState);
    }



    // Повертає ID поточного рівня
    public int getCurrentLevelId() {
        return levelManager.getCurrentLevelId(); // Отримуємо ID від LevelManager
    }

    // Геттери для доступу до даних
    public List<GameObject> getGameObjects() {
        return gameObjects; // Повертаємо список ігрових об’єктів
    }

    public Player getPlayer() {
        return player; // Повертаємо гравця
    }

    public List<Police> getPolice() {
        return police; // Повертаємо список поліцейських
    }

    public List<SecurityCamera> getCameras() {
        return cameras; // Повертаємо список камер
    }

    public List<Interactable> getInteractables() {
        return interactables; // Повертаємо список інтерактивних об’єктів
    }

    public List<Puzzle> getPuzzles() {
        return puzzles; // Повертаємо список головоломок
    }

    // Повертає кімнату за позицією
    public Room getRoomForPosition(Vector2D position) {
        for (Room room : rooms) {
            Bounds bounds = room.getBounds(); // Отримуємо межі кімнати
            if (bounds.contains(position.x, position.y)) { // Перевіряємо, чи позиція в межах
                return room; // Повертаємо кімнату
            }
        }
        return null; // Повертаємо null, якщо кімната не знайдена
    }
}