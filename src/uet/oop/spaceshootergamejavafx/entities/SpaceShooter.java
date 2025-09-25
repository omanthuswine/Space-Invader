package uet.oop.spaceshootergamejavafx.entities;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.media.AudioClip;
import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Main game class for the Space Shooter game.
 * This class manages the game lifecycle, rendering, input handling,
 * game state updates, waves, bosses, and UI elements.
 */
public class SpaceShooter extends Application {

    public static final int WIDTH = 512;  // Width of the game window
    public static final int HEIGHT = 800; // Height of the game window
    public static int numLives = 3;       // Number of player lives (static)

    private int score;                    // Current player score
    private boolean gameRunning;          // Flag to track if the game is running
    private boolean gamePaused = false;   // Flag to track if the game is paused

    private Label scoreLabel;             // UI label to display score
    private Label livesLabel;             // UI label to display lives left
    private Label waveLabel;              // UI label to display current wave

    private List<GameObject> gameObjects;  // List of all active game objects
    private List<GameObject> newObjects;   // List of newly spawned game objects to add
    private Player player;                 // Player object
    private AIPlayerController aiController; // AI controller for player (optional)
    private boolean isAIControlled = false;   // Flag to toggle AI control

    private Pane gameRootPane;             // Root pane for game scene
    private Canvas canvas;                 // Canvas for rendering graphics
    private GraphicsContext gc;            // Graphics context for drawing on canvas

    private Scene gameScene;               // Main game scene
    private Scene menuScene;               // Menu scene
    private Scene gameOverScene;           // Game over scene
    private Stage primaryStage;            // Primary stage (window)

    private Random random = new Random(); // Random generator for spawning and events
    private int enemySpawnTimer = 0;      // Timer counter to control enemy spawning

    private AudioClip enemyExplosionSound;    // Sound effect for enemy explosion
    private AudioClip bossDefeatedSound;      // Sound effect for boss defeat
    private AudioClip powerUpCollectedSound;  // Sound effect for power-up collection

    // --- WAVE AND BOSS MANAGEMENT ---
    private int waveNumber = 1;            // Current wave number
    private boolean waveBossFightActive = false;  // Flag if boss fight in wave is active
    private int bossesToDefeatInWave;      // Number of bosses to defeat in current wave
    private int bossesDefeatedThisWave;    // Bosses defeated so far in current wave

    private BossEnemy stationaryBossInstance = null; // Reference to stationary boss instance
    private boolean stationaryBossSpawned = false;   // Whether stationary boss has spawned
    private boolean stationaryBossDefeated = false;  // Whether stationary boss is defeated
    private boolean finalBossesTriggered = false;    // Whether final bosses have been triggered
    private boolean finalBossFightActive = false;    // Whether final boss fight is active

    private final int SCORE_REWARD_PER_BOSS = 50;          // Score reward for defeating normal boss
    private final int SCORE_REWARD_STATIONARY_BOSS = 150;  // Score reward for defeating stationary boss

    private final int SCORE_THRESHOLD_WAVE1_BOSS = 150;        // Score needed to spawn wave 1 boss
    private final int SCORE_THRESHOLD_WAVE2_BOSSES = 450;      // Score needed for wave 2 bosses
    private final int SCORE_THRESHOLD_WAVE3_FINAL_BOSSES = 900; // Score needed for final bosses

    private final int BASE_ENEMY_SPAWN_INTERVAL = 170;    // Base interval for enemy spawning
    private int currentEnemySpawnInterval;                 // Current spawn interval (may vary)

    private int powerUpSpawnTimer = 0;          // Timer for spawning power-ups
    private final int POWERUP_SPAWN_INTERVAL = 500; // Interval to spawn power-ups

    private javafx.animation.AnimationTimer gameLoop;  // Main game loop timer

    // --- BACKGROUND SCROLLING VARIABLES ---
    private Image backgroundImage;          // Background image
    private double backgroundY1 = 0;        // Y position of first background image
    private double backgroundY2;            // Y position of second background image (for looping)
    private double actualBackgroundImageHeight;  // Height of the background image
    private final double BACKGROUND_SCROLL_SPEED = 0.5; // Scrolling speed of background

    /**
     * Main method launching the application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Start method called by JavaFX runtime to initialize the game.
     * Sets up scenes, UI elements, event handlers, and starts the game loop.
     * @param stage Primary stage provided by JavaFX
     */
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Space Shooter");

        // Initialize main game loop using AnimationTimer
        gameLoop = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameRunning) {
                    if (!gamePaused) {
                        updateGame();
                    }
                    renderGame();
                }
            }
        };

        gameRootPane = new Pane();
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        gameRootPane.getChildren().add(canvas);

        loadBackgroundImage();

        // Initialize score label UI
        scoreLabel = new Label("Score: 0");
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        scoreLabel.setLayoutX(10);
        scoreLabel.setLayoutY(10);

        // Initialize lives label UI
        livesLabel = new Label("Lives: " + SpaceShooter.numLives);
        livesLabel.setTextFill(Color.WHITE);
        livesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        livesLabel.setLayoutX(WIDTH - 80);
        livesLabel.setLayoutY(10);

        // Initialize wave label UI, centered horizontally
        waveLabel = new Label("Wave: 1");
        waveLabel.setTextFill(Color.GOLD);
        waveLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        waveLabel.layoutXProperty().bind(gameRootPane.widthProperty().subtract(waveLabel.widthProperty()).divide(2));
        waveLabel.setLayoutY(10);

        // Add labels to root pane
        gameRootPane.getChildren().addAll(scoreLabel, livesLabel, waveLabel);

        gameScene = new Scene(gameRootPane, WIDTH, HEIGHT);
        initEventHandlers(gameScene);

        // Initialize game object lists and player
        gameObjects = new ArrayList<>();
        newObjects = new ArrayList<>();
        player = new Player(WIDTH / 2.0, HEIGHT - 120);
        aiController = new AIPlayerController(player);

        loadSounds();

        // Setup menu scene and show primary stage
        menuScene = new Scene(createMenu(), WIDTH, HEIGHT);
        primaryStage.setScene(menuScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }



    /**
     * Loads the background image from resource folder.
     * If loading fails or image not found, sets a fallback background color.
     */
    private void loadBackgroundImage() {
        try {
            // Load image as InputStream from resource path
            java.io.InputStream bgStream = getClass().getResourceAsStream("/back_ground2.jpg");
            if (bgStream != null) {
                backgroundImage = new Image(bgStream);
                actualBackgroundImageHeight = backgroundImage.getHeight();
                if (actualBackgroundImageHeight > 0) {
                    // Set initial position for second background image for scrolling loop
                    backgroundY2 = backgroundY1 - actualBackgroundImageHeight;
                } else {
                    backgroundImage = null;
                }
                try {
                    bgStream.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("Lỗi: Không tìm thấy file ảnh nền /back_ground2.jpg");
                backgroundImage = null;
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi load ảnh nền: " + e.getMessage());
            e.printStackTrace();
            backgroundImage = null;
        }
        // Fallback background color if image failed to load
        if (backgroundImage == null) {
            gameRootPane.setStyle("-fx-background-color: #00001A;");
        }
    }

    /**
     * Loads sound effects from resource files.
     * Prints error messages if files are missing or failed to load.
     */
    private void loadSounds() {
        try {
            URL explosionURL = getClass().getResource("/enemy_explosion.wav");
            if (explosionURL != null)
                enemyExplosionSound = new AudioClip(explosionURL.toExternalForm());
            else
                System.err.println("Lỗi: Không tìm thấy /enemy_explosion.wav");

            URL bossDefeatedURL = getClass().getResource("/boss_defeated.mp3");
            if (bossDefeatedURL != null)
                bossDefeatedSound = new AudioClip(bossDefeatedURL.toExternalForm());
            else
                System.err.println("Lỗi: Không tìm thấy /boss_defeated.mp3");

            URL powerUpCollectURL = getClass().getResource("/powerup_collect.mp3"); // Hoặc .wav
            if (powerUpCollectURL != null) {
                powerUpCollectedSound = new AudioClip(powerUpCollectURL.toExternalForm());
            } else {
                System.err.println("Lỗi: Không tìm thấy /powerup_collect.mp3"); // Hoặc .wav
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi load âm thanh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adjusts the difficulty dynamically based on current wave and score.
     * Decreases enemy spawn interval as wave number and score increase.
     */
    private void adjustDifficulty() {
        int waveFactor = waveNumber - 1;
        // Minimum spawn interval capped at 40
        currentEnemySpawnInterval = Math.max(40, BASE_ENEMY_SPAWN_INTERVAL - (waveFactor * 28) - (score / 90));
    }

    /**
     * Creates a procedural explosion effect at the specified coordinates.
     * Plays the explosion sound if available.
     *
     * @param x X-coordinate of the explosion
     * @param y Y-coordinate of the explosion
     */
    private void createProceduralExplosion(double x, double y) {
        long explosionDuration = 700; // Duration of the explosion effect in milliseconds
        ProceduralExplosion explosion = new ProceduralExplosion(x, y, explosionDuration) {
            private boolean actualDead = false;

            @Override
            public boolean isDead() { return actualDead; }

            @Override
            public double getWidth() { return 0; }

            @Override
            public double getHeight() { return 0; }

            @Override
            public void setDead(boolean dead) { this.actualDead = dead; }
        };
        newObjects.add(explosion);

        if (enemyExplosionSound != null) {
            enemyExplosionSound.play();
        }
    }


    /**
     * Cập nhật trạng thái game mỗi frame trong vòng lặp game.
     * Bao gồm:
     * - Điều chỉnh độ khó theo wave và điểm
     * - Cập nhật vị trí background cho hiệu ứng cuộn
     * - Cập nhật trạng thái tất cả các đối tượng trong game (player, enemy, đạn,...)
     * - Xử lý AI hoặc input người chơi
     * - Xử lý bắn đạn của player và enemy
     * - Sinh các đối tượng mới (enemy, power-up)
     * - Quản lý trạng thái wave, spawn boss
     * - Kiểm tra va chạm và loại bỏ các đối tượng chết hoặc ra khỏi màn hình
     * - Cập nhật giao diện điểm số, mạng, wave
     * - Kiểm tra điều kiện kết thúc game
     */
    private void updateGame() {
        adjustDifficulty();  // Điều chỉnh độ khó (spawn interval,...)
        updateBackground();  // Cập nhật vị trí background (cuộn nền)

        // 1. Cập nhật trạng thái tất cả đối tượng (gọi update của từng đối tượng)
        for (GameObject obj : new ArrayList<>(gameObjects)) {
            obj.update();
        }

        // 2. Nếu AI được bật, cập nhật hành vi AI cho player
        if (isAIControlled && !player.isDead()) {
            aiController.updateAI(this.gameObjects);
        }

        // 3. Xử lý hành động bắn của player (AI hoặc người chơi)
        if (player.getWantsToShoot() && !player.isDead()) {
            player.shoot(newObjects);
            player.setWantsToShoot(false);
        }

        // 4. Cho phép enemy thường bắn đạn khi thỏa điều kiện (không phải boss)
        for (GameObject obj : gameObjects) {
            if (obj instanceof Enemy && !(obj instanceof BossEnemy) && !obj.isDead()) {
                Enemy enemy = (Enemy) obj;
                if (!enemy.hasFiredInitialShot() && enemy.getY() > 20 && enemy.getY() < HEIGHT * 0.65) {
                    enemy.shoot(newObjects);
                    enemy.setInitialShotFired(true);
                }
            }
        }

        // 5. Spawn thêm enemy thường và power-up mới nếu tới thời điểm
        spawnNormalEnemies();
        spawnPowerUp();

        // 6. Quản lý trạng thái wave và spawn boss
        manageWaveStateAndBossSpawning();

        // 7. Kiểm tra va chạm giữa các đối tượng
        checkCollisions();

        // 8. Loại bỏ các đối tượng ra khỏi màn hình (đạn, enemy,...)
        checkEntitiesOutOfBounds();

        // 9. Thêm các đối tượng mới sinh ra (đạn, power-up, explosion) vào danh sách gameObjects
        gameObjects.addAll(newObjects);
        newObjects.clear();

        // 10. Loại bỏ các đối tượng đã chết khỏi danh sách gameObjects
        Iterator<GameObject> iter = gameObjects.iterator();
        while (iter.hasNext()) {
            GameObject iterObj = iter.next();
            if (iterObj.isDead()) {
                if (iterObj instanceof BossEnemy) {
                    BossEnemy deadBoss = (BossEnemy) iterObj;
                    System.out.println("DEBUG: Boss object (" + deadBoss.hashCode() + ") is dead and removed from gameObjects.");
                    // Tăng điểm thưởng tùy boss
                    score += (deadBoss == stationaryBossInstance && stationaryBossSpawned) ? SCORE_REWARD_STATIONARY_BOSS : SCORE_REWARD_PER_BOSS;

                    if (waveBossFightActive || finalBossFightActive) {
                        if (deadBoss != stationaryBossInstance) {
                            bossesDefeatedThisWave++;
                            System.out.println("DEBUG: A wave/final Boss fully died. Defeated count: " + bossesDefeatedThisWave);
                        }
                    }
                }
                iter.remove();
            }
        }

        // 11. Cập nhật lại trạng thái wave và spawn boss ngay nếu có thay đổi do loại bỏ boss
        manageWaveStateAndBossSpawning();

        // 12. Cập nhật UI hiển thị điểm số, số mạng, wave hiện tại
        scoreLabel.setText("Score: " + score);
        livesLabel.setText("Lives: " + SpaceShooter.numLives);
        waveLabel.setText("Wave: " + waveNumber);

        // 13. Kiểm tra điều kiện kết thúc game (player chết hoặc hết mạng)
        if (player.isDead() || SpaceShooter.numLives <= 0) {
            resetGame();
        }
    }

    /**
     * Cập nhật vị trí background để tạo hiệu ứng cuộn nền liên tục theo chiều dọc.
     * Dùng 2 hình nền chạy song song để tạo hiệu ứng seamless (liền mạch).
     */
    private void updateBackground() {
        if (backgroundImage != null && actualBackgroundImageHeight > 0) {
            backgroundY1 += BACKGROUND_SCROLL_SPEED;
            backgroundY2 += BACKGROUND_SCROLL_SPEED;

            // Nếu ảnh background thứ nhất đi hết màn hình thì đặt lại phía trên ảnh thứ hai
            if (backgroundY1 >= HEIGHT) {
                backgroundY1 = backgroundY2 - actualBackgroundImageHeight;
            }
            // Tương tự cho ảnh background thứ hai
            if (backgroundY2 >= HEIGHT) {
                backgroundY2 = backgroundY1 - actualBackgroundImageHeight;
            }
        }
    }


    /**
     * Vẽ lại toàn bộ khung hình game mỗi frame.
     * - Xóa màn hình hiện tại
     * - Vẽ background (cuộn nền liên tục với 2 ảnh nền)
     * - Vẽ tất cả các đối tượng game (player, enemy, đạn, power-up,...)
     */
    private void renderGame() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        if (backgroundImage != null && actualBackgroundImageHeight > 0) {
            double imgOriginalWidth = backgroundImage.getWidth();
            double sx = (imgOriginalWidth - WIDTH) / 2.0;  // Lấy vùng ảnh nền cắt theo chiều ngang
            double sw = WIDTH;

            if (imgOriginalWidth < WIDTH) {
                sx = 0;
                sw = imgOriginalWidth;
            }

            // Vẽ 2 ảnh nền liên tục để tạo hiệu ứng cuộn nền seamless
            gc.drawImage(backgroundImage, sx, 0, sw, actualBackgroundImageHeight,
                    (WIDTH - sw) / 2.0, backgroundY1, sw, actualBackgroundImageHeight);
            gc.drawImage(backgroundImage, sx, 0, sw, actualBackgroundImageHeight,
                    (WIDTH - sw) / 2.0, backgroundY2, sw, actualBackgroundImageHeight);
        } else {
            // Nếu không có ảnh nền, vẽ màu nền mặc định
            gc.setFill(Color.rgb(0, 0, 26));
            gc.fillRect(0, 0, WIDTH, HEIGHT);
        }

        // Vẽ tất cả đối tượng game
        for (GameObject obj : gameObjects) {
            obj.render(gc);
        }

        if (gamePaused) {
            // Vẽ một lớp phủ mờ
            gc.setFill(Color.rgb(0, 0, 0, 0.5)); // Màu đen với 50% độ trong suốt
            gc.fillRect(0, 0, WIDTH, HEIGHT);

            // Vẽ chữ "PAUSED"
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));
            String pauseText = "PAUSED";

            // Tính toán để căn giữa chữ
            javafx.scene.text.Text text = new javafx.scene.text.Text(pauseText);
            text.setFont(gc.getFont());
            double textWidth = text.getLayoutBounds().getWidth();
            gc.fillText(pauseText, (WIDTH - textWidth) / 2, HEIGHT / 2.0);
        }
    }

    /**
     * Spawn enemy thường theo thời gian và điều kiện game hiện tại.
     * Không spawn khi đang có boss fight (trừ trường hợp wave 3 có stationary boss đang sống).
     * Enemy được spawn ngẫu nhiên ở vị trí ngang trên màn hình.
     */
    private void spawnNormalEnemies() {
        boolean canSpawnNormal = true;

        // Không spawn enemy thường khi đang fight boss (trừ trường hợp đặc biệt wave 3)
        if (waveBossFightActive || finalBossFightActive) {
            canSpawnNormal = false;
        }
        if (waveNumber == 3 && stationaryBossSpawned && (stationaryBossInstance != null && !stationaryBossInstance.isDead()) && !finalBossFightActive) {
            canSpawnNormal = true;
        }

        if (canSpawnNormal) {
            enemySpawnTimer++;
            if (enemySpawnTimer >= currentEnemySpawnInterval) {
                enemySpawnTimer = 0;

                double minCenterX = Enemy.WIDTH / 2.0;
                double maxCenterX = SpaceShooter.WIDTH - (Enemy.WIDTH / 2.0);
                double spawnableWidthForCenter = maxCenterX - minCenterX;
                if (spawnableWidthForCenter <= 0) spawnableWidthForCenter = Math.max(1, SpaceShooter.WIDTH - Enemy.WIDTH);

                double centerX = minCenterX + random.nextDouble() * spawnableWidthForCenter;
                double centerY = -Enemy.HEIGHT / 2.0;  // Spawn ngay trên màn hình

                Enemy enemy = new Enemy(centerX, centerY);
                newObjects.add(enemy);
            }
        }
    }

    /**
     * Spawn power-up theo thời gian.
     * Power-up có 3 loại: SHIELD, TRIPLE_SHOT, HEALTH_PACK được chọn ngẫu nhiên.
     * Spawn ở vị trí ngẫu nhiên ngang trên màn hình.
     */
    private void spawnPowerUp() {
        powerUpSpawnTimer++;
        if (powerUpSpawnTimer >= POWERUP_SPAWN_INTERVAL) {
            powerUpSpawnTimer = 0;

            double centerX = PowerUp.WIDTH / 2.0 + random.nextDouble() * (WIDTH - PowerUp.WIDTH);
            double centerY = -PowerUp.HEIGHT / 2.0;

            PowerUp.PowerUpType randomType;
            int typeChoice = random.nextInt(3);
            switch (typeChoice) {
                case 0:
                    randomType = PowerUp.PowerUpType.SHIELD;
                    break;
                case 1:
                    randomType = PowerUp.PowerUpType.TRIPLE_SHOT;
                    break;
                default:
                    randomType = PowerUp.PowerUpType.HEALTH_PACK;
                    break;
            }
            PowerUp powerUp = new PowerUp(centerX, centerY, randomType);
            newObjects.add(powerUp);
        }
    }

    /**
     * Quản lý trạng thái các wave và spawn boss theo điểm và tình trạng boss đã bị hạ.
     * - Wave 1, 2: spawn các boss tương ứng khi đạt điểm nhất định
     * - Wave 3: spawn stationary boss, sau đó final bosses khi đạt điểm
     * - Kết thúc game khi final bosses bị hạ hết
     */
    private void manageWaveStateAndBossSpawning() {
        switch (waveNumber) {
            case 1:
                if (!waveBossFightActive && score >= SCORE_THRESHOLD_WAVE1_BOSS) {
                    spawnBossesForWave(1);
                }
                if (waveBossFightActive && bossesDefeatedThisWave >= bossesToDefeatInWave) {
                    if (bossDefeatedSound != null) bossDefeatedSound.play();
                    waveNumber = 2;
                    waveBossFightActive = false;
                    bossesDefeatedThisWave = 0;
                    showTempMessage("WAVE 2", WIDTH / 2.0, HEIGHT / 3.0, 2.5);
                }
                break;

            case 2:
                if (!waveBossFightActive && score >= SCORE_THRESHOLD_WAVE2_BOSSES) {
                    spawnBossesForWave(2);
                }
                if (waveBossFightActive && bossesDefeatedThisWave >= bossesToDefeatInWave) {
                    if (bossDefeatedSound != null) bossDefeatedSound.play();
                    waveNumber = 3;
                    waveBossFightActive = false;
                    bossesDefeatedThisWave = 0;
                    spawnStationaryBoss();
                    showTempMessage("WAVE 3", WIDTH / 2.0, HEIGHT / 3.0, 2.5);
                }
                break;

            case 3:
                if (stationaryBossSpawned && stationaryBossInstance != null && stationaryBossInstance.isDead() && !stationaryBossDefeated) {
                    stationaryBossDefeated = true;
                    System.out.println("Stationary Boss DEFEATED and processed in manageWaveState! Score: " + score);
                    if (bossDefeatedSound != null) bossDefeatedSound.play();
                    showTempMessage("Stationary Boss Defeated!", WIDTH / 2.0, HEIGHT / 3.0, 2.5);
                    stationaryBossInstance = null;
                }

                if (stationaryBossDefeated && score >= SCORE_THRESHOLD_WAVE3_FINAL_BOSSES && !finalBossesTriggered && !finalBossFightActive) {
                    spawnBossesForWave(3);
                    showTempMessage("FINAL BOSSES!", WIDTH / 2.0, HEIGHT / 3.0, 2.5);
                }

                if (finalBossFightActive && bossesDefeatedThisWave >= bossesToDefeatInWave) {
                    System.out.println("ALL FINAL BOSSES DEFEATED! YOU WIN! Final Score: " + score);
                    gameRunning = false;
                    if (bossDefeatedSound != null) bossDefeatedSound.play();
                    Platform.runLater(this::showGameWonScreen);
                }
                break;
        }
    }


    /**
     * Spawns bosses for the given wave number.
     * Resets the count of defeated bosses and sets the appropriate flags.
     *
     * @param waveNum the wave number to spawn bosses for
     */
    private void spawnBossesForWave(int waveNum) {
        bossesDefeatedThisWave = 0;
        if (waveNum == 1) {
            BossEnemy boss = new BossEnemy(WIDTH / 2.0, 120, this.player, false);
            newObjects.add(boss);
            waveBossFightActive = true;
            finalBossFightActive = false;
            bossesToDefeatInWave = 1;
        } else if (waveNum == 2) {
            BossEnemy boss1 = new BossEnemy(WIDTH / 4.0 + 30, 120, this.player, false);
            BossEnemy boss2 = new BossEnemy(WIDTH * 3.0 / 4.0 - 30, 150, this.player, false);
            newObjects.add(boss1);
            newObjects.add(boss2);
            waveBossFightActive = true;
            finalBossFightActive = false;
            bossesToDefeatInWave = 2;
        } else if (waveNum == 3) {
            BossEnemy bossA = new BossEnemy(WIDTH / 4.0, 100, this.player, false);
            BossEnemy bossB = new BossEnemy(WIDTH / 2.0, 130, this.player, false);
            BossEnemy bossC = new BossEnemy(WIDTH * 3.0 / 4.0, 100, this.player, false);
            newObjects.add(bossA);
            newObjects.add(bossB);
            newObjects.add(bossC);
            finalBossesTriggered = true;
            finalBossFightActive = true;
            waveBossFightActive = false;
            bossesToDefeatInWave = 3;
        }
    }

    /**
     * Spawns a stationary boss if not already spawned or if the previous one is dead.
     */
    private void spawnStationaryBoss() {
        if (!stationaryBossSpawned && (stationaryBossInstance == null || stationaryBossInstance.isDead())) {
            stationaryBossInstance = new BossEnemy(WIDTH / 2.0, 100, this.player, true);
            newObjects.add(stationaryBossInstance);
            stationaryBossSpawned = true;
            stationaryBossDefeated = false;
        }
    }

    /**
     * Checks all relevant collisions:
     * - Player bullets vs enemies (including bosses)
     * - Player vs enemies (direct collision)
     * - Player vs enemy bullets (normal and boss bullets)
     * - Player vs power-ups
     */
    private void checkCollisions() {
        // Player bullets vs enemies
        for (GameObject bulletObj : new ArrayList<>(gameObjects)) {
            if (bulletObj instanceof Bullet && !bulletObj.isDead()) {
                Bullet bullet = (Bullet) bulletObj;
                for (GameObject enemyObj : new ArrayList<>(gameObjects)) {
                    if (enemyObj instanceof Enemy && !enemyObj.isDead()) {
                        Enemy enemy = (Enemy) enemyObj;
                        if (bullet.getBounds().intersects(enemy.getBounds())) {
                            createProceduralExplosion(bullet.getX(), bullet.getY());
                            bullet.setDead(true);
                            if (enemy instanceof BossEnemy) {
                                BossEnemy bossHit = (BossEnemy) enemy;
                                bossHit.reduceHealth(1);
                            } else {
                                createProceduralExplosion(enemy.getX(), enemy.getY());
                                enemy.setDead(true);
                                score += 10;
                            }
                        }
                    }
                }
            }
        }

        if (!player.isDead()) {
            // Player vs enemies (direct collision)
            for (GameObject enemyObj : new ArrayList<>(gameObjects)) {
                if (enemyObj instanceof Enemy && !enemyObj.isDead()) {
                    if (player.getBounds().intersects(enemyObj.getBounds())) {
                        if (!player.isShieldActive() && !player.isTempInvincible()) {
                            createProceduralExplosion(player.getX(), player.getY());
                            SpaceShooter.numLives--;
                            player.resetMovementFlags();
                            player.setPosition(WIDTH / 2.0, HEIGHT - 120);
                            if (SpaceShooter.numLives <= 0) {
                                player.setDead(true);
                            } else {
                                player.activateTempInvincibility();
                            }
                        } else {
                            if (player.isShieldActive()) System.out.println("Shield blocked direct enemy collision!");
                            if (player.isTempInvincible()) System.out.println("Temp Invincibility blocked direct enemy collision!");
                        }
                        if (!(enemyObj instanceof BossEnemy)) {
                            createProceduralExplosion(enemyObj.getX(), enemyObj.getY());
                            enemyObj.setDead(true);
                        }
                        if (player.isDead()) break;
                    }
                }
            }

            // Player vs enemy bullets (normal)
            if (!player.isDead()) {
                for (GameObject enemyBulletObj : new ArrayList<>(gameObjects)) {
                    if (enemyBulletObj instanceof EnemyBullet && !enemyBulletObj.isDead()) {
                        if (player.getBounds().intersects(enemyBulletObj.getBounds())) {
                            if (!player.isShieldActive() && !player.isTempInvincible()) {
                                createProceduralExplosion(player.getX(), player.getY());
                                SpaceShooter.numLives--;
                                player.resetMovementFlags();
                                player.setPosition(WIDTH / 2.0, HEIGHT - 120);
                                if (SpaceShooter.numLives <= 0) {
                                    player.setDead(true);
                                } else {
                                    player.activateTempInvincibility();
                                }
                            } else {
                                if (player.isShieldActive()) System.out.println("Shield blocked an enemy bullet!");
                                if (player.isTempInvincible()) System.out.println("Temp Invincibility blocked an enemy bullet!");
                            }
                            createProceduralExplosion(enemyBulletObj.getX(), enemyBulletObj.getY());
                            enemyBulletObj.setDead(true);
                            if (player.isDead()) break;
                        }
                    }
                }
            }

            // Player vs boss bullets
            if (!player.isDead()) {
                for (GameObject gameObject : new ArrayList<>(gameObjects)) {
                    if (gameObject instanceof BossEnemy && !gameObject.isDead()) {
                        BossEnemy boss = (BossEnemy) gameObject;
                        List<EnemyBullet> bossBullets = boss.getBullets();
                        if (bossBullets != null) {
                            for (EnemyBullet bossBullet : new ArrayList<>(bossBullets)) {
                                if (bossBullet.isDead()) continue;
                                if (player.getBounds().intersects(bossBullet.getBounds())) {
                                    if (!player.isShieldActive() && !player.isTempInvincible()) {
                                        createProceduralExplosion(player.getX(), player.getY());
                                        SpaceShooter.numLives--;
                                        player.resetMovementFlags();
                                        player.setPosition(WIDTH / 2.0, HEIGHT - 120);
                                        if (SpaceShooter.numLives <= 0) {
                                            player.setDead(true);
                                        } else {
                                            player.activateTempInvincibility();
                                        }
                                    } else {
                                        if (player.isShieldActive()) System.out.println("Shield blocked a BOSS bullet!");
                                        if (player.isTempInvincible()) System.out.println("Temp Invincibility blocked a BOSS bullet!");
                                    }
                                    createProceduralExplosion(bossBullet.getX(), bossBullet.getY());
                                    bossBullet.setDead(true);
                                    if (player.isDead()) break;
                                }
                            }
                        }
                        if (player.isDead()) break;
                    }
                }
            }

            // Player vs power-ups
            if (!player.isDead()) {
                for (GameObject powerUpObj : new ArrayList<>(gameObjects)) {
                    if (powerUpObj instanceof PowerUp && !powerUpObj.isDead()) {
                        PowerUp powerUp = (PowerUp) powerUpObj;
                        if (player.getBounds().intersects(powerUp.getBounds())) {
                            if (powerUpCollectedSound != null) {
                                powerUpCollectedSound.setVolume(0.6);
                                powerUpCollectedSound.play();
                            }
                            switch (powerUp.getType()) {
                                case HEALTH_PACK:
                                    if (SpaceShooter.numLives < 5) SpaceShooter.numLives++;
                                    score += 5;
                                    System.out.println("Player collected Health Pack!");
                                    break;
                                case SHIELD:
                                    player.activateShield();
                                    break;
                                case TRIPLE_SHOT:
                                    player.activateTripleShot();
                                    break;
                            }
                            powerUp.setDead(true);
                        }
                    }
                }
            }
        }
    }



    /**
     * Checks all game entities to determine if they have moved out of the visible screen bounds.
     * Marks entities as dead if they are out of bounds, and updates lives or counters accordingly.
     */
    private void checkEntitiesOutOfBounds() {
        for (GameObject obj : gameObjects) {
            if (obj.isDead()) continue;

            if (obj instanceof Enemy) {
                if (obj.getY() - obj.getHeight() / 2 > HEIGHT) {
                    if (!obj.isDead()) {
                        obj.setDead(true);
                        if (!(obj instanceof BossEnemy)) {
                            SpaceShooter.numLives--;
                        } else {
                            if (obj != stationaryBossInstance) {
                                if (waveBossFightActive || finalBossFightActive) {
                                    bossesDefeatedThisWave++;
                                }
                                SpaceShooter.numLives -= 2;
                                System.out.println("DEBUG: A non-stationary Boss escaped. Lives: " + SpaceShooter.numLives + ". Defeated count for wave: " + bossesDefeatedThisWave);
                            }
                        }
                        if (SpaceShooter.numLives <= 0) player.setDead(true);
                    }
                }
            } else if (obj instanceof Bullet && (obj.getY() + obj.getHeight() / 2) < 0) {
                obj.setDead(true);
            } else if ((obj instanceof EnemyBullet || obj instanceof PowerUp) && (obj.getY() - obj.getHeight() / 2) > HEIGHT) {
                obj.setDead(true);
            }
        }
    }

    /**
     * Resets the game state and mechanics to start a new game session.
     * Resets scores, lives, timers, wave states, background positions, difficulty, and player state.
     */
    private void restartGameMechanics() {
        this.score = 0;
        SpaceShooter.numLives = 3;
        this.enemySpawnTimer = 0;
        this.powerUpSpawnTimer = 0;
        this.waveNumber = 1;
        this.waveBossFightActive = false;
        this.bossesToDefeatInWave = 0;
        this.bossesDefeatedThisWave = 0;
        this.stationaryBossInstance = null;
        this.stationaryBossSpawned = false;
        this.stationaryBossDefeated = false;
        this.finalBossesTriggered = false;
        this.finalBossFightActive = false;

        if (backgroundImage != null && actualBackgroundImageHeight > 0) {
            this.backgroundY1 = 0;
            this.backgroundY2 = -actualBackgroundImageHeight;
        } else if (backgroundImage != null && backgroundImage.getHeight() > 0) {
            actualBackgroundImageHeight = backgroundImage.getHeight();
            this.backgroundY1 = 0;
            this.backgroundY2 = -actualBackgroundImageHeight;
        }

        adjustDifficulty();
        player.setPosition(WIDTH / 2.0, HEIGHT - 120);
        player.setDead(false);
        player.resetMovementFlags();
        gameObjects.clear();
        newObjects.clear();
        gameObjects.add(player);
    }

    /**
     * Creates the main menu pane with title and buttons for starting the game,
     * viewing instructions, and quitting the application.
     *
     * @return a Pane containing the menu UI components
     */
    private Pane createMenu() {
        VBox menuLayout = new VBox(30);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setPadding(new Insets(50, 20, 50, 20));
        menuLayout.setStyle("-fx-background-color: #0A2463;");

        Label titleLabel = new Label("Welcome to\nSpace Shooter!");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#ADD8E6"));
        titleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        titleLabel.setWrapText(true);

        Button startButton = createStyledMenuButton("START");
        startButton.setOnAction(e -> startGame());

        Button instructionsButton = createStyledMenuButton("INSTRUCTIONS");
        instructionsButton.setOnAction(e -> showInstructions());

        Button quitButton = createStyledMenuButton("QUIT");
        quitButton.setOnAction(e -> Platform.exit());

        menuLayout.getChildren().addAll(titleLabel, startButton, instructionsButton, quitButton);
        return menuLayout;
    }


    /**
     * Creates a styled menu button with specified text.
     * The button has custom font, colors, rounded corners, border, padding, and drop shadow effects.
     * It also changes style on mouse hover.
     *
     * @param text the text to display on the button
     * @return the styled Button instance
     */
    private Button createStyledMenuButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        button.setTextFill(Color.WHITE);
        button.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #4D2DB7, #8A2BE2);" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: #E0B0FF;" +
                        "-fx-border-radius: 30;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 12 25 12 25;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 3, 3);"
        );
        button.setPrefWidth(220);

        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #5D3DC7, #9A3CE2);" +
                        "-fx-background-radius: 30;-fx-border-color: white;-fx-border-radius: 30;-fx-border-width: 2;" +
                        "-fx-padding: 12 25 12 25;-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 5, 5);"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #4D2DB7, #8A2BE2);" +
                        "-fx-background-radius: 30;-fx-border-color: #E0B0FF;-fx-border-radius: 30;-fx-border-width: 2;" +
                        "-fx-padding: 12 25 12 25;-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 3, 3);"
        ));
        return button;
    }

    /**
     * Starts a new game by resetting game mechanics, marking the game as running,
     * switching to the game scene, and starting the game loop.
     * Displays a temporary message indicating the start of the first wave.
     */
    private void startGame() {
        restartGameMechanics();
        gameRunning = true;
        this.gamePaused = false; // Đảm bảo game bắt đầu không bị pause
        primaryStage.setScene(gameScene);
        if (gameLoop != null) {
            gameLoop.start();
        }
        showTempMessage("WAVE 1", WIDTH / 2.0, HEIGHT / 3.0, 2.5);
    }

    /**
     * Stops the current game session and shows the losing screen.
     */
    private void resetGame() {
        gameRunning = false;
        this.gamePaused = false; // Đảm bảo game không bị pause khi hiển thị màn hình thua
        showLosingScreen();
    }

    /**
     * Displays the "Game Over" screen with final score and options to retry or exit.
     * Sets up UI components including labels and buttons with appropriate styles and event handlers.
     */
    private void showLosingScreen() {
        VBox gameOverLayout = new VBox(25);
        gameOverLayout.setAlignment(Pos.CENTER);
        gameOverLayout.setPadding(new Insets(50));
        gameOverLayout.setStyle("-fx-background-color: #1A1A1A;");

        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setFont(Font.font("Arial", FontWeight.BOLD, 52));
        gameOverLabel.setTextFill(Color.RED);

        Label finalScoreLabel = new Label("Your Score: " + score);
        finalScoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        finalScoreLabel.setTextFill(Color.WHITE);

        Button tryAgainButton = new Button("Try Again");
        tryAgainButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tryAgainButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20;");
        tryAgainButton.setPrefWidth(180);
        tryAgainButton.setOnAction(e -> startGame());

        Button exitButton = new Button("Exit Game");
        exitButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        exitButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20;");
        exitButton.setPrefWidth(180);
        exitButton.setOnAction(e -> primaryStage.setScene(menuScene));

        gameOverLayout.getChildren().addAll(gameOverLabel, finalScoreLabel, tryAgainButton, exitButton);

        if (gameOverScene == null) {
            gameOverScene = new Scene(gameOverLayout, WIDTH, HEIGHT);
        } else {
            gameOverScene.setRoot(gameOverLayout);
        }
        primaryStage.setScene(gameOverScene);
    }


    /**
     * Hiển thị màn hình khi người chơi chiến thắng.
     * Giao diện có thông báo chiến thắng, điểm số cuối cùng,
     * và các nút để chơi lại hoặc quay về menu chính.
     */
    private void showGameWonScreen() {
        VBox winLayout = new VBox(20);
        winLayout.setAlignment(Pos.CENTER);
        winLayout.setStyle("-fx-background-color: #2E7D32; -fx-padding: 30px;");

        Label winMsg = new Label("YOU WIN!");
        winMsg.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        winMsg.setTextFill(Color.GOLD);

        Label finalScoreMsg = new Label("Final Score: " + this.score);
        finalScoreMsg.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        finalScoreMsg.setTextFill(Color.WHITE);

        Button playAgainButton = new Button("Play Again");
        playAgainButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        String baseStyle = createStyledMenuButton("").getStyle();
        // Thay đổi màu gradient từ màu mặc định sang màu xanh lá
        playAgainButton.setStyle(baseStyle.replace("#4D2DB7", "#4CAF50").replace("#8A2BE2", "#8BC34A"));
        playAgainButton.setOnAction(e -> startGame());
        playAgainButton.setPrefWidth(200);

        Button backToMenuButton = new Button("Back to Menu");
        backToMenuButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        // Thay đổi màu gradient sang màu cam
        backToMenuButton.setStyle(baseStyle.replace("#4D2DB7", "#FF9800").replace("#8A2BE2", "#FFC107"));
        backToMenuButton.setOnAction(e -> primaryStage.setScene(menuScene));
        backToMenuButton.setPrefWidth(200);

        winLayout.getChildren().addAll(winMsg, finalScoreMsg, playAgainButton, backToMenuButton);
        this.gamePaused = false; // Đảm bảo không bị che bởi màn hình pause
        Scene winScene = new Scene(winLayout, WIDTH, HEIGHT);
        primaryStage.setScene(winScene);
    }

    /**
     * Hiển thị hộp thoại hướng dẫn chơi game.
     * Bao gồm các phím điều khiển và các thông tin cơ bản về gameplay.
     */
    private void showInstructions() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Instructions");
        dialog.setHeaderText("Space Shooter Instructions");

        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(20));

        String[] instructionsText = {
                "Use A, W, S, D or Arrow keys to move your spaceship.",
                "Press SPACE to shoot bullets and destroy enemies.",
                "Press SHIFT to toggle AI control mode.",
                "Press P to PAUSE or RESUME the game.",
                "If an enemy reaches the bottom of the screen, you lose a life.",
                "The game ends if you lose all lives.",
                "Collect power-ups to increase your score or gain benefits (max 5 lives).",
                "Defeat bosses and complete waves to achieve a high score!",
                "Good luck and have fun!"
        };

        for (String line : instructionsText) {
            Label lineLabel = new Label(line);
            lineLabel.setWrapText(true);
            lineLabel.setTextFill(Color.BLACK);
            dialogContent.getChildren().add(lineLabel);
        }

        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        dialog.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #F0F0F0;");
        dialog.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        dialog.showAndWait();
    }

    /**
     * Khởi tạo các handler sự kiện bàn phím cho một Scene cụ thể.
     * - SHIFT: bật/tắt chế độ AI điều khiển.
     * - P: bật/tắt tạm dừng game.
     * - Các phím di chuyển và bắn chỉ có tác dụng khi AI tắt, game đang chạy và không bị tạm dừng.
     *
     * @param targetScene Scene cần gán handler sự kiện
     */
    private void initEventHandlers(Scene targetScene) {
        targetScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SHIFT) {
                // Chuyển đổi chế độ AI điều khiển
                isAIControlled = !isAIControlled;
                player.resetMovementFlags(); // Reset trạng thái di chuyển
                player.setWantsToShoot(false);
                System.out.println("AI Control: " + isAIControlled);
                if (isAIControlled) {
                    // Xóa focus khỏi các nút để tránh bắt phím space
                    gameRootPane.requestFocus();
                }
                event.consume();
                return; // Không xử lý thêm các phím khác trong trường hợp SHIFT
            }

            if (event.getCode() == KeyCode.P && gameRunning) { // Chỉ cho phép pause khi game đang chạy
                togglePause();
                event.consume(); // Ngăn sự kiện được xử lý thêm
                return;
            }

            if (!isAIControlled && gameRunning && !player.isDead() && !gamePaused) {
                // Xử lý các phím điều khiển khi AI tắt, game đang chạy, player không chết VÀ KHÔNG PAUSE
                switch (event.getCode()) {
                    case LEFT, A -> player.setMoveLeft(true);
                    case RIGHT, D -> player.setMoveRight(true);
                    case UP, W -> player.setMoveForward(true);
                    case DOWN, S -> player.setMoveBackward(true);
                    case SPACE -> player.setWantsToShoot(true);
                }
            }
        });

        targetScene.setOnKeyReleased(event -> {
            if (!isAIControlled && !gamePaused) { // Chỉ xử lý khi nhả phím nếu không AI và không pause
                // Xử lý khi nhả phím để dừng di chuyển
                switch (event.getCode()) {
                    case LEFT, A -> player.setMoveLeft(false);
                    case RIGHT, D -> player.setMoveRight(false);
                    case UP, W -> player.setMoveForward(false);
                    case DOWN, S -> player.setMoveBackward(false);
                    // Không cần xử lý khi nhả phím space
                }
            }
        });
    }

    /**
     * Chuyển đổi trạng thái tạm dừng/tiếp tục của game.
     */
    private void togglePause() {
        gamePaused = !gamePaused;
        if (gamePaused) {
            System.out.println("Game Paused");
            // Tùy chọn: Dừng âm thanh nếu có thể (AudioClip không hỗ trợ trực tiếp)
        } else {
            System.out.println("Game Resumed");
            // Tùy chọn: Tiếp tục âm thanh
            // Đảm bảo player không bị kẹt trạng thái di chuyển/bắn từ trước khi pause
            if (!isAIControlled) { // Nếu người chơi đang điều khiển
                player.resetMovementFlags(); // Reset các cờ di chuyển
                player.setWantsToShoot(false); // Không muốn bắn ngay khi resume
            }
        }
    }


    // Label dùng để hiển thị thông báo tạm thời trên màn hình
    private Label tempMessageLabel = null;

    // Timer dùng để ẩn thông báo sau khoảng thời gian nhất định
    private javafx.animation.PauseTransition tempMessageTimer = null;

    /**
     * Hiển thị một thông báo tạm thời trên giao diện game ở vị trí ngang giữa,
     * với tọa độ y và thời gian hiển thị cụ thể.
     *
     * @param message         Nội dung thông báo
     * @param x               Tọa độ x (hiện đang không dùng, vì thông báo căn giữa ngang)
     * @param y               Tọa độ y (chiều cao của label)
     * @param durationSeconds Thời gian hiển thị (giây)
     */
    private void showTempMessage(String message, double x, double y, double durationSeconds) {
        // Nếu đã có label hiện thông báo trước đó, xóa nó khỏi gameRootPane để chuẩn bị hiển thị mới
        if (tempMessageLabel != null && gameRootPane.getChildren().contains(tempMessageLabel)) {
            gameRootPane.getChildren().remove(tempMessageLabel);
        }
        // Dừng timer trước đó nếu còn đang chạy để tránh xung đột
        if (tempMessageTimer != null) {
            tempMessageTimer.stop();
        }

        // Tạo Label mới với nội dung và định dạng
        tempMessageLabel = new Label(message);
        tempMessageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        tempMessageLabel.setTextFill(Color.ORANGERED);

        // Căn giữa label theo chiều ngang của gameRootPane
        tempMessageLabel.layoutXProperty().bind(
                gameRootPane.widthProperty().subtract(tempMessageLabel.widthProperty()).divide(2)
        );
        // Đặt vị trí theo chiều dọc
        tempMessageLabel.setLayoutY(y);

        // Thêm label vào root nếu chưa có
        if (!gameRootPane.getChildren().contains(tempMessageLabel)) {
            gameRootPane.getChildren().add(tempMessageLabel);
        }

        // Tạo PauseTransition để ẩn label sau khi hết thời gian durationSeconds
        tempMessageTimer = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(durationSeconds));
        tempMessageTimer.setOnFinished(event -> {
            if (tempMessageLabel != null && gameRootPane.getChildren().contains(tempMessageLabel)) {
                gameRootPane.getChildren().remove(tempMessageLabel);
                tempMessageLabel = null;
            }
        });
        tempMessageTimer.play();
    }
}