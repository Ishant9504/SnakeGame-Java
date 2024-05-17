import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

class GamePanel extends JPanel implements ActionListener {
    private static final int SCREEN_WIDTH = 600;
    private static final int SCREEN_HEIGHT = 600;
    private static final int UNIT_SIZE = 25;
    private static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    private static final int DELAY = 75;

    private final int[] x = new int[GAME_UNITS];
    private final int[] y = new int[GAME_UNITS];
    private int bodyParts;
    private int applesEaten;
    private int appleX;
    private int appleY;
    private char direction;
    private boolean running = false;
    private boolean gameOver = false;
    private Timer timer;
    private final Random random;
    private Clip backgroundMusicClip;
    private int highestScore = 0;

    private JButton playAgainButton;
    private JButton exitButton;
    private JLabel highestScoreLabel;

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.setLayout(null);

        this.addKeyListener(new MyKeyAdapter());

        highestScoreLabel = new JLabel("Highest Score: " + highestScore);
        highestScoreLabel.setForeground(Color.WHITE);
        highestScoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        highestScoreLabel.setBounds(10, 10, 200, 30);
        this.add(highestScoreLabel);

        loadHighestScore();
        
        
        playAgainButton = new JButton("Play Again");
        playAgainButton.setBounds(SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2 - 25, 200, 50);
        playAgainButton.setFocusable(false);
        playAgainButton.addActionListener(e -> startGame());
        playAgainButton.setVisible(false);
        this.add(playAgainButton);

        
        exitButton = new JButton("Exit");
        exitButton.setBounds(SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2 + 35, 200, 50);
        exitButton.setFocusable(false);
        exitButton.addActionListener(e -> System.exit(0));
        exitButton.setVisible(false);
        this.add(exitButton);

        
        showStartScreen();
    }

    public void startGame() {
        applesEaten = 0;
        bodyParts = 6;
        direction = 'R';
        gameOver = false;  

        playAgainButton.setVisible(false);
        exitButton.setVisible(false);
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
        playBackgroundMusic();
        this.requestFocusInWindow();
    }

    public void showStartScreen() {
        running = false;
        gameOver = false;
        repaint();
    }

    public void playBackgroundMusic() {
        try {
            File musicPath = new File("bgmusic.wav");
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                backgroundMusicClip = AudioSystem.getClip();
                backgroundMusicClip.open(audioInput);
                backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                System.out.println("Can't find bgmusic.wav");
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void playSoundEffect(String soundFileName) {
        try {
            File soundPath = new File(soundFileName);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
            } else {
                System.out.println("Can't find " + soundFileName);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            for (int i = 0; i < SCREEN_WIDTH / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
            }
            for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
                g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
            }

            g.setColor(Color.red);
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    g.setColor(new Color(45, 180, 0));
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }

            g.setColor(Color.red);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());
        } else if (gameOver) {
            gameOver(g);
            playAgainButton.setVisible(true);
            exitButton.setVisible(true);
        } else {
            // Draw start screen
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 75));
            FontMetrics metrics1 = getFontMetrics(g.getFont());
            g.drawString("Snake Game", (SCREEN_WIDTH - metrics1.stringWidth("Snake Game")) / 2, SCREEN_HEIGHT / 2 - 50);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            FontMetrics metrics2 = getFontMetrics(g.getFont());
            g.drawString("Press Space to Start", (SCREEN_WIDTH - metrics2.stringWidth("Press Space to Start")) / 2, SCREEN_HEIGHT / 2 + 50);
        }
    }

    public void newApple() {
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                if (y[0] < 0) {
                    y[0] = SCREEN_HEIGHT - UNIT_SIZE;
                }
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                if (y[0] >= SCREEN_HEIGHT) {
                    y[0] = 0;
                }
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                if (x[0] < 0) {
                    x[0] = SCREEN_WIDTH - UNIT_SIZE;
                }
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                if (x[0] >= SCREEN_WIDTH) {
                    x[0] = 0;
                }
                break;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
            playSoundEffect("apple.wav");
        }
    }

    public void checkCollision() {
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
                gameOver = true;  
                playSoundEffect("game_over.wav");
            }
        }

        if (!running) {
            timer.stop();
            if (backgroundMusicClip != null) {
                backgroundMusicClip.stop();
            }
            if (applesEaten > highestScore) {
                highestScore = applesEaten;
                highestScoreLabel.setText("Highest Score: " + highestScore);
                saveHighestScore();
            }
        }
    }

    public void gameOver(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 75));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics1.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2 - 50);

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics2.stringWidth("Score: " + applesEaten)) / 2, SCREEN_HEIGHT / 2 + 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollision();
        }
        repaint();
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    if (!running && !gameOver) {
                        startGame();
                    }
                    break;
            }
        }
    }

    private void loadHighestScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
            String line = reader.readLine();
            if (line != null) {
                highestScore = Integer.parseInt(line);
                highestScoreLabel.setText("Highest Score: " + highestScore);
            }
        } catch (IOException e) {
            System.out.println("Could not load highest score");
        }
    }

    private void saveHighestScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"))) {
            writer.write(String.valueOf(highestScore));
        } catch (IOException e) {
            System.out.println("Could not save highest score");
        }
    }
}
