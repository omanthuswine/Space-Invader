package uet.oop.spaceshootergamejavafx.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents a bullet fired by an enemy in the Space Shooter game.
 * Supports both regular bullets and boss bullets with customizable attributes.
 */
public class EnemyBullet extends GameObject {

    /** Diameter of a regular bullet. */
    public static final int DIAMETER = 8;

    /** Diameter of a boss bullet. */
    public static final int BOSS_BULLET_DIAMETER = 10;

    /** Horizontal velocity of the bullet. */
    private double vx;

    /** Vertical velocity of the bullet. */
    private double vy;

    /** Color of the bullet. */
    private Color bulletColor;

    /** Indicates whether this is a boss bullet. */
    private boolean isBossBullet;

    /** Marks if the bullet should be removed. */
    private boolean dead = false;

    /**
     * Constructs a regular enemy bullet at the given position.
     *
     * @param x initial X position
     * @param y initial Y position
     */
    public EnemyBullet(double x, double y) {
        super(x, y, DIAMETER, DIAMETER);
        this.vx = 0;
        this.vy = 1;
        this.bulletColor = Color.RED;
        this.isBossBullet = false;
    }

    /**
     * Constructs an enemy bullet (regular or boss) with custom attributes.
     *
     * @param x             initial X position
     * @param y             initial Y position
     * @param vx            horizontal velocity
     * @param vy            vertical velocity
     * @param color         bullet color
     * @param isBossBullet  true if this is a boss bullet
     */
    public EnemyBullet(double x, double y, double vx, double vy, Color color, boolean isBossBullet) {
        super(x, y, isBossBullet ? BOSS_BULLET_DIAMETER : DIAMETER, isBossBullet ? BOSS_BULLET_DIAMETER : DIAMETER);
        this.vx = vx;
        this.vy = vy;
        this.bulletColor = color;
        this.isBossBullet = isBossBullet;
    }

    /**
     * Sets the velocity of the bullet.
     *
     * @param vx horizontal velocity
     * @param vy vertical velocity
     */
    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }

    /**
     * Updates the bullet's position.
     * Marks it as dead if it moves out of the game area.
     */
    @Override
    public void update() {
        this.x += vx;
        this.y += vy;

        if (this.y - (this.height / 2.0) > SpaceShooter.HEIGHT) {
            this.setDead(true);
        }

        if (this.x + (this.width / 2.0) < 0 || this.x - (this.width / 2.0) > SpaceShooter.WIDTH) {
            this.setDead(true);
        }
    }

    /**
     * Renders the bullet on the canvas.
     *
     * @param gc the GraphicsContext to draw on
     */
    @Override
    public void render(GraphicsContext gc) {
        double radius = this.width / 2.0;
        gc.setFill(this.bulletColor);
        gc.fillOval(this.x - radius, this.y - radius, this.width, this.height);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeOval(this.x - radius, this.y - radius, this.width, this.height);
    }

    /**
     * Returns the width (diameter) of the bullet.
     *
     * @return bullet width
     */
    @Override
    public double getWidth() {
        return this.width;
    }

    /**
     * Returns the height (diameter) of the bullet.
     *
     * @return bullet height
     */
    @Override
    public double getHeight() {
        return this.height;
    }

    /**
     * Checks whether the bullet should be removed.
     *
     * @return true if dead, false otherwise
     */
    @Override
    public boolean isDead() {
        return this.dead;
    }

    /**
     * Marks the bullet as dead.
     *
     * @param dead true if bullet should be removed
     */
    @Override
    public void setDead(boolean dead) {
        this.dead = dead;
    }

    /**
     * Gets the horizontal velocity of the bullet.
     *
     * @return horizontal speed
     */
    public double getVx() {
        return vx;
    }

    /**
     * Gets the vertical velocity of the bullet.
     *
     * @return vertical speed
     */
    public double getVy() {
        return vy;
    }
}
