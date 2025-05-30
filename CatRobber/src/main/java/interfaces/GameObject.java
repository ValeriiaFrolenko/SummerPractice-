package interfaces;

import interfaces.Renderable;
import interfaces.Positioned;
import interfaces.Savable;

// Базовий інтерфейс для всіх ігрових об’єктів
public interface GameObject extends Renderable, Positioned, Savable {
    // Повертає тип об’єкта (наприклад, "Player", "Police", "Door")
    String getType();
}