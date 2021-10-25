package uk.ac.soton.ecs.cp6g18.ch1;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.typography.Font;
import org.openimaj.image.typography.hershey.HersheyFont;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 1 - OpenIMAJ Fundamentals
 *
 * Exercises
 *
 * @author Charles Powell
 */
public class Ch1Exercises {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) {
        // Exercise 1 - Playing With The Sample Application
        //exercise1();
    }

    ////////////////
    // EXERCISE 1 //
    ////////////////

    /**
     * Exercise 1 - PLaying With The Sample Application.
     *
     * Take a look at the App.java from within your IDE.
     * Can you modify the code to render something other than “hello world” in a different font and colour?
     */
    public static void exercise1(){
        //Create an image
        MBFImage image = new MBFImage(320,70, ColourSpace.RGB);

        //Fill the image with white
        image.fill(RGBColour.WHITE);

        /**
         * Changing the image text.
         */
        String imageText = "Image text";
        Font textFont = HersheyFont.TIMES_BOLD;
        int fontSize = 30;
        Float[] textColour = RGBColour.CYAN;

        image.drawText(imageText, 60, 40, textFont, fontSize, textColour);

        //Display the image
        DisplayUtilities.display(image);
    }

    ///////////////////////
    // END OF EXERCISE 1 //
    ///////////////////////
}
