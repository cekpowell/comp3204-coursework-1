package uk.ac.soton.ecs.cp6g18.ch2;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.Ellipse;

import java.net.URL;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 2 - Processing Your First Image.
 *
 * Tutorial Code.
 *
 * @author Charles Powell
 */
public class Ch2Tutorial {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{

        //////////////////////////////////
        // BASICS OF IMAGES IN OPENIMAJ //
        //////////////////////////////////

        /**
         * OpenIMAJ tries to keep images simply as arrays of pixels (unlike the built in Java packages).
         *
         * Images are stored as 2d arrays of pixel values - each array within the image is a row of pixels, and each row
         * is stored as an array of floats.
         */

        /**
         * Read/write images using the Image Utilities class.
         */

        // loading an image using the ImageUtilities class
        MBFImage image = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/sinaface.jpg"));

        /**
         * Two types of image - MBFImage and FImage
         * - FImage : Greyscale image which represents each pixel as a value between 0 and 1.
         * - MBFImage : Multi-band image (i.e., multiple values for each pixel, i.e., colour). WHat these bands
         * represent is given by the image's public colourSpace field (e.g., RGB, CYMK).
         *  - A multi-band MBFImage is made up of a number of FImages - e.g., three for RGB, four for CYMK.
         *
         * A 'band' is like a colour within the colour spectrum of the image - e.g., RGB has three bands, one for each
         * of red, blue and green.
         */

        // outputting the colour space of the image
        //System.out.println("Image Colour Space : " + image.colourSpace);

        /**
         * Images can be displayed using the DisplayUtilities class.
         *
         * We can display the whole image, or display individual bands.
         */

        // displaying the image
        //DisplayUtilities.display(image);

        // displaying the first band of the image (Red channel as it is RGB)
        //DisplayUtilities.display(image.getBand(0), "Red Channel");

        ////////////////////////////
        // BASIC IMAGE PROCESSING //
        ////////////////////////////

        /**
         * The most basic thing you can do to process an image is play with it's pixel values.
         *
         * OpenIMAJ makes this very easy as an image is just an array of floats - so we can just loop over it
         * like any standard array.
         */

        // creating an image clone
        MBFImage clone = image.clone();

        // isolating the red band of the image iterativley
        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                // setting the green and blue bands to 0 - image is now only red.
                clone.getBand(1).pixels[y][x] = 0;
                clone.getBand(2).pixels[y][x] = 0;
            }
        }

        // isolating the red band of the image quickly
        clone.getBand(1).fill(0f);
        clone.getBand(2).fill(0f);

        // displaying the processed image.
        //DisplayUtilities.display(clone);

        ///////////////////////////////////
        // MORE COMPLEX IMAGE PROCESSING //
        ///////////////////////////////////

        /**
         * The more complex image processing operations are wrapped in OpenIMAJ processor interfaces.
         *
         * There are different types of processor interface, which differ based on how their internal algorithms work.
         *  - ImageProcessors
         *  - KernelProcessors
         *  - PixelProcessors
         *  - GridProcessors
         *
         * All processors have a common theme - they take in an image and spit out a processed image (or data).
         */

        /**
         * One example basic image processing operation is edge detection.
         *
         * A popular edge detection algorithm is the Canny Edge Detector.
         *
         * When applied to an image, each pixel in each band of the image is replaced with the edge response at that
         * point (i.e., the contrast).
         *
         * If a particular edge is only strong within one band or another, then that colour will be strong in the
         * processed image.
         */

        // performing edge detection on the image
        image.processInplace(new CannyEdgeDetector());


        // displaying the processed image
        //DisplayUtilities.display(image);

        //////////////////////
        // ADDING TO IMAGES //
        //////////////////////

        /**
         * We can also draw ontop of our image in OpenIMAJ.
         *
         * On every image, there is a set of drawing functions that can be called to draw points, lines, shapes and
         * text on images.
         */

        // drawing speech bubbles on top of the image.
        image.drawShapeFilled(new Ellipse(700f, 450f, 20f, 10f, 0f), RGBColour.WHITE);
        image.drawShapeFilled(new Ellipse(650f, 425f, 25f, 12f, 0f), RGBColour.WHITE);
        image.drawShapeFilled(new Ellipse(600f, 380f, 30f, 15f, 0f), RGBColour.WHITE);
        image.drawShapeFilled(new Ellipse(500f, 300f, 100f, 70f, 0f), RGBColour.WHITE);
        image.drawText("OpenIMAJ is", 425, 300, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
        image.drawText("Awesome", 425, 330, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
        DisplayUtilities.display(image);
    }
}