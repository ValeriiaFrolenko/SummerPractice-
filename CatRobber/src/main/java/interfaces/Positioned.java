package interfaces;

import javafx.geometry.Bounds;
import utils.Vector2D;

// Інтерфейс для об’єктів із позицією і колізіями
public interface Positioned {
    // Повертає позицію об’єкта, передає в GameManager для колізій
    Vector2D getPosition();
    Vector2D getImaginePosition();

    // Встановлює позицію, отримує від GameManager (наприклад, при русі)
    void setPosition(Vector2D position);
    void setImaginePosition(Vector2D position);

    // Повертає межі для колізій, передає в GameManager
    Bounds getBounds();
    Bounds getImagineBounds();
}