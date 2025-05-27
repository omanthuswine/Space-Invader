package uet.oop.spaceshootergamejavafx.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Lớp BossEnemy đại diện cho một kẻ địch mạnh (Boss) trong game.
 * Boss có nhiều máu hơn, các kiểu di chuyển và tấn công phức tạp hơn so với Enemy thường.
 */
public class BossEnemy extends Enemy {

    private int health;
    private final int MAX_HEALTH;

    /** Chiều rộng mặc định của Boss. */
    public static final double WIDTH = 100;
    /** Chiều cao mặc định của Boss. */
    public static final double HEIGHT = 100;

    private Image sprite;

    // Các hằng số điều chỉnh hành vi và thuộc tính của Boss
    private static final double ADJ_BASE_HORIZONTAL_SPEED = 1.0;
    private static final double ADJ_BASE_VERTICAL_SPEED = 1.0;
    private static final double ADJ_CIRCULAR_ANGLE_INCREMENT = 0.025; // Tốc độ quay khi di chuyển tròn
    /** Khoảng thời gian (tính bằng frame/cập nhật) giữa các lần bắn của Boss. */
    protected static final int ADJ_SHOOT_INTERVAL = 100;
    /** Tốc độ của đạn do Boss bắn ra. */
    protected static final double ADJ_BOSS_BULLET_SPEED = 1.5;
    /** Số lượng đạn được bắn ra trong một loạt đạn theo hình tròn. */
    private static final int ADJ_BULLET_COUNT_CIRCULAR = 8;
    /** Thời gian (tính bằng frame/cập nhật) cho mỗi trạng thái di chuyển của Boss. */
    private final int STATE_DURATION = 300;

    // Các biến trạng thái di chuyển
    private double horizontalSpeed;
    private double verticalSpeed;
    private double angle; // Dùng cho di chuyển tròn
    private double currentCenterX_forCircular, currentCenterY_forCircular; // Tâm của quỹ đạo tròn
    private double radius = 70; // Bán kính của quỹ đạo tròn

    /**
     * Enum định nghĩa các trạng thái di chuyển khác nhau của Boss.
     */
    private enum MovementState {
        /** Di chuyển theo đường zigzag. */
        ZIGZAG,
        /** Di chuyển theo quỹ đạo tròn. */
        CIRCULAR
    }
    private MovementState state;
    private int stateTimer; // Bộ đếm thời gian cho trạng thái hiện tại

    private Random random;
    /** Danh sách các viên đạn do Boss này bắn ra. */
    protected List<EnemyBullet> bullets;
    /** Cooldown giữa các lần bắn. */
    protected int shootCooldown;

    private Player targetPlayer; // Mục tiêu của Boss (thường là người chơi)
    private boolean isStationary; // Boss có đứng yên hay không

    /**
     * Khởi tạo một đối tượng BossEnemy.
     *
     * @param x            Vị trí ban đầu theo trục X (tâm).
     * @param y            Vị trí ban đầu theo trục Y (tâm).
     * @param targetPlayer Đối tượng Player mà Boss sẽ nhắm tới.
     * @param isStationary True nếu Boss đứng yên, false nếu Boss di chuyển.
     */
    public BossEnemy(double x, double y, Player targetPlayer, boolean isStationary) {
        super(x, y); // Gọi constructor của lớp cha (Enemy)
        this.width = BossEnemy.WIDTH; // Ghi đè kích thước từ lớp cha
        this.height = BossEnemy.HEIGHT;

        this.MAX_HEALTH = 25; // Lượng máu tối đa của Boss
        this.health = this.MAX_HEALTH;

        this.targetPlayer = targetPlayer;
        this.isStationary = isStationary;

        this.angle = 0;
        this.random = new Random();
        this.bullets = new ArrayList<>();

        if (!this.isStationary) {
            this.state = MovementState.ZIGZAG; // Trạng thái di chuyển ban đầu
            this.horizontalSpeed = (random.nextBoolean() ? 1 : -1) * ADJ_BASE_HORIZONTAL_SPEED;
            this.verticalSpeed = (random.nextBoolean() ? 1 : -1) * ADJ_BASE_VERTICAL_SPEED;
        } else {
            this.state = null; // Boss đứng yên không có trạng thái di chuyển
            this.horizontalSpeed = 0;
            this.verticalSpeed = 0;
        }

        // Khởi tạo tâm cho quỹ đạo tròn ban đầu dựa trên vị trí xuất hiện
        this.currentCenterX_forCircular = x;
        this.currentCenterY_forCircular = y;

        this.stateTimer = 0;
        this.shootCooldown = ADJ_SHOOT_INTERVAL / 2; // Bắn lần đầu nhanh hơn một chút

        this.sprite = null;
        try {
            // Tải ảnh sprite cho Boss
            this.sprite = new Image(getClass().getResourceAsStream("/boss.gif"));
        } catch (Exception e) {
            System.err.println("Không thể load sprite cho Boss: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật trạng thái của Boss mỗi frame.
     * Bao gồm di chuyển, bắn đạn, và cập nhật đạn của Boss.
     */
    @Override
    public void update() {
        if (this.isDead()) { // Nếu Boss đã chết, không làm gì cả
            return;
        }

        // Cập nhật di chuyển nếu Boss không đứng yên
        if (!isStationary) {
            if (state != null) { // Đảm bảo state không null (trường hợp Boss đứng yên được khởi tạo với state null)
                switch (state) {
                    case ZIGZAG:
                        x += horizontalSpeed;
                        y += verticalSpeed;
                        // Đảo chiều khi chạm biên màn hình (hoặc một giới hạn nào đó)
                        if (x - width / 2 <= 0) { x = width / 2; horizontalSpeed = ADJ_BASE_HORIZONTAL_SPEED; }
                        else if (x + width / 2 >= SpaceShooter.WIDTH) { x = SpaceShooter.WIDTH - width / 2; horizontalSpeed = -ADJ_BASE_HORIZONTAL_SPEED; }
                        if (y - height / 2 <= 0) { y = height / 2; verticalSpeed = ADJ_BASE_VERTICAL_SPEED; }
                        else if (y + height / 2 >= SpaceShooter.HEIGHT / 1.8) { y = SpaceShooter.HEIGHT / 1.8 - height / 2; verticalSpeed = -ADJ_BASE_VERTICAL_SPEED; } // Giới hạn di chuyển xuống
                        break;
                    case CIRCULAR:
                        angle += ADJ_CIRCULAR_ANGLE_INCREMENT;
                        // Tâm của quỹ đạo tròn có thể di chuyển nhẹ theo vị trí hiện tại của Boss
                        currentCenterX_forCircular += (this.x - currentCenterX_forCircular) * 0.008;
                        currentCenterY_forCircular += (this.y - currentCenterY_forCircular) * 0.008;

                        double newX = currentCenterX_forCircular + radius * Math.cos(angle);
                        double newY = currentCenterY_forCircular + radius * Math.sin(angle);
                        // Giữ Boss trong màn hình khi di chuyển tròn
                        if (newX - width/2 < 0) newX = width/2; else if (newX + width/2 > SpaceShooter.WIDTH) newX = SpaceShooter.WIDTH - width/2;
                        if (newY - height/2 < 0) newY = height/2; else if (newY + height/2 > SpaceShooter.HEIGHT * 0.70) newY = SpaceShooter.HEIGHT * 0.70 - height/2; // Giới hạn di chuyển xuống
                        x = newX; y = newY;
                        break;
                }
            }
            // Chuyển trạng thái di chuyển sau một khoảng thời gian
            stateTimer++;
            if (stateTimer >= STATE_DURATION) {
                stateTimer = 0;
                switchStateRandomly();
            }
        }

        // Logic bắn đạn của Boss
        if (shootCooldown > 0) {
            shootCooldown--;
        } else {
            shootCircularPattern(); // Boss bắn theo kiểu hình tròn
            shootCooldown = ADJ_SHOOT_INTERVAL; // Reset cooldown
        }

        // Cập nhật và xóa đạn của Boss nếu chúng ra khỏi màn hình hoặc đã "chết"
        if (this.bullets != null) {
            Iterator<EnemyBullet> iter = bullets.iterator();
            while (iter.hasNext()) {
                EnemyBullet bullet = iter.next();
                bullet.update();
                if (bullet.isDead() || bullet.getY() > SpaceShooter.HEIGHT + bullet.getHeight() || bullet.getY() < -bullet.getHeight() * 2 ||
                        bullet.getX() > SpaceShooter.WIDTH + bullet.getWidth() || bullet.getX() < -bullet.getWidth() * 2) {
                    iter.remove();
                }
            }
        }

        // Kiểm tra nếu Boss di chuyển ra khỏi màn hình (phía dưới)
        if (!isStationary) {
            if (y + height / 2 < 0) y = -height / 2 + 1; // Đảm bảo không bị kẹt ở mép trên
            if (y - height / 2 > SpaceShooter.HEIGHT) {
                this.setDead(true); // Boss thoát khỏi màn hình
            }
        }
    }

    /**
     * Chuyển đổi trạng thái di chuyển của Boss một cách ngẫu nhiên.
     */
    private void switchStateRandomly() {
        if (isStationary || this.isDead()) return;

        MovementState newState;
        int availableStates = MovementState.values().length;
        // Đảm bảo có ít nhất một trạng thái và không chọn lại trạng thái hiện tại nếu có nhiều hơn một trạng thái
        if (availableStates == 0 || (availableStates == 1 && state != null)) return;
        do {
            newState = MovementState.values()[random.nextInt(availableStates)];
        } while (newState == state && availableStates > 1);

        state = newState;
        stateTimer = 0; // Reset bộ đếm thời gian cho trạng thái mới

        // Thiết lập các thông số ban đầu cho trạng thái mới
        if (state == MovementState.ZIGZAG) {
            horizontalSpeed = (random.nextBoolean() ? 1 : -1) * ADJ_BASE_HORIZONTAL_SPEED;
            verticalSpeed = (random.nextBoolean() ? 1 : -1) * ADJ_BASE_VERTICAL_SPEED;
        } else if (state == MovementState.CIRCULAR) {
            currentCenterX_forCircular = this.x; // Đặt tâm quỹ đạo tròn là vị trí hiện tại
            currentCenterY_forCircular = Math.min(this.y, SpaceShooter.HEIGHT / 2.2); // Giới hạn tâm Y để không quá thấp
            angle = random.nextDouble() * 2 * Math.PI; // Góc bắt đầu ngẫu nhiên
        }
    }

    /**
     * Thực hiện hành động bắn đạn theo một pattern hình tròn.
     * Một trong số các viên đạn sẽ nhắm vào người chơi (nếu có).
     */
    protected void shootCircularPattern() {
        if (this.isDead()) return;

        double spawnBulletCenterX = this.x;
        double spawnBulletCenterY = this.y;
        Color salvoColor; // Màu cho loạt đạn này

        // Chọn màu ngẫu nhiên cho loạt đạn
        int colorChoice = random.nextInt(3);
        switch (colorChoice) {
            case 0: salvoColor = Color.LIMEGREEN; break;
            case 1: salvoColor = Color.RED; break;
            default: salvoColor = Color.DEEPSKYBLUE;
        }

        // Chọn một viên đạn ngẫu nhiên trong loạt để nhắm vào người chơi
        int aimedBulletIndex = random.nextInt(ADJ_BULLET_COUNT_CIRCULAR);

        for (int i = 0; i < ADJ_BULLET_COUNT_CIRCULAR; i++) {
            double speedX, speedY;
            if (i == aimedBulletIndex && targetPlayer != null && !targetPlayer.isDead()) {
                // Tính toán vector vận tốc để nhắm vào người chơi
                double playerX = targetPlayer.getX();
                double playerY = targetPlayer.getY();
                double deltaX = playerX - spawnBulletCenterX;
                double deltaY = playerY - spawnBulletCenterY;
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                if (distance < 1.0) { // Tránh chia cho 0 nếu Boss ở ngay trên Player
                    // Nếu quá gần, bắn theo góc mặc định của pattern tròn
                    double angleRadFallback = (2 * Math.PI / ADJ_BULLET_COUNT_CIRCULAR) * i;
                    speedX = Math.cos(angleRadFallback) * ADJ_BOSS_BULLET_SPEED;
                    speedY = Math.sin(angleRadFallback) * ADJ_BOSS_BULLET_SPEED;
                } else {
                    speedX = (deltaX / distance) * ADJ_BOSS_BULLET_SPEED;
                    speedY = (deltaY / distance) * ADJ_BOSS_BULLET_SPEED;
                }
            } else {
                // Bắn theo góc cố định của pattern tròn
                double angleRad = (2 * Math.PI / ADJ_BULLET_COUNT_CIRCULAR) * i;
                speedX = Math.cos(angleRad) * ADJ_BOSS_BULLET_SPEED;
                speedY = Math.sin(angleRad) * ADJ_BOSS_BULLET_SPEED;
            }
            EnemyBullet bullet = new EnemyBullet(spawnBulletCenterX, spawnBulletCenterY, speedX, speedY, salvoColor, true); // true = isBossBullet
            this.bullets.add(bullet);
        }
    }

    /**
     * Vẽ Boss và thanh máu của nó lên màn hình.
     * @param gc Đối tượng GraphicsContext để vẽ.
     */
    @Override
    public void render(GraphicsContext gc) {
        if (isDead()) { // Nếu Boss đã chết, không vẽ gì cả
            return;
        }

        // Vẽ sprite của Boss
        if (sprite != null) {
            gc.drawImage(sprite, x - width/2, y - height/2, width, height);
        } else {
            // Vẽ hình chữ nhật màu tím đậm thay thế nếu không load được sprite
            gc.setFill(Color.rgb(128, 0, 128, 0.8));
            gc.fillRect(x - width/2, y - height/2, width, height);
        }

        // Vẽ thanh máu của Boss (chỉ khi Boss còn sống và ở trong tầm nhìn)
        if (this.y > -this.height/2 && this.health > 0) {
            double healthBarActualWidth = this.width * 0.8; // Chiều rộng thực tế của thanh máu
            double healthBarHeight = 10;
            double healthBarOffsetX = (this.width - healthBarActualWidth) / 2; // Căn giữa thanh máu
            double healthBarOffsetY = 15; // Khoảng cách từ mép trên của Boss lên phía trên thanh máu
            double healthBarX_abs = (this.x - this.width / 2) + healthBarOffsetX;
            double healthBarY_abs = (this.y - this.height / 2) - healthBarOffsetY - healthBarHeight; // Vẽ phía trên Boss

            // Vẽ nền thanh máu (màu xám đậm)
            gc.setFill(Color.rgb(80, 80, 80, 0.9));
            gc.fillRect(healthBarX_abs, healthBarY_abs, healthBarActualWidth, healthBarHeight);

            // Vẽ phần máu hiện tại
            double currentHealthPercentage = (double)this.health / MAX_HEALTH;
            if (currentHealthPercentage < 0) currentHealthPercentage = 0; // Đảm bảo không âm
            double currentHealthWidth = currentHealthPercentage * healthBarActualWidth;

            // Chọn màu cho thanh máu dựa trên phần trăm máu còn lại
            if (currentHealthPercentage > 0.6) gc.setFill(Color.LIMEGREEN);
            else if (currentHealthPercentage > 0.3) gc.setFill(Color.YELLOW);
            else gc.setFill(Color.RED);
            gc.fillRect(healthBarX_abs, healthBarY_abs, currentHealthWidth, healthBarHeight);

            // Vẽ viền cho thanh máu
            gc.setStroke(Color.rgb(200,200,200,0.9));
            gc.setLineWidth(1.5);
            gc.strokeRect(healthBarX_abs, healthBarY_abs, healthBarActualWidth, healthBarHeight);
        }

        // Vẽ đạn của Boss
        if (this.bullets != null) {
            for (EnemyBullet bullet : bullets) {
                bullet.render(gc);
            }
        }
    }

    /**
     * Giảm máu của Boss khi bị trúng đạn.
     * Nếu máu về 0 hoặc thấp hơn, Boss sẽ bị đánh dấu là đã chết.
     * @param amount Lượng sát thương gây ra.
     */
    public void reduceHealth(int amount) {
        if (this.isDead() || this.health <= 0) return; // Không giảm máu nếu đã chết hoặc máu đã hết

        this.health -= amount;
        if (this.health <= 0) {
            this.health = 0; // Đảm bảo máu không âm
            this.setDead(true); // Đánh dấu là chết ngay lập tức
            // System.out.println("DEBUG: Boss ID " + this.hashCode() + " health depleted. Set dead. Clearing its bullets.");

            // Dừng các hành động khác của Boss khi chết
            this.shootCooldown = Integer.MAX_VALUE; // Ngăn Boss bắn thêm

            // Xóa tất cả đạn hiện tại của Boss để tránh đạn "ma"
            if(this.bullets != null) {
                this.bullets.clear();
            }
            // Việc xóa Boss khỏi danh sách gameObjects và cộng điểm sẽ được xử lý bởi lớp SpaceShooter.
        }
    }

    /**
     * Lấy chiều rộng của Boss.
     * @return Chiều rộng của Boss.
     */
    @Override public double getWidth() { return BossEnemy.WIDTH; }

    /**
     * Lấy chiều cao của Boss.
     * @return Chiều cao của Boss.
     */
    @Override public double getHeight() { return BossEnemy.HEIGHT; }

    /**
     * Lấy lượng máu hiện tại của Boss.
     * @return Lượng máu hiện tại.
     */
    public int getHealth() { return health; }

    /**
     * Lấy danh sách các viên đạn đang hoạt động của Boss.
     * @return Danh sách các đối tượng EnemyBullet.
     */
    public List<EnemyBullet> getBullets() { return this.bullets; }
    // Phương thức isDead() và setDead(boolean) được kế thừa từ lớp Enemy (GameObject).
}