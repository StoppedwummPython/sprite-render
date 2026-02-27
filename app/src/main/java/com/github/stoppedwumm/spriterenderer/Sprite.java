package com.github.stoppedwumm.spriterenderer;

import engine.core.CoreGame;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Sprite {
    private Color[][] pixelMap;
    private int width;
    private int height;

    public Sprite(String resourcePath) throws IOException {
        BufferedImage img = ImageIO.read(getClass().getResourceAsStream(resourcePath));
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.pixelMap = new Color[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int argb = img.getRGB(x, y);
                if ((argb >> 24) != 0) { // Check Alpha
                    pixelMap[x][y] = new Color(argb, true);
                } else {
                    pixelMap[x][y] = null;
                }
            }
        }
    }

    public void draw(CoreGame game, double x, double y, int scale) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color c = pixelMap[i][j];
                if (c != null) {
                    // 1. Set the color (Was setPenColor, now color)
                    game.color(c);
                    
                    // 2. Move turtle to the specific pixel position
                    // Note: engine coordinates are relative to center
                    game.moveTo(x + (i * scale), y + (j * scale));
                    
                    // 3. Draw a filled square representing the pixel/scaled block
                    game.drawRect(scale, scale, true);
                }
            }
        }
    }
}