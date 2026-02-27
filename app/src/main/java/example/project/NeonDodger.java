package my.project;

import engine.annot.Game;
import engine.core.CoreGame;
import engine.input.Input;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

@Game(name = "Neon Dodger")
public class NeonDodger extends CoreGame {

    // --- Game State Enum ---
    private enum State {
        MENU, PLAYING, GAME_OVER
    }

    private State currentState = State.MENU;

    // --- Player Settings ---
    private double playerX = 0;
    private final double PLAYER_Y = 250; // Near bottom (Screen is -300 to +300)
    private final int PLAYER_SIZE = 20;
    private final double PLAYER_SPEED = 6.0;

    // --- Game Logic ---
    private ArrayList<FallingBlock> enemies;
    private int score = 0;
    private int difficultyTimer = 0;
    private double spawnRate = 30; // Lower is faster spawning
    private Random random;

    // --- Input Flags ---
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean actionKey = false;
    private boolean actionKeyPrev = false; // To prevent spamming on hold

    @Override
    public void start() {
        // Initialize logic
        enemies = new ArrayList<>();
        random = new Random();
        resetGame();
    }

    private void resetGame() {
        playerX = 0;
        enemies.clear();
        score = 0;
        spawnRate = 30;
        difficultyTimer = 0;
    }

    @Override
    public void input(Input input) {
        // Map engine input to game flags
        moveLeft = input.isLeft();
        moveRight = input.isRight();
        
        // Simple distinct press check for Space/Enter
        boolean currentSpace = input.isSpace();
        actionKey = currentSpace && !actionKeyPrev;
        actionKeyPrev = currentSpace;
    }

    @Override
    public void draw() {
        // 1. Clear Screen
        clear();

        // 2. State Machine
        switch (currentState) {
            case MENU:
                drawMenu();
                if (actionKey) {
                    resetGame();
                    currentState = State.PLAYING;
                }
                break;

            case PLAYING:
                updateGame();
                drawGame();
                break;

            case GAME_OVER:
                drawGameOver();
                if (actionKey) {
                    currentState = State.MENU;
                }
                break;
        }
    }

    // --- Update Logic ---
    private void updateGame() {
        // Move Player
        if (moveLeft) playerX -= PLAYER_SPEED;
        if (moveRight) playerX += PLAYER_SPEED;

        // Clamp Player to Screen Edges (-400 to 400)
        if (playerX < -380) playerX = -380;
        if (playerX > 380) playerX = 380;

        // Difficulty scaling
        difficultyTimer++;
        if (difficultyTimer % 200 == 0 && spawnRate > 5) {
            spawnRate--; // Spawn faster over time
        }

        // Spawn Enemies
        if (difficultyTimer % (int)spawnRate == 0) {
            double rX = -380 + random.nextInt(760);
            enemies.add(new FallingBlock(rX, -320)); // Spawn above screen
        }

        // Update Enemies & Collision
        Iterator<FallingBlock> it = enemies.iterator();
        while (it.hasNext()) {
            FallingBlock e = it.next();
            e.y += 5 + (score / 500.0); // Fall speed increases slightly with score

            // Check Collision (Simple AABB)
            if (Math.abs(playerX - e.x) < PLAYER_SIZE && Math.abs(PLAYER_Y - e.y) < PLAYER_SIZE) {
                currentState = State.GAME_OVER;
            }

            // Remove if off screen
            if (e.y > 320) {
                it.remove();
                score += 10;
            }
        }
    }

    // --- Drawing Logic ---

    private void drawMenu() {
        drawText("NEON DODGER", -180, -50, 6, Color.CYAN);
        drawText("PRESS SPACE", -150, 50, 4, Color.WHITE);
        
        // Draw decorative player
        penUp();
        moveTo(0, 0);
        penDown();
        color(Color.BLUE);
        drawSquare(40, false);
    }

    private void drawGame() {
        // Draw Player
        penUp();
        moveTo(playerX, PLAYER_Y);
        penDown();
        color(Color.CYAN);
        drawSquare(PLAYER_SIZE, true); // Filled center
        color(Color.WHITE);
        drawSquare(PLAYER_SIZE, false); // Outline

        // Draw Enemies
        for (FallingBlock e : enemies) {
            penUp();
            moveTo(e.x, e.y);
            penDown();
            color(Color.RED);
            drawSquare(PLAYER_SIZE, true);
        }

        // Draw HUD
        drawText("SCORE " + score, -380, -280, 3, Color.YELLOW);
    }

    private void drawGameOver() {
        // Keep game visible in background but dimmed (simulated by not clearing enemies)
        for (FallingBlock e : enemies) {
            penUp();
            moveTo(e.x, e.y);
            penDown();
            color(Color.DARK_GRAY); // Dimmed enemies
            drawSquare(PLAYER_SIZE, true);
        }

        drawText("GAME OVER", -150, -20, 6, Color.RED);
        drawText("SCORE " + score, -100, 40, 4, Color.WHITE);
        drawText("SPACE TO RESET", -160, 100, 3, Color.GRAY);
    }

    // --- Helper Class ---
    private static class FallingBlock {
        double x, y;
        FallingBlock(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    // ==========================================
    //       TEXT RENDERING ENGINE
    // ==========================================
    
    /**
     * Renders pixel-font text using the Turtle engine's drawSquare method.
     */
    public void drawText(String text, int x, int y, int scale, Color color) {
        if (text == null) return;
        text = text.toUpperCase(); 
        int cursorX = x;
        int spacing = scale; 

        for (char c : text.toCharArray()) {
            boolean[][] letterMap = getBitmap(c);
            int charHeight = letterMap.length;     
            int charWidth = letterMap[0].length;   

            for (int r = 0; r < charHeight; r++) {
                for (int cIdx = 0; cIdx < charWidth; cIdx++) {
                    if (letterMap[r][cIdx]) {
                        // Calculate position
                        int drawX = cursorX + (cIdx * scale);
                        int drawY = y + (r * scale);

                        penUp();
                        moveTo(drawX, drawY);
                        penDown();
                        this.color(color);
                        // Draw a filled square for the pixel
                        drawSquare(scale, true); 
                    }
                }
            }
            cursorX += (charWidth * scale) + spacing;
        }
    }

    // Condensed Font Bitmap Data
    private boolean[][] getBitmap(char c) {
        boolean T = true, F = false;
        switch (c) {
            case 'A': return new boolean[][]{{F,T,T,T,F},{T,F,F,F,T},{T,T,T,T,T},{T,F,F,F,T},{T,F,F,F,T}};
            case 'B': return new boolean[][]{{T,T,T,T,F},{T,F,F,F,T},{T,T,T,T,F},{T,F,F,F,T},{T,T,T,T,F}};
            case 'C': return new boolean[][]{{F,T,T,T,T},{T,F,F,F,F},{T,F,F,F,F},{T,F,F,F,F},{F,T,T,T,T}};
            case 'D': return new boolean[][]{{T,T,T,T,F},{T,F,F,F,T},{T,F,F,F,T},{T,F,F,F,T},{T,T,T,T,F}};
            case 'E': return new boolean[][]{{T,T,T,T,T},{T,F,F,F,F},{T,T,T,T,F},{T,F,F,F,F},{T,T,T,T,T}};
            case 'F': return new boolean[][]{{T,T,T,T,T},{T,F,F,F,F},{T,T,T,T,F},{T,F,F,F,F},{T,F,F,F,F}};
            case 'G': return new boolean[][]{{F,T,T,T,T},{T,F,F,F,F},{T,F,T,T,T},{T,F,F,F,T},{F,T,T,T,T}};
            case 'H': return new boolean[][]{{T,F,F,F,T},{T,F,F,F,T},{T,T,T,T,T},{T,F,F,F,T},{T,F,F,F,T}};
            case 'I': return new boolean[][]{{T,T,T,T,T},{F,F,T,F,F},{F,F,T,F,F},{F,F,T,F,F},{T,T,T,T,T}};
            case 'J': return new boolean[][]{{F,F,F,F,T},{F,F,F,F,T},{F,F,F,F,T},{T,F,F,F,T},{F,T,T,T,F}};
            case 'K': return new boolean[][]{{T,F,F,F,T},{T,F,F,T,F},{T,T,T,F,F},{T,F,F,T,F},{T,F,F,F,T}};
            case 'L': return new boolean[][]{{T,F,F,F,F},{T,F,F,F,F},{T,F,F,F,F},{T,F,F,F,F},{T,T,T,T,T}};
            case 'M': return new boolean[][]{{T,F,F,F,T},{T,T,F,T,T},{T,F,T,F,T},{T,F,F,F,T},{T,F,F,F,T}};
            case 'N': return new boolean[][]{{T,F,F,F,T},{T,T,F,F,T},{T,F,T,F,T},{T,F,F,T,T},{T,F,F,F,T}};
            case 'O': return new boolean[][]{{F,T,T,T,F},{T,F,F,F,T},{T,F,F,F,T},{T,F,F,F,T},{F,T,T,T,F}};
            case 'P': return new boolean[][]{{T,T,T,T,F},{T,F,F,F,T},{T,T,T,T,F},{T,F,F,F,F},{T,F,F,F,F}};
            case 'Q': return new boolean[][]{{F,T,T,T,F},{T,F,F,F,T},{T,F,F,F,T},{T,F,F,T,T},{F,T,T,T,T}};
            case 'R': return new boolean[][]{{T,T,T,T,F},{T,F,F,F,T},{T,T,T,T,F},{T,F,F,T,F},{T,F,F,F,T}};
            case 'S': return new boolean[][]{{F,T,T,T,T},{T,F,F,F,F},{T,T,T,T,F},{F,F,F,F,T},{T,T,T,T,F}};
            case 'T': return new boolean[][]{{T,T,T,T,T},{F,F,T,F,F},{F,F,T,F,F},{F,F,T,F,F},{F,F,T,F,F}};
            case 'U': return new boolean[][]{{T,F,F,F,T},{T,F,F,F,T},{T,F,F,F,T},{T,F,F,F,T},{F,T,T,T,F}};
            case 'V': return new boolean[][]{{T,F,F,F,T},{T,F,F,F,T},{T,F,F,F,T},{F,T,F,T,F},{F,F,T,F,F}};
            case 'W': return new boolean[][]{{T,F,F,F,T},{T,F,F,F,T},{T,F,T,F,T},{T,T,F,T,T},{T,F,F,F,T}};
            case 'X': return new boolean[][]{{T,F,F,F,T},{F,T,F,T,F},{F,F,T,F,F},{F,T,F,T,F},{T,F,F,F,T}};
            case 'Y': return new boolean[][]{{T,F,F,F,T},{F,T,F,T,F},{F,F,T,F,F},{F,F,T,F,F},{F,F,T,F,F}};
            case 'Z': return new boolean[][]{{T,T,T,T,T},{F,F,F,T,F},{F,F,T,F,F},{F,T,F,F,F},{T,T,T,T,T}};
            case '0': return new boolean[][]{{F,T,T,T,F},{T,F,F,T,T},{T,F,T,F,T},{T,T,F,F,T},{F,T,T,T,F}};
            case '1': return new boolean[][]{{F,F,T,F,F},{F,T,T,F,F},{F,F,T,F,F},{F,F,T,F,F},{F,T,T,T,F}};
            case '2': return new boolean[][]{{F,T,T,T,F},{T,F,F,F,T},{F,F,T,T,F},{F,T,F,F,F},{T,T,T,T,T}};
            case '3': return new boolean[][]{{T,T,T,T,T},{F,F,F,F,T},{F,T,T,T,F},{F,F,F,F,T},{T,T,T,T,F}};
            case '4': return new boolean[][]{{T,F,F,F,T},{T,F,F,F,T},{T,T,T,T,T},{F,F,F,F,T},{F,F,F,F,T}};
            case '5': return new boolean[][]{{T,T,T,T,T},{T,F,F,F,F},{T,T,T,T,F},{F,F,F,F,T},{T,T,T,T,F}};
            case '6': return new boolean[][]{{F,T,T,T,F},{T,F,F,F,F},{T,T,T,T,F},{T,F,F,F,T},{F,T,T,T,F}};
            case '7': return new boolean[][]{{T,T,T,T,T},{F,F,F,F,T},{F,F,F,T,F},{F,F,T,F,F},{F,F,T,F,F}};
            case '8': return new boolean[][]{{F,T,T,T,F},{T,F,F,F,T},{F,T,T,T,F},{T,F,F,F,T},{F,T,T,T,F}};
            case '9': return new boolean[][]{{F,T,T,T,F},{T,F,F,F,T},{F,T,T,T,T},{F,F,F,F,T},{F,T,T,T,F}};
            case ' ': return new boolean[][]{{F,F,F,F,F},{F,F,F,F,F},{F,F,F,F,F},{F,F,F,F,F},{F,F,F,F,F}};
            default: return new boolean[][]{{T,F,F,F,T},{F,T,F,T,F},{F,F,T,F,F},{F,T,F,T,F},{T,F,F,F,T}}; // X for unknown
        }
    }
}