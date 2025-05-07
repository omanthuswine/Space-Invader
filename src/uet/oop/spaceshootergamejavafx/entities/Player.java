package uet.oop.spaceshootergamejavafx.entities;

import javafx.scene.canvas.GraphicsContext;
import java.util.List;

/**
 * Skeleton for Player. Students must implement movement, rendering,
 * shooting, and health/state management.
 */
public class Player extends GameObject{

    // Hitbox dimensions
    private static final int WIDTH = 40;
    private static final int HEIGHT = 40;

    // Movement speed
    private static final double SPEED = 5;

    // Movement flags
    private boolean moveLeft;
    private boolean moveRight;
    private boolean moveForward;
    private boolean moveBackward;

    // Player health
    private int health;

    // State flag for removal
    private boolean dead;

    /**
     * Constructs a Player at the given position.
     * @param x initial X position
     * @param y initial Y position
     */
    public Player(double x, double y) {
        super(x, y, WIDTH, HEIGHT);
        // TODO: initialize health, dead flag, load sprite if needed
    }

    /**
     * Returns the width of the player.
     */
    @Override
    public double getWidth() {
        // TODO: return width
        return WIDTH;
    }

    /**
     * Returns the height of the player.
     */
    @Override
    public double getHeight() {
        // TODO: return height
        return HEIGHT;
    }

    /**
     * Returns current health of the player.
     */
    public int getHealth() {
        // TODO: return health
        return health;
    }

    /**
     * Sets player's health.
     */
    public void setHealth(int health) {
        // TODO: update health
        this.health = health;
    }

    /**
     * Updates player position based on movement flags.
     */
    @Override
    public void update() {
        // TODO: implement movement with SPEED and screen bounds
    }

    /**
     * Renders the player on the canvas.
     */
    @Override
    public void render(GraphicsContext gc) {
        // TODO: draw sprite or placeholder shape
    }

    /**
     * Sets movement flags.
     */
    public void setMoveLeft(boolean moveLeft) {
        // TODO: update moveLeft flag
        this.moveLeft = moveLeft;
    }

    public void setMoveRight(boolean moveRight) {
        // TODO: update moveRight flag
        this.moveRight = moveRight;
    }

    public void setMoveForward(boolean moveForward) {
        // TODO: update moveForward flag
        this.moveForward = moveForward;
    }

    public void setMoveBackward(boolean moveBackward) {
        // TODO: update moveBackward flag
        this.moveBackward = moveBackward;
    }

    /**
     * Shoots a bullet from the player.
     */
    public void shoot(List<GameObject> newObjects) {
        // TODO: create and add new Bullet at (x, y - HEIGHT/2)
    }

    /**
     * Marks the player as dead.
     */
    public void setDead(boolean dead) {
        // TODO: update dead flag
        this.dead = dead;
    }

    /**
     * Checks whether the player is dead.
     */
    @Override
    public boolean isDead() {
        // TODO: return dead flag
        return dead;
    }
}
