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
    private CameraDirection direction; // поточний напрямок, у якому дивиться камера
    private String currentAnimation; // поточна анімація
    private Polygon fieldOfView; // полігон, який представляє поле зору камери
    private double animationTime; // час, який пройшов з моменту початку анімації
    private int animationFrame; // поточний кадр анімації, який відображається
    private boolean isAlert; // прапорець, чи знаходиться камера в стані тривоги
    private double alertTimer; // таймер, що визначає, скільки часу камера буде залишатися в стані тривоги
    private Image[] frames; // масив зображень (кадрів)
    private String spritePath; // шлях до спрайту (зображення) камери
    private double imageX, imageY, imageWidth, imageHeight; // координати, висота та ширина камери
    private double floorPointY; // вертикальна координата точки, що вважається "підлогою" (нижня межа поля зору)
    private static final double ALERT_DURATION = 15.0; // Тривалість червоного трикутника
    private static final double FOV_HEIGHT = 100.0; // Висота трикутника (до підлоги)
    private static final double FOV_HALF_ANGLE = Math.toRadians(15.0); // Половина кута 30 градусів
    private static final double X_OFFSET = 13.0; // Зміщення по X для вершини трикутника
    private static final double DIRECTION_SWITCH_INTERVAL = 5.0; // Інтервал зміни напрямку (секунди)

    // Поля для грат
    private boolean hasGrating; // Чи має камера грати
    private Grating grating; // Внутрішній об'єкт для керування гратами
    private GameLoader loader;

    /** Перелік напрямків камери **/
    public enum CameraDirection { LEFT, RIGHT }

    /**
     * Внутрішній клас для керування гратами
     */
    private class Grating {
        private double x, y; // Координати грат
        private double width, height; // Розміри грат
        private Image openImage; // Зображення опущених грат
        private Image closedImage; // Зображення закритих грат

        public Grating(double x, double y, double width, double height, Image openImage, Image closedImage) {
            this.x = x;
            this.y = y-height;
            this.width = width;
            this.height = height;
            this.openImage = openImage; // Шлях до зображення опущених грат
            this.closedImage =closedImage; // Шлях до зображення закритих грат
        }

        // Рендеринг грат
        public void render(GraphicsContext gc, boolean isAlert) {
            Image imageToDraw = isAlert ? closedImage : openImage;
            gc.drawImage(imageToDraw, x, y, width, height);
        }
    }

    /**
     * Конструктор, що ініціалізує об'єкт камери на основі заданої позиції та початкових даних із JSON
     * @param vector2D визначає координати нижнього лівого кута камери
     * @param defaultData JSON-об'єкт із параметрами камери
     */
    public SecurityCamera(Vector2D vector2D, JSONObject defaultData) {
        this.loader =new GameLoader();
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
        this.frames = loader.splitSpriteSheet(spritePath, 3); // Завантажуємо 3 зображення
        // Ініціалізація поля зору та анімації
        this.fieldOfView = new Polygon();
        this.animationTime = 0;
        this.animationFrame = 0; // Завжди перший кадр
        this.alertTimer = 0;
        updateFieldOfView();

        // Ініціалізація грат
        this.hasGrating = defaultData.optBoolean("hasGrating", false);
        if (hasGrating) {
            double gratingX = defaultData.getDouble("gratingX");
            double gratingY = defaultData.getDouble("gratingY");
            double gratingWidth = defaultData.getDouble("gratingWidth");
            double gratingHeight = defaultData.getDouble("gratingHeight");

            Image[] images = loader.splitSpriteSheet("camera/gates.png", 2);
            this.grating = new Grating(gratingX, gratingY, gratingWidth, gratingHeight, images[0], images[1]);
        }
    }

    /**
     * Оновлює полігон поля зору
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
                    vertexX - baseWidth - 90, baseY, // Ліва точка основи
                    vertexX - baseWidth - 50, baseY // Права точка основи
            );
        } else { // RIGHT
            fieldOfView.getPoints().addAll(
                    vertexX, vertexY, // Вершина (біля нижнього правого кута -13)
                    vertexX + baseWidth + 50, baseY, // Ліва точка основи
                    vertexX + baseWidth + 90, baseY // Права точка основи
            );
        }
    }

    /**
     * Оновлює анімацію камери
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

    /**
     * Перевіряє, чи гравець у полі зору камери
     */
    public void detectPlayer(Player player, List<Police> police) {
        updateFieldOfView();
        if (player == null || player.isInvisible()) {
            return;
        }
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

        if (playerInFOV && !isAlert) {
            isAlert = true;
            alertTimer = ALERT_DURATION;
            setAnimationState("alert");
            GameManager.getInstance().alert();
        }
    }

    public void checkPlayerGateCollisions(Player player) {
        if (isAlert){
        if (player == null || grating == null || !hasGrating) return;

        Bounds playerBounds = new BoundingBox(player.getBounds().getMinX(), player.getBounds().getMinY(), player.getBounds().getWidth(), player.getBounds().getHeight()); // або твій метод getBounds()
        Bounds gatesBounds = new BoundingBox(grating.x, grating.y, grating.width, grating.height);

        if (playerBounds.intersects(gatesBounds)) {
            if (player.getDirection().equals(Player.Direction.RIGHT)) {
                player.adjustPlayerPosition(1, Player.Direction.LEFT);
            } else {
                player.adjustPlayerPosition(1, Player.Direction.RIGHT);
            }
        }
        }
    }

    /**
     * Рендерить камеру та грати (якщо є)
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
            } else { // RIGHT
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

        // Рендеримо грати, якщо вони є
        if (hasGrating && grating != null) {
            grating.render(gc, isAlert);
        }
    }

    /**
     * Повертає поточний кадр анімації
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
     * Встановлює стан анімації
     */
    @Override
    public void setAnimationState(String state) {
        if (!state.equals(currentAnimation)) {
            currentAnimation = state;
            animationFrame = 0;
            animationTime = 0;
        }
    }

    /**
     * Зберігає інформацію про камеру та грати (якщо є)
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
        data.put("currentAnimation", currentAnimation);
        data.put("isAlert", isAlert);
        data.put("hasGrating", hasGrating);
        if (hasGrating && grating != null) {
            data.put("gratingX", grating.x);
            data.put("gratingY", grating.y);
            data.put("gratingWidth", grating.width);
            data.put("gratingHeight", grating.height);
        }
        return data;
    }

    /**
     * Встановлює стан камери та грат на основі наданого JSON
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

        // Завантаження даних про грати
        this.hasGrating = data.optBoolean("hasGrating", false);
        if (hasGrating) {
            double gratingX = data.optDouble("gratingX", 0);
            double gratingY = data.optDouble("gratingY", 0);
            double gratingWidth = data.optDouble("gratingWidth", 0);
            double gratingHeight = data.optDouble("gratingHeight", 0);
            Image[] images = loader.splitSpriteSheet("camera/gates.png", 2);
            this.grating = new Grating(gratingX, gratingY, gratingWidth, gratingHeight, images[0], images[1]);
        } else {
            this.grating = null;
        }
    }


    /**
     * Повертає тип об'єкта.
     * @return Рядок "Camera".
     */
    @Override
    public String getType() {
        return "Camera";
    }

    /**
     * Повертає позицію об'єкта.
     * @return Vector2D з координатами.
     */
    @Override
    public Vector2D getPosition() {
        return new Vector2D(imageX, imageY);
    }

    /**
     * Повертає позицію візуального спрайту.
     * @return Vector2D з координатами.
     */
    @Override
    public Vector2D getImagePosition() {
        return new Vector2D(imageX, imageY);
    }

    /**
     * Встановлює позицію об'єкта.
     * @param position Нова позиція.
     */
    @Override
    public void setPosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
        updateFieldOfView();
    }

    /**
     * Встановлює позицію візуального спрайту.
     * @param position Нова позиція.
     */
    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
        updateFieldOfView();
    }

    /**
     * Повертає межі об'єкта.
     * @return Об'єкт Bounds.
     */
    @Override
    public Bounds getBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Повертає межі візуального спрайту.
     * @return Об'єкт Bounds.
     */
    @Override
    public Bounds getImageBounds() {
        return new BoundingBox(imageX, imageY, imageWidth, imageHeight);
    }

    /**
     * Повертає шар рендерингу для камери.
     * @return 1 (камера рендериться на тому ж шарі, що й поліція).
     */
    @Override
    public int getRenderLayer() {
        return 1;
    }

    /**
     * Визначає, чи є камера видимою.
     * @return Завжди true.
     */
    @Override
    public boolean isVisible() {
        return true;
    }
}