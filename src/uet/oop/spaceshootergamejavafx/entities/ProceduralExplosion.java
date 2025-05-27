package uet.oop.spaceshootergamejavafx.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lớp ProceduralExplosion tạo hiệu ứng vụ nổ với các hạt (particles) di chuyển và mờ dần theo thời gian.
 * Vụ nổ kéo dài trong khoảng thời gian nhất định và tự động kết thúc.
 */
public abstract class ProceduralExplosion extends GameObject {

    private List<Particle> particles;
    private boolean animationFinished = false;
    private long startTime;
    private final long DURATION_MS; // Thời gian tồn tại của vụ nổ (ms)

    private static final int NUM_PARTICLES = 30; // Số lượng hạt
    private static final double MAX_PARTICLE_SPEED = 2.5;
    private static final double MIN_PARTICLE_SPEED = 0.5;
    private static final double PARTICLE_DECELERATION = 0.03; // Tỷ lệ giảm tốc hạt
    private static final int MAX_PARTICLE_SIZE = 8;
    private static final int MIN_PARTICLE_SIZE = 2;

    // Mảng màu sắc dùng cho các hạt vụ nổ
    private static final Color[] EXPLOSION_COLORS = {
            Color.YELLOW, Color.ORANGE, Color.RED, Color.DARKRED
    };

    private Random random = new Random();
    private boolean dead;

    /**
     * Tạo một vụ nổ tại tọa độ (x, y) với thời lượng chỉ định.
     * @param x hoành độ tâm vụ nổ
     * @param y tung độ tâm vụ nổ
     * @param durationMs thời gian tồn tại vụ nổ (millisecond)
     */
    public ProceduralExplosion(double x, double y, long durationMs) {
        super(x, y, 0, 0); // Kích thước không xác định cụ thể do phụ thuộc các hạt
        this.DURATION_MS = durationMs;
        this.startTime = System.currentTimeMillis();
        this.particles = new ArrayList<>();
        createParticles();
    }

    /**
     * Khởi tạo danh sách các hạt với vị trí, vận tốc, kích thước, màu sắc ngẫu nhiên.
     */
    private void createParticles() {
        for (int i = 0; i < NUM_PARTICLES; i++) {
            double angle = random.nextDouble() * 2 * Math.PI; // Góc di chuyển ngẫu nhiên
            double speed = MIN_PARTICLE_SPEED + random.nextDouble() * (MAX_PARTICLE_SPEED - MIN_PARTICLE_SPEED);
            double velocityX = Math.cos(angle) * speed;
            double velocityY = Math.sin(angle) * speed;
            int size = MIN_PARTICLE_SIZE + random.nextInt(MAX_PARTICLE_SIZE - MIN_PARTICLE_SIZE + 1);
            Color color = EXPLOSION_COLORS[random.nextInt(EXPLOSION_COLORS.length)];

            particles.add(new Particle(this.x, this.y, velocityX, velocityY, size, color));
        }
    }

    /**
     * Cập nhật trạng thái vụ nổ.
     * Khi thời gian vượt quá DURATION_MS sẽ đánh dấu vụ nổ kết thúc và chết.
     */
    @Override
    public void update() {
        if (isDead()) {
            return;
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime > DURATION_MS) {
            animationFinished = true;
            this.setDead(true);
            return;
        }

        for (Particle p : particles) {
            p.update(PARTICLE_DECELERATION, elapsedTime, DURATION_MS);
        }
    }

    /**
     * Vẽ vụ nổ lên canvas.
     * Bỏ qua nếu vụ nổ đã chết hoặc kết thúc animation.
     * @param gc GraphicsContext của canvas
     */
    @Override
    public void render(GraphicsContext gc) {
        if (isDead() || animationFinished) {
            return;
        }
        for (Particle p : particles) {
            p.render(gc);
        }
    }

    /**
     * Lớp nội bộ đại diện cho một hạt của vụ nổ.
     */
    private static class Particle {
        double x, y;
        double velX, velY;
        double size;
        Color baseColor;
        double initialSize;

        /**
         * Khởi tạo hạt với vị trí, vận tốc, kích thước và màu sắc ban đầu.
         * @param x vị trí x
         * @param y vị trí y
         * @param velX vận tốc theo trục x
         * @param velY vận tốc theo trục y
         * @param size kích thước ban đầu
         * @param color màu sắc
         */
        public Particle(double x, double y, double velX, double velY, double size, Color color) {
            this.x = x;
            this.y = y;
            this.velX = velX;
            this.velY = velY;
            this.size = size;
            this.initialSize = size;
            this.baseColor = color;
        }

        /**
         * Cập nhật vị trí, giảm vận tốc và giảm kích thước dựa trên thời gian trôi qua.
         * @param deceleration tỉ lệ giảm vận tốc mỗi lần update
         * @param elapsedTime thời gian đã trôi qua kể từ khi vụ nổ bắt đầu
         * @param totalDuration tổng thời gian tồn tại vụ nổ
         */
        public void update(double deceleration, long elapsedTime, long totalDuration) {
            x += velX;
            y += velY;

            velX *= (1 - deceleration);
            velY *= (1 - deceleration);

            double lifeRatio = (double) elapsedTime / totalDuration;
            size = initialSize * (1 - lifeRatio);
            if (size < 0) size = 0;
        }

        /**
         * Vẽ hạt lên canvas dưới dạng hình tròn với độ trong suốt theo kích thước.
         * @param gc GraphicsContext của canvas
         */
        public void render(GraphicsContext gc) {
            if (size <= 0) return;

            double alpha = 1.0 - (size / initialSize);
            if(alpha < 0) alpha = 0;
            if(alpha > 1) alpha = 1;

            gc.setFill(Color.rgb(
                    (int)(baseColor.getRed() * 255),
                    (int)(baseColor.getGreen() * 255),
                    (int)(baseColor.getBlue() * 255),
                    alpha));
            gc.fillOval(x - size / 2, y - size / 2, size, size);
        }
    }
}
