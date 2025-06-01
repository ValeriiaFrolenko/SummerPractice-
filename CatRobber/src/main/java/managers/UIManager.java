package managers;

import interfaces.Renderable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.Node;
import ui.UIWindow;

import java.util.List;
import java.util.Map;

// Керує інтерфейсом користувача, реалізує Renderable
public class UIManager implements Renderable {
    // Поля
    private Canvas canvas; // Canvas для рендерингу
    private UIWindow currentWindow; // Поточне вікно (Menu, Settings, Shop)
    private Map<WindowType, UIWindow> windows; // Усі вікна за типами
    private List<String> interactionPrompts; // Підказки взаємодії
    private Pane overlayPane; // Панель для діалогів і головоломок
    private VBox dialogBox; // Контейнер для діалогів
    private Label interactionLabel; // Текст підказки

    // Енум для типів вікон
    public enum WindowType { MENU, SETTINGS, SHOP }

    // Конструктор
    // Отримує canvas від GameWindow
    public UIManager(Canvas canvas) {}

    // Показує вікно за типом
    // Отримує тип (MENU, SETTINGS, SHOP), встановлює currentWindow
    public void showWindow(WindowType type) {}

    // Обробляє ввід
    // Отримує KeyEvent від InputHandler, передає в GameManager або Puzzle
    public void handleInput(KeyEvent event) {}

    // Показує діалог
    // Отримує текст від Note, Computer
    public void showDialog(String text) {}

    // Показує підказку взаємодії
    // Отримує текст від Interactable.getInteractionPrompt
    public void showInteractionPrompt(String prompt) {}

    // Приховує підказку взаємодії
    public void hideInteractionPrompt() {}

    // Методи Renderable
    // Малює UI, отримує GraphicsContext від GameWindow
    @Override
    public void render(GraphicsContext gc) {}

    // Повертає шар рендерингу (2 для UI)
    @Override
    public int getRenderLayer() { return 2; }

    // Перевіряє видимість (true якщо currentWindow не null)
    @Override
    public boolean isVisible() { return false; }
}