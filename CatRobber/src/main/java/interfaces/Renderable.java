package interfaces;

import javafx.scene.canvas.GraphicsContext;

// Інтерфейс для об’єктів, які рендеряться на екрані
public interface Renderable {
    // Малює об’єкт, отримує GraphicsContext від GameManager
    void render(GraphicsContext gc);

    // Повертає шар рендерингу (наприклад, 1 для об’єктів, 2 для UI)
    int getRenderLayer();

    // Перевіряє, чи об’єкт видимий (наприклад, невидимий гравець)
    boolean isVisible();
}