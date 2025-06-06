package entities;

import interfaces.*;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.json.JSONObject;
import utils.GameLoader;
import utils.Vector2D;

// Представляє інтерактивний об’єкт (нотатка, комп’ютер, драбина тощо)
public class InteractiveObject implements GameObject, Interactable {
    // Поля
    private Vector2D position; // Позиція об’єкта, з JSON
    private Image sprite; // Спрайт об’єкта, завантажений через GameLoader
    private String spritePath; // Шлях до спрайту, з JSON
    private Type type; // Тип об’єкта (NOTE, PICTURE, COMPUTER, LADDER), з JSON
    private double width; // Ширина, з JSON
    private double height; // Висота, з JSON

    // Типи інтерактивних об’єктів
    public enum Type { NOTE, PICTURE, COMPUTER, LADDER }

    // Конструктор: ініціалізує об’єкт із JSON-даними
    public InteractiveObject(Vector2D position, JSONObject properties) {
        this.position = position;
        this.type = Type.valueOf(properties.optString("type", "NOTE"));
        this.spritePath = properties.optString("spritePath", "interactables/default.png");
        this.width = properties.optDouble("width", 32.0);
        this.height = properties.optDouble("height", 32.0);
        GameLoader loader = new GameLoader();
        this.sprite = loader.loadImage(spritePath);
    }

    // --- Ініціалізація та оновлення ---

    // Взаємодія з гравцем (викликається з GameManager.checkInteractions())
    @Override
    public void interact(Player player) {
        // TODO: Реалізувати взаємодію
        // Залежно від type викликати відповідну дію (напр., player.climb() для LADDER)
    }

    // Перевіряє, чи можлива взаємодія
    @Override
    public boolean canInteract(Player player) {
        // TODO: Реалізувати перевірку
        // Перевірити відстань до гравця через getInteractionRange()
        return false;
    }

    // --- Рендеринг ---

    // Рендерить об’єкт на canvas (викликається з GameManager.render())
    @Override
    public void render(GraphicsContext gc) {
        // TODO: Реалізувати рендеринг
        // Намалювати sprite на gc за position, width, height
    }

    // --- Серіалізація ---

    // Повертає JSON для збереження (викликається з SaveManager.saveInteractables())
    @Override
    public JSONObject getSerializableData() {
        // TODO: Реалізувати серіалізацію
        // Створити JSONObject з position, type, spritePath тощо
        return null;
    }

    // Відновлює стан із JSON (викликається з SaveManager.loadGame())
    @Override
    public void setFromData(JSONObject data) {
        // TODO: Реалізувати десеріалізацію
        // Оновити position, type, spritePath із data
    }

    // --- Геттери/Сеттери ---

    // Повертає тип об’єкта
    @Override
    public String getType() {
        return type.toString();
    }

    // Повертає позицію об’єкта
    @Override
    public Vector2D getPosition() {
        return new Vector2D(position.x, position.y);
    }

    // Повертає уявну позицію
    @Override
    public Vector2D getImaginePosition() {
        return getPosition();
    }

    // Встановлює позицію об’єкта
    @Override
    public void setPosition(Vector2D position) {
        this.position = position;
    }

    // Встановлює уявну позицію
    @Override
    public void setImaginePosition(Vector2D position) {
        this.position = position;
    }

    // Повертає межі для колізій
    @Override
    public Bounds getBounds() {
        return new BoundingBox(position.x, position.y, width, height);
    }

    // Повертає межі для рендерингу
    @Override
    public Bounds getImagineBounds() {
        return getBounds();
    }

    // Повертає діапазон взаємодії
    @Override
    public double getInteractionRange() {
        return 50.0; // Фіксована відстань
    }

    // Повертає підказку для UI
    @Override
    public String getInteractionPrompt() {
        return "Interact with " + type.toString();
    }

    // Повертає шар рендерингу
    @Override
    public int getRenderLayer() {
        return 1; // Об’єкти рендеряться на шарі 1
    }

    // Перевіряє видимість
    @Override
    public boolean isVisible() {
        return true; // Об’єкти завжди видимі
    }
}