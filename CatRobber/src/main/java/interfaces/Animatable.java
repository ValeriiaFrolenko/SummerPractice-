package interfaces;

import javafx.scene.image.Image;

// Інтерфейс для об’єктів із анімаціями
public interface Animatable {
    // Оновлює анімацію, отримує deltaTime від GameManager
    void updateAnimation(double deltaTime);

    // Встановлює стан анімації (наприклад, "IDLE", "RUN")
    void setAnimationState(String state);

    // Повертає поточний кадр анімації для рендерингу
    Image getCurrentFrame();
}