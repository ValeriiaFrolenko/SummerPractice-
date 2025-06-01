package utils;

// Представляє 2D-вектор або позицію для обчислень у грі
public class Vector2D {
    // Поля
    public double x; // Координата x
    public double y; // Координата y

    // Конструктор
    // Ініціалізує вектор з координатами x, y
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Додає інший вектор
    // Отримує other, повертає новий Vector2D
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    // Обчислює відстань до іншого вектора
    // Отримує other, повертає відстань (число)
    public double distance(Vector2D other) { return Math.sqrt(x * x + y * y) ; }

    // Множить вектор на скаляр
    // Отримує scalar, повертає новий Vector2D
    public Vector2D multiply(double scalar) { return new Vector2D(this.x * scalar, this.y * scalar); }
}