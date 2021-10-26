package uk.ac.soton.ecs.cp6g18.ch3;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.connectedcomponent.GreyscaleConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;

import javax.swing.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 3 - Introduction to Clustering, Segmentation and Connected Components
 *
 * Tutorial Code
 *
 * @author Charles Powell
 */
public class Ch3Tutorial {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{
        // creating the display frame
        JFrame displayFrame = DisplayUtilities.createNamedWindow("Display", "OpenIMAJ Tutorial: Chapter 3: Tutorial");

        ///////////////////////////
        // TUTORIAL INTRODUCTION //
        ///////////////////////////

        /**
         * We will demonstrate here how an image can be broken down into a number of regions.
         *
         * The process of seperating an image into regions is known as segmentation.
         *
         * Researchers try to optimise segmentation algorithms to try and seperate the objects in the image
         * from the background.
         */

        // loading an image to be used in the tutorial
        MBFImage image = ImageUtilities.readMBF(new URL("https://images.theconversation.com/files/350851/original/file-20200803-22-dfm95n.jpg?ixlib=rb-1.1.0&q=45&auto=format&w=1200&h=1200.0&fit=crop"));

        // displaying the image
        DisplayUtilities.display(image, displayFrame);

        ////////////////
        // CLUSTERING //
        ////////////////

        /**
         * To segment the image, we are going to use a machine learning technique called Clustering.
         *
         * Clustering algorithms automatically group similar things together.
         *
         * Each group of similar thing is known as a class.
         */

        // K MEANS CLUSTERING //

        /**
         * We will use a popular clustering algorithm called K-Means Clustering to group together all of the similar
         * colours in our image.
         *
         * The K-Means clustering algorithm requires that we specify the number of classes we wish to find before
         * running the algorithm.
         */

        // MEASURING SIMILARITY BETWEEN COLOURS //

        /**
         * If we are grouping pixels of similar colours together, we need to be able to tell what makes colours
         * 'similar'.
         *
         * In order to measure the similarity between a pair of colours, the "distance" between the colours can be
         * measured.
         */

        // LAB COLOUR SPACE //

        /**
         * Typically, the distance is measured as euclidean distance, but in the RGB colour space, such distances do
         * not reflect what humans percieve as similar/dissimilar colours.
         *
         * To work around this problem, it is common to transform an image into an alternative colour space.
         *
         * The LAB (pronounced L-A-B) colour space is specifically designed so that the Euclidean distance between
         * colours closeley matches the percieved similarity of human vision.
         */

        // transforming the image into the LAB colour space
        image = ColourSpace.convert(image, ColourSpace.CIE_Lab);

        // RUNNING THE K-MEANS CLUSTERING ALGORITHM //

        /**
         * Constructing the K-Means algorithm
         *
         * Method parameter (2) specifies the number of clusters/classes we want the algorithm to find.
         *
         * We can specify a second argument to configure the number of iterations the algorithm carries out -
         * the default is 30 if we do not specify.
         */
        FloatKMeans cluster = FloatKMeans.createExact(2);

        /**
         * The FloatKMeans algorithm takes it's input as an array of floating point vectors.
         *
         * We can flatten the pixels of an image into the required form using the 'getPixelVectorNative()' method.
         */

        // gathering the image data from the image
        float[][] imageData = image.getPixelVectorNative(new float[image.getWidth() * image.getHeight()][3]); // 3 as each pixel has 3 bands?

        /**
         * When we have the information in the required form, we can run the K-Means algorithm to group the pixels
         * into the requested number of classes.
         */

        // running the K-Means algorithm
        FloatCentroidsResult result = cluster.cluster(imageData);

        /**
         * Each class/cluster produced by the K-Means algorithm has an index, starting from 0.
         *
         * Each class is represented by it's centroid - the average location of all the points belonging to the class.
         *
         * As we are in the LAB domain, when we say location, we really mean colour. So each centroid is a value
         * corresponding to the average colour of all pixels in that class.
         *
         * So, the 'cluster' algorithm finds K cluters of pixels in the image based on their colour, and returns us a
         * list of centroids, which represent the average value of the pixels in each class. In the LAB domain, this
         * average value refers to location. In the RGB (or similar) domain, this average value refers to average colour.
         *
         * We can print out the co-ordinates of each centroid (the LAB colour value).
         */

        // iterating through the centroids in the result and printing their LAB co-ordinates
        float[][] centroids = result.centroids;
        for(float[] fs : centroids){
            System.out.println(Arrays.toString(fs));
        }

        // ASSIGNING EACH PIXEL TO IT'S RESPECTIVE CLASS USING THE CENTROIDS //

        /**
         * We can use the centroids we found to assign each pixel in the image into it's class.
         *
         * This process is known as CLASSIFICATION.
         *
         * We do this by using a HardAssigner. There are loads of different types of these.
         *
         * The FloatCentroidResult object has a method defualtHardAssigner() that can return an assigner fit for our
         * purpose.
         *
         * HardAssigners have a method called assign() that takes a vector (the L, a ,b value of a single pixel) and
         * returns the index of the class that it belongs to.
         *
         * We will use this method to create an image that visualises the pixels and their respective classes by
         * replacing each pixel in the input image with the centroid of it's respective class (i.e., the average
         * colour of the class it belongs to).
         *
         * Basically, what we are doing is creating an image where all pixels are split into one of K classes where each
         * class contains pixels of a similar colour.
         */

        // gathering the HardAssigner
        HardAssigner<float[], ?, ?> assigner = result.defaultHardAssigner();

        // iterating through the pixels in the image
        for(int y = 0; y < image.getHeight(); y ++){
            for(int x = 0; x < image.getWidth(); x++){
                // getting the LAB colour value for the pixel
                float[] pixelVector = image.getPixelNative(x, y);

                // getting the index of the centroid the pixel belongs to
                int centroid = assigner.assign(pixelVector);

                // setting the pixel's colour to the centroid colour of the class it is a member of
                image.setPixelNative(x, y, centroids[centroid]);
            }
        }

        // converting the new image into RGB space and displaying it
        image = ColourSpace.convert(image, ColourSpace.RGB);
        DisplayUtilities.display(image, displayFrame);

        ////////////////
        // SEGMENTING //
        ////////////////

        /**
         * To actually produce a segmentation of the image, we need to group together all pixels with the same class
         * that are touching eachother.
         *
         * Each set of pixels representing a segment is known as a CONNECTED COMPONENT.
         *
         * Connected Components in OpenIMAJ are modelled by the ConnectedComponent class.
         *
         * The GreyscaleConnectedComponentLabeler class can be used to find the connected components in the image.
         *
         * Note that this class only processes grey scale images, so we need to flatten our RGB image into greeyscale
         * in order to process it.
         *
         * The flatten method creates a greyscale image from an RGB one by averaging the value of the three bands for
         * each pixel.
         */

        // finding the connected components in the image
        GreyscaleConnectedComponentLabeler labeler = new GreyscaleConnectedComponentLabeler();
        List<ConnectedComponent> components = labeler.findComponents(image.flatten());

        /**
         * The ConnectedComponent class has many useful methods for extracting the useful information out of the object.
         *
         * We will use the information to label each connected component in our image.
         *
         * We will only render in components that are over a certain size (50 pixels in this case).
         */

        // count to keep track of the number of regions
        int i = 0;

        // iterating through the components in the image
        for(ConnectedComponent component : components){
            // only considering components that have an area greater than 50 pixels.
            if (component.calculateArea() < 50)
                continue;

            // adding a label to the image at the centroid point of this component
            image.drawText("Point: " + (i++), component.calculateCentroidPixel(), HersheyFont.TIMES_MEDIUM, 20);
        }

        // displaying the labelled image
        DisplayUtilities.display(image, displayFrame);


        /////////////
        // SUMMARY //
        /////////////

        /**
         * What we did:
         *
         * - Divised a segmentation algorithm by:
         *      - Performing K-Means Clustering on an image to seperate the image's pixels into 2 classes based on
         *      their colour.
         *      - Coloured each pixel to the centroid colour of the class it belongs to
         *      - Labelled the connected components within the image.
         */
    }
}