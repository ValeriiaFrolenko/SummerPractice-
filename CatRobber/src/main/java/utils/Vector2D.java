package utils;

public class Vector2D {
    double x, y;
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void add(Vector2D other) {
        x += other.x;
        y += other.y;
    }

    public double distance(Vector2D other) {
        return Math.sqrt(x * x + y * y);
    }

}
