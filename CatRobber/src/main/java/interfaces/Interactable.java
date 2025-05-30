package interfaces;

import entities.Player;

// Інтерфейс для об’єктів, з якими взаємодіє гравець
public interface Interactable {
    // Виконує взаємодію (наприклад, відкриває UI, запускає головоломку), отримує Player
    void interact(Player player);

    // Перевіряє можливість взаємодії (залежить від відстані), передає в GameManager
    boolean canInteract(Player player);

    // Повертає дистанцію взаємодії, передає в GameManager
    double getInteractionRange();

    // Повертає підказку для UI (наприклад, "Натисни E"), передає в UIManager
    String getInteractionPrompt();
}