package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BicubicInterpolation {

    static class Interpolation {

        public double oneDimensionalBicubicInterpolation(double[] p, double x) {
            return p[1] + 0.5 * x * (p[2] - p[0] + x * (2.0 * p[0] - 5.0 * p[1] + 4.0 * p[2] - p[3] + x * (3.0 * (p[1] - p[2]) + p[3] - p[0])));
        }

        public double twoDimensionalBicubicInterpolation(double[][] p, double x, double y) {

            final double[] arr = new double[4];

            arr[0] = oneDimensionalBicubicInterpolation(p[0], y);
            arr[1] = oneDimensionalBicubicInterpolation(p[1], y);
            arr[2] = oneDimensionalBicubicInterpolation(p[2], y);
            arr[3] = oneDimensionalBicubicInterpolation(p[3], y);

            return oneDimensionalBicubicInterpolation(arr, x);
        }
    }

    public static void main(String[] args) throws IOException {

        String imagePath = "src/cat.jpg";

        BufferedImage originalImage = ImageIO.read(new File(imagePath));
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        int scaleFactor = 3;

        int newWidth = width * scaleFactor;
        int newHeight = height * scaleFactor;

        BufferedImage newImage = new BufferedImage(newWidth, newHeight, originalImage.getType());

        Interpolation interpolation = new Interpolation();

        for (int x = 0; x < newWidth; x++) {
            for (int y = 0; y < newHeight; y++) {

                double u = (double) x / newWidth * (width - 1);
                double v = (double) y / newHeight * (height - 1);

                int upperLeftX = (int) Math.floor(u) - 1;
                int upperLeftY = (int) Math.floor(v) - 1;

                double[][] redMatrixForPixelXY = new double[4][4];
                double[][] greenMatrixForPixelXY = new double[4][4];
                double[][] blueMatrixForPixelXY = new double[4][4];

                for (int m = 0; m < 4; m++) {
                    for (int n = 0; n < 4; n++) {
                        int suitableX = checkBounds(upperLeftX + m, 0, width - 1);
                        int suitableY = checkBounds(upperLeftY + n, 0, height - 1);
                        int rgb = originalImage.getRGB(suitableX, suitableY);

                        redMatrixForPixelXY[m][n] = (rgb >> 16) & 0xFF;
                        greenMatrixForPixelXY[m][n] = (rgb >> 8) & 0xFF;
                        blueMatrixForPixelXY[m][n] = rgb & 0xFF;
                    }
                }

                double xFraction = u - Math.floor(u);
                double yFraction = v - Math.floor(v);

                int combinedRed = (int) checkBounds(interpolation.twoDimensionalBicubicInterpolation(redMatrixForPixelXY, xFraction, yFraction), 0, 255);
                int combinedGreen = (int) checkBounds(interpolation.twoDimensionalBicubicInterpolation(greenMatrixForPixelXY, xFraction, yFraction), 0, 255);
                int combinedBlue = (int) checkBounds(interpolation.twoDimensionalBicubicInterpolation(blueMatrixForPixelXY, xFraction, yFraction), 0, 255);

                int interpolatedRgb = (combinedRed << 16) | (combinedGreen << 8) | combinedBlue;

                newImage.setRGB(x, y, interpolatedRgb);
            }
        }

        String outputPath = "src/new.jpg";
        ImageIO.write(newImage, "jpg", new File(outputPath));
    }

    private static int checkBounds(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private static double checkBounds(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
