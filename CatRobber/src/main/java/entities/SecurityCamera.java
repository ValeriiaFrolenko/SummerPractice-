package entities;

import interfaces.*;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import managers.GameManager;
import org.json.JSONObject;
import utils.GameLoader;
import utils.Vector2D;

import java.util.List;

public class SecurityCamera implements GameObject, Animatable {
    // Поля
    private CameraDirection direction; //поточний напрямок, у якому дивиться камера
    private String currentAnimation; //поточна анімація
    private Polygon fieldOfView; //полігон, який представляє поле зору камери
    private double animationTime; //час, який пройшов з моменту початку анімації
    private int animationFrame; //поточний кадр анімації, який відображається
    private boolean isAlert; //прапорець, чи знаходиться камера в стані тривоги
    private double alertTimer; //таймер, що визначає, скільки часу камера буде залишатися в стані тривоги
    private Image[] frames; //масив зображень (кадрів)
    private String spritePath; //шлях до спрайту (зображення) камери
    private double imageX, imageY, imageWidth, imageHeight; //координати, висота та ширина камери
    private double floorPointY; //вертикальна координата точки, що вважається "підлогою" (нижня межа поля зору)
    private static final double ALERT_DURATION = 15.0; // Тривалість червоного трикутника
    private static final double FOV_HEIGHT = 100.0; // Висота трикутника (до підлоги)
    private static final double FOV_HALF_ANGLE = Math.toRadians(15.0); // Половина кута 30 градусів
    private static final double X_OFFSET = 13.0; // Зміщення по X для вершини трикутника
    private static final double DIRECTION_SWITCH_INTERVAL = 5.0; // Інтервал зміни напрямку (секунди)

    /** Перелік напрямків камери **/
    public enum CameraDirection { LEFT, RIGHT }

    /**
     * Конструктор, що ініціалізує об'єкт камери на основі заданої позиції та початкових даних із JSON
     * @param vector2D визначає координати нижнього лівого кута камери
     * @param defaultData JSON-об'єкт із параметрами камери
     */
    public SecurityCamera(Vector2D vector2D, JSONObject defaultData) {
        // Ініціалізація позиції та розмірів із JSON
        double jsonImageX = vector2D.getX();
        double jsonImageY = vector2D.getY();
        this.imageWidth = defaultData.getDouble("width");
        this.imageHeight = defaultData.getDouble("height");
        this.imageX = jsonImageX;
        this.imageY = jsonImageY - imageHeight;
        this.floorPointY = defaultData.getDouble("floorPointY");
        // Ініціалізація напрямку, анімації та стану тривоги
        this.direction = CameraDirection.valueOf(defaultData.optString("direction", "RIGHT"));
        this.currentAnimation = defaultData.getString("currentAnimation");
        this.isAlert = defaultData.optBoolean("isAlert", false);
        // Завантаження трьох зображень, використовуємо лише перше
        this.spritePath = "camera/camera.png";
        GameLoader loader = new GameLoader();
        this.frames = loader.splitSpriteSheet(spritePath, 3); // Завантажуємо 3 зображення
        // Ініціалізація поля зору та анімації
        this.fieldOfView = new Polygon();
        this.animationTime = 0;
        this.animationFrame = 0; // Завжди перший кадр
        this.alertTimer = 0;
        updateFieldOfView();
    }

    /**
     *  Оновлює полігон поля зору
     *  Поле зору — це трикутник, який починається з нижнього краю камери та розходиться донизу до уявної "підлоги" на координаті floorPointY
     */
    private void updateFieldOfView() {
        fieldOfView.getPoints().clear();
        double vertexX, vertexY;

        // Визначаємо вершину трикутника (нижній лівий/правий кут камери ±13 пікселів)
        vertexY = imageY + imageHeight; // Нижній край камери
        if (direction == CameraDirection.LEFT) {
            vertexX = imageX + X_OFFSET; // +13 пікселів від нижнього лівого кута
        } else { // RIGHT
            vertexX = imageX + imageWidth - X_OFFSET; // -13 пікселів від нижнього правого кута
        }

        // Координати основи трикутника (на підлозі)
        double baseY = floorPointY; // На рівні підлоги
        double baseWidth = FOV_HEIGHT * Math.tan(FOV_HALF_ANGLE); // Ширина основи

        if (direction == CameraDirection.LEFT) {
            fieldOfView.getPoints().addAll(
                    vertexX, vertexY, // Вершина (біля нижнього лівого кута +13)
                    vertexX - baseWidth - 90, baseY, // Ліва точка основи (зміщена ліворуч на baseWidth + 50 пікселів)
                    vertexX - baseWidth - 50, baseY // Права точка основи (зміщена вправо на baseWidth + 30 пікселів)
            );
        } else { // RIGHT
            fieldOfView.getPoints().addAll(
                    vertexX, vertexY, // Вершина (біля нижнього правого кута -13)
                    vertexX + baseWidth + 50, baseY, // Ліва точка основи (зміщена вліво на baseWidth + 30 пікселів)
                    vertexX + baseWidth + 90, baseY // Права точка основи (зміщена праворуч на baseWidth + 50 пікселів)
            );
        }
    }

    /**
     * Метод оновлює анімацію камери під час кожного кадру гри
     * @param deltaTime час, що пройшов з останнього кадру
     */
    @Override
    public void updateAnimation(double deltaTime) {
        animationTime += deltaTime;

        // Зміна напрямку кожні DIRECTION_SWITCH_INTERVAL секунд
        if (animationTime >= DIRECTION_SWITCH_INTERVAL) {
            direction = (direction == CameraDirection.LEFT) ? CameraDirection.RIGHT : CameraDirection.LEFT;
            animationTime = 0; // Скидаємо таймер
            updateFieldOfView(); // Оновлюємо трикутник при зміні напрямку
        }

        // Оновлення таймера тривоги
        if (isAlert) {
            alertTimer -= deltaTime;
            if (alertTimer <= 0) {
                isAlert = false;
                setAnimationState("normal");
            }
        }
    }

    // Перевіряє, чи гравець у полі зору камери
    public void detectPlayer(Player player, List<Police> police) {
        updateFieldOfView();
        if (player == null) return;

        Bounds playerBounds = player.getBounds();
        boolean playerInFOV = false;
        double[] points = fieldOfView.getPoints().stream().mapToDouble(Double::doubleValue).toArray();
        Polygon tempPolygon = new Polygon(points);
        for (double x = playerBounds.getMinX(); x <= playerBounds.getMaxX(); x += 1.0) {
            for (double y = playerBounds.getMinY(); y <= playerBounds.getMaxY(); y += 1.0) {
                if (tempPolygon.contains(new Point2D(x, y))) {
                    playerInFOV = true;
                    break;
                }
            }
            if (playerInFOV) break;
        }

        if (playerInFOV && !isAlert) { // Активуємо тривогу лише якщо ще не активна
            isAlert = true;
            alertTimer = ALERT_DURATION; // 10.0
            setAnimationState("alert");
            GameManager.getInstance().alert(); // Викликаємо глобальну тривогу
        }
    }

    // Рендерить камеру на canvas

    /**
     * Рендерить камеру на екран (canvas), враховуючи її напрямок та стан тривоги
     * @param gc графічний контекст, на який виконується малювання
     */
    @Override
    public void render(GraphicsContext gc) {
        Image frame = getCurrentFrame();
        if (frame != null) {
            gc.setImageSmoothing(false);
            double renderX = imageX;
            double renderY = imageY;
            double renderWidth = imageWidth;
            double renderHeight = imageHeight;

            // Рендеримо перший кадр із дзеркаленням для RIGHT
            if (direction == CameraDirection.LEFT) {
                gc.save();
                gc.translate(renderX + renderWidth, renderY);
                gc.scale(-1, 1);
                gc.drawImage(frame, 0, 0, renderWidth, renderHeight);
                gc.restore();
            } else { // LEFT
                gc.drawImage(frame, renderX, renderY, renderWidth, renderHeight);
            }

            // Оновлюємо поле зору перед рендерингом
            updateFieldOfView();

            // Малюємо трикутник поля зору
            gc.setFill(isAlert ? new Color(1.0, 0.0, 0.0, 0.3) : new Color(0.0, 1.0, 0.0, 0.3));
            double[] points = fieldOfView.getPoints().stream().mapToDouble(Double::doubleValue).toArray();
            double[] xPoints = new double[3];
            double[] yPoints = new double[3];
            for (int i = 0; i < 3; i++) {
                xPoints[i] = points[i * 2];
                yPoints[i] = points[i * 2 + 1];
            }
            gc.fillPolygon(xPoints, yPoints, 3);
        }
    }

    /**
     * Метод, що повертає поточний кадр анімації
     */
    @Override
    public Image getCurrentFrame() {
        if (frames == null || frames.length == 0) {
            System.err.println("Немає кадрів для анімації: " + currentAnimation);
            return null;
        }
        return frames[0]; // Завжди перший кадр
    }

    /**
     * Метод, що встановлює стан анімації
     */
    @Override
    public void setAnimationState(String state) {
        if (!state.equals(currentAnimation)) {
            currentAnimation = state;
            animationFrame = 0;
            animationTime = 0;
        }
    }

    // Повертає JSON для збереження

    /**
     * Метод, що дозволяє зберігати всю інформацію про камеру у файл (положення, розміри, напрямок, анімацію, тощо)
     * @return об'єкт JSONObject, який містить серіалізовані дані камери
     */
    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("type", getType());
        data.put("x", imageX);
        data.put("y", imageY + imageHeight);
        data.put("width", imageWidth);
        data.put("height", imageHeight);
        data.put("floorPointY", floorPointY);
        data.put("direction", direction.toString());
        data.put("animation", currentAnimation);
        data.put("isAlert", isAlert);
        return data;
    }

    /**
     * Встановлює стан камери на основі наданого об'єкта JSONObject
     * @param data об'єкт JSONObject, який містить дані для відновлення стану камери
     */
    @Override
    public void setFromData(JSONObject data) {
        this.imageX = data.optDouble("x", imageX);
        this.imageY = data.optDouble("y", imageY + imageHeight) - imageHeight;
        this.imageWidth = data.optDouble("width", imageWidth);
        this.imageHeight = data.optDouble("height", imageHeight);
        this.floorPointY = data.optDouble("floorPointY", floorPointY);
        this.isAlert = data.optBoolean("isAlert", isAlert);
        try {
            this.direction = CameraDirection.valueOf(data.optString("direction", direction.toString()));
            updateFieldOfView();
        } catch (IllegalArgumentException e) {
            System.err.println("Невірне значення direction: " + data.optString("direction") + ". Залишаю поточний.");
        }
        this.currentAnimation = data.optString("animation", currentAnimation);
    }

    // Геттери/Сеттери

    /**
     * Повертає тип об'єкта
     * @return рядок, що описує тип об'єкта "Camera"
     */
    @Override
    public String getType() {
        return "Camera";
    }

    /**
     * Повертає позицію камери у вигляді вектора
     * @return об'єкт {@link Vector2D} з координатами позиції камери (imageX, imageY)
     */
    @Override
    public Vector2D getPosition() {
        return new Vector2D(imageX, imageY);
    }

    /**
     * Повертає позицію зображення камери
     * @return об'єкт Vector2D з координатами зображення камери
     */
    @Override
    public Vector2D getImagePosition() {
        return new Vector2D(imageX, imageY);
    }

    /**
     * Встановлює позицію камери
     * @param position нова позиція у вигляді об'єкта Vector2D
     */
    @Override
    public void setPosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
        updateFieldOfView();
    }

    /**
     * Встановлює позицію зображення камери
     * @param position нова позиція зображення у вигляді об'єкта Vector2D
     */
    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
        updateFieldOfView();
    }

    /**
     * Повертає межі камери, який використовується для рендерингу та колізій
     * @return об'єкт Bounds, що описує позицію та розміри камери
     */
    @Override
    public Bounds getBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Повертає межі зображення камери
     * @return об'єкт Bounds, що описує позицію та розміри зображення камери
     */
    @Override
    public Bounds getImageBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Повертає рівень шару рендерингу, на якому буде відображатись камера
     * @return 1 - значення рівня шару
     */
    @Override
    public int getRenderLayer() {
        return 1;
    }

    /**
     * Повертає інформацію про видимість камери
     * @return завжди true, оскільки камера завжди видима
     */
    @Override
    public boolean isVisible() {
        return true;
    }
}