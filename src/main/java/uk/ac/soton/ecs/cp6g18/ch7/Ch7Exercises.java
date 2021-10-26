package uk.ac.soton.ecs.cp6g18.ch7;

import org.openimaj.image.MBFImage;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

import javax.swing.*;

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
        /**
         * Process Operation : Removal of green and blue colour spectrums
         */

        // window to hold the video
        JFrame videoWindow = new JFrame("OpenIMAJ Tutorial: Chapter 7: Exercise 1");

        // gathering the video
        Video<MBFImage> video = new VideoCapture(480,360);

        // displaying the video
        VideoDisplay<MBFImage> videoDisplay = VideoDisplay.createVideoDisplay(video, videoWindow);

        // processing the video via EventListner
        videoDisplay.addVideoListener(
            new VideoDisplayListener<MBFImage>() {

                /**
                 * Called before the frame is updated.
                 * @param frame The frame about to be updated (i.e., displayed).
                 */
                public void beforeUpdate(MBFImage frame){
                    frame.getBand(1).fill(0f);
                    frame.getBand(2).fill(0f);
                }

                /**
                 * Called after the frame is updated
                 * @param display The video display after the frame has been updated (i.e., displayed).
                 */
                public void afterUpdate(VideoDisplay<MBFImage> display) {
                }
            }
        );
    }

    ///////////////////////
    // END OF EXERCISE 1 //
    ///////////////////////
}
