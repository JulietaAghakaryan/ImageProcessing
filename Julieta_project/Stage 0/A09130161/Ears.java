import ij.*;
import java.awt.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.lang.Math;
import ij.ImagePlus;
import java.awt.Color;

// ASSUMPTION: The image processor passed to the run method already has the binary_layer_3 + median(4) + minimum(4) run
public class Ears implements PlugInFilter {
    int BLACK = 0;
    int WHITE = 16777215;
    int LINE_COLOR = 65280;
    int ESTIMATED_VERT_MARGIN = 50;
    int ESTIMATED_HORZ_MARGIN = 25;

    public int setup (String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run (ImageProcessor ip) {
        this.clearCenter(ip);
    }

    private void clearCenter(ImageProcessor ip) {
        int width = ip.getWidth();
        int center = width / 2;
        clearLeftHalfCenter(ip, center);
        clearRightHalfCenter(ip, center + 1);
        drawLeftBoundingBox(ip, center);
        drawRightBoundingBox(ip, center);
    }

    private void clearLeftHalfCenter(ImageProcessor ip, int center) {
        int width = ip.getWidth();
        int col = center;
        boolean isFinished = false;

        while (!isFinished) {
            isFinished = !clearColumn(ip, col);
            col -= 1;
        }
    }

    private void clearRightHalfCenter(ImageProcessor ip, int center) {
        int width = ip.getWidth();
        int col = center;
        boolean isFinished = false;

        while (!isFinished) {
            isFinished = !clearColumn(ip, col);
            col += 1;
        }
    }

    private void drawLeftBoundingBox(ImageProcessor ip, int center) {
        int[] centroid = calcLeftCentroid(ip, center);
        drawHorizontalLine(ip, centroid[1] + ESTIMATED_VERT_MARGIN, centroid[0] - ESTIMATED_HORZ_MARGIN, centroid[0] + ESTIMATED_HORZ_MARGIN);
        drawHorizontalLine(ip, centroid[1] - ESTIMATED_VERT_MARGIN, centroid[0] - ESTIMATED_HORZ_MARGIN, centroid[0] + ESTIMATED_HORZ_MARGIN);
        drawVerticalLine(ip, centroid[0] + ESTIMATED_HORZ_MARGIN, centroid[1] - ESTIMATED_VERT_MARGIN, centroid[1] + ESTIMATED_VERT_MARGIN);
        drawVerticalLine(ip, centroid[0] - ESTIMATED_HORZ_MARGIN, centroid[1] - ESTIMATED_VERT_MARGIN, centroid[1] + ESTIMATED_VERT_MARGIN);
    }

    private void drawRightBoundingBox(ImageProcessor ip, int center) {
        int[] centroid = calcRightCentroid(ip, center);
        drawHorizontalLine(ip, centroid[1] + ESTIMATED_VERT_MARGIN, centroid[0] - ESTIMATED_HORZ_MARGIN, centroid[0] + ESTIMATED_HORZ_MARGIN);
        drawHorizontalLine(ip, centroid[1] - ESTIMATED_VERT_MARGIN, centroid[0] - ESTIMATED_HORZ_MARGIN, centroid[0] + ESTIMATED_HORZ_MARGIN);
        drawVerticalLine(ip, centroid[0] + ESTIMATED_HORZ_MARGIN, centroid[1] - ESTIMATED_VERT_MARGIN, centroid[1] + ESTIMATED_VERT_MARGIN);
        drawVerticalLine(ip, centroid[0] - ESTIMATED_HORZ_MARGIN, centroid[1] - ESTIMATED_VERT_MARGIN, centroid[1] + ESTIMATED_VERT_MARGIN);
    }

    private int[] calcLeftCentroid(ImageProcessor ip, int center) {
        int height = ip.getHeight();
        int weightSum = 0;
        int colCoordSum = 0;
        int rowCoordSum = 0;

        for (int col = 0; col < center; col++) {
            for (int row = 0; row < height; row++) {
                Color color = new Color(ip.getPixel(col, row));
                int weight = 0;
                if (isBlack(color)) {
                    weight = 1;
                }
                weightSum += weight;
                colCoordSum += weight * col;
                rowCoordSum += weight * row;
            }
        }

        int colCenter = colCoordSum / weightSum;
        int rowCenter = rowCoordSum / weightSum;
        int[] centroid = {colCenter, rowCenter};
        return centroid;
    }

    private int[] calcRightCentroid(ImageProcessor ip, int center) {
        int height = ip.getHeight();
        int width = ip.getWidth();
        int weightSum = 0;
        int colCoordSum = 0;
        int rowCoordSum = 0;

        for (int col = center; col < width; col++) {
            for (int row = 0; row < height; row++) {
                Color color = new Color(ip.getPixel(col, row));
                int weight = 0;
                if (isBlack(color)) {
                    weight = 1;
                }
                weightSum += weight;
                colCoordSum += weight * col;
                rowCoordSum += weight * row;
            }
        }

        int colCenter = colCoordSum / weightSum;
        int rowCenter = rowCoordSum / weightSum;
        int[] centroid = {colCenter, rowCenter};
        return centroid;
    }

    private boolean clearColumn(ImageProcessor ip, int column) {
        int height = ip.getHeight();
        boolean isBlackPresent = false;
        for (int row = 0; row < height; row++) {
            Color color = new Color(ip.getPixel(column, row));
            if (isBlack(color)) {
                isBlackPresent = true;
                ip.putPixel(column, row, WHITE);
            }
        }

        return isBlackPresent;
    }

    private boolean isBlack(Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        return red == 0 && green == 0 && blue == 0;
    }

    private void drawVerticalLine(ImageProcessor ip, int col, int rowStart, int rowEnd) {
        for (int row = rowStart; row < rowEnd; row++) {
            ip.putPixel(col, row, LINE_COLOR);
        }
    }

    private void drawHorizontalLine(ImageProcessor ip, int row, int colStart, int colEnd) {
        for (int col = colStart; col < colEnd; col++) {
            ip.putPixel(col, row, LINE_COLOR);
        }
    }

}

// Narek Tumanyan