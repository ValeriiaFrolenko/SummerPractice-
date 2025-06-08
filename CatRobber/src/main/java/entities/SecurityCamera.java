package entities;

import interfaces.*;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.json.JSONObject;
import utils.GameLoader;
import utils.Vector2D;

public class SecurityCamera implements GameObject, Animatable {
    // Поля
    private CameraDirection direction;
    private String currentAnimation;
    private Polygon fieldOfView;
    private double animationTime;
    private int animationFrame;
    private boolean isAlert;
    private double alertTimer;
    private Image[] frames;
    private String spritePath;
    private double imageX;
    private double imageY;
    private double imageWidth;
    private double imageHeight;
    private double floorPointY;
    private static final double ALERT_DURATION = 5.0; // Тривалість червоного трикутника
    private static final double FOV_HEIGHT = 100.0; // Висота трикутника (до підлоги)
    private static final double FOV_HALF_ANGLE = Math.toRadians(15.0); // Половина кута 30 градусів
    private static final double X_OFFSET = 13.0; // Зміщення по X для вершини трикутника
    private static final double DIRECTION_SWITCH_INTERVAL = 7.0; // Інтервал зміни напрямку (секунди)

    public enum CameraDirection { LEFT, RIGHT }

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

    // Оновлює полігон поля зору
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
                    vertexX - baseWidth -50, baseY // Права точка основи (зміщена вправо на baseWidth + 30 пікселів)
            );
        } else { // RIGHT
            fieldOfView.getPoints().addAll(
                    vertexX, vertexY, // Вершина (біля нижнього правого кута -13)
                    vertexX + baseWidth +50, baseY, // Ліва точка основи (зміщена вліво на baseWidth + 30 пікселів)
                    vertexX + baseWidth + 90, baseY // Права точка основи (зміщена праворуч на baseWidth + 50 пікселів)
            );
        }
    }

    // Оновлює анімацію камери
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

    // Оновлює кадр анімації
    public void updateFrame() {
        animationFrame = 0; // Завжди перший кадр
    }

    // Перевіряє, чи гравець у полі зору камери
    public void detectPlayer(Player player) {
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

        if (playerInFOV) {
            isAlert = true;
            alertTimer = ALERT_DURATION;
            setAnimationState("alert");
            player.increaseDetection();
        }
    }

    // Рендерить камеру на canvas
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

    // Повертає поточний кадр анімації
    @Override
    public Image getCurrentFrame() {
        if (frames == null || frames.length == 0) {
            System.err.println("Немає кадрів для анімації: " + currentAnimation);
            return null;
        }
        return frames[0]; // Завжди перший кадр
    }

    // Встановлює стан анімації
    @Override
    public void setAnimationState(String state) {
        if (!state.equals(currentAnimation)) {
            currentAnimation = state;
            animationFrame = 0;
            animationTime = 0;
        }
    }

    // Повертає JSON для збереження
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
        data.put("alertTimer", alertTimer);
        return data;
    }

    // Відновлює стан із JSON
    @Override
    public void setFromData(JSONObject data) {
        this.imageX = data.optDouble("x", imageX);
        this.imageY = data.optDouble("y", imageY + imageHeight) - imageHeight;
        this.imageWidth = data.optDouble("width", imageWidth);
        this.imageHeight = data.optDouble("height", imageHeight);
        this.floorPointY = data.optDouble("floorPointY", floorPointY);
        this.isAlert = data.optBoolean("isAlert", isAlert);
        this.alertTimer = data.optDouble("alertTimer", alertTimer);
        try {
            this.direction = CameraDirection.valueOf(data.optString("direction", direction.toString()));
            updateFieldOfView();
        } catch (IllegalArgumentException e) {
            System.err.println("Невірне значення direction: " + data.optString("direction") + ". Залишаю поточний.");
        }
        this.currentAnimation = data.optString("animation", currentAnimation);
    }

    // Геттери/Сеттери
    @Override
    public String getType() {
        return "Camera";
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
        updateFieldOfView();
    }

    @Override
    public void setImagePosition(Vector2D position) {
        this.imageX = position.getX();
        this.imageY = position.getY();
        updateFieldOfView();
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
    public int getRenderLayer() {
        return 1;
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}