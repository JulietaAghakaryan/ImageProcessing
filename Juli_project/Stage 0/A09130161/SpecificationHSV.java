import ij.*;
import java.awt.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.lang.Math;
import ij.ImagePlus;
import java.awt.Color;
import java.util.*;
import java.util.concurrent.*;

//Reference to Nare Tumanyan

public class SpecificationHSV implements PlugInFilter {
    ImageProcessor referenceImageProcessor;
    TreeMap<Double, Double> originalHughHistogram = new TreeMap<Double, Double>();
    TreeMap<Double, Double> originalSatHistogram = new TreeMap<Double, Double>();
    TreeMap<Double, Double> originalValHistogram = new TreeMap<Double, Double>();
    TreeMap<Double, Double> referenceHughHistogram = new TreeMap<Double, Double>();
    TreeMap<Double, Double> referenceSatHistogram = new TreeMap<Double, Double>();
    TreeMap<Double, Double> referenceValHistogram = new TreeMap<Double, Double>();

    public int setup (String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run (ImageProcessor ip) {
        setReferenceImageProcessor();
        setHistogram(ip, originalHughHistogram, originalSatHistogram, originalValHistogram);
        setHistogram(referenceImageProcessor, referenceHughHistogram, referenceSatHistogram, referenceValHistogram);
        accumulateHistogram(originalHughHistogram, imageSize(ip));
        accumulateHistogram(originalSatHistogram, imageSize(ip));
        accumulateHistogram(originalValHistogram, imageSize(ip));
        accumulateHistogram(referenceHughHistogram, imageSize(referenceImageProcessor));
        accumulateHistogram(referenceSatHistogram, imageSize(referenceImageProcessor));
        accumulateHistogram(referenceValHistogram, imageSize(referenceImageProcessor));
        equalizeChannels(ip);
    }

    private void setReferenceImageProcessor() {
        referenceImageProcessor = IJ.openImage().getProcessor();
    }

    private void equalizeChannels(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();

        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                double[] hsv = getHSV(col, row, ip);
                float hugh = (float) newHugh(hsv[0]);
                float sat = (float) newSat(hsv[1]);
                float val = (float) newVal(hsv[2]);
                int rgb = Color.HSBtoRGB(hugh, sat, val);
                ip.putPixel(col, row, rgb);
            }
        }
    }

    private double newHugh(double hugh) {
        double originalValue = originalHughHistogram.get(hugh);
        return closestIndexToValue(originalValue, referenceHughHistogram);
    }

    private double newSat(double sat) {
        double originalValue = originalSatHistogram.get(sat);
        return closestIndexToValue(originalValue, referenceSatHistogram);
    }

    private double newVal(double val) {
        double originalValue = originalValHistogram.get(val);
        return closestIndexToValue(originalValue, referenceValHistogram);
    }

    private void setHistogram(ImageProcessor ip, TreeMap<Double, Double> hughHistogram, TreeMap<Double, Double> satHistogram, TreeMap<Double, Double> valHistogram) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        int imageSize = width * height;

        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                double[] hsv = getHSV(col, row, ip);
                increaseHistogram(hughHistogram, hsv[0]);
                increaseHistogram(satHistogram, hsv[1]);
                increaseHistogram(valHistogram, hsv[2]);
            }
        }
    }

    private void increaseHistogram(TreeMap<Double, Double> histogram, double key) {
        if (histogram.containsKey(key)) {
            histogram.put(key, histogram.get(key) + 1.0);
        } else {
            histogram.put(key, 1.0);
        }
    }

    private void accumulateHistogram(TreeMap<Double, Double> histogram, int imageSize) {
        double previousValue = 0.0;
        for (Map.Entry<Double, Double> entry : histogram.entrySet()) {
            histogram.put(entry.getKey(), (histogram.get(entry.getKey()) / imageSize) + previousValue);
            previousValue = histogram.get(entry.getKey());
        }
    }

    private double closestIndexToValue(double value, TreeMap<Double, Double> histogram) {
        double key = 0.0;
        for (Map.Entry<Double, Double> entry : histogram.entrySet()) {
            if (value < histogram.get(entry.getKey())) {
                return entry.getKey();
            }
            key = entry.getKey();
        }
        return key;
    }

    private void logHistogram(TreeMap<Double, Double> histogram) {
        for (Map.Entry<Double, Double> entry : histogram.entrySet()) {
            IJ.log("Key: " + entry.getKey() + ". Value: " + entry.getValue());
        }
    }

    private int imageSize(ImageProcessor ip) {
        return ip.getWidth() * ip.getHeight();
    }

    private double[] getHSV(int col, int row, ImageProcessor ip) {
        Color color = new Color(ip.getPixel(col, row));
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        float[] hsvFloat = new float[3];
        Color.RGBtoHSB(r, g, b, hsvFloat);
        double[] hsv = new double[3];
        hsv[0] = (double) hsvFloat[0];
        hsv[1] = (double) hsvFloat[1];
        hsv[2] = (double) hsvFloat[2];
        return hsv;
    }

}