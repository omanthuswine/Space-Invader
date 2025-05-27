package uet.oop.spaceshootergamejavafx.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * PowerUp đại diện cho các vật phẩm hỗ trợ người chơi trong game.
 * PowerUp sẽ di chuyển xuống phía dưới màn hình và biến mất khi ra khỏi biên.
 */
public class PowerUp extends GameObject {

    /**
     * Các loại PowerUp có thể xuất hiện trong game.
     */
    public enum PowerUpType {
        HEALTH_PACK,  // Hồi máu
        SHIELD,       // Khiên bảo vệ
        TRIPLE_SHOT   // Bắn 3 viên đạn
    }

    public static final int WIDTH = 40;
    public static final int HEIGHT = 40;
    private static final double SPEED = 2;

    private boolean dead;
    private PowerUpType type;
    private Image sprite;
    private Image shieldSprite;
    private Image tripleShotSprite;

    /**
     * Tạo một PowerUp mới tại vị trí (x, y) với loại được chỉ định.
     * @param x hoành độ tâm PowerUp
     * @param y tung độ tâm PowerUp
     * @param type loại PowerUp
     */
    public PowerUp(double x, double y, PowerUpType type) {
        super(x, y, WIDTH, HEIGHT);
        this.dead = false;
        this.type = type;

        try {
            // Load sprite mặc định cho HEALTH_PACK hoặc power-up chung
            this.sprite = new Image(getClass().getResourceAsStream("/powerup.png"));

            // Load sprite riêng cho Shield
            java.io.InputStream shieldStream = getClass().getResourceAsStream("/powerup_shield.gif");
            if (shieldStream != null) {
                this.shieldSprite = new Image(shieldStream);
                shieldStream.close();
            } else {
                System.err.println("Không tìm thấy /powerup_shield.gif");
            }

            // Load sprite riêng cho Triple Shot
            java.io.InputStream tripleShotStream = getClass().getResourceAsStream("/powerup_tripleshot.png");
            if (tripleShotStream != null) {
                this.tripleShotSprite = new Image(tripleShotStream);
                tripleShotStream.close();
            } else {
                System.err.println("Không tìm thấy /powerup_tripleshot.png");
            }

        } catch (Exception e) {
            System.err.println("Không thể load power-up sprite(s): " + e.getMessage());
            e.printStackTrace();
            this.sprite = null;
            this.shieldSprite = null;
            this.tripleShotSprite = null;
        }
    }

    /**
     * Lấy loại PowerUp hiện tại.
     * @return loại PowerUp
     */
    public PowerUpType getType() {
        return type;
    }

    /**
     * Cập nhật vị trí của PowerUp mỗi frame, di chuyển xuống dưới.
     * Nếu ra khỏi màn hình sẽ đánh dấu là dead.
     */
    @Override
    public void update() {
        y += SPEED;
        if (y - height / 2 > SpaceShooter.HEIGHT) {
            dead = true;
        }
    }

    /**
     * Vẽ PowerUp lên canvas.
     * Nếu có sprite tương ứng sẽ vẽ, không thì vẽ hình chữ nhật màu thay thế.
     * @param gc GraphicsContext của canvas game
     */
    @Override
    public void render(GraphicsContext gc) {
        Image currentSprite = null;
        Color fallbackColor = Color.GOLD;

        switch (type) {
            case SHIELD:
                currentSprite = shieldSprite;
                fallbackColor = Color.DEEPSKYBLUE;
                break;
            case TRIPLE_SHOT:
                currentSprite = tripleShotSprite;
                fallbackColor = Color.ORANGERED;
                break;
            case HEALTH_PACK:
            default:
                currentSprite = sprite;
                fallbackColor = Color.LIGHTGREEN;
                break;
        }

        if (currentSprite != null) {
            gc.drawImage(currentSprite, x - width / 2, y - height / 2, width, height);
        } else {
            gc.setFill(fallbackColor);
            gc.fillRect(x - width / 2, y - height / 2, width, height);
            gc.setStroke(Color.WHITE);
            gc.strokeRect(x - width / 2, y - height / 2, width, height);
        }
    }

    @Override
    public double getWidth() {
        return WIDTH;
    }

    @Override
    public double getHeight() {
        return HEIGHT;
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    @Override
    public void setDead(boolean dead) {
        this.dead = dead;
    }
}
