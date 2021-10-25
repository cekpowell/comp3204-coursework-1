package uk.ac.soton.ecs.cp6g18.ch3;

import org.apache.commons.lang.ArrayUtils;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.processor.PixelProcessor;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;
import org.openimaj.image.segmentation.SegmentationUtilities;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;

import javax.swing.*;
import java.net.URL;
import java.util.List;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 3 - Introduction to Clustering, Segmentation and Connected Components
 *
 * Exercises
 *
 * @author Charles Powell
 */
public class Ch3Exercises {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{
        // Exercise 1 - Display Utilities.
        exercise1();

        // Exercise 2 - Drawing.
        //exercise2();
    }

    ////////////////
    // EXERCISE 1 //
    ////////////////

    /**
     * Exercise 1 - The PixelProcessor
     *
     * Rather than looping over the image pixels using two for loops, it is possible to use a PixelProcessor to
     * accomplish the same task.
     *
     * Can you re-implement the loop that replaces each pixel with its class centroid using a PixelProcessor?
     *
     * What are the advantages and disadvantages of using a PixelProcessor?
     */
    public static void exercise1() throws Exception{
        // creating the display frame
        JFrame displayFrame = DisplayUtilities.createNamedWindow("Display", "OpenIMAJ Tutorial: Chapter 3: Exercise 1", false);

        // loading an image to be used in exercise
        MBFImage image = ImageUtilities.readMBF(new URL("https://images.theconversation.com/files/350851/original/file-20200803-22-dfm95n.jpg?ixlib=rb-1.1.0&q=45&auto=format&w=1200&h=1200.0&fit=crop"));

        // transforming the image into the LAB colour space
        image = ColourSpace.convert(image, ColourSpace.CIE_Lab);

        // constructing the runner
        FloatKMeans cluster = FloatKMeans.createExact(2);

        // gathering the image data from the image
        float[][] imageData = image.getPixelVectorNative(new float[image.getWidth() * image.getHeight()][3]); // 3 as each pixel has 3 bands?

        // running the K-Means algorithm
        FloatCentroidsResult result = cluster.cluster(imageData);

        // getting the centroids from the result of the clustering algorithm
        final float[][] centroids = result.centroids;

        // gathering the HardAssigner
        final HardAssigner<float[], ?, ?> assigner = result.defaultHardAssigner();

        /**
         * Using PixelProcessor to process the pixels
         */
        image.processInplace(new PixelProcessor<Float[]>() {
            @Override
            public Float[] processPixel(Float[] pixel) {
                // getting this pixel's centroid index
                int centroid = assigner.assign(ArrayUtils.toPrimitive(pixel));

                // returning the new pixel colour (the centroid of the class it belongs to)
                return ArrayUtils.toObject(centroids[centroid]);
            }
        });

        // converting the new image into RGB space and displaying it
        image = ColourSpace.convert(image, ColourSpace.RGB);
        DisplayUtilities.display(image, displayFrame);

        /**
         * Advantages of Pixel Processor
         *
         *  - Cleaner code (don't need to have nested for loops)
         *  - ??
         *
         * Disadvantages of Pixel Processor
         *
         *  - Have to apply the same process to all pixels - e.g., can no longer only process pixels in certain
         *  locations as we no longer have access to the co-ordinates of the pixels.
         *  - ??
         */
    }

    ///////////////////////
    // END OF EXERCISE 1 //
    ///////////////////////

    ////////////////
    // EXERCISE 2 //
    ////////////////

    /**
     * Exercise 2 - A Real Segmentation Algorithm
     *
     * The segmentation algorithm we just implemented can work reasonably well, but is rather naïve. OpenIMAJ contains
     * an implementation of a popular segmentation algorithm called the FelzenszwalbHuttenlocherSegmenter.
     *
     * Try using the FelzenszwalbHuttenlocherSegmenter for yourself and see how it compares to the basic segmentation
     * algorithm we implemented. You can use the SegmentationUtilities.renderSegments() static method to draw the
     * connected components produced by the segmenter.
     */
    public static void exercise2() throws Exception{
        // creating the display frame
        JFrame displayFrame = DisplayUtilities.createNamedWindow("Display", "OpenIMAJ Tutorial: Chapter 3: Exercise 2");

        // loading an image to be used in the tutorial
        MBFImage image = ImageUtilities.readMBF(new URL("https://images.theconversation.com/files/350851/original/file-20200803-22-dfm95n.jpg?ixlib=rb-1.1.0&q=45&auto=format&w=1200&h=1200.0&fit=crop"));

        // creating the segmenter
        FelzenszwalbHuttenlocherSegmenter<MBFImage> segmenter = new FelzenszwalbHuttenlocherSegmenter<MBFImage>();

        // segmenting the image
        List<ConnectedComponent> components = segmenter.segment(image);

        // rendering the segments to the image
        SegmentationUtilities.renderSegments(image, components);

        // displaying the image
        DisplayUtilities.display(image, displayFrame);
    }

    ///////////////////////
    // END OF EXERCISE 2 //
    ///////////////////////
}
