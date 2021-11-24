package uk.ac.soton.ecs.cp6g18.ch4;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.connectedcomponent.GreyscaleConnectedComponentLabeler;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.statistics.distribution.Histogram;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;

import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 4 - Global Image Features
 *
 * Tutorial Code
 *
 * @author Charles Powell
 */
public class Ch4Tutorial {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{
        // creating the display frame
        JFrame displayFrame = DisplayUtilities.createNamedWindow("Display", "OpenIMAJ Tutorial: Chapter 4: Tutorial");

        // loading an image to be used in the tutorial
        MBFImage image = ImageUtilities.readMBF(new URL("https://images.theconversation.com/files/350851/original/file-20200803-22-dfm95n.jpg?ixlib=rb-1.1.0&q=45&auto=format&w=1200&h=1200.0&fit=crop"));

        ///////////////////////////
        // TUTORIAL INTRODUCTION //
        ///////////////////////////

        // GLOBAL FEATURES AND FEATURE VECTORS //

        /**
         * We will be extracting Global Features from images.
         * 
         * These Global Features are numerical representations of the image, and can be used
         * as similarity measures between images - e.g., find the most similar images from a set.
         * 
         * Images are made up of pixels, these are the most basic form of numerical representation
         * of images. However, we can do calculations on the pixel values to get other numerical 
         * representations that mean different things. These numerical representations are known
         * as Feature Vectors and they represent different Features within the image.
         */

        ///////////////////////
        // COLOUR HISTOGRAMS //
        ///////////////////////

        // INTRODUCTION //

        /**
         * A very simple and easy to understand feature of an image is a Colour Histogram.
         * 
         * A colour histogram tells us the proportion of each colour within an image.
         * 
         * As pixels are represented by different amount of red, green and blue, we can take
         * this values and accumulate them in our histogram (e.g., when we see a red pixel
         * we add 1 to our "red pixel count" in the histogram).
         * 
         * A colour histogram can keep track of counts for any number of colours in any number
         * of dimensions, but the usual method is to split red, green and blue values  of a pixel
         * into a smallish number of "bins" into which the colours are thrown.
         * 
         * This produces a three-dimensional cube with loads of small cubes acting as bins that acrrue
         * counts for that colour.
         * 
         * The collection of the data within the colour histogram forms a feature vector within the dimension
         * of the number of bins in the histogram.
         */

        // COLOUR HISTOGRAM OF IMAGE //

        /**
         * OpenIMAJ contains a multidimensional colour histogram implementation that is contructed using
         * the number of bins required for each dimension.
         */

        // constructing a three-dimensional colour histogram with four bins for each dimension.
        MultidimensionalHistogram histogram = new MultidimensionalHistogram(4,4,4);

        /**
         * This creates a colour historgram with the required number of dimensions and required number
         * of bins for each dimension.
         * 
         * In this case, we have a histogram with 4 x 4 x 4 = 64 bins.
         */

        /**
         * The MultidimensionalHistogram data structure does not do anything on its own.
         * 
         * The HistogramModel class provides a means for creating a MultiDimensionalHistogram
         * from an image.
         * 
         * The HistogramModel class assumes the image has been normalised and returns a normalised
         * histogram.
         */

        // creating a histogram from an image using HistogramModel
        HistogramModel model = new HistogramModel(4, 4, 4);
        model.estimateModel(image);
        histogram = model.histogram;

        // printing the histogram
        System.out.println(histogram);

        // COLOUR HISTOGRAM OF MULTIPLE IMAGES //

        /**
         * We can re-use the same HistogramModel instance to create histograms for multiple images.
         * 
         * Note that, when re-using the HistogramModel instance with a new image, we lose the information 
         * of the previous image.
         */

        // creating a list of histograms from three images.

        URL[] imageURLs = new URL[] {
            new URL( "http://openimaj.org/tutorial/figs/hist1.jpg" ),
            new URL( "http://openimaj.org/tutorial/figs/hist2.jpg" ), 
            new URL( "http://openimaj.org/tutorial/figs/hist3.jpg" ) 
        };
        List<MultidimensionalHistogram> histograms = new ArrayList<MultidimensionalHistogram>();
        model = new HistogramModel(4, 4, 4);
        for( URL u : imageURLs ) {
            model.estimateModel(ImageUtilities.readMBF(u));
            histograms.add( model.histogram.clone() );
        }

        // COMPARING COLOUR HISTOGRAMS //

        /**
         * We can use the colour histograms of multiple images to determine how similar the images
         * are - i.e., determine how similar the colours within the images are.
         * 
         * The Histogram class extends the MultiDimensionalDoubleFV class, which is a feature vector
         * represented by multi-dimensional set of double precision numbers.
         * 
         * This class provides us with a compare() method which allows comparison between two multi-dimensional
         * sets of doubles.
         * 
         * The method takes the other feature vector to compare against and a comparison method which are 
         * implemented in the DoubleFVCOmparison class.
         */

        /**
         * For example, we can compare two histograms by calculating the euclidean distance between them in the
         * feature space that they occupy.
         * 
         * The Euclidean Distance Score is symmetric:
         *      - If Histogram1.compare(Histogram2) = Histogram2.compare(Histogram1).
         */
        
        MultidimensionalHistogram histogram1 = histograms.get(0);
        MultidimensionalHistogram histogram2 = histograms.get(1);

        double distanceScore = histogram1.compare(histogram2, DoubleFVComparison.EUCLIDEAN);

        /**
         * The above comparison will give us a score on how similar (or dissimilar) the histograms are based
         * on their Euclidean distance in their feature space.
         * 
         * Two very similar histograms will be very close together in the feature space, and so will have small distance
         * values.
         * 
         * Two very different histograms will be far apart and have a large distance score.
         */

        /**
         * We can compare the distance between all three histograms with a simple loop.
         */

        for( int i = 0; i < histograms.size(); i++ ) {
            for( int j = i; j < histograms.size(); j++ ) {
                double distance = histograms.get(i).compare( histograms.get(j), DoubleFVComparison.EUCLIDEAN );

                System.out.println("Distance between histogram :" + i + " and histogram : " + j + " : " + distance);
            }
        }
    }
}