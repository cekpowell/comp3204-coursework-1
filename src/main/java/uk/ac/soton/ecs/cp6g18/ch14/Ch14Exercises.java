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
 * Exercises
 *
 * @author Charles Powell
 */
public class Ch14Exercises {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{
        // Exercise 1 - Paralellise the Outer Loop
        exercise1();
    }

    ////////////////
    // EXERCISE 1 //
    ////////////////

    /**
     * Exercise 1 - Parrelise the Outer Loop
     * 
     * As we discussed earlier in the tutorial, there were three primary ways 
     * in which we could have approached the parallelisation of the image-averaging program. 
     * 
     * Instead of parallelising the inner loop, can you modify the code to parallelise the outer loop instead? 
     * 
     * Does this make the code faster? 
     * 
     * What are the pros and cons of doing this?
     */
    public static void exercise1() throws Exception{
        /**
         * Gathering the dataset
         */

        VFSGroupDataset<MBFImage> allImages = Caltech101.getImages(ImageUtilities.MBFIMAGE_READER);

        GroupedDataset<String, ListDataset<MBFImage>, MBFImage> images = GroupSampler.sample(allImages, 8, false);

        /**
         * Processing the images with parallised outer loop
         */

         // timing
        Timer t1 = Timer.timer();

        // calculating average images
        final List<MBFImage> output = new ArrayList<MBFImage>();
        final ResizeProcessor resize = new ResizeProcessor(200);
        Parallel.forEach(images.values(), new Operation<ListDataset<MBFImage>>() {
            public void perform(ListDataset<MBFImage> images) {
                MBFImage current = new MBFImage(200, 200, ColourSpace.RGB);

                for (MBFImage image : images) {
                    MBFImage tmp = new MBFImage(200, 200, ColourSpace.RGB);
                    tmp.fill(RGBColour.WHITE);
                    MBFImage small = image.process(resize).normalise();
                    int x = (200 - small.getWidth()) / 2;
                    int y = (200 - small.getHeight()) / 2;
                    tmp.drawImage(small, x, y);
                    current.addInplace(tmp);
                }

                current.divideInplace((float) images.size());
                output.add(current);
            }
        });

        // timing
        System.out.println("Outside loop Parallel Time : " + t1.duration() + "ms");

        // displaying average images
        DisplayUtilities.display("Parellised Average Images", output);

        /**
         * Conclusion
         */

        /**
         * Parallelising the outside loop makes the code faster, but not as much as parallising
         * the inside loop does.
         * 
         * This is because the outside loop is not run as many times as the inside loop is.
         * 
         * Pros:
         *      - Each group is mutually exclusive, so no code needs to be syncrhonized.
         * 
         * Cons:
         *      - Not as much time saved as when parallising the inside loop.
         */

    }

    ///////////////////////
    // END OF EXERCISE 1 //
    ///////////////////////
}
