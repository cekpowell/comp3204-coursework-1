package uk.ac.soton.ecs.cp6g18.ch8;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint.FacialKeypointType;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

import java.util.List;

import javax.swing.*;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 8 - Finding Faces
 *
 * Exercises
 *
 * @author Charles Powell
 */
public class Ch8Exercises {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{
        // Exercise 1 - Drawing Facial Keypoints
        //exercise1();

        // Exercise 2 - Speech Bubbles
        exercise2();
    }

    ////////////////
    // EXERCISE 1 //
    ////////////////

    /**
     * Exercise 1 - Drawing Facial Keypoints
     *
     * Use the information in the tutorial to plot the facial keypoints detected
     * by the FKEFaceDetector.
     */
    public static void exercise1() throws Exception{
        /**
         * Creating VideoCapture and Video Display.
         */

        VideoCapture vc = new VideoCapture(640, 480);

        VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(vc);

        /**
         * Detecting face and displaying facial keypoints.
         */

        vd.addVideoListener( 
            new VideoDisplayListener<MBFImage>() {
                public void beforeUpdate( MBFImage frame ) {
                    /**
                     * Detecting faces.
                     */
                    FaceDetector<KEDetectedFace,FImage> fd = new FKEFaceDetector();
                    List<KEDetectedFace> faces = fd.detectFaces( Transforms.calculateIntensity(frame));

                    /**
                     * Drawing rectangular box around faces.
                     */
                    for( KEDetectedFace face : faces ) {
                        // drawing rectangle around face
                        frame.drawShape(face.getBounds(), RGBColour.RED);

                        // drawing the facial keypoints
                        for(FacialKeypoint keypoint : face.getKeypoints()){
                            // translating the point to fit be placed inside the face rectangle
                            Point2dImpl point = new Point2dImpl(keypoint.position);
                            point.translate((float) face.getBounds().minX(), (float) face.getBounds().minY());

                            frame.drawPoint(point, RGBColour.GREEN, 5);
                        }
                    }
                }
                public void afterUpdate( VideoDisplay<MBFImage> display ) {

                }
        });
    }

    ///////////////////////
    // END OF EXERCISE 1 //
    ///////////////////////

    ////////////////
    // EXERCISE 2 //
    ////////////////

    /**
     * Exercise 2 - Speech Bubbles
     *
     * Try and take the speech bubble from the previous image tutorial and make it 
     * come from the mouth in the video. 
     * 
     * Hints: use getKeypoint(FacialKeypointType) to get the keypoint of the left corner 
     * of the mouth and plot the ellipses depending on that point. You may need to use smaller 
     * ellipses and text if your video is running at 320x240.
     */
    public static void exercise2() throws Exception{
        /**
         * Creating VideoCapture and Video Display.
         */

        VideoCapture vc = new VideoCapture(640, 480);

        VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(vc);

        /**
         * Detecting face and displaying facial keypoints.
         */

        vd.addVideoListener( 
            new VideoDisplayListener<MBFImage>() {
                public void beforeUpdate( MBFImage frame ) {
                    /**
                     * Detecting faces.
                     */
                    FaceDetector<KEDetectedFace,FImage> fd = new FKEFaceDetector();
                    List<KEDetectedFace> faces = fd.detectFaces( Transforms.calculateIntensity(frame));

                    /**
                     * Drawing rectangular box around faces.
                     */
                    for( KEDetectedFace face : faces ) {
                        // drawing rectangle around face
                        frame.drawShape(face.getBounds(), RGBColour.RED);

                        // drawing the facial keypoints
                        for(FacialKeypoint keypoint : face.getKeypoints()){
                            // translating the point to fit be placed inside the face rectangle
                            Point2dImpl point = new Point2dImpl(keypoint.position);
                            point.translate((float) face.getBounds().minX(), (float) face.getBounds().minY());

                            frame.drawPoint(point, RGBColour.GREEN, 5);
                        }

                        /**
                         * Drawing speech bubbles.
                         */

                        // getting position of left corner of mouth
                        Point2dImpl mouthLeftCorner = face.getKeypoint(FacialKeypointType.MOUTH_LEFT).position;

                        // translating point to be within facial rectangle
                        mouthLeftCorner.translate((float) face.getBounds().minX(), (float) face.getBounds().minY());

                        // drawing ellipses based on left corner of mouth.
                        frame.drawShapeFilled(new Ellipse(mouthLeftCorner.x - 10F, mouthLeftCorner.y - 10F, 20F, 10F, 0F), RGBColour.WHITE);
                        frame.drawShapeFilled(new Ellipse(mouthLeftCorner.x - 60F, mouthLeftCorner.y - 35F, 25F, 12F, 0F), RGBColour.WHITE);
                        frame.drawShapeFilled(new Ellipse(mouthLeftCorner.x - 110F, mouthLeftCorner.y - 80F, 30F, 15F, 0F), RGBColour.WHITE);
                        frame.drawShapeFilled(new Ellipse(mouthLeftCorner.x - 210F, mouthLeftCorner.y - 160F, 100F, 70F, 0F), RGBColour.WHITE);

                        // drawing speech bubble text
                        frame.drawText("OpenIMAJ is", (int) mouthLeftCorner.x - 285, (int) mouthLeftCorner.y - 160, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
                        frame.drawText("Awesome", (int) mouthLeftCorner.x - 285, (int) mouthLeftCorner.y - 130, HersheyFont.ASTROLOGY, 20, RGBColour.BLACK);
                    }
                }
                public void afterUpdate( VideoDisplay<MBFImage> display ) {

                }
        });
    }

    ///////////////////////
    // END OF EXERCISE 2 //
    ///////////////////////
}
