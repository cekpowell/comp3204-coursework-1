package uk.ac.soton.ecs.cp6g18.ch4;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 4 - Global Image Features
 *
 * Exercises
 *
 * @author Charles Powell
 */
public class Ch4Exercises {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{
        // Exercise 1 - Finding and Displaying Similar Images
        exercise1();

        // Exercise 2 - Exploring Comparison Measures
        exercise2();
    }

    ////////////////
    // EXERCISE 1 //
    ////////////////

    /**
     * Exercise 1 - Finding and Displaying Similar Images
     *
     * Which images are most similar? Does that match with what you expect if you look at the images? Can you make 
     * the application display the two most similar images that are not the same?
     */
    public static void exercise1() throws Exception{

        // WHICH IMAGES ARE MOST SIMILAR //

        /**
         * The most similar images are images 1 and 2.
         * 
         * This is to be expected as they have similar colours (i.e., reds and oranges),
         * where as image 3 differs to both of these (i.e., mainly blacks and greys).
         */

        // FINDING MOST SIMILAR IMAGES //

        /**
         * Creating list of histograms
         */

        URL[] imageURLs = new URL[] {
            new URL( "http://openimaj.org/tutorial/figs/hist1.jpg" ),
            new URL( "http://openimaj.org/tutorial/figs/hist2.jpg" ),
            new URL( "http://openimaj.org/tutorial/figs/hist3.jpg" )
        };
        List<MultidimensionalHistogram> histograms = new ArrayList<MultidimensionalHistogram>();
        HistogramModel model = new HistogramModel(4, 4, 4);
        for( URL u : imageURLs ) {
            model.estimateModel(ImageUtilities.readMBF(u));
            histograms.add( model.histogram.clone() );
        }

        /**
         * Comparing histograms and determining which images are the most similar;
         */

        double smallestDistance = Double.MAX_VALUE;
        int smallestDistanceHistogram1 = 0;
        int smallestDistanceHistogram2 = 0;

        for( int i = 0; i < histograms.size(); i++ ) {
            for( int j = i; j < histograms.size(); j++ ) {
                if(i != j){
                    double distance = histograms.get(i).compare( histograms.get(j), DoubleFVComparison.EUCLIDEAN );

                    if(distance < smallestDistance){
                        smallestDistance = distance;
                        smallestDistanceHistogram1 = i;
                        smallestDistanceHistogram2 = j;
                    }
                }
            }
        }

        /**
         * Displaying the most similar image histograms.
         */

        System.out.println("Images : " + smallestDistanceHistogram1 + " and : " + smallestDistanceHistogram2 + " are most similar with a distance of : " + smallestDistance);
    }

    ///////////////////////
    // END OF EXERCISE 1 //
    ///////////////////////

    ////////////////
    // EXERCISE 2 //
    ////////////////

    /**
     * Exercise 2 - Exploring Comparison Measures
     *
     * What happens when you use a different comparison measure (such as 
     * DoubleFVComparison.INTERSECTION)?
     */
    public static void exercise2() throws Exception{

        // TESTING DIFFERENT COMPARISON MEASURES //

        /**
         * Creating list of histograms
         */

        URL[] imageURLs = new URL[] {
            new URL( "http://openimaj.org/tutorial/figs/hist1.jpg" ),
            new URL( "http://openimaj.org/tutorial/figs/hist2.jpg" ),
            new URL( "http://openimaj.org/tutorial/figs/hist3.jpg" )
        };
        List<MultidimensionalHistogram> histograms = new ArrayList<MultidimensionalHistogram>();
        HistogramModel model = new HistogramModel(4, 4, 4);
        for( URL u : imageURLs ) {
            model.estimateModel(ImageUtilities.readMBF(u));
            histograms.add( model.histogram.clone() );
        }

        /**
         * Comparing histograms with different comparison measures
         */
        
        for( int i = 0; i < histograms.size(); i++ ) {
            for( int j = i; j < histograms.size(); j++ ) {
                double distance = histograms.get(i).compare( histograms.get(j), DoubleFVComparison.EUCLIDEAN );
                double intersection = histograms.get(i).compare( histograms.get(j), DoubleFVComparison.INTERSECTION );
                double correlation = histograms.get(i).compare( histograms.get(j), DoubleFVComparison.CORRELATION );
                double cosineDist = histograms.get(i).compare( histograms.get(j), DoubleFVComparison.COSINE_DIST );

                System.out.println("Distance between histogram :" + i + " and histogram : " + j + " : " + distance);
                System.out.println("Intersection between histogram :" + i + " and histogram : " + j + " : " + intersection);
                System.out.println("Correlation between histogram :" + i + " and histogram : " + j + " : " + correlation);
                System.out.println("CosineDist between histogram :" + i + " and histogram : " + j + " : " + cosineDist);
            }
        }

        // CONCLUSION (for tested comparison measures) //

        /**
         * Intersection - How many colours the two images share - e.g., 1 for the same image as all colours are shared.
         * Cosine Distance - The cosine similarity between the two histograms in their feature space.
         * Correlation - Measure of how similar the colours are ? - e.g., higher the number the more similar the image histograms.
         */
    }

    ///////////////////////
    // END OF EXERCISE 2 //
    ///////////////////////
}
