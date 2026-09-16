package com.pharmacy.management.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Utility class for image preprocessing operations.
 * Prepares images for optimal OCR recognition.
 * 
 * Validates Requirement 4.2: Image preprocessing with grayscale conversion,
 * contrast enhancement, noise reduction, and binarization
 */
public class ImageUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageUtil.class);
    
    private ImageUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Preprocess an image for OCR.
     * Applies grayscale conversion, contrast enhancement, noise reduction, and binarization.
     * 
     * Requirement 4.2
     * 
     * @param imageFile the image file to preprocess
     * @return preprocessed BufferedImage
     * @throws IOException if image cannot be read
     */
    public static BufferedImage preprocessForOCR(File imageFile) throws IOException {
        logger.debug("Preprocessing image: {}", imageFile.getName());
        
        // Read the image
        BufferedImage original = ImageIO.read(imageFile);
        if (original == null) {
            throw new IOException("Unable to read image file: " + imageFile.getName());
        }
        
        // Step 1: Convert to grayscale
        BufferedImage grayscale = convertToGrayscale(original);
        
        // Step 2: Enhance contrast
        BufferedImage enhanced = enhanceContrast(grayscale);
        
        // Step 3: Apply noise reduction (simple blur)
        BufferedImage denoised = reduceNoise(enhanced);
        
        // Step 4: Binarize (convert to black and white)
        BufferedImage binarized = binarize(denoised);
        
        logger.debug("Image preprocessing completed");
        return binarized;
    }
    
    /**
     * Convert image to grayscale.
     */
    private static BufferedImage convertToGrayscale(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage grayscale = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        Graphics2D g = grayscale.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        
        return grayscale;
    }
    
    /**
     * Enhance image contrast.
     */
    private static BufferedImage enhanceContrast(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage enhanced = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        // Simple contrast enhancement by stretching histogram
        int min = 255, max = 0;
        
        // Find min and max pixel values
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                if (pixel < min) min = pixel;
                if (pixel > max) max = pixel;
            }
        }
        
        // Apply contrast stretching
        float scale = 255.0f / (max - min);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                int newPixel = Math.min(255, Math.max(0, (int)((pixel - min) * scale)));
                int rgb = (newPixel << 16) | (newPixel << 8) | newPixel;
                enhanced.setRGB(x, y, rgb);
            }
        }
        
        return enhanced;
    }
    
    /**
     * Reduce noise using simple averaging filter.
     */
    private static BufferedImage reduceNoise(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage denoised = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        // Apply 3x3 averaging filter
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sum = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        sum += image.getRGB(x + dx, y + dy) & 0xFF;
                    }
                }
                int avg = sum / 9;
                int rgb = (avg << 16) | (avg << 8) | avg;
                denoised.setRGB(x, y, rgb);
            }
        }
        
        return denoised;
    }
    
    /**
     * Binarize image (convert to black and white using threshold).
     */
    private static BufferedImage binarize(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage binarized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        
        // Calculate threshold using Otsu's method (simple version)
        int threshold = calculateOtsuThreshold(image);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                int newPixel = pixel > threshold ? 255 : 0;
                int rgb = (newPixel << 16) | (newPixel << 8) | newPixel;
                binarized.setRGB(x, y, rgb);
            }
        }
        
        return binarized;
    }
    
    /**
     * Calculate optimal threshold using Otsu's method.
     */
    private static int calculateOtsuThreshold(BufferedImage image) {
        int[] histogram = new int[256];
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;
        
        // Build histogram
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                histogram[pixel]++;
            }
        }
        
        // Calculate threshold
        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }
        
        float sumB = 0;
        int wB = 0;
        int wF = 0;
        float maxVariance = 0;
        int threshold = 0;
        
        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;
            
            wF = totalPixels - wB;
            if (wF == 0) break;
            
            sumB += i * histogram[i];
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;
            
            float variance = wB * wF * (mB - mF) * (mB - mF);
            
            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = i;
            }
        }
        
        return threshold;
    }
}
