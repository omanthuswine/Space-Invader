package uet.oop.spaceshootergamejavafx.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Lớp Bullet đại diện cho một viên đạn trong trò chơi.
 * Đạn được bắn ra bởi Player hoặc Enemy và di chuyển theo một vận tốc nhất định.
 */
public class Bullet extends GameObject {

    /** Chiều rộng mặc định của viên đạn. */
    public static final int WIDTH = 4;
    /** Chiều cao mặc định của viên đạn. */
    public static final int HEIGHT = 15;
    /** Tốc độ mặc định của viên đạn (thường là đạn của Player bắn lên). */
    public static final double DEFAULT_SPEED = 5.0;

    private double vx; // Vận tốc theo trục X
    private double vy; // Vận tốc theo trục Y
    private boolean dead; // Trạng thái "chết" của viên đạn (đã va chạm hoặc ra khỏi màn hình)

    /**
     * Khởi tạo một viên đạn với vị trí và vận tốc tùy chỉnh.
     * Thường được sử dụng cho đạn của Enemy hoặc các loại đạn đặc biệt của Player.
     *
     * @param x  Vị trí ban đầu theo trục X (tâm của viên đạn).
     * @param y  Vị trí ban đầu theo trục Y (tâm của viên đạn).
     * @param vx Vận tốc theo trục X.
     * @param vy Vận tốc theo trục Y.
     */
    public Bullet(double x, double y, double vx, double vy) {
        super(x, y, WIDTH, HEIGHT); // Gọi constructor của lớp cha GameObject
        this.vx = vx;
        this.vy = vy;
        this.dead = false;
    }

    /**
     * Khởi tạo một viên đạn bắn thẳng lên từ một vị trí xác định.
     * Đây là constructor mặc định cho đạn của Player.
     *
     * @param x Vị trí ban đầu theo trục X (tâm của viên đạn).
     * @param y Vị trí ban đầu theo trục Y (tâm của viên đạn).
     */
    public Bullet(double x, double y) {
        this(x, y, 0, -DEFAULT_SPEED); // Mặc định vx = 0, vy = -DEFAULT_SPEED (bắn thẳng lên)
    }

    /**
     * Cập nhật trạng thái của viên đạn mỗi frame.
     * Di chuyển viên đạn và kiểm tra xem nó có ra khỏi màn hình không.
     */
    @Override
    public void update() {
        // Di chuyển viên đạn dựa trên vận tốc hiện tại
        x += vx;
        y += vy;

        // Kiểm tra nếu viên đạn bay ra khỏi biên màn hình
        // Viên đạn được coi là "chết" nếu hoàn toàn ra khỏi vùng nhìn thấy
        if (y + height / 2 < 0 ||                 // Ra khỏi mép trên
                y - height / 2 > SpaceShooter.HEIGHT || // Ra khỏi mép dưới
                x + width / 2 < 0 ||                  // Ra khỏi mép trái
                x - width / 2 > SpaceShooter.WIDTH) { // Ra khỏi mép phải
            this.dead = true;
        }
    }

    /**
     * Vẽ viên đạn lên màn hình.
     * Mặc định viên đạn của Player có màu vàng.
     * @param gc Đối tượng GraphicsContext để vẽ.
     */
    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.YELLOW);
        // Vẽ hình chữ nhật đại diện cho viên đạn, với x, y là tâm
        gc.fillRect(this.x - this.width / 2.0, this.y - this.height / 2.0, this.width, this.height);
    }

    /**
     * Lấy chiều rộng của viên đạn.
     * @return Chiều rộng của viên đạn.
     */
    @Override
    public double getWidth() {
        return this.width; // Trả về giá trị width đã được khởi tạo trong constructor của GameObject
    }

    /**
     * Lấy chiều cao của viên đạn.
     * @return Chiều cao của viên đạn.
     */
    @Override
    public double getHeight() {
        return this.height; // Trả về giá trị height đã được khởi tạo trong constructor của GameObject
    }

    /**
     * Đặt trạng thái "chết" cho viên đạn.
     * Khi một viên đạn được đánh dấu là "chết", nó sẽ được loại bỏ khỏi game ở lần cập nhật tiếp theo.
     * @param dead True nếu viên đạn đã chết, false nếu còn sống.
     */
    @Override
    public void setDead(boolean dead) {
        this.dead = dead;
    }

    /**
     * Kiểm tra xem viên đạn có đang ở trạng thái "chết" hay không.
     * @return True nếu viên đạn đã chết, false nếu còn sống.
     */
    @Override
    public boolean isDead() {
        return dead;
    }
}