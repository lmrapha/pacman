package fun.pacman;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.*;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Model class containing the logic of the Pacman Game
 *
 * @author lmr
 */
public class Model extends JPanel implements ActionListener, Serializable {

    private Dimension dimension;
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);
    private boolean inGame = false;
    private boolean isDying = false;
    private static final int BLOCK_SIZE = 24;
    private static final int BLOCKS = 15;
    private static final int SCREEN_SIZE = BLOCKS * BLOCK_SIZE;
    private static final int MAX_GHOSTS = 12;
    private static final int PACMAN_SPEED = 6;
    private int ghosts = 6;
    private int lives;
    private int score;
    private int[] deltaX;
    private int[] deltaY;
    private int[] ghostX;
    private int[] ghostY;
    private int[] ghostDeltaX;
    private int[] ghostDeltaY;
    private int[] ghostSpeed;
    private Image heart;
    private Image ghost;
    private Image up;
    private Image down;
    private Image left;
    private Image right;
    private int pacmanX;
    private int pacmanY;
    private int pacmanDeltaX;
    private int pacmanDeltaY;
    private int reqDeltaX;
    private int reqDeltaY;

    /**
     * 0 = BLUE
     * 1 = LEFT BORDER
     * 2 = TOP BORDER
     * 4 RIGHT BORDER
     * 8 BOTTOM BORDER
     * 16 WHITE DOTS
     */
    private final short levelData[] = {
            19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 28, 0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            0,  0,  0,  0,  0,  0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            19, 18, 18, 18, 18, 18, 16, 16, 16, 16, 24, 24, 24, 24, 20,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 24, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18, 18, 18, 18, 20,
            17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
            21, 0,  0,  0,  0,  0,  0,   0, 17, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
    };

    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};
    private static final int MAX_SPEED = 6;
    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    public Model() {

        loadImages();
        initVariables();
        addKeyListener(new TAdapter());
        setFocusable(true);
        initGame();
    }

    private void loadImages() {
        down = new ImageIcon("src/main/java/fun/pacman/images/down.gif").getImage();
        up = new ImageIcon("src/main/java/fun/pacman/images/up.gif").getImage();
        left = new ImageIcon("src/main/java/fun/pacman/images/left.gif").getImage();
        right = new ImageIcon("src/main/java/fun/pacman/images/right.gif").getImage();
        ghost = new ImageIcon("src/main/java/fun/pacman/images/ghost.gif").getImage();
        heart = new ImageIcon("src/main/java/fun/pacman/images/heart.png").getImage();
    }

    private void initVariables() {
        screenData = new short[BLOCKS * BLOCKS];
        dimension = new Dimension(400, 400);

        ghostX = new int[MAX_GHOSTS];
        ghostDeltaX = new int[MAX_GHOSTS];
        ghostY = new int[MAX_GHOSTS];
        ghostDeltaY = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];

        deltaX = new int[4];
        deltaY = new int[4];

        timer = new Timer(40, this);
        timer.start();

    }

     private void playGame(Graphics2D graphics2D) {

        if (isDying) {
            death();
        } else {
            movePacman();
            drawPacman(graphics2D);
            moveGhosts(graphics2D);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D graphics2D) {
        String start = "Press SPACE to start";
        graphics2D.setColor(Color.yellow);
        graphics2D.drawString(start, (SCREEN_SIZE)/4, 150);
    }

    private void drawScore(Graphics2D graphics2D) {

        graphics2D.setFont(smallFont);
        graphics2D.setColor(new Color(5, 181, 79));
        String s = "Score: " + score;
        graphics2D.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);

        for (int i = 0; i < lives; i++) {
            graphics2D.drawImage(heart, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

     private void checkMaze() {

        int i = 0;
        boolean finished = true;

        while (i < BLOCKS * BLOCKS && finished) {

            if ((screenData[i]) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {
            score += 50;

            if (ghosts < MAX_GHOSTS) {
                ghosts++;
            }

            if (currentSpeed < MAX_SPEED) {
                currentSpeed++;
            }

            initLevel();
        }
    }

    private void death() {
        lives--;

        if (lives == 0) {
            inGame = false;
        }

        continueLevel();
    }

    private void moveGhosts(Graphics2D graphics2D) {

        int pos;
        int count;

        for (int i = 0; i < ghosts; i++) {

            if (ghostX[i] % BLOCK_SIZE == 0 && ghostY[i] % BLOCK_SIZE == 0) {
                pos = ghostX[i] / BLOCK_SIZE + BLOCKS * (ghostY[i] / BLOCK_SIZE);

                count = 0;

                if ((screenData[pos] & 1) == 0 && ghostDeltaX[i] != 1) {
                    deltaX[count] = -1;
                    deltaY[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && ghostDeltaY[i] != 1) {
                    deltaX[count] = 0;
                    deltaY[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && ghostDeltaX[i] != -1) {
                    deltaX[count] = 1;
                    deltaY[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && ghostDeltaY[i] != -1) {
                    deltaX[count] = 0;
                    deltaY[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        ghostDeltaX[i] = 0;
                        ghostDeltaY[i] = 0;
                    } else {
                        ghostDeltaX[i] = -ghostDeltaX[i];
                        ghostDeltaY[i] = -ghostDeltaY[i];
                    }

                } else {
                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghostDeltaX[i] = deltaX[count];
                    ghostDeltaY[i] = deltaY[count];
                }
            }

            ghostX[i] = ghostX[i] + (ghostDeltaX[i] * ghostSpeed[i]);
            ghostY[i] = ghostY[i] + (ghostDeltaY[i] * ghostSpeed[i]);

            drawGhost(graphics2D, ghostX[i] + 1, ghostY[i] + 1);

            if (pacmanX > (ghostX[i] - 12) && pacmanX < (ghostX[i] + 12)
                    && pacmanY > (ghostY[i] - 12) && pacmanY < (ghostY[i] + 12)
                    && inGame) {
                isDying = true;
            }
        }
    }

    private void drawGhost(Graphics2D graphics2D, int x, int y) {
        graphics2D.drawImage(ghost, x, y, this);
    }

    private void movePacman() {

        int pos;
        short ch;

        if (pacmanX % BLOCK_SIZE == 0 && pacmanY % BLOCK_SIZE == 0) {
            pos = pacmanX / BLOCK_SIZE + BLOCKS * (pacmanY / BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
            }

            if (reqDeltaX != 0 || reqDeltaY != 0) {

                if (!((reqDeltaX == -1 && reqDeltaY == 0 && (ch & 1) != 0)
                        || (reqDeltaX == 1 && reqDeltaY == 0 && (ch & 4) != 0)
                        || (reqDeltaX == 0 && reqDeltaY == -1 && (ch & 2) != 0)
                        || (reqDeltaX == 0 && reqDeltaY == 1 && (ch & 8) != 0))) {
                    pacmanDeltaX = reqDeltaX;
                    pacmanDeltaY = reqDeltaY;
                }
            }

                if ((pacmanDeltaX == -1 && pacmanDeltaY == 0 && (ch & 1) != 0)
                        || (pacmanDeltaX == 1 && pacmanDeltaY == 0 && (ch & 4) != 0)
                        || (pacmanDeltaX == 0 && pacmanDeltaY == -1 && (ch & 2) != 0)
                        || (pacmanDeltaX == 0 && pacmanDeltaY == 1 && (ch & 8) != 0)) {
                    pacmanDeltaX = 0;
                    pacmanDeltaY = 0;
                }
            }

            pacmanX = pacmanX + PACMAN_SPEED * pacmanDeltaX;
            pacmanY = pacmanY + PACMAN_SPEED * pacmanDeltaY;

    }

    private void drawPacman(Graphics2D graphics2D) {

        if (reqDeltaX == -1) {
            graphics2D.drawImage(left, pacmanX + 1, pacmanY + 1, this);
        } else if (reqDeltaX == 1) {
            graphics2D.drawImage(right, pacmanX + 1, pacmanY + 1, this);
        } else if (reqDeltaY == -1) {
            graphics2D.drawImage(up, pacmanX + 1, pacmanY + 1, this);
        } else {
            graphics2D.drawImage(down, pacmanX + 1, pacmanY + 1, this);
        }

    }

    private void drawMaze(Graphics2D graphics2D) {

        short i = 0;
        int x;
        int y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                graphics2D.setColor(new Color(0,72,251));
                graphics2D.setStroke(new BasicStroke(5));

                if ((levelData[i] == 0)) {
                    graphics2D.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                }

                if ((screenData[i] & 1) != 0) {
                    graphics2D.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) {
                    graphics2D.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) {
                    graphics2D.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) {
                    graphics2D.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) {
                    graphics2D.setColor(new Color(255,255,255));
                    graphics2D.fillOval(x + 10, y + 10, 6, 6);
                }

                i++;
            }
        }

    }

      private void initGame() {
        lives = 3;
        score = 0;

        initLevel();

        ghosts = 6;
        currentSpeed = 3;

    }

    private void initLevel() {

        short i;
        for (i = 0; i < BLOCKS * BLOCKS; i++) {
            new ArrayList(screenData[i] = levelData[i]);
        }

        continueLevel();
    }

    private void continueLevel() {
        int dx = 1;
        int random;

        for (int i = 0; i < ghosts; i++) {
            ghostY[i] = 4 * BLOCK_SIZE;
            ghostX[i] = 4 * BLOCK_SIZE;

            ghostDeltaY[i] = 0;
            ghostDeltaX[i] = dx;

            dx = -dx;

            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random];
        }

        pacmanX = 7 * BLOCK_SIZE;
        pacmanY = 11 * BLOCK_SIZE;
        pacmanDeltaX = 0;
        pacmanDeltaY = 0;

        reqDeltaX = 0;
        reqDeltaY = 0;

        isDying = false;

    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, dimension.width, dimension.height);

        drawMaze(g2d);
        drawScore(g2d);

        if (inGame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }


    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    reqDeltaX = -1;
                    reqDeltaY = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    reqDeltaX = 1;
                    reqDeltaY = 0;
                } else if (key == KeyEvent.VK_UP) {
                    reqDeltaX = 0;
                    reqDeltaY = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    reqDeltaX = 0;
                    reqDeltaY = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                }
            } else {
                if (key == KeyEvent.VK_SPACE) {
                    inGame = true;
                    initGame();
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();

    }
}
