package utils;

/**
 * Клас для представлення двовимірного вектора або позиції у грі.
 */
public class Vector2D {
    /** Координата x вектора. */
    public double x;

    /** Координата y вектора. */
    public double y;

    /**
     * Конструктор для ініціалізації вектора з заданими координатами.
     *
     * @param x координата x
     * @param y координата y
     */
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Додає інший вектор до поточного.
     *
     * @param other інший вектор для додавання
     * @return новий вектор, що є сумою поточного та іншого вектора
     */
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    /**
     * Обчислює відстань між поточним вектором і іншим вектором.
     *
     * @param other інший вектор
     * @return відстань між векторами (евклідова відстань)
     */
    public double distance(Vector2D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Множить поточний вектор на скаляр.
     *
     * @param scalar скаляр для множення
     * @return новий вектор, помножений на скаляр
     */
    public Vector2D multiply(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    /**
     * Встановлює значення координати x.
     *
     * @param v нове значення для x
     */
    public void setX(double v) {
        this.x = v;
    }

    /**
     * Повертає значення координати x.
     *
     * @return координата x
     */
    public double getX() {
        return x;
    }

    /**
     * Повертає значення координати y.
     *
     * @return координата y
     */
    public double getY() {
        return y;
    }
}