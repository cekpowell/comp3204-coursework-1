package uk.ac.soton.ecs.cp6g18.ch7;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.edges.CannyEdgeDetector;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.xuggle.XuggleVideo;

import javax.swing.*;
import java.net.URL;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 7 - Processing Video
 *
 * Tutorial Code.
 *
 * @author Charles Powell
 */
public class Ch7Tutorial {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{

        //////////////////
        // INTRODUCTION //
        //////////////////

        /**
         * Here, we will see how to deal with videos using OpenIMAJ .
         *
         * OpenIMAJ has a set of tools for loading, displaying and processing various kinds of video.
         */

        //////////////////////////////
        // VIDEO BASICS IN OPENIMAJ //
        //////////////////////////////

        // CREATING A VIDEO //

        /**
         * All videos in OpenIMAJ are subtypes of the 'Video' class.
         *
         * This class is typed on the type of the underlying frame (i.e., the type of image that makes up each frame
         * of the video.
         *
         * We will start by making a video which holds coloured frames (MBFImage).
         */

        // creating a coloured video
        Video<MBFImage> video;

        // LOADING A VIDEO FROM A FILE/URL //

        /**
         * We can use the 'Xuggle' library to load videos from a file or external URL.
         */

        // loading video from URL
        video = new XuggleVideo(new URL("http://static.openimaj.org/media/tutorial/keyboardcat.flv"));

        // LOADING LIVE VIDEO FROM A CAMERA //

        /**
         * We can use the 'VideoCapture' class to capture live video input from the computer's primary capture
         * device.
         *
         * NOTE: It appears the width and height of the video capture device MUST be in the ratio 3:2.
         */

        // loading video from primary capture device (width x height)
        video = new VideoCapture(320, 240);

        /**
         * Alternative constructors can be used with the VideoCapture.getVideoDevices() method to load from alternative
         * video capture devices (if your computer has multiple).
         */

        // DISPLAYING THE VIDEO //

        /**
         * To display a video to the screen, we must create a 'VideoDisplay' instance using the static
         * 'createVideoDisplay' method and passing in the video object.
         *
         * A 'VideoDisplay' instance is typed with the type of the underlying frame (i.e., just as the 'Video' object
         * was).
         */

        // window to hold the video display
        JFrame rawVideoDisplayWindow = new JFrame("Raw Video Display");

        // displaying the video
        VideoDisplay<MBFImage> rawVideoDisplay = VideoDisplay.createVideoDisplay(video, rawVideoDisplayWindow);

        ///////////////////////
        // PROCESSING VIDEOS //
        ///////////////////////

        /**
         * We want to process videos.
         *
         * Each video is a collection of frames (images), and we process videos by processing their individual frames.
         *
         * We will demonstrate how we can process videos by applying an EdgeDetector to the video.
         */

        // FRAME ITERATION PROCESSING //

        /**
         * Videos are lists of frames and so they are iterable as such!
         *
         * We can loop over the frames of a video using a for loop in order to process each frame individually.
         *
         * We can then display each frame into an image display (which creates a 'video').
         *      - That is, the display window is constantly being updated with the processed video frames, creating
         *      a video in the display window.
         */

        // raw video is already being displayed - processing it processing it and displaying it again makes it laggy,
        // so making a new video display instance
        Video<MBFImage> video2 = new VideoCapture(320, 240);

        // processing the video via frame iteration
        processViaIteration(video2);

        // EVENT DRIVEN PROCESSING //

        /**
         * Another approach to video processing is to use an event driven technique.
         *
         * This technique ties processing to image display automatically.
         *
         * It works by adding a VideoEventListener to the video display and providing
         * implementations for methods that are called on each frame of the video before and after
         * it is updated (i.e., displayed).
         *
         * The VideoEventListener is given the frame before it is displayed, and the video display after the frame
         * is rendered, and we can implement methods to configure these objects.
         */

        // raw video is already being displayed - processing it processing it and displaying it again makes it laggy,
        // so making a new video display instance
        Video<MBFImage> video3 = new VideoCapture(320, 240);

        // processing video via EventListener
        //processViaEventListener(video3);
    }

    /**
     * Processes video by iterating over the frames of the video and displaying each one as an image.
     *
     * @param video The video being processed.
     */
    public static void processViaIteration(Video<MBFImage> video){
        // creating window to hold the processed video
        JFrame iteratingVideoDisplayWindow = new JFrame("Video Processed Via Frame Iteration");

        // looping over the video frames
        for(MBFImage frame : video){
            // applying an edge detector to the frame
            DisplayUtilities.display(frame.process(new CannyEdgeDetector()), iteratingVideoDisplayWindow);
        }
    }

    /**
     * Processes the video using a VideoListenerEvent to make changes to each frame before it is displayed,
     * or to the video display after each frame is displayed.
     *
     * @param video The video being processed.
     */
    public static void processViaEventListener(Video<MBFImage> video){
        // creating window to hold the processed video
        JFrame eventProcessedVideoDisplayWindow = new JFrame("Video Processed Via Event Listeners");

        // creating video display
        VideoDisplay<MBFImage> eventProcessedVideoDisplay = VideoDisplay.createVideoDisplay(video, eventProcessedVideoDisplayWindow);

        // adding video listener to the video display
        eventProcessedVideoDisplay.addVideoListener(
            // creating the video display listener
            new VideoDisplayListener<MBFImage>() {

                /**
                 * This method is called before the frame is updated.
                 * @param frame The frame about to be updated (i.e., displayed).
                 */
                public void beforeUpdate(MBFImage frame) {
                    /**
                     * Here, we can process the frame before it is displayed - just like we did with the iterable
                     * approach.
                     */
                    frame.processInplace(new CannyEdgeDetector());
                }

                /**
                 * This methid us called after the frame is updated
                 * @param display The video display after the frame has been updated (i.e., displayed).
                 */
                public void afterUpdate(VideoDisplay<MBFImage> display) {
                }
            }
        );
    }

}