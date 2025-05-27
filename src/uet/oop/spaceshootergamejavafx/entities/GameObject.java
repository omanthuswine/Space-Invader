package uet.oop.spaceshootergamejavafx.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Bounds;
import javafx.scene.shape.Rectangle;

/**
 * Abstract base class for all game objects.
 * Provides common properties such as position and size,
 * and defines abstract methods for updating and rendering.
 */
public abstract class GameObject {

    /** X coordinate of the object. */
    protected double x;

    /** Y coordinate of the object. */
    protected double y;

    /** Width of the object. */
    protected double width;

    /** Height of the object. */
    protected double height;

    /**
     * Constructs a game object at the specified position with given dimensions.
     *
     * @param x      initial X position
     * @param y      initial Y position
     * @param width  object width
     * @param height object height
     */
    public GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Updates the game object's state each frame.
     * Typically used to update position or check for interactions.
     */
    public abstract void update();

    /**
     * Renders the game object on the canvas.
     *
     * @param gc the GraphicsContext used for drawing
     */
    public abstract void render(GraphicsContext gc);

    /**
     * Checks whether this object should be removed from the game.
     *
     * @return true if the object is marked as dead, false otherwise
     */
    public abstract boolean isDead();

    /**
     * Sets whether this object should be removed from the game.
     *
     * @param dead true to mark the object as dead
     */
    public abstract void setDead(boolean dead);

    /**
     * Returns the current X coordinate of the object.
     *
     * @return X position
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the current Y coordinate of the object.
     *
     * @return Y position
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the bounding box of this object for collision detection.
     *
     * @return Bounds representing the object's collision area
     */
    public Bounds getBounds() {
        return new Rectangle(
                x - getWidth() / 2,
                y - getHeight() / 2,
                getWidth(),
                getHeight()
        ).getBoundsInLocal();
    }

    /**
     * Returns the width of the object.
     *
     * @return object width
     */
    public abstract double getWidth();

    /**
     * Returns the height of the object.
     *
     * @return object height
     */
    public abstract double getHeight();

    /**
     * Sets the position of this object.
     *
     * @param x new X coordinate
     * @param y new Y coordinate
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
