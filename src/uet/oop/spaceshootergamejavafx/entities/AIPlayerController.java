package uet.oop.spaceshootergamejavafx.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * AIPlayerController chịu trách nhiệm điều khiển tàu của người chơi (Player)
 * bằng trí tuệ nhân tạo. Nó phân tích trạng thái game, đưa ra quyết định
 * di chuyển, né đạn, tấn công kẻ địch và thu thập vật phẩm.
 */
public class AIPlayerController {

    /** Đối tượng Player mà AI này điều khiển. */
    private Player player;
    /** Danh sách các đối tượng trong tầm nhìn của AI (trạng thái game hiện tại). */
    private List<GameObject> gameObjectsView;
    /** Đối tượng sinh số ngẫu nhiên cho các quyết định có tính ngẫu nhiên. */
    private Random random = new Random();

    /** Viên đạn của địch được coi là nguy hiểm nhất hiện tại. */
    private EnemyBullet threateningBullet = null;
    /** Thời gian ước tính (TTI - Time To Impact) thực tế của viên đạn nguy hiểm nhất. */
    private double threateningBulletActualTTI = Double.MAX_VALUE;
    /** Đối tượng địch (thân tàu) đang gây ra mối đe dọa va chạm trực tiếp. */
    private GameObject threateningEnemyBody = null;
    /** Kẻ địch hiện tại mà AI đang nhắm mục tiêu để tấn công. */
    private GameObject currentTargetEnemy = null;
    /** Vật phẩm hiện tại mà AI đang nhắm mục tiêu để thu thập. */
    private PowerUp targetPowerUp = null;

    /** Thời điểm AI đưa ra quyết định bắn lần cuối (dùng cho độ trễ sau khi bắn). */
    private long lastAIShotDecisionTime = 0;
    /** Độ trễ tối thiểu (ms) mà AI sẽ tạm dừng các quyết định khác sau khi quyết định bắn. Giúp AI không hành động quá dồn dập ngay sau khi bắn. */
    private final long POST_AI_SHOT_DECISION_PAUSE_MS = 1; // Phản ứng sau khi bắn gần như tức thời
    /** Thời điểm AI cố gắng bắn lần cuối (dùng cho cooldown giữa các lần bắn). */
    private long lastAITriedToShootTime = 0;
    /** Cooldown tối thiểu (ms) giữa các lần AI cố gắng bắn. */
    private final long AI_ATTEMPT_SHOOT_COOLDOWN_MS = 20; // AI có thể cố gắng bắn cực nhanh (0.02 giây/lần)

    /** Lưu trữ thời điểm AI bắn vào các kẻ địch thường, để tránh bắn liên tục vào một mục tiêu đã xử lý. */
    private Map<GameObject, Long> recentlyShotNormalEnemiesTimeStamps = new HashMap<>();
    /** Thời gian (ms) mà một kẻ địch thường sẽ bị bỏ qua sau khi AI bắn vào nó. */
    private final long NORMAL_ENEMY_SHOT_EXPIRY_MS = 150; // Bỏ qua kẻ địch thường trong thời gian rất ngắn

    /** Ngưỡng khoảng cách để xem xét va chạm thân tàu với địch. */
    private final double ENEMY_PROXIMITY_THRESHOLD = 55.0;

    /** Vị trí Y ưa thích mà AI cố gắng duy trì ở cuối màn hình. */
    private final double AI_PREFERRED_BOTTOM_Y_POSITION;
    /** Ngưỡng sai lệch cho phép so với vị trí Y ưa thích. */
    private final double AI_Y_POSITION_TOLERANCE = 5.0;
    /** Hệ số xác định ngưỡng Y cao cho vật phẩm (ví dụ: 50% chiều cao màn hình). */
    private final double POWERUP_HIGH_Y_THRESHOLD_FACTOR = 0.50;
    /** Điểm phạt khi vật phẩm ở vị trí quá cao. */
    private final int POWERUP_HIGH_PENALTY = 700;

    /**
     * Chân trời dự đoán tối thiểu (giây) để AI xem xét một viên đạn là nguy hiểm.
     * Cũng được sử dụng làm chân trời an toàn cho các di chuyển chiến thuật.
     */
    private final double DODGE_BULLET_MIN_REACTION_WINDOW_SEC = 1.5; // Tăng tầm nhìn xa để né đạn và di chuyển chiến thuật
    /** Chân trời dự đoán an toàn (giây) cho các di chuyển chiến thuật (căn bắn, lấy powerup, né thân địch). */
    private final double TACTICAL_MOVE_SAFE_HORIZON = DODGE_BULLET_MIN_REACTION_WINDOW_SEC;

    /** Ngưỡng TTI (giây) để AI thực hiện né tránh khẩn cấp đối với một viên đạn. */
    private final double DODGE_IMMEDIATE_DODGE_THRESHOLD_SEC = 0.028; // Né tránh trở nên cực kỳ khẩn cấp chỉ khi TTI rất gần
    /** Vùng đệm theo trục X khi phát hiện đạn có khả năng trúng người chơi. */
    private final double DODGE_PLAYER_X_DETECTION_BUFFER = 10.0;

    /** Hệ số dung sai khi căn chỉnh theo trục X với mục tiêu. */
    private final double ALIGNMENT_TOLERANCE_FACTOR = 0.75;
    /** Tốc độ di chuyển ngang giả định của người chơi (để tính toán né tránh). */
    private final double ASSUMED_PLAYER_HORIZONTAL_SPEED = Player.SPEED;

    /** Khoảng cách an toàn tối thiểu theo trục Y phía trên người chơi. */
    private final double SAFE_Y_DISTANCE_ABOVE_PLAYER;
    /** Khoảng cách an toàn tối thiểu theo trục Y phía dưới người chơi. */
    private final double SAFE_Y_DISTANCE_BELOW_PLAYER;
    /** Khoảng cách né tránh chiến lược tiêu chuẩn khi né đạn hoặc thân địch. */
    private final double STRATEGIC_DODGE_DISTANCE;

    /** Bán kính vùng an toàn mà AI cố gắng duy trì không có đạn địch. */
    private final double MAINTAIN_SAFE_ZONE_RADIUS;
    /** Chân trời dự đoán (giây) cho việc duy trì an toàn vị trí khi AI không có hành động cụ thể (ví dụ: di chuyển ngẫu nhiên). */
    private final double MAINTAIN_SAFE_ZONE_HORIZON;
    /** Khoảng cách AI dịch chuyển khi thực hiện di chuyển ngẫu nhiên để đổi vị trí. */
    private final double REPOSITION_SHIFT_DISTANCE;

    /** Bán kính vùng an toàn xung quanh vị trí AI khi bắn. */
    private final double FIRING_POSITION_SAFE_RADIUS = 80.0;
    /** Thời gian phản ứng (giây) khi kiểm tra an toàn vị trí bắn (gần như tức thời). */
    private final double FIRING_POSITION_REACTION_TIME = 0.00001;


    /**
     * Khởi tạo AIPlayerController.
     * @param player Đối tượng Player mà AI sẽ điều khiển.
     * @throws IllegalArgumentException nếu player là null.
     */
    public AIPlayerController(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player object cannot be null in AIPlayerController constructor");
        }
        this.player = player;
        this.AI_PREFERRED_BOTTOM_Y_POSITION = SpaceShooter.HEIGHT * 0.92 - (this.player.getHeight() / 2);
        this.SAFE_Y_DISTANCE_ABOVE_PLAYER = this.player.getHeight() * 1.2;
        this.SAFE_Y_DISTANCE_BELOW_PLAYER = this.player.getHeight() * 0.8;
        this.STRATEGIC_DODGE_DISTANCE = this.player.getWidth() * 0.85;

        this.MAINTAIN_SAFE_ZONE_RADIUS = 80.0;
        this.MAINTAIN_SAFE_ZONE_HORIZON = 0.4;
        this.REPOSITION_SHIFT_DISTANCE = player.getWidth() * 0.4;
    }

    /**
     * Cập nhật logic của AI dựa trên trạng thái game hiện tại.
     * Đây là phương thức chính điều khiển hành vi của AI mỗi frame.
     * @param currentGameState Danh sách các đối tượng GameObject hiện có trong game.
     */
    public void updateAI(List<GameObject> currentGameState) {
        this.gameObjectsView = new ArrayList<>(currentGameState);
        player.resetMovementFlags(); // Reset các cờ di chuyển của player ở đầu mỗi lượt cập nhật AI

        // Dọn dẹp danh sách kẻ địch thường đã bị bắn gần đây
        long currentTimeForCleanup = System.currentTimeMillis();
        Iterator<Map.Entry<GameObject, Long>> cleanupIterator = recentlyShotNormalEnemiesTimeStamps.entrySet().iterator();
        while (cleanupIterator.hasNext()) {
            Map.Entry<GameObject, Long> entry = cleanupIterator.next();
            GameObject enemy = entry.getKey();
            Long shotTime = entry.getValue();
            // Loại bỏ nếu kẻ địch đã chết hoặc đã hết thời gian bỏ qua
            if (enemy.isDead() || currentTimeForCleanup - shotTime > NORMAL_ENEMY_SHOT_EXPIRY_MS) {
                cleanupIterator.remove();
            }
        }

        // Đánh giá các mối đe dọa từ đạn và thân địch
        assessThreateningBullets();
        assessProximityThreats();

        // Ưu tiên hàng đầu: Nếu vị trí hiện tại không an toàn trong 1.5 giây tới, thực hiện né tránh
        if (!isCurrentPositionSafeAndClear(AI_PREFERRED_BOTTOM_Y_POSITION, DODGE_BULLET_MIN_REACTION_WINDOW_SEC)) {
            performStrategicDodge(); // Có thể bao gồm cả dịch chuyển tức thời nếu cần
            adjustYToPreferredPosition(); // Điều chỉnh lại trục Y sau khi né/dịch chuyển
            return; // Kết thúc lượt cập nhật AI để xử lý frame mới với trạng thái mới
        }

        // Nếu AI vừa mới quyết định bắn, tạm dừng một chút (rất ngắn)
        if (System.currentTimeMillis() - lastAIShotDecisionTime < POST_AI_SHOT_DECISION_PAUSE_MS) {
            player.setWantsToShoot(false); // Không muốn bắn trong lúc tạm dừng này
            // Trong lúc tạm dừng, vẫn ưu tiên né nếu có mối đe dọa cực kỳ khẩn cấp
            if (threateningBullet != null && threateningBulletActualTTI < DODGE_IMMEDIATE_DODGE_THRESHOLD_SEC * 1.1) {
                performStrategicDodge();
            } else if (threateningEnemyBody != null) {
                performEnemyBodyDodge(threateningEnemyBody);
            }
            adjustYToPreferredPosition();
            return;
        }
        player.setWantsToShoot(false); // Đảm bảo không bắn nếu không có quyết định bắn mới

        // Xử lý né va chạm thân địch nếu có và AI không đang di chuyển né đạn
        if (threateningEnemyBody != null && !(player.isMoveLeftSet() || player.isMoveRightSet())) {
            performEnemyBodyDodge(threateningEnemyBody);
            if(player.isMoveLeftSet() || player.isMoveRightSet()){ // Nếu đã thực hiện né thân địch
                adjustYToPreferredPosition();
                return;
            }
        }

        // Tìm mục tiêu (kẻ địch hoặc vật phẩm) và thực hiện hành động
        findBestEnemyTargetToAttack();
        boolean shotThisFrame = false;
        boolean isMovingXForAttack = false;

        if (currentTargetEnemy != null) {
            double desiredTargetX = currentTargetEnemy.getX();
            // Kiểm tra xem có cần căn chỉnh theo trục X để bắn không
            boolean needsToAlignX = Math.abs(player.getX() - desiredTargetX) > player.getWidth() * ALIGNMENT_TOLERANCE_FACTOR * 0.05;

            if (needsToAlignX) {
                double nextStepX = player.getX() + Math.signum(desiredTargetX - player.getX()) * (player.getWidth() * 0.35);
                // Di chuyển để căn X nếu vị trí tiếp theo an toàn trong 1.5 giây
                if (isZoneAroundXClear(nextStepX, AI_PREFERRED_BOTTOM_Y_POSITION, MAINTAIN_SAFE_ZONE_RADIUS, TACTICAL_MOVE_SAFE_HORIZON)) {
                    if (player.getX() < desiredTargetX && canMoveRightSafely()) {
                        player.setMoveRight(true);
                        isMovingXForAttack = true;
                    } else if (player.getX() > desiredTargetX && canMoveLeftSafely()) {
                        player.setMoveLeft(true);
                        isMovingXForAttack = true;
                    }
                }
            }

            // Kiểm tra xem đã căn chỉnh đủ để bắn chưa
            boolean isAligned = isTargetAligned(currentTargetEnemy, player.getWidth() * ALIGNMENT_TOLERANCE_FACTOR);

            if (isAligned && !isMovingXForAttack) { // Nếu đã căn chỉnh và không đang di chuyển để căn chỉnh
                // Tính toán thời gian cần thiết để bắn và sẵn sàng né sau đó
                double timeForShotExecution = (AI_ATTEMPT_SHOOT_COOLDOWN_MS / 1000.0);
                double timeToReactAfterShotAndPause = DODGE_IMMEDIATE_DODGE_THRESHOLD_SEC + (POST_AI_SHOT_DECISION_PAUSE_MS / 1000.0);
                double safetyBufferForShooting = 0.12;
                double totalTimeNeededToShootAndBeReadyToDodge =
                        timeForShotExecution + timeToReactAfterShotAndPause + safetyBufferForShooting;

                boolean canShootAndStillDodgeThreat = (threateningBullet == null || threateningBulletActualTTI >= totalTimeNeededToShootAndBeReadyToDodge);
                // Vị trí bắn phải an toàn trong 1.5 giây tới
                boolean currentPosClearForFiring = isZoneAroundXClear(player.getX(), AI_PREFERRED_BOTTOM_Y_POSITION, FIRING_POSITION_SAFE_RADIUS, TACTICAL_MOVE_SAFE_HORIZON);


                if (canShootAndStillDodgeThreat && currentPosClearForFiring) {
                    tryToShoot(); // Cố gắng bắn (kiểm tra cooldown 20ms nội bộ)
                    if (player.getWantsToShoot()) { // Nếu AI thực sự muốn bắn (cooldown đã qua)
                        this.lastAIShotDecisionTime = System.currentTimeMillis(); // Ghi nhận thời điểm quyết định bắn
                        shotThisFrame = true;
                        if (currentTargetEnemy instanceof Enemy && !(currentTargetEnemy instanceof BossEnemy)) {
                            recentlyShotNormalEnemiesTimeStamps.put(currentTargetEnemy, System.currentTimeMillis());
                        }
                    }
                } else if (!canShootAndStillDodgeThreat && threateningBullet != null) { // Không đủ thời gian bắn và né đạn chính
                    performStrategicDodge();
                    shotThisFrame = false;
                    if(player.getWantsToShoot()) player.setWantsToShoot(false);
                } else if (!currentPosClearForFiring) { // Vị trí bắn không an toàn đủ lâu
                    // Nếu có đạn đang đe dọa, ưu tiên né nó
                    if(threateningBullet != null) {
                        performStrategicDodge();
                    } else {
                        // Nếu không có đạn cụ thể nào nhưng vị trí vẫn không an toàn, cũng cố gắng né
                        performStrategicDodge();
                    }
                    shotThisFrame = false;
                    if(player.getWantsToShoot()) player.setWantsToShoot(false);
                }
            }
        }

        // Logic thu thập vật phẩm nếu không tấn công, không bắn và không đang di chuyển
        boolean isMovingXForPowerUp = false;
        if (!isMovingXForAttack && !shotThisFrame && !(player.isMoveLeftSet() || player.isMoveRightSet())) {
            findBestPowerUpTarget();
            if (targetPowerUp != null) {
                boolean powerUpIsReachableY = Math.abs(targetPowerUp.getY() - AI_PREFERRED_BOTTOM_Y_POSITION) < player.getHeight() * 2.8 ||
                        (targetPowerUp.getY() > AI_PREFERRED_BOTTOM_Y_POSITION && targetPowerUp.getY() < SpaceShooter.HEIGHT - targetPowerUp.getHeight()/2);
                if (powerUpIsReachableY && Math.abs(targetPowerUp.getX() - player.getX()) < SpaceShooter.WIDTH / 1.8) {
                    // Di chuyển lấy vật phẩm nếu đường đi an toàn trong 1.5 giây
                    if (isZoneAroundXClear(targetPowerUp.getX(), AI_PREFERRED_BOTTOM_Y_POSITION, MAINTAIN_SAFE_ZONE_RADIUS, TACTICAL_MOVE_SAFE_HORIZON)) {
                        if (Math.abs(player.getX() - targetPowerUp.getX()) > player.getWidth() * 0.08) {
                            if (player.getX() < targetPowerUp.getX()) player.setMoveRight(true);
                            else player.setMoveLeft(true);
                            isMovingXForPowerUp = true;
                        }
                    } else {
                        targetPowerUp = null; // Đường đi không an toàn, bỏ qua vật phẩm này
                    }
                } else {
                    targetPowerUp = null; // Vật phẩm quá xa hoặc không thể với tới, bỏ qua
                }
            }
        }

        // Điều chỉnh vị trí Y về vị trí ưa thích
        adjustYToPreferredPosition();

        // Logic di chuyển ngẫu nhiên nếu không có hành động nào khác và vị trí hiện tại tương đối an toàn
        if (!isMovingXForAttack && !isMovingXForPowerUp && !shotThisFrame &&
                !player.isMoveLeftSet() && !player.isMoveRightSet() &&
                isCurrentPositionSafeAndClear(AI_PREFERRED_BOTTOM_Y_POSITION, MAINTAIN_SAFE_ZONE_HORIZON) && // An toàn trong 0.4s cho di chuyển ngẫu nhiên
                Math.abs(player.getY() - AI_PREFERRED_BOTTOM_Y_POSITION) < AI_Y_POSITION_TOLERANCE) {

            if (random.nextInt(280) < 1) { // Tần suất di chuyển ngẫu nhiên
                boolean tryMoveRight = random.nextBoolean();
                double checkXRandom = player.getX() + (tryMoveRight ? REPOSITION_SHIFT_DISTANCE * 0.6 : -REPOSITION_SHIFT_DISTANCE * 0.6);
                // Di chuyển ngẫu nhiên nếu vị trí mới an toàn trong 0.4 giây
                if (tryMoveRight) {
                    if (canMoveRightSafely() && isZoneAroundXClear(checkXRandom, AI_PREFERRED_BOTTOM_Y_POSITION, MAINTAIN_SAFE_ZONE_RADIUS, MAINTAIN_SAFE_ZONE_HORIZON)) {
                        player.setMoveRight(true);
                    }
                } else {
                    if (canMoveLeftSafely() && isZoneAroundXClear(checkXRandom, AI_PREFERRED_BOTTOM_Y_POSITION, MAINTAIN_SAFE_ZONE_RADIUS, MAINTAIN_SAFE_ZONE_HORIZON)) {
                        player.setMoveLeft(true);
                    }
                }
            }
        }
    }

    /**
     * Kiểm tra xem vị trí hiện tại của người chơi (hoặc một vị trí Y cụ thể) có an toàn
     * trong một khoảng thời gian dự đoán (`reactionHorizon`) hay không.
     * @param playerYPos Vị trí Y của người chơi để kiểm tra.
     * @param reactionHorizon Chân trời dự đoán (giây) để kiểm tra an toàn.
     * @return true nếu vị trí an toàn, false nếu ngược lại.
     */
    private boolean isCurrentPositionSafeAndClear(double playerYPos, double reactionHorizon) {
        return isZoneAroundXClear(player.getX(), playerYPos, MAINTAIN_SAFE_ZONE_RADIUS, reactionHorizon);
    }

    /**
     * Kiểm tra xem một vùng cụ thể (tâm tại checkX, playerY với bán kính safeRadius)
     * có bị đạn địch xâm phạm trong khoảng thời gian projectionHorizon hay không.
     * @param checkX Hoành độ X của tâm vùng cần kiểm tra.
     * @param playerY Tung độ Y của tâm vùng cần kiểm tra (thường là AI_PREFERRED_BOTTOM_Y_POSITION).
     * @param safeRadius Bán kính của vùng an toàn cần kiểm tra.
     * @param projectionHorizon Chân trời dự đoán (giây) để kiểm tra.
     * @return true nếu vùng đó an toàn, false nếu có đạn dự kiến bay vào.
     */
    private boolean isZoneAroundXClear(double checkX, double playerY, double safeRadius, double projectionHorizon) {
        for (GameObject obj : gameObjectsView) {
            if (obj instanceof EnemyBullet && !obj.isDead()) {
                EnemyBullet bullet = (EnemyBullet) obj;
                // Số bước dự đoán dựa trên chân trời và một bước thời gian nhỏ (0.025s)
                int numSteps = Math.max(1, (int) (projectionHorizon / 0.025));

                for (int i = 0; i <= numSteps; i++) {
                    double t = (numSteps == 0) ? 0.001 : ((double) i / numSteps) * projectionHorizon;
                    if (t < 0.001 && i > 0) t = 0.001; // Đảm bảo t có giá trị dương nhỏ nếu i > 0

                    // Dự đoán vị trí của đạn tại thời điểm t
                    double predBulletX = bullet.getX() + bullet.getVx() * t;
                    double predBulletY = bullet.getY() + bullet.getVy() * t;
                    // Tính khoảng cách bình phương giữa vị trí dự đoán của đạn và tâm vùng kiểm tra
                    double distanceSq = (checkX - predBulletX) * (checkX - predBulletX) +
                            (playerY - predBulletY) * (playerY - predBulletY);
                    // Tính tổng bán kính (vùng an toàn + nửa chiều rộng viên đạn)
                    double combinedRadius = safeRadius + bullet.getWidth() / 2;

                    // Nếu khoảng cách nhỏ hơn tổng bán kính -> va chạm dự kiến
                    if (distanceSq < combinedRadius * combinedRadius) {
                        return false; // Vùng không an toàn
                    }
                }
            }
        }
        return true; // Vùng an toàn
    }

    /**
     * Tìm hướng dịch chuyển ngang an toàn nhất (trái, phải, hoặc đứng yên).
     * Được sử dụng bởi logic né tránh chung khi không có mối đe dọa cụ thể nào được ưu tiên.
     * @param currentX Vị trí X hiện tại.
     * @param playerY Vị trí Y của người chơi.
     * @param safeRadius Bán kính vùng an toàn.
     * @param projectionHorizon Chân trời dự đoán an toàn.
     * @param shiftDistance Khoảng cách dịch chuyển thử nghiệm.
     * @return -1 (sang trái), 1 (sang phải), 0 (đứng yên hoặc không có hướng an toàn).
     */
    private int findSafestHorizontalShift(double currentX, double playerY, double safeRadius, double projectionHorizon, double shiftDistance) {
        boolean currentPosClear = isZoneAroundXClear(currentX, playerY, safeRadius, projectionHorizon);

        boolean canGoLeft = canMoveLeftSafely() && (currentX - shiftDistance >= 0);
        boolean leftPosClear = false;
        if (canGoLeft) {
            leftPosClear = isZoneAroundXClear(currentX - shiftDistance, playerY, safeRadius, projectionHorizon);
        }

        boolean canGoRight = canMoveRightSafely() && (currentX + shiftDistance <= SpaceShooter.WIDTH);
        boolean rightPosClear = false;
        if (canGoRight) {
            rightPosClear = isZoneAroundXClear(currentX + shiftDistance, playerY, safeRadius, projectionHorizon);
        }

        if (currentPosClear) {
            return 0; // Vị trí hiện tại an toàn
        } else {
            // Vị trí hiện tại không an toàn, tìm hướng né
            if (leftPosClear && rightPosClear) { // Cả hai hướng đều an toàn
                // Ưu tiên né khỏi viên đạn nguy hiểm nhất nếu có
                if (threateningBullet != null) {
                    double predBulletX = threateningBullet.getX() + threateningBullet.getVx() * threateningBulletActualTTI;
                    if (predBulletX > currentX) return -1; // Đạn bên phải, né trái
                    else return 1; // Đạn bên trái hoặc giữa, né phải
                }
                return (random.nextBoolean() ? -1 : 1); // Nếu không có đạn cụ thể, chọn ngẫu nhiên
            } else if (leftPosClear) {
                return -1; // Chỉ có né trái an toàn
            } else if (rightPosClear) {
                return 1; // Chỉ có né phải an toàn
            } else {
                return 0; // Không có hướng né nào an toàn
            }
        }
    }

    /**
     * Thực hiện hành động né tránh chiến lược, bao gồm cả các phương án dự phòng như né hoảng loạn hoặc dịch chuyển tức thời.
     * Được gọi khi vị trí hiện tại của AI được xác định là không an toàn.
     */
    private void performStrategicDodge() {
        EnemyBullet bulletToDodge = threateningBullet; // Ưu tiên viên đạn nguy hiểm nhất đã được xác định
        // Nếu không có đạn nguy hiểm chính hoặc nó quá xa, tìm viên đạn gần nhất có khả năng va chạm sớm
        if (bulletToDodge == null || threateningBulletActualTTI > DODGE_IMMEDIATE_DODGE_THRESHOLD_SEC * 1.2) {
            bulletToDodge = findClosestBulletThreateningPosition(AI_PREFERRED_BOTTOM_Y_POSITION, DODGE_IMMEDIATE_DODGE_THRESHOLD_SEC * 1.2);
        }

        // Trường hợp 1: Không xác định được viên đạn cụ thể nào gây nguy hiểm (bulletToDodge là null)
        // Điều này có thể xảy ra nếu mối nguy hiểm là do thân địch, hoặc do một viên đạn xa hơn
        // (ngoài DODGE_IMMEDIATE_DODGE_THRESHOLD_SEC) khiến isCurrentPositionSafeAndClear(1.5s) thất bại.
        if (bulletToDodge == null) {
            if (threateningEnemyBody != null) { // Ưu tiên né thân địch nếu có
                performEnemyBodyDodge(threateningEnemyBody);
            } else {
                // Không có đạn hay thân địch cụ thể nào gần, nhưng vị trí hiện tại không an toàn trong 1.5s.
                // Đây là trường hợp cần dịch chuyển tức thời như một giải pháp cuối cùng.
                findAndExecuteShortestTeleport();
            }
            // Nếu performEnemyBodyDodge đã đặt cờ di chuyển, hoặc teleport đã xảy ra,
            // việc return ở updateAI() sau khi gọi performStrategicDodge() sẽ đảm bảo AI đánh giá lại.
            return; // Kết thúc sau khi xử lý trường hợp không có bulletToDodge cụ thể
        }

        // Trường hợp 2: Có bulletToDodge cụ thể để né
        double playerX = player.getX();
        double playerY = AI_PREFERRED_BOTTOM_Y_POSITION;
        double idealDodgeHorizon = DODGE_BULLET_MIN_REACTION_WINDOW_SEC; // Chân trời an toàn lý tưởng: 1.5 giây

        // Kiểm tra né lý tưởng (khoảng cách lớn, an toàn trong 1.5s)
        double dodgeTargetLeftX = playerX - STRATEGIC_DODGE_DISTANCE;
        boolean canPotentiallyDodgeLeft = canMoveLeftSafely() && dodgeTargetLeftX >= 0;
        boolean idealSafeLeft = false;
        if (canPotentiallyDodgeLeft) {
            idealSafeLeft = isZoneAroundXClear(dodgeTargetLeftX, playerY, MAINTAIN_SAFE_ZONE_RADIUS, idealDodgeHorizon);
        }

        double dodgeTargetRightX = playerX + STRATEGIC_DODGE_DISTANCE;
        boolean canPotentiallyDodgeRight = canMoveRightSafely() && dodgeTargetRightX <= SpaceShooter.WIDTH;
        boolean idealSafeRight = false;
        if (canPotentiallyDodgeRight) {
            idealSafeRight = isZoneAroundXClear(dodgeTargetRightX, playerY, MAINTAIN_SAFE_ZONE_RADIUS, idealDodgeHorizon);
        }

        // Dự đoán vị trí X của viên đạn tại tầm Y của người chơi để quyết định hướng né tốt hơn
        double predictedBulletXAtPlayerYLevel = bulletToDodge.getX() + bulletToDodge.getVx() * Math.min(threateningBulletActualTTI, 0.25);


        if (idealSafeLeft || idealSafeRight) { // Nếu có ít nhất một hướng né lý tưởng
            if (idealSafeLeft && idealSafeRight) { // Cả hai hướng đều an toàn lý tưởng
                // Chọn hướng dựa trên vị trí dự đoán của đạn
                if (predictedBulletXAtPlayerYLevel > playerX + player.getWidth() * 0.05) player.setMoveLeft(true);
                else if (predictedBulletXAtPlayerYLevel < playerX - player.getWidth() * 0.05) player.setMoveRight(true);
                else { // Đạn ở giữa hoặc không chắc chắn, chọn hướng ngẫu nhiên hoặc ưu tiên một bên
                    if (playerX < SpaceShooter.WIDTH / 2) player.setMoveRight(true); else player.setMoveLeft(true);
                }
            } else if (idealSafeLeft) {
                player.setMoveLeft(true);
            } else { // idealSafeRight
                player.setMoveRight(true);
            }
        } else {
            // Fallback 1: Né hoảng loạn (khoảng cách lớn, tầm nhìn rất ngắn)
            double shortPanicHorizon = Math.min(0.20, threateningBulletActualTTI > 0 ? threateningBulletActualTTI * 0.4 : 0.1);
            if (shortPanicHorizon < 0.05) shortPanicHorizon = 0.05; // Đảm bảo không quá nhỏ

            boolean panicSafeLeft = false;
            if (canPotentiallyDodgeLeft) {
                panicSafeLeft = isZoneAroundXClear(dodgeTargetLeftX, playerY, MAINTAIN_SAFE_ZONE_RADIUS, shortPanicHorizon);
            }
            boolean panicSafeRight = false;
            if (canPotentiallyDodgeRight) {
                panicSafeRight = isZoneAroundXClear(dodgeTargetRightX, playerY, MAINTAIN_SAFE_ZONE_RADIUS, shortPanicHorizon);
            }

            if (panicSafeLeft || panicSafeRight) { // Nếu có ít nhất một hướng né hoảng loạn
                if (panicSafeLeft && panicSafeRight) {
                    if (predictedBulletXAtPlayerYLevel > playerX + player.getWidth() * 0.05) player.setMoveLeft(true);
                    else if (predictedBulletXAtPlayerYLevel < playerX - player.getWidth() * 0.05) player.setMoveRight(true);
                    else { if (playerX < SpaceShooter.WIDTH / 2) player.setMoveRight(true); else player.setMoveLeft(true); }
                } else if (panicSafeLeft) {
                    player.setMoveLeft(true);
                } else { // panicSafeRight
                    player.setMoveRight(true);
                }
            } else {
                // Fallback 2: Né vi mô hoảng loạn (khoảng cách nhỏ, tầm nhìn rất ngắn)
                double smallDodgeDistance = STRATEGIC_DODGE_DISTANCE * 0.5;
                double panicSmallDodgeLeftX = playerX - smallDodgeDistance;
                double panicSmallDodgeRightX = playerX + smallDodgeDistance;

                boolean panicSafeSmallLeft = false;
                if (canMoveLeftSafely() && panicSmallDodgeLeftX >=0) {
                    panicSafeSmallLeft = isZoneAroundXClear(panicSmallDodgeLeftX, playerY, MAINTAIN_SAFE_ZONE_RADIUS, shortPanicHorizon);
                }
                boolean panicSafeSmallRight = false;
                if (canMoveRightSafely() && panicSmallDodgeRightX <= SpaceShooter.WIDTH) {
                    panicSafeSmallRight = isZoneAroundXClear(panicSmallDodgeRightX, playerY, MAINTAIN_SAFE_ZONE_RADIUS, shortPanicHorizon);
                }

                if (panicSafeSmallLeft || panicSafeSmallRight) { // Nếu có ít nhất một hướng né vi mô
                    if (panicSafeSmallLeft && panicSafeSmallRight) {
                        // Ưu tiên né ra xa vị trí dự đoán của đạn
                        if (predictedBulletXAtPlayerYLevel > playerX) player.setMoveLeft(true);
                        else player.setMoveRight(true);
                    } else if (panicSafeSmallLeft) {
                        player.setMoveLeft(true);
                    } else { // panicSafeSmallRight
                        player.setMoveRight(true);
                    }
                } else {
                    // Fallback 3: Dịch chuyển tức thời như giải pháp cuối cùng
                    findAndExecuteShortestTeleport();
                }
            }
        }
    }

    /**
     * Tìm và thực hiện dịch chuyển tức thời đến vị trí an toàn gần nhất theo chiều ngang.
     * Chỉ được gọi như một giải pháp cuối cùng khi các phương pháp né tránh khác thất bại.
     * Vị trí dịch chuyển đến phải đảm bảo an toàn trong DODGE_BULLET_MIN_REACTION_WINDOW_SEC (1.5 giây).
     */
    private void findAndExecuteShortestTeleport() {
        double preferredY = AI_PREFERRED_BOTTOM_Y_POSITION;
        double currentX = player.getX();
        double safeHorizon = DODGE_BULLET_MIN_REACTION_WINDOW_SEC; // Yêu cầu an toàn 1.5s cho điểm đến

        double searchStep = player.getWidth() * 0.20; // Bước nhảy khi tìm kiếm, ví dụ 20% chiều rộng player
        double maxSearchRadius = SpaceShooter.WIDTH / 2.5; // Khoảng cách tìm kiếm tối đa

        for (double r = searchStep; r <= maxSearchRadius; r += searchStep) {
            // Kiểm tra bên phải
            double targetXRight = currentX + r;
            if (targetXRight + player.getWidth() / 2.0 < SpaceShooter.WIDTH) { // Đảm bảo player không ra ngoài biên
                if (isZoneAroundXClear(targetXRight, preferredY, MAINTAIN_SAFE_ZONE_RADIUS, safeHorizon)) {
                    player.teleportTo(targetXRight, preferredY); // Giả định Player có phương thức này
                    // System.out.println("AI Teleported Right to: " + targetXRight + " at radius " + r);
                    return; // Đã dịch chuyển
                }
            }

            // Kiểm tra bên trái
            double targetXLeft = currentX - r;
            if (targetXLeft - player.getWidth() / 2.0 > 0) { // Đảm bảo player không ra ngoài biên
                if (isZoneAroundXClear(targetXLeft, preferredY, MAINTAIN_SAFE_ZONE_RADIUS, safeHorizon)) {
                    player.teleportTo(targetXLeft, preferredY); // Giả định Player có phương thức này
                    // System.out.println("AI Teleported Left to: " + targetXLeft + " at radius " + r);
                    return; // Đã dịch chuyển
                }
            }
        }
        // Không tìm thấy vị trí dịch chuyển an toàn trong phạm vi tìm kiếm
        // System.out.println("AI: No safe teleport spot found.");
    }


    /**
     * Tìm viên đạn gần nhất có khả năng gây nguy hiểm cho người chơi trong một chân trời phản ứng nhất định.
     * @param playerYPos Vị trí Y của người chơi.
     * @param reactionHorizon Chân trời phản ứng (giây) để tìm kiếm.
     * @return EnemyBullet nguy hiểm nhất hoặc null nếu không có.
     */
    private EnemyBullet findClosestBulletThreateningPosition(double playerYPos, double reactionHorizon) {
        EnemyBullet closestThreat = null;
        double minTimeToActualCollision = reactionHorizon;
        double playerCurrentX = player.getX();
        double playerHalfWidth = player.getWidth() / 2;
        double playerHalfHeight = player.getHeight() / 2;

        for (GameObject obj : gameObjectsView) {
            if (obj instanceof EnemyBullet && !obj.isDead()) {
                EnemyBullet bullet = (EnemyBullet) obj;
                double bulletHalfWidth = bullet.getWidth() / 2;
                double bulletHalfHeight = bullet.getHeight() / 2;

                for (double t = 0.01; t <= reactionHorizon; t += 0.02) {
                    double predBulletX = bullet.getX() + bullet.getVx() * t;
                    double predBulletY = bullet.getY() + bullet.getVy() * t;

                    if (Math.abs(predBulletX - playerCurrentX) < (playerHalfWidth + bulletHalfWidth) &&
                            Math.abs(predBulletY - playerYPos) < (playerHalfHeight + bulletHalfHeight)) {
                        double yDiff = playerYPos - predBulletY;
                        boolean yIsUnsafe;
                        if (yDiff > 0) {
                            yIsUnsafe = (yDiff < SAFE_Y_DISTANCE_ABOVE_PLAYER * 0.7);
                        } else {
                            yIsUnsafe = (Math.abs(yDiff) < SAFE_Y_DISTANCE_BELOW_PLAYER * 0.7);
                        }
                        if (yIsUnsafe) {
                            if (t < minTimeToActualCollision) {
                                minTimeToActualCollision = t;
                                closestThreat = bullet;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return closestThreat;
    }

    /** Kiểm tra xem AI có thể di chuyển sang trái một cách an toàn không (không ra ngoài biên). */
    private boolean canMoveLeftSafely() {
        return player.getX() - player.getWidth() / 2 > 1.0;
    }

    /** Kiểm tra xem AI có thể di chuyển sang phải một cách an toàn không (không ra ngoài biên). */
    private boolean canMoveRightSafely() {
        return player.getX() + player.getWidth() / 2 < SpaceShooter.WIDTH - 1.0;
    }

    /** Điều chỉnh vị trí Y của AI về vị trí ưa thích nếu có sai lệch đáng kể. */
    private void adjustYToPreferredPosition() {
        double playerY = player.getY();
        if (Math.abs(playerY - AI_PREFERRED_BOTTOM_Y_POSITION) > AI_Y_POSITION_TOLERANCE) {
            if (playerY > AI_PREFERRED_BOTTOM_Y_POSITION) {
                if (playerY - player.getHeight() / 2 > 2) {
                    player.setMoveForward(true);
                }
            } else {
                if (playerY + player.getHeight() / 2 < SpaceShooter.HEIGHT - 2) {
                    player.setMoveBackward(true);
                }
            }
        }
    }

    /**
     * Đánh giá tất cả các viên đạn của địch để xác định viên đạn nguy hiểm nhất
     * (có TTI thấp nhất trong phạm vi DODGE_BULLET_MIN_REACTION_WINDOW_SEC).
     */
    private void assessThreateningBullets() {
        this.threateningBullet = null;
        this.threateningBulletActualTTI = Double.MAX_VALUE;
        double currentReactionWindow = DODGE_BULLET_MIN_REACTION_WINDOW_SEC; // Bây giờ là 1.5 giây

        for (GameObject obj : gameObjectsView) {
            if (obj instanceof EnemyBullet && !obj.isDead()) {
                EnemyBullet bullet = (EnemyBullet) obj;
                // Bỏ qua đạn bay lên nếu nó đã ở quá xa phía trên người chơi
                if (bullet.getVy() < -0.1 && bullet.getY() > player.getY() + player.getHeight() * 1.5) {
                    continue;
                }
                double timeToImpactY = Double.MAX_VALUE;

                // Tính thời gian đạn chạm đến Y của người chơi (player.getY() được coi là tâm Y)
                if (bullet.getVy() > 0.01) { // Đạn bay xuống
                    if (bullet.getY() < player.getY()) { // Chỉ xét nếu đạn ở trên tâm người chơi
                        timeToImpactY = (player.getY() - bullet.getY()) / bullet.getVy();
                    }
                } else if (bullet.getVy() < -0.01) { // Đạn bay lên
                    if (bullet.getY() > player.getY()) { // Chỉ xét nếu đạn ở dưới tâm người chơi
                        timeToImpactY = (bullet.getY() - player.getY()) / (-bullet.getVy());
                    }
                } else { // Đạn bay ngang hoặc rất chậm theo trục Y
                    // Nếu đạn gần như cùng độ cao Y với người chơi
                    if (Math.abs(bullet.getY() - player.getY()) < (player.getHeight() / 2 + bullet.getHeight() / 2 + 5.0)) {
                        timeToImpactY = 0.02; // Coi như là mối đe dọa Y tức thời
                    } else {
                        continue;
                    }
                }

                // Nếu TTI nằm trong cửa sổ phản ứng
                if (timeToImpactY >= 0 && timeToImpactY < currentReactionWindow) {
                    double predictedBulletX = bullet.getX() + bullet.getVx() * timeToImpactY; // Vị trí X dự đoán của đạn
                    // Vùng phát hiện va chạm X của người chơi (có buffer)
                    double playerDetectionMinX = player.getX() - player.getWidth() / 2 - DODGE_PLAYER_X_DETECTION_BUFFER;
                    double playerDetectionMaxX = player.getX() + player.getWidth() / 2 + DODGE_PLAYER_X_DETECTION_BUFFER;

                    // Nếu X dự đoán nằm trong vùng phát hiện của người chơi
                    if (predictedBulletX >= playerDetectionMinX && predictedBulletX <= playerDetectionMaxX) {
                        if (timeToImpactY < this.threateningBulletActualTTI) { // Nếu viên đạn này nguy hiểm hơn
                            this.threateningBulletActualTTI = timeToImpactY;
                            this.threateningBullet = bullet;
                        }
                    }
                }
            }
        }
    }

    /** Tìm kẻ địch tốt nhất để tấn công dựa trên vị trí và loại (ưu tiên Boss). */
    private void findBestEnemyTargetToAttack() {
        currentTargetEnemy = null;
        double bestScore = -Double.MAX_VALUE;
        BossEnemy tempBossTarget = null;
        final double DEEP_ENEMY_BONUS = 11000.0;
        final double VERY_DEEP_ENEMY_BONUS = 16000.0;
        long currentTimeForTargeting = System.currentTimeMillis();

        for (GameObject obj : gameObjectsView) {
            if (obj.isDead()) continue;
            double score = 0;
            if (obj instanceof BossEnemy) {
                tempBossTarget = (BossEnemy) obj;
                continue;
            } else if (obj instanceof Enemy) {
                Enemy enemy = (Enemy) obj;

                Long shotTimestamp = recentlyShotNormalEnemiesTimeStamps.get(enemy);
                if (shotTimestamp != null && currentTimeForTargeting - shotTimestamp < NORMAL_ENEMY_SHOT_EXPIRY_MS) {
                    continue; // Bỏ qua nếu vừa bắn vào kẻ địch này
                }

                score = 1000.0;
                score -= Math.abs(player.getX() - enemy.getX()) * 2.8;
                score -= Math.abs(AI_PREFERRED_BOTTOM_Y_POSITION - enemy.getY()) * 0.15;
                if (enemy.getY() > SpaceShooter.HEIGHT * 0.75) score += VERY_DEEP_ENEMY_BONUS * 0.7;
                else if (enemy.getY() > SpaceShooter.HEIGHT * 0.60) score += DEEP_ENEMY_BONUS * 0.7;
                if (enemy.getY() < AI_PREFERRED_BOTTOM_Y_POSITION - SpaceShooter.HEIGHT * 0.70) {
                    score -= 6000;
                }
            } else {
                continue;
            }
            if (score > bestScore) {
                bestScore = score;
                currentTargetEnemy = obj;
            }
        }

        if (tempBossTarget != null) {
            double bossScore = 28000.0;
            bossScore -= Math.abs(player.getX() - tempBossTarget.getX()) * 2.2;
            bossScore -= Math.abs(AI_PREFERRED_BOTTOM_Y_POSITION - tempBossTarget.getY()) * 0.1;
            if (tempBossTarget.getY() > SpaceShooter.HEIGHT * 0.75) bossScore += VERY_DEEP_ENEMY_BONUS;
            else if (tempBossTarget.getY() > SpaceShooter.HEIGHT * 0.60) bossScore += DEEP_ENEMY_BONUS;
            if (tempBossTarget.getY() < AI_PREFERRED_BOTTOM_Y_POSITION - SpaceShooter.HEIGHT * 0.75) {
                bossScore -= 9000;
            }
            if (bossScore > bestScore || currentTargetEnemy == null) {
                currentTargetEnemy = tempBossTarget;
            }
        }
    }

    /** Tìm vật phẩm tốt nhất để thu thập dựa trên loại và khoảng cách. */
    private void findBestPowerUpTarget() {
        targetPowerUp = null;
        double bestScore = -Double.MAX_VALUE;
        for (GameObject obj : gameObjectsView) {
            if (obj instanceof PowerUp && !obj.isDead()) {
                PowerUp pu = (PowerUp) obj;
                double currentScore = 0;
                switch (pu.getType()) {
                    case SHIELD: currentScore = 1350; if(SpaceShooter.numLives <=1) currentScore +=1250; break;
                    case TRIPLE_SHOT: currentScore = 750; break;
                    case HEALTH_PACK:
                        currentScore = (SpaceShooter.numLives == 1) ? 1950 :
                                ((SpaceShooter.numLives == 2) ? 850 : 250);
                        break;
                }
                currentScore -= getDistance(player.getX(), AI_PREFERRED_BOTTOM_Y_POSITION, pu.getX(), pu.getY()) * 0.8;
                if (pu.getY() < AI_PREFERRED_BOTTOM_Y_POSITION - player.getHeight() * 3.0) {
                    currentScore -= POWERUP_HIGH_PENALTY * 1.8;
                } else if (pu.getY() < AI_PREFERRED_BOTTOM_Y_POSITION - player.getHeight() * 1.5) {
                    currentScore -= POWERUP_HIGH_PENALTY * 0.6;
                }
                if(Math.abs(player.getX() - pu.getX()) > SpaceShooter.WIDTH * 0.40) {
                    currentScore -= 400;
                }
                if (currentScore > bestScore) {
                    boolean powerUpIsReachableY = Math.abs(pu.getY() - AI_PREFERRED_BOTTOM_Y_POSITION) < player.getHeight() * 3.0 ||
                            (pu.getY() > AI_PREFERRED_BOTTOM_Y_POSITION && pu.getY() < SpaceShooter.HEIGHT - pu.getHeight()/2);
                    if (powerUpIsReachableY) {
                        bestScore = currentScore;
                        targetPowerUp = pu;
                    }
                }
            }
        }
    }

    /** Đánh giá mối đe dọa va chạm trực tiếp với thân tàu địch. */
    private void assessProximityThreats() {
        threateningEnemyBody = null;
        double minEffectiveDistance = Double.MAX_VALUE;
        for (GameObject obj : gameObjectsView) {
            if (obj instanceof Enemy && !obj.isDead()) {
                double combinedHalfWidths = (player.getWidth() + obj.getWidth()) / 2.0;
                double combinedHalfHeights = (player.getHeight() + obj.getHeight()) / 2.0;
                double deltaX = Math.abs(player.getX() - obj.getX());
                double deltaY = Math.abs(player.getY() - obj.getY());

                if (deltaX < combinedHalfWidths + ENEMY_PROXIMITY_THRESHOLD * 0.4 &&
                        deltaY < combinedHalfHeights + ENEMY_PROXIMITY_THRESHOLD * 0.4) {
                    double effectiveDistance = Math.max(0, deltaX - combinedHalfWidths) + Math.max(0, deltaY - combinedHalfHeights);
                    if (effectiveDistance < minEffectiveDistance) {
                        minEffectiveDistance = effectiveDistance;
                        threateningEnemyBody = obj;
                    }
                }
            }
        }
    }

    /** Thực hiện né va chạm với thân tàu địch. */
    private void performEnemyBodyDodge(GameObject enemy) {
        double deltaXToEnemy = player.getX() - enemy.getX();
        double dodgeOffset = STRATEGIC_DODGE_DISTANCE * 0.6;
        // Né thân địch với tầm nhìn an toàn TACTICAL_MOVE_SAFE_HORIZON (1.5s)
        if (deltaXToEnemy > 0) { // Địch ở bên trái
            if (canMoveRightSafely() && isZoneAroundXClear(player.getX() + dodgeOffset, AI_PREFERRED_BOTTOM_Y_POSITION, MAINTAIN_SAFE_ZONE_RADIUS, TACTICAL_MOVE_SAFE_HORIZON)) {
                player.setMoveRight(true);
            } else if (canMoveLeftSafely() && isZoneAroundXClear(player.getX() - dodgeOffset, AI_PREFERRED_BOTTOM_Y_POSITION, MAINTAIN_SAFE_ZONE_RADIUS, TACTICAL_MOVE_SAFE_HORIZON)) {
                player.setMoveLeft(true); // Né tiếp sang trái nếu không thể né phải
            }
        } else { // Địch ở bên phải
            if (canMoveLeftSafely() && isZoneAroundXClear(player.getX() - dodgeOffset, AI_PREFERRED_BOTTOM_Y_POSITION, MAINTAIN_SAFE_ZONE_RADIUS, TACTICAL_MOVE_SAFE_HORIZON)) {
                player.setMoveLeft(true);
            } else if (canMoveRightSafely() && isZoneAroundXClear(player.getX() + dodgeOffset, AI_PREFERRED_BOTTOM_Y_POSITION, MAINTAIN_SAFE_ZONE_RADIUS, TACTICAL_MOVE_SAFE_HORIZON)) {
                player.setMoveRight(true); // Né tiếp sang phải nếu không thể né trái
            }
        }
    }

    /** Kiểm tra xem người chơi có đang căn chỉnh với mục tiêu không. */
    private boolean isTargetAligned(GameObject target, double tolerance) {
        if (target == null) return false;
        boolean targetAbovePlayer = target.getY() < player.getY() - player.getHeight() * 0.25;
        boolean targetInRangeY = target.getY() > player.getY() - SpaceShooter.HEIGHT * 0.90; // Mục tiêu không quá xa về phía trên
        return targetAbovePlayer && targetInRangeY &&
                Math.abs(player.getX() - target.getX()) < tolerance;
    }

    /** Cố gắng thực hiện hành động bắn (nếu cooldown cho phép). */
    private void tryToShoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAITriedToShootTime > AI_ATTEMPT_SHOOT_COOLDOWN_MS) { // Cooldown 20ms
            player.setWantsToShoot(true);
            lastAITriedToShootTime = currentTime;
        }
    }

    /** Tính khoảng cách Euclid giữa hai điểm. */
    private double getDistance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** Kiểm tra xem người chơi có đang thực hiện bất kỳ hành động di chuyển nào không. */
    private boolean isPlayerCurrentlyMoving() {
        return player.isMoveForwardSet() || player.isMoveBackwardSet() || player.isMoveLeftSet() || player.isMoveRightSet();
    }
}