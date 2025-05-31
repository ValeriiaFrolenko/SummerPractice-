package ui;

import interfaces.Renderable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

// Абстрактний базовий клас для вікон UI (Menu, Settings, Shop), реалізує Renderable
public abstract class UIWindow implements Renderable {
    // Поля
    protected Pane pane; // JavaFX-контейнер для UI
    protected boolean isVisible; // Стан видимості

    // Конструктор
    public UIWindow() {}

    // Активує вікно
    // Викликається UIManager.showWindow
    public void activate() {}

    // Деактивує вікно
    // Викликається UIManager при перемиканні вікон
    public void deactivate() {}

    // Обробляє ввід
    // Отримує KeyEvent від UIManager, передає в GameManager або Puzzle
    public abstract void handleInput(KeyEvent event);

    // Методи Renderable
    // Малює UI, отримує GraphicsContext від UIManager
    @Override
    public void render(GraphicsContext gc) {}

    // Повертає шар рендерингу (2 для UI)
    @Override
    public int getRenderLayer() { return 2; }

    // Перевіряє видимість
    @Override
    public boolean isVisible() { return false; }

    // Повертає JavaFX-панель для відображення
    // Передає в UIManager для overlayPane
    public Pane getPane() { return null; }
}