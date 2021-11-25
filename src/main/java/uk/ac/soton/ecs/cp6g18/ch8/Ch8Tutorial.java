package uk.ac.soton.ecs.cp6g18.ch8;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.FacialKeypoint;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

import java.util.List;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 8 - Finding Faces
 *
 * Tutorial Code.
 *
 * @author Charles Powell
 */
public class Ch8Tutorial {

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
         * OpenIMAJ contains sets of classes that contain implementations for some of the state
         * of the art face detection and recognition algorithms.
         * 
         * These classes are provided as a sub-project of the OpenIMAJ code called 'faces'.
         * 
         * The OpenIMAJ maven archetype adds the 'face' library as a dependency, and so we can use
         * these classes straight away.
         */

        // CREATING VIDEO CAPTURE OBJECT //

        /**
         * We will start by creating a video capture object and displaying it's associated video.
         * 
         * We will later add a VideoListener onto this VideoCapture object to find faces within
         * the video. Actually, what we are doing is continually finding faces within the frames
         * of the video.
         */

        VideoCapture vc = new VideoCapture(640, 480);

        VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(vc);

        //////////////////////////
        // BASIC FACE DETECTION //
        //////////////////////////

        /**
         * For finding faces in images (or in this case video frames), we use a face detector.
         * 
         * The FaceDetector interface provides the API for face detectors and there are currently
         * two implementations within OpenIMAJ:
         *      - The 'HaarCascadeDetector'.
         *      - The 'SandeepFaceDetector'.
         * 
         * The HaarCascadeDetector is considerably more robust, and so we will be using this.
         */

        // HaarCascadeDetector //

        /**
         * We start by creating a new VideoListener on the video display object.
         * 
         * Within this video listener object we will do the following in the 'beforeUpdate()' method:
         *      - Instantatie a new HaarCascadeDetector.
         *      - Run the 'detectFaces()' method on the HaarCascadeDetector instance.
         *          - This method takes in a single band image, and so we must transform our
         *            multi-band image into a single band image.
         *          - The 'detectFaces()' method returns a list of DetectedFace objects which
         *            contain information about the faces in the image.
         *      - Use the information returned by the 'detectFaces()' method to get the rectangular
         *        boxes surrounding each face and draw them into our video frame.
         * 
         * As this is being done in the 'beforeUpdate()' method, the video display will end up showing
         * the bounded boxes on the displayed video.
         */

        // vd.addVideoListener( 
        //     new VideoDisplayListener<MBFImage>() {
        //         public void beforeUpdate( MBFImage frame ) {
        //             /**
        //              * Detecting faces.
        //              */
        //             FaceDetector<DetectedFace,FImage> fd = new HaarCascadeDetector(40);
        //             List<DetectedFace> faces = fd.detectFaces( Transforms.calculateIntensity(frame));

        //             /**
        //              * Drawing rectangular box around faces.
        //              */
        //             for( DetectedFace face : faces ) {
        //                 // drawing rectangle around face
        //                 frame.drawShape(face.getBounds(), RGBColour.RED);
        //             }
        //         }
        //         public void afterUpdate( VideoDisplay<MBFImage> display ) {

        //         }
        // });

        /////////////////////////////
        // IMPROVED FACE DETECTION //
        /////////////////////////////

        /**
         * OpenIMAJ has other face detectors that can go a bit further than just finding the face in an image.
         */

        // FKEFaceDetector //

        /**
         * The FKEFaceDetector finds facial keypoints (the corners of the eyes, nose and mouth).
         * 
         * The FKEFaceDetector returns a slightly different object called a KEDetectedFace, which contains extra
         * information about where the keypoints of the face are located.
         */

        /**
         * We can use the FKEFaceDetector detector instead by simply instantiating that object instead 
         * of the HaarCascascadeDetector, and by adjusting the typing for the detected faces.
         */


        // vd.addVideoListener( 
        //     new VideoDisplayListener<MBFImage>() {
        //         public void beforeUpdate( MBFImage frame ) {
        //             /**
        //              * Detecting faces.
        //              */
        //             FaceDetector<KEDetectedFace,FImage> fd = new FKEFaceDetector();
        //             List<KEDetectedFace> faces = fd.detectFaces( Transforms.calculateIntensity(frame));

        //             /**
        //              * Drawing rectangular box around faces.
        //              */
        //             for( DetectedFace face : faces ) {
        //                 // drawing rectangle around face.
        //                 frame.drawShape(face.getBounds(), RGBColour.RED);
        //             }
        //         }
        //         public void afterUpdate( VideoDisplay<MBFImage> display ) {

        //         }
        // });
        
        // PLOTTING FACIAL KEYPOINTS //

        /**
         * The code above won't do anything different to the basic facial detector - i.e., it won't plot the
         * facial keypoints that it has detected automatically.
         * 
         * To plot the facial keypoints, we can use the getKeypoints() method on each detected face.
         * 
         * Each keypoint has a position (public field) which is relative to the face, so we need to translate
         * the points to the position of the face within the video frame plotting them.
         * 
         * To do this, we can use the translate() method of the Point2d class and the minX() and minY() methods
         * of the Rectangle class.
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
}