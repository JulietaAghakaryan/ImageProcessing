import ij.*;
import java.awt.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.lang.Math;
import ij.ImagePlus;
import java.awt.Color;

public class Cumulative implements PlugInFilter {
    double histogram[][] = new double[3][256];

    private void setHistogram(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        int imageSize = width * height;
        int r;
        int g;
        int b;
        Color color;

        // Initialize
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                color = new Color(ip.getPixel(col, row));
                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();
                histogram[0][r] += 1;
                histogram[1][b] += 1;
                histogram[2][g] += 1;
            }
        }

        // Accumulate
        for (int i = 0; i < 256; i++) {
            if (i == 0) {
                histogram[0][i] /= imageSize;
                histogram[1][i] /= imageSize;
                histogram[2][i] /= imageSize;
            }
            else {
                histogram[0][i] = (histogram[0][i] / imageSize) + histogram[0][i-1];
                histogram[1][i] = (histogram[1][i] / imageSize) + histogram[1][i-1];
                histogram[2][i] = (histogram[2][i] / imageSize) + histogram[2][i-1];
            }
        }
    }

    private void logHistogram() {
        for (int i = 0; i < 256; i++) {
            logDouble(i, histogram[2][i]);
        }
    }

    public int setup (String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run (ImageProcessor ip) {
        setHistogram(ip);
        logHistogram();
    }

    private void logDouble(int index, double doubleValue) {
        IJ.log(Double.toString(doubleValue));
    }

}