package uet.oop.spaceshootergamejavafx.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.List;

/**
 * Represents an enemy object in the Space Shooter game.
 * Handles movement, rendering, and death state.
 */
public class Enemy extends GameObject {

    /** Width of the enemy hitbox. */
    protected static final int WIDTH = 40;

    /** Height of the enemy hitbox. */
    protected static final int HEIGHT = 40;

    /** Vertical movement speed of the enemy. */
    public static double SPEED = 0.5;

    /** Indicates whether the enemy is dead and should be removed. */
    private boolean dead;

    /** Sprite image for the enemy. */
    private Image sprite;

    /** Flag to track if the enemy has fired its initial shot. */
    private boolean initialShotFired = false;

    /**
     * Constructs an Enemy at the given coordinates.
     *
     * @param x initial X position
     * @param y initial Y position
     */
    public Enemy(double x, double y) {
        super(x, y, WIDTH, HEIGHT);
        this.dead = false;

        try {
            sprite = new Image(getClass().getResourceAsStream("/enemy.png"));
        } catch (Exception e) {
            System.err.println("Không thể load enemy sprite!");
            sprite = null;
        }
    }

    /**
     * Updates the enemy's position each frame by moving it downward.
     */
    @Override
    public void update() {
        this.y += SPEED;
    }

    /**
     * Renders the enemy on the canvas.
     * If the sprite is not available, renders a red rectangle instead.
     *
     * @param gc the GraphicsContext to draw on
     */
    @Override
    public void render(GraphicsContext gc) {
        if (sprite != null) {
            gc.drawImage(sprite, x - WIDTH / 2, y - HEIGHT / 2, WIDTH, HEIGHT);
        } else {
            gc.setFill(Color.RED);
            gc.fillRect(x - WIDTH / 2, y - HEIGHT / 2, WIDTH, HEIGHT);
        }
    }

    /**
     * Returns the width of the enemy hitbox.
     *
     * @return width in pixels
     */
    @Override
    public double getWidth() {
        return WIDTH;
    }

    /**
     * Returns the height of the enemy hitbox.
     *
     * @return height in pixels
     */
    @Override
    public double getHeight() {
        return HEIGHT;
    }

    /**
     * Marks this enemy as dead, so it can be removed.
     *
     * @param dead true if the enemy should be removed, false otherwise
     */
    public void setDead(boolean dead) {
        this.dead = dead;
    }

    /**
     * Checks if the enemy is dead.
     *
     * @return true if dead, false otherwise
     */
    @Override
    public boolean isDead() {
        return dead;
    }

    /**
     * Makes the enemy shoot a bullet and adds it to the game objects list.
     *
     * @param newObjects the list to add the new bullet to
     */
    public void shoot(List<GameObject> newObjects) {
        EnemyBullet bullet = new EnemyBullet(this.x, this.y + HEIGHT / 2);
        newObjects.add(bullet);
    }

    /**
     * Checks whether the enemy has fired its initial shot.
     *
     * @return true if it has fired, false otherwise
     */
    public boolean hasFiredInitialShot() {
        return initialShotFired;
    }

    /**
     * Sets the initial shot fired flag.
     *
     * @param initialShotFired true if the enemy has fired its first shot
     */
    public void setInitialShotFired(boolean initialShotFired) {
        this.initialShotFired = initialShotFired;
    }

}

