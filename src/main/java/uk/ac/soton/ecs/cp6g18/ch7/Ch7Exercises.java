package uk.ac.soton.ecs.cp6g18.ch7;

import org.apache.commons.lang.ArrayUtils;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.PixelProcessor;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;
import org.openimaj.image.segmentation.SegmentationUtilities;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;

import javax.swing.*;
import java.net.URL;
import java.util.List;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 7 - Processing Video
 *
 * Exercises
 *
 * @author Charles Powell
 */
public class Ch7Exercises {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{
        // Exercise 1 - Applying Different Types of Image Processing to the Video
        exercise1();
    }

    ////////////////
    // EXERCISE 1 //
    ////////////////

    /**
     * Exercise 1 - Applying Different Types of Image Processing to the Video
     *
     * Try a different operation and see how it affects the frames of your video.
     */
    public static void exercise1() throws Exception{

    }

    ///////////////////////
    // END OF EXERCISE 1 //
    ///////////////////////
}
