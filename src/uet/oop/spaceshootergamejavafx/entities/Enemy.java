package uet.oop.spaceshootergamejavafx.entities;

import javafx.scene.canvas.GraphicsContext;

/**
 * Skeleton for Enemy. Students must implement movement, rendering,
 * and death state without viewing the original implementation.
 */
public class Enemy extends GameObject {

    // Hitbox dimensions
    protected static final int WIDTH = 30;
    protected static final int HEIGHT = 30;

    // Movement speed
    public static double SPEED = 1;

    // Flag to indicate if enemy should be removed
    private boolean dead;

    /**
     * Constructs an Enemy at the given coordinates.
     * @param x initial X position
     * @param y initial Y position
     */
    public Enemy(double x, double y) {
        super(x, y, WIDTH, HEIGHT);
        // TODO: load sprite if needed and initialize dead flag
    }

    /**
     * Updates enemy position each frame.
     */
    @Override
    public void update() {
        // TODO: implement vertical movement by SPEED
    }

    /**
     * Renders the enemy on the canvas.
     * @param gc the GraphicsContext to draw on
     */
    @Override
    public void render(GraphicsContext gc) {
        // TODO: draw sprite or fallback shape (e.g., colored rectangle)
    }

    /**
     * Returns the current width of the enemy.
     * @return WIDTH
     */
    @Override
    public double getWidth() {
        // TODO: return width
        return WIDTH;
    }

    /**
     * Returns the current height of the enemy.
     * @return HEIGHT
     */
    @Override
    public double getHeight() {
        // TODO: return height
        return HEIGHT;
    }

    /**
     * Marks this enemy as dead (to be removed).
     * @param dead true if enemy should be removed
     */
    public void setDead(boolean dead) {
        // TODO: update dead flag
    }

    /**
     * Checks if this enemy is dead.
     * @return true if dead, false otherwise
     */
    @Override
    public boolean isDead() {
        // TODO: return dead flag
        return dead;
    }
}
