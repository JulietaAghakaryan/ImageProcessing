import ij.*;
import java.awt.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.lang.Math;
import ij.ImagePlus;
import java.awt.Color;

//Reference to Nare Tumanyan

public class Specification implements PlugInFilter {
    ImageProcessor referenceImageProcessor;
    double[][] referenceHistogram = new double[3][256];
    double[][] originalHistogram = new double[3][256];

    public int setup (String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run (ImageProcessor ip) {
        setReferenceImageProcessor();
        setHistogram(ip, originalHistogram);
        setHistogram(referenceImageProcessor, referenceHistogram);
        equalizeChannels(ip);
    }

    private void setReferenceImageProcessor() {
        referenceImageProcessor = IJ.openImage().getProcessor();
    }

    private void equalizeChannels(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        int redMapping[] = new int[256];
        int greenMapping[] = new int[256];
        int blueMapping[] = new int[256];

        // calculate mappings
        for (int i = 0; i < 256; i++) {
            int calibratedRedColor = getClosestValueIndex(referenceHistogram[0], originalHistogram[0][i]);
            int calibratedGreenColor = getClosestValueIndex(referenceHistogram[1], originalHistogram[1][i]);
            int calibratedBlueColor = getClosestValueIndex(referenceHistogram[2], originalHistogram[2][i]);

            redMapping[i] = calibratedRedColor;
            greenMapping[i] = calibratedGreenColor;
            blueMapping[i] = calibratedBlueColor;
        }

        // equalize
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                Color color = new Color(ip.getPixel(col, row));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                int calibrated[] = new int[3];
                calibrated[0] = redMapping[r];
                calibrated[1] = greenMapping[g];
                calibrated[2] = blueMapping[b];
                ip.putPixel(col, row, calibrated);
            }
        }
    }

    private int getClosestValueIndex(double[] referenceHistogram, double value) {
        int index = 255;
        while (referenceHistogram[index] >= value && index > 0) {
            index--;
        }
        return index;
    }

    private void setHistogram(ImageProcessor ip, double[][] histogram) {
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

}