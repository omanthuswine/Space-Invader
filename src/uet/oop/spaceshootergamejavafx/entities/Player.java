package uet.oop.spaceshootergamejavafx.entities;

import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import java.util.List;
import java.net.URL;

/**
 * Player class đại diện cho người chơi điều khiển được trong game.
 * Bao gồm các trạng thái power-up, logic di chuyển, bắn đạn và render.
 */
public class Player extends GameObject {

    /** Chiều rộng và chiều cao cố định của Player */
    public static final int WIDTH = 50; // [7]
    /** Chiều cao cố định của Player */
    public static final int HEIGHT = 50; // [7]

    /** Tốc độ di chuyển của Player */
    static final double SPEED = 3; // [7]

    /** Ảnh đại diện của Player */
    private Image sprite; // [7]

    /** Trạng thái di chuyển */
    private boolean moveLeft, moveRight, moveForward, moveBackward; // [7]

    /** Trạng thái sống/chết của Player */
    private boolean dead; // [7]

    /** Âm thanh khi bắn */
    private AudioClip shootSound; // [7]

    /** Trạng thái power-up Khiên */
    private boolean shieldActive; // [7]
    private long shieldActivationTime; // [7]
    private static final long SHIELD_DURATION_MS = 4000; // [7]

    /** Trạng thái power-up Triple Shot */
    private boolean tripleShotActive; // [7]
    private long tripleShotActivationTime; // [7]
    private static final long TRIPLE_SHOT_DURATION_MS = 7000; // [7]

    /** Trạng thái bất tử tạm thời */
    private boolean tempInvincible; // [7]
    private long tempInvincibilityStartTime; // [7]
    private static final long TEMP_INVINCIBILITY_DURATION_MS = 2000; // [7]

    /** Cờ cho biết Player đang muốn bắn */
    private boolean wantsToShoot; // [7]

    private boolean forceSingleShotByAI = false; // [1]

    /**
     * Khởi tạo Player với vị trí ban đầu.
     * @param x hoành độ tâm Player
     * @param y tung độ tâm Player
     */
    public Player(double x, double y) {
        super(x, y, WIDTH, HEIGHT); // [7]
        this.dead = false; // [7]

        // Load sprite hình ảnh player
        try {
            this.sprite = new Image(getClass().getResourceAsStream("/player.png")); // [7]
        } catch (Exception e) {
            System.err.println("Không thể load player sprite: " + e.getMessage()); // [7]
            this.sprite = null; // [7]
        }

        // Load âm thanh bắn
        try {
            URL soundURL = getClass().getResource("/player_shoot.wav"); // [7]
            if (soundURL != null) { // [7]
                shootSound = new AudioClip(soundURL.toExternalForm()); // [7]
            } else {
                System.err.println("Không tìm thấy file âm thanh bắn!"); // [7]
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi load âm thanh bắn: " + e.getMessage()); // [7]
        }
    }

    /** Kích hoạt power-up Khiên */
    public void activateShield() {
        this.shieldActive = true; // [7]
        this.shieldActivationTime = System.currentTimeMillis(); // [7]
        this.tempInvincible = false; // [7]
        System.out.println("Player: Shield Activated!"); // [7]
    }

    /** Kích hoạt power-up Triple Shot */
    public void activateTripleShot() {
        this.tripleShotActive = true; // [7]
        this.tripleShotActivationTime = System.currentTimeMillis(); // [7]
        System.out.println("Player: Triple Shot Activated!"); // [7]
    }

    /** Kích hoạt trạng thái bất tử tạm thời (khi hồi sinh) */
    public void activateTempInvincibility() {
        if (!this.shieldActive) { // [7]
            this.tempInvincible = true; // [7]
            this.tempInvincibilityStartTime = System.currentTimeMillis(); // [7]
            System.out.println("Player: Temp Invincibility Activated!"); // [7]
        }
    }

    public boolean isShieldActive() { return shieldActive; } // [7]
    public boolean isTempInvincible() { return tempInvincible; } // [7]
    public void setWantsToShoot(boolean wants) { this.wantsToShoot = wants; } // [7]
    public boolean getWantsToShoot() { return this.wantsToShoot; } // [7]

    // Getter cho trạng thái di chuyển — hỗ trợ AI hoặc UI điều khiển
    public boolean isMoveLeftSet() { return moveLeft; } // [7]
    public boolean isMoveRightSet() { return moveRight; } // [7]
    public boolean isMoveForwardSet() { return moveForward; } // [7]
    public boolean isMoveBackwardSet() { return moveBackward; } // [7]

    public void setForceSingleShotByAI(boolean force) { // [1]
        this.forceSingleShotByAI = force; // [1]
    } // [1]

    @Override
    public double getWidth() { return WIDTH; } // [7]

    @Override
    public double getHeight() { return HEIGHT; } // [7]

    /**
     * Cập nhật trạng thái power-up, bất tử và vị trí mỗi frame.
     */
    @Override
    public void update() {
        // Hết hạn power-up Khiên
        if (shieldActive && System.currentTimeMillis() - shieldActivationTime > SHIELD_DURATION_MS) { // [7]
            shieldActive = false; // [7]
            System.out.println("Player: Shield Deactivated."); // [7]
        }

        // Hết hạn Triple Shot
        if (tripleShotActive && System.currentTimeMillis() - tripleShotActivationTime > TRIPLE_SHOT_DURATION_MS) { // [7]
            tripleShotActive = false; // [7]
            System.out.println("Player: Triple Shot Deactivated."); // [7]
        }

        // Hết bất tử tạm thời
        if (tempInvincible && System.currentTimeMillis() - tempInvincibilityStartTime > TEMP_INVINCIBILITY_DURATION_MS) { // [7]
            tempInvincible = false; // [7]
            System.out.println("Player: Temp Invincibility Deactivated."); // [7]
        }

        // Di chuyển — kiểm tra giới hạn biên màn hình
        double halfWidth = this.width / 2.0; // [7]
        double halfHeight = this.height / 2.0; // [7]

        if (moveLeft) this.x = Math.max(halfWidth, this.x - SPEED); // [7]
        if (moveRight) this.x = Math.min(SpaceShooter.WIDTH - halfWidth, this.x + SPEED); // [7]
        if (moveForward) this.y = Math.max(halfHeight, this.y - SPEED); // [7]
        if (moveBackward) this.y = Math.min(SpaceShooter.HEIGHT - halfHeight, this.y + SPEED); // [7]
    }

    /**
     * Vẽ player, hiệu ứng khiên và hiệu ứng nhấp nháy nếu bất tử.
     * @param gc GraphicsContext của canvas game
     */
    @Override
    public void render(GraphicsContext gc) {
        boolean shouldDrawPlayer = true; // [7]

        // Hiệu ứng nhấp nháy nếu bất tử tạm thời và không có khiên
        if (tempInvincible && !shieldActive) { // [7]
            long timeSinceInvincible = System.currentTimeMillis() - tempInvincibilityStartTime; // [7]
            if ((timeSinceInvincible / 150) % 2 != 0) shouldDrawPlayer = false; // [7]
        }

        if (shouldDrawPlayer) { // [7]
            if (sprite != null) { // [7]
                gc.drawImage(sprite, this.x - this.width / 2, this.y - this.height / 2, this.width, this.height); // [7]
            } else {
                gc.setFill(Color.BLUE); // [7]
                gc.fillRect(this.x - this.width / 2, this.y - this.height / 2, this.width, this.height); // [7]
            }
        }

        // Vẽ viền khiên nếu đang bật
        if (shieldActive) { // [7]
            gc.setStroke(Color.CYAN); // [7]
            gc.setLineWidth(2.5); // [7]
            double shieldPadding = 8; // [7]
            gc.strokeOval(this.x - this.width / 2 - shieldPadding / 2, // [7]
                    this.y - this.height / 2 - shieldPadding / 2, // [7]
                    this.width + shieldPadding, this.height + shieldPadding); // [7]
        }
    }

    // Setter di chuyển
    public void setMoveLeft(boolean moveLeft) { this.moveLeft = moveLeft; } // [7]
    public void setMoveRight(boolean moveRight) { this.moveRight = moveRight; } // [7]
    public void setMoveForward(boolean moveForward) { this.moveForward = moveForward; } // [7]
    public void setMoveBackward(boolean moveBackward) { this.moveBackward = moveBackward; } // [7]

    /**
     * Bắn đạn: nếu có power-up Triple Shot thì bắn 3, không thì bắn 1.
     * @param newObjects danh sách object mới cần thêm vào game loop
     */
    public void shoot(List<GameObject> newObjects) {
        double bulletSpawnX = this.x; // [7]
        double playerTopEdgeY = this.y - (this.height / 2.0); // [7]
        double bulletCenterY = playerTopEdgeY - (Bullet.HEIGHT / 2.0); // [3, 7]

        boolean currentShotIsTriple = this.tripleShotActive; // [1, 7]
        if (this.forceSingleShotByAI) { // [1]
            currentShotIsTriple = false; // [1]
            this.forceSingleShotByAI = false; // [1]
        } // [1]

        if (currentShotIsTriple) { // [7]
            double spreadAngle = 0.25; // [7]
            double bulletSpeed = Bullet.DEFAULT_SPEED; // [3, 7]

            // Tia giữa
            newObjects.add(new Bullet(bulletSpawnX, bulletCenterY, 0, -bulletSpeed)); // [3, 7]
            // Tia trái
            newObjects.add(new Bullet(bulletSpawnX, bulletCenterY, -Math.sin(spreadAngle) * bulletSpeed, -Math.cos(spreadAngle) * bulletSpeed)); // [3, 7]
            // Tia phải
            newObjects.add(new Bullet(bulletSpawnX, bulletCenterY, Math.sin(spreadAngle) * bulletSpeed, -Math.cos(spreadAngle) * bulletSpeed)); // [3, 7]
        } else {
            newObjects.add(new Bullet(bulletSpawnX, bulletCenterY)); // [3, 7]
        }

        if (shootSound != null) { // [7]
            shootSound.setVolume(0.3); // [7]
            shootSound.play(); // [7]
        }
    }

    @Override
    public void setDead(boolean dead) { this.dead = dead; } // [7]

    @Override
    public boolean isDead() { return dead; } // [7]

    /** Reset toàn bộ cờ di chuyển */
    public void resetMovementFlags() {
        this.moveLeft = false; // [7]
        this.moveRight = false; // [7]
        this.moveForward = false; // [7]
        this.moveBackward = false; // [7]
    }

    // PHƯƠNG THỨC MỚI ĐỂ DỊCH CHUYỂN TỨC THỜI
    /**
     * Dịch chuyển Player đến một vị trí mới ngay lập tức.
     * Vị trí sẽ được giới hạn trong biên màn hình.
     * @param newX Hoành độ mới
     * @param newY Tung độ mới
     */
    public void teleportTo(double newX, double newY) {
        // Đảm bảo vị trí mới không vượt ra ngoài biên màn hình
        // Giả sử this.x và this.y là tâm của Player
        double halfWidth = this.getWidth() / 2.0;
        double halfHeight = this.getHeight() / 2.0;

        this.x = Math.max(halfWidth, Math.min(SpaceShooter.WIDTH - halfWidth, newX));
        this.y = Math.max(halfHeight, Math.min(SpaceShooter.HEIGHT - halfHeight, newY));
    }
}