package com.github.stoppedwumm.spriterenderer;

import engine.core.CoreGame;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

public class Sprite {
    private Color[][] pixelMap;
    private int width;
    private int height;

    // Load from local resources
    public Sprite(String resourcePath) throws IOException {
        BufferedImage img = ImageIO.read(getClass().getResourceAsStream(resourcePath));
        load(img);
    }

    // Load from a URL (useful for web-hosted assets)
    public Sprite(URL url) throws IOException {
        BufferedImage img = ImageIO.read(url);
        load(img);
    }

    private void load(BufferedImage img) {
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.pixelMap = new Color[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = img.getRGB(x, y);
                // Check if pixel is transparent (Alpha channel)
                if ((argb >> 24) != 0) {
                    pixelMap[x][y] = new Color(argb, true);
                } else {
                    pixelMap[x][y] = null; // Transparent
                }
            }
        }
    }

    /**
     * Draws the sprite using JavaBase Turtle commands.
     * @param game The instance of CoreGame (your game)
     * @param x Start X position
     * @param y Start Y position
     * @param scale Scaling factor (1 = original size)
     */
    public void draw(CoreGame game, int x, int y, int scale) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color c = pixelMap[i][j];
                if (c != null) {
                    // Set turtle color
                    game.setPenColor(c);
                    
                    // Logic to draw a "pixel block" based on scale
                    for (int sx = 0; sx < scale; sx++) {
                        for (int sy = 0; sy < scale; sy++) {
                            game.moveTo(x + (i * scale) + sx, y + (j * scale) + sy);
                        }
                    }
                }
            }
        }
    }
}
