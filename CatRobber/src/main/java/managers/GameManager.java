package managers;

import entities.*;
import interfaces.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import org.json.JSONObject;
import puzzles.Puzzle;
import utils.GameLoader;
import utils.InputHandler;
import utils.Vector2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Клас GameManager керує логікою гри, включаючи об'єкти, стан, колізії, взаємодії
public class GameManager implements Savable {
    // Поля
    private static GameManager instance; // Singleton-екземпляр GameManager
    private final LevelManager levelManager; // Менеджер рівнів (завантаження даних рівня)
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

    // Конструктор, ініціалізує менеджери та списки
    public GameManager() {
        gameObjects = new ArrayList<>(); // Створюємо список для всіх ігрових об’єктів
        renderableObjects = new ArrayList<>(); // Створюємо список для об’єктів із рендерингом
        animatableObjects = new ArrayList<>(); // Створюємо список для об’єктів з анімаціями
        police = new ArrayList<>(); // Створюємо список для поліцейських
        cameras = new ArrayList<>(); // Створюємо список для камер
        interactables = new ArrayList<>(); // Створюємо список для інтерактивних об’єктів
        puzzles = new ArrayList<>(); // Створюємо список для головоломок
        collisionMap = new ArrayList<>(); // Створюємо список для карти колізій
        rooms = new ArrayList<>(); // Створюємо список для кімнат
        levelManager = new LevelManager(); // Створюємо LevelManager для управління рівнями
        saveManager = new SaveManager(); // Створюємо SaveManager для збереження гри
        gameState = GameState.MENU; // Встановлюємо початковий стан MENU
    }

    // Повертає єдиний екземпляр GameManager (патерн Singleton)
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager(); // Створюємо новий екземпляр, якщо не існує
        }
        return instance; // Повертаємо екземпляр для використання в GameWindow
    }

    // Обробляє ввід гравця, викликається з GameWindow.update()
    public void handleInput(InputHandler inputHandler, double deltaTime) {
        if (player == null) return;
        checkInteractions(); // Перевіряємо взаємодії перед обробкою вводу
        managePlayerMoving(inputHandler, deltaTime);
        managePlayerOpenDoor(inputHandler);
        checkCollisions();
    }

    //Відкриття дверей за допомогою кнопки Е
    private void managePlayerOpenDoor(InputHandler inputHandler) {
        if (inputHandler.isKeyPressed(KeyCode.E)) {
            if (closestInteractable != null && closestInteractable instanceof Door) {
                Door door = (Door) closestInteractable;
                door.open(player);
                int id = door.getSharedId();
                for (Interactable interactable: interactables){
                    if (interactable instanceof Door && ((Door) interactable).getSharedId() == id ){
                        ((Door) interactable).open(player); //Відкртваємо також двері з іншої сторони
                    }
                }
            }
        }
    }
    //Рух гравця
    private void managePlayerMoving(InputHandler inputHandler, double deltaTime) {
        boolean isMoving = false;
        if (inputHandler.isKeyPressed(KeyCode.LEFT)||inputHandler.isKeyPressed(KeyCode.A)) {
            player.setDirection(Player.Direction.LEFT);
            if (closestInteractable != null && closestInteractable instanceof Door) {
                closestInteractable.interact(player); // Телепортація при натисканні стрілки
            }
            player.move(Player.Direction.LEFT, deltaTime);
            isMoving = true;
        } else if (inputHandler.isKeyPressed(KeyCode.RIGHT) ||inputHandler.isKeyPressed(KeyCode.D)) {
            player.setDirection(Player.Direction.RIGHT);
            if (closestInteractable != null && closestInteractable instanceof Door) {
                closestInteractable.interact(player);
            }
            player.move(Player.Direction.RIGHT, deltaTime);
            isMoving = true;
        } else if (inputHandler.isKeyPressed(KeyCode.UP)||inputHandler.isKeyPressed(KeyCode.W)) {
            player.setDirection(Player.Direction.UP);
            if (closestInteractable != null && closestInteractable instanceof Door) {
                closestInteractable.interact(player);
            }
            isMoving = true;
        } else if (inputHandler.isKeyPressed(KeyCode.DOWN)||inputHandler.isKeyPressed(KeyCode.S)) {
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
        // Очищаємо всі списки перед новим завантаженням
        gameObjects.clear();
        renderableObjects.clear();
        animatableObjects.clear();
        police.clear();
        cameras.clear();
        interactables.clear();
        // Додаємо нові об’єкти
        gameObjects.addAll(objects);
        for (GameObject obj : objects) {
            // Розподіляємо об’єкти за типами
            if (obj instanceof Renderable) renderableObjects.add((Renderable) obj); // Додаємо до рендерингу
            if (obj instanceof Animatable) animatableObjects.add((Animatable) obj); // Додаємо до анімацій
            if (obj instanceof Player) player = (Player) obj; // Зберігаємо гравця
            if (obj instanceof Police) police.add((Police) obj); // Додаємо поліцейських
            if (obj instanceof SecurityCamera) cameras.add((SecurityCamera) obj); // Додаємо камери
            if (obj instanceof Interactable) {interactables.add((Interactable) obj);
         System.out.println("Додано інтерактивний об'єкт: " + obj.getClass().getSimpleName());}
        }
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

    // Завантажує рівень, викликається з GameWindow
    public void loadLevel(int levelId, boolean isNewGame) {
        gameState = GameState.PLAYING; // Встановлюємо стан гри PLAYING
        levelManager.loadLevel(levelId, isNewGame); // Завантажуємо рівень через LevelManager
        currentLevel = levelManager.getLevelData(); // Отримуємо JSON-дані рівня від LevelManager
        loadBackgroundImage(); // Завантажуємо фонове зображення
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

    // Оновлює логіку гри, викликається з GameWindow.update()
    public void update(double deltaTime) {
        if (gameState != GameState.PLAYING) return; // Виходимо, якщо гра не в стані PLAYING
        if (player != null) {
            player.updateAnimation(deltaTime); // Оновлюємо анімацію гравця
        }
        for (Animatable animatable : animatableObjects) {
            animatable.updateAnimation(deltaTime); // Оновлюємо анімації всіх Animatable об’єктів
        }
        for (Police police : police) {
            police.update(deltaTime); // Оновлюємо логіку поліцейських
            police.detectPlayer(player.getPosition()); // Перевіряємо, чи бачить поліцейський гравця
        }
        for (SecurityCamera camera : cameras) {
            camera.detectPlayer(player.getPosition()); // Перевіряємо, чи бачить камера гравця
            camera.updateFrame(); // Оновлюємо кадр анімації камери
        }
        checkCollisions(); // Перевіряємо колізії
        checkInteractions(); // Перевіряємо взаємодії
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
        gc.setStroke(javafx.scene.paint.Color.RED); // Встановлюємо червоний колір для контурів
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

        for (Interactable interactable : interactables) {
            if (interactable.canInteract(player)) {
                closestInteractable = interactable;
                System.out.println("Can interact with: " + interactable.getClass().getSimpleName() +
                        ", Player direction: " + player.getDirection());
                break; // Берем першу доступну взаємодію
            }
        }
    }
    // Завершує гру, змінюючи стан
    public void gameOver() {
        gameState = GameState.GAME_OVER; // Встановлюємо стан GAME_OVER
    }

    // Зберігає гру через SaveManager
    public void saveGame() {
        saveManager.saveGame(gameState); // Викликаємо збереження, передаючи поточний стан
    }

    // Реалізує інтерфейс Savable: повертає дані для збереження
    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject(); // Створюємо JSON-об’єкт
        data.put("gameState", gameState.toString()); // Додаємо стан гри
        data.put("currentLevelId", levelManager.getCurrentLevelId()); // Додаємо ID рівня
        return data; // Повертаємо дані для SaveManager
    }

    // Реалізує інтерфейс Savable: відновлює дані зі збереження
    @Override
    public void setFromData(JSONObject data) {
        gameState = GameState.valueOf(data.getString("gameState")); // Відновлюємо стан гри
        int levelId = data.getInt("currentLevelId"); // Отримуємо ID рівня
        loadLevel(levelId, false); // Завантажуємо рівень як збережену гру
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