package uk.ac.soton.ecs.cp6g18.ch14;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.time.Timer;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 14 - Parallel Processing
 *
 * Tutorial Code.
 *
 * @author Charles Powell
 */
public class Ch14Tutorial {

    /////////////////////////////////////////////////////
    // PRE-REQUISITE INFORMATION - ADVANCED TECHNIQUES //
    /////////////////////////////////////////////////////

    /**
     * OpenIMAJ has a number of advanced features that are not directly
     * related to multi-media processing, analysis and generation.
     * 
     * This part of the tutorial looks at some of those features.
     */

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{

        //////////////////
        // INTRODUCTION //
        //////////////////

        // PARALLEL PROCESSING //

        /**
         * Modern computers tend to have multiple processors.
         * 
         * By making use of all the processing availability of a machine, the programs
         * run on the machine can run much faster.
         * 
         * Normally, we can do this in Java using classes found in the 
         * java.util.concurrent package.
         * 
         * In OpenIMAJ, we can make use of the Parallel class, which contains a number
         * of methods that allow for us to effecienty and effectivley write multi-threaded
         * loops.
         */


        ///////////////////////////////////
        // EXAMPLE USE OF PARALLEL CLASS //
        ///////////////////////////////////

        /**
         * To get started, we will see the Parallel class's version of a for loop.
         */

        Parallel.forIndex(0, 10, 1, new Operation<Integer>() {
            public void perform(Integer i) {
                System.out.println(i);
            }
           });

        /**
         * When running this code, we will see the numbers 1 through 10 printed to
         * the screen, but not necesarrily in that order (as they are run in parallel
         * and the order of operations is non-deterministic).
         */

        //////////////////////////////////////////////////
        // APPLYING PARALLELISATION IN IMAGE PROCESSING //
        //////////////////////////////////////////////////

        /**
         * We will now consider a more realistic scenario where we may want to consider
         * parallelisation.
         * 
         * We will build a program to compute the normalised average of the images in a 
         * dataset.
         * 
         * We will do this with both non-parallelised and parallelised versions in order 
         * to compare the two.
         */

        // GATHERING THE DATASET //

        /**
         * We start by loading the dataset (the Caltech101 dataset).
         * 
         * Here, we load the images directly, as we only need the images, and not the image
         * data.
         */

        VFSGroupDataset<MBFImage> allImages = Caltech101.getImages(ImageUtilities.MBFIMAGE_READER);
        
        /**
         * We will then restrict ourselves to using a subset of the first 8 groups (image
         * categories) from within the dataset.
         */

        GroupedDataset<String, ListDataset<MBFImage>, MBFImage> images = GroupSampler.sample(allImages, 8, false);

        // PERFORMING THE NON-PARALLELISED IMAGE PROCESSING //

        /**
         * For each group, we want to find the average image.
         * 
         * Before looking at the parallised version, we will consider the un-parallelised version.
         * 
         * We can find the average image for each group by looping through the images in the group, 
         * resampling and normalising each image before drawing it in the center of a white image, 
         * and then adding it to an accumulator.
         * 
         * At the end of the loop, we divide the accumulated image by the number of samples used to create it.
         * 
         * We will also add some code to time how long it takes to do this operation.
         */

        // timing
        Timer t1 = Timer.timer();

        // calculating average images
        List<MBFImage> output = new ArrayList<MBFImage>();
        final ResizeProcessor resize = new ResizeProcessor(200);
        for (ListDataset<MBFImage> clzImages : images.values()) {
            MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

            for (MBFImage i : clzImages) {
                MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
                tmp.fill(RGBColour.WHITE);
                MBFImage small = i.process(resize).normalise();
                int x = (200 - small.getWidth()) / 2;
                int y = (200 - small.getHeight()) / 2;
                tmp.drawImage(small, x, y);
                current.addInplace(tmp);
            }

            current.divideInplace((float) clzImages.size());
            output.add(current);
        }

        // timing
        System.out.println("Non-parallel Time : " + t1.duration() + "ms");

        // displaying average images
        DisplayUtilities.display("Average Images", output);

        /**
         * Time taken to compute ~ 16000ms
         */

        // PERFORMING PARALLELISED IMAGE PROCESSING //

        /**
         * Looking at the code, we have three options for parallelisation:
         * 
         *      - Parallelise the outer loop
         *      - Parallelise the inner loop
         *      - Parallelise both
         *
         * There are many trade-offs to consider when picking an option.
         * 
         * Here, we will chose to parallelise the inner loop.
         */

        /**
         * The processing method with the inner loop parallelised can be
         * defined as follows.
         * 
         * Notice that we put a synchronized block around the section of code
         * that increments the accumulator so that multiple threads cannot 
         * do this at once.
         */

         // timing
        Timer t2 = Timer.timer();

        // calculating average images
        List<MBFImage> parrelisedOutput = new ArrayList<MBFImage>();
        for (ListDataset<MBFImage> clzImages : images.values()) {
            final MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

            Parallel.forEach(clzImages, new Operation<MBFImage>() {
                public void perform(MBFImage i) {
                    final MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
                    tmp.fill(RGBColour.WHITE);
                    final MBFImage small = i.process(resize).normalise();
                    final int x = (200 - small.getWidth()) / 2;
                    final int y = (200 - small.getHeight()) / 2;
                    tmp.drawImage(small, x, y);
                    synchronized (current) {
                        current.addInplace(tmp);
                    }
                }
            });

            current.divideInplace((float) clzImages.size());
            output.add(current);
        }

        // timing
        System.out.println("Inside Loop Parallel Time : " + t2.duration() + "ms");

        // displaying average images
        DisplayUtilities.display("Inside Loop Paralellised Average Images", parrelisedOutput);

        /**
         * Time taken to compute ~ 6000ms (10000ms quicker!)
         */
    }
}