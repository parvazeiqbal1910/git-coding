import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * FlappyCat - simple Flappy Bird style game with a cat.
 * Single-file Java Swing game. Controls: mouse click or SPACE to flap.
 */
public class FlappyCat extends JPanel implements ActionListener, MouseListener, KeyListener {
    // Window size
    static final int WIDTH = 480;
    static final int HEIGHT = 640;

    // Game loop timer (ms)
    Timer timer;

    // Cat properties
    int catX = WIDTH / 4;
    int catY = HEIGHT / 2;
    int catSize = 36;
    double velocity = 0;
    double gravity = 0.55;
    double flapStrength = -9.0;

    // Pipes
    class Pipe { int x, gapY, width = 80, gapHeight = 160; boolean passed = false; }
    ArrayList<Pipe> pipes = new ArrayList<>();
    int pipeSpacing = 200;
    int pipeSpeed = 4;
    Random rand = new Random();

    // Game state
    boolean running = false;
    boolean gameOver = false;
    int score = 0;
    int highScore = 0;
    int tick = 0;

    public FlappyCat() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(120, 200, 255)); // sky-like
        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);

        // Start timer
        timer = new Timer(17, this); // ~60 FPS
        timer.start();
        resetGame();
    }

    void resetGame() {
        catY = HEIGHT / 2;
        velocity = 0;
        pipes.clear();
        score = 0;
        gameOver = false;
        running = true;
        tick = 0;

        // Add a couple of initial pipes
        for (int i = 0; i < 3; i++) addPipe(WIDTH + i * pipeSpacing);
    }

    void addPipe(int startX) {
        Pipe p = new Pipe();
        p.x = startX;
        p.gapY = 120 + rand.nextInt(HEIGHT - 300);
        pipes.add(p);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!running) return;

        tick++;

        // Physics
        velocity += gravity;
        catY += (int) velocity;

        // Move pipes
        for (Pipe p : pipes) {
            p.x -= pipeSpeed;
            // check pass for scoring
            if (!p.passed && p.x + p.width < catX) {
                p.passed = true;
                score++;
                if (score > highScore) highScore = score;
            }
        }

        // Remove pipes off screen and add new ones
        if (!pipes.isEmpty() && pipes.get(0).x + pipes.get(0).width < 0) {
            pipes.remove(0);
            addPipe(pipes.get(pipes.size()-1).x + pipeSpacing);
        }

        // Increase difficulty slowly
        if (tick % 1000 == 0 && pipeSpeed < 9) pipeSpeed++;

        // Collisions
        for (Pipe p : pipes) {
            Rectangle catRect = new Rectangle(catX - catSize/2, catY - catSize/2, catSize, catSize);
            Rectangle topRect = new Rectangle(p.x, 0, p.width, p.gapY);
            Rectangle bottomRect = new Rectangle(p.x, p.gapY + p.gapHeight, p.width, HEIGHT - (p.gapY + p.gapHeight));
            if (catRect.intersects(topRect) || catRect.intersects(bottomRect)) {
                gameOver = true;
                running = false;
            }
        }

        // Ground & ceiling collisions
        if (catY - catSize/2 < 0) {
            catY = catSize/2;
            velocity = 0;
        }
        if (catY + catSize/2 > HEIGHT - 48) { // ground zone
            catY = HEIGHT - 48 - catSize/2;
            gameOver = true;
            running = false;
        }

        repaint();
    }

    // Draw cat as a simple cartoon (circle + ears + face)
    void drawCat(Graphics2D g) {
        int cx = catX;
        int cy = catY;
        int s = catSize;

        // body (circle)
        g.setColor(new Color(255, 200, 120));
        g.fillOval(cx - s/2, cy - s/2, s, s);

        // ears (triangles)
        int earSize = s/3;
        Polygon leftEar = new Polygon(
                new int[]{cx - s/4 - earSize/2, cx - s/4 + earSize/2, cx - s/4},
                new int[]{cy - s/2, cy - s/2, cy - s/2 - earSize},
                3);
        Polygon rightEar = new Polygon(
                new int[]{cx + s/4 - earSize/2, cx + s/4 + earSize/2, cx + s/4},
                new int[]{cy - s/2, cy - s/2, cy - s/2 - earSize},
                3);
        g.setColor(new Color(200, 140, 80));
        g.fill(leftEar);
        g.fill(rightEar);

        // eyes
        g.setColor(Color.black);
        int eyeW = s/8;
        int eyeH = s/8;
        g.fillOval(cx - s/6 - eyeW/2, cy - s/10 - eyeH/2, eyeW, eyeH);
        g.fillOval(cx + s/12 - eyeW/2, cy - s/10 - eyeH/2, eyeW, eyeH);

        // nose
        g.setColor(new Color(180, 80, 120));
        int noseW = s/10;
        g.fillOval(cx - noseW/2, cy + s/20, noseW, noseW);

        // whiskers
        g.setStroke(new BasicStroke(2));
        g.drawLine(cx - s/2 + 6, cy + s/8, cx - s/8, cy + s/8);
        g.drawLine(cx + s/8, cy + s/8, cx + s/2 - 6, cy + s/8);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;

        // smooth
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // background already set by panel; draw some clouds (simple)
        for (int i = 0; i < 4; i++) {
            int cx = (i * 140 + (tick/6 % 480));
            int cy = 60 + (i%2)*30;
            g.setColor(new Color(255, 255, 255, 200));
            g.fillOval((cx + 20) % WIDTH, cy, 60, 30);
            g.fillOval((cx + 40) % WIDTH, cy-6, 60, 36);
            g.fillOval((cx) % WIDTH, cy+4, 50, 26);
        }

        // draw pipes
        for (Pipe p : pipes) {
            g.setColor(new Color(80, 180, 90));
            // top pipe
            g.fillRect(p.x, 0, p.width, p.gapY);
            // bottom pipe
            g.fillRect(p.x, p.gapY + p.gapHeight, p.width, HEIGHT - (p.gapY + p.gapHeight));
            // pipe rim
            g.setColor(new Color(60, 150, 70));
            g.fillRect(p.x, p.gapY - 8, p.width, 8);
            g.fillRect(p.x, p.gapY + p.gapHeight, p.width, 8);
        }

        // ground
        g.setColor(new Color(60, 180, 75));
        g.fillRect(0, HEIGHT - 48, WIDTH, 48);
        g.setColor(new Color(150, 100, 60));
        g.fillRect(0, HEIGHT - 48, WIDTH, 6);

        // draw cat
        drawCat(g);

        // HUD - score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.drawString(String.valueOf(score), WIDTH/2 - 10, 60);

        // Game over text
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 190));
            g.fillRect(40, HEIGHT/2 - 70, WIDTH - 80, 140);
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Game Over", WIDTH/2 - 80, HEIGHT/2 - 18);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Score: " + score, WIDTH/2 - 50, HEIGHT/2 + 8);
            g.drawString("High: " + highScore, WIDTH/2 - 50, HEIGHT/2 + 32);
            g.drawString("Click or press SPACE to restart", WIDTH/2 - 150, HEIGHT/2 + 66);
        } else if (!running) {
            g.setColor(new Color(0,0,0,120));
            g.fillRect(24, HEIGHT/2 - 70, WIDTH - 48, 140);
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.drawString("Click or press SPACE to flap", WIDTH/2 - 140, HEIGHT/2 - 8);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.drawString("Try to fly the cat through the gaps!", WIDTH/2 - 120, HEIGHT/2 + 18);
        }
    }

    // Input: flap when clicked or SPACE pressed
    void flap() {
        if (gameOver) {
            resetGame();
            return;
        }
        running = true;
        velocity = flapStrength;
    }

    @Override public void mouseClicked(MouseEvent e) { flap(); }
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) flap();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Flappy Cat");
            FlappyCat game = new FlappyCat();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.getContentPane().add(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            // Helpful focus
            game.requestFocusInWindow();
        });
    }
}