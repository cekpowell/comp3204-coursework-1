package uk.ac.soton.ecs.cp6g18.ch13;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.model.EigenImages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 13 - Face Recognition 101 - Eigenfaces
 *
 * Tutorial Code.
 *
 * @author Charles Powell
 */
public class Ch13Tutorial {

    ///////////////////////////////////////////////////
    // PRE-REQUISITE INFORMATION - FACES IN OPENIMAJ //
    ///////////////////////////////////////////////////

    // INTRODUCTION //

    /**
     * OpenIMAJ contains a number of tools for face detection, recognition and similarity
     * comparison.
     * 
     * OpenIMAJ implements the following pipleline:
     * 
     *      - Face Detection
     *      - Face Alignment
     *      - Facial Feature Extraction
     *      - Face Recognition/Classification
     * 
     * Each stage of the pipeline is configurable, and OpenIMAJ offers the possibility to implement
     * more.
     * 
     * The pipeline is designed to allow users to focus on a specific area of the pipeline without
     * having to worry about the other components.
     * 
     * In addition, it is fairly easy to modify and evaluate a complete pipeline.
     * 
     * i.e., The face detection and recognition components can be managed seperately, or the 
     * FaceRecognitionEngine class can be used to simplify the process.
     * 
     * On top of the pipeline, OpenIMAJ also has tools for tracking faces in videos and 
     * comparing the similarity of faces.
     */

    // FACE DETECTION //

    /**
     * Given an image, a Face Detector tie to localise all of the faces in the image.
     * 
     * All OpenIMAJ face detectors are sub-classes of FaceDetector, and they all produce
     * sub-classes of DetectedFace as their output.
     * 
     * OpenIMAJ implements a number of different face detectors:
     * 
     *      (See tutorial for information on each)
     * 
     *      - SandeepFaceDetector
     *      - HaarCascadeDetector
     *      - FKEFaceDetector
     *      - CLMFaceDetector
     *      - IdentifyFaceDetector
     */

    // FACE ALIGNMENT //

    /**
     * Many forms of face recogniser (process further down the pipeline) work better if 
     * the image patches used for training and querying are aligned to a common view.
     * 
     * This alignment allows the recognition system to focus on the appearance of the face
     * without having to worry about the variations of pose.
     * 
     * The FaceAligners classes take in faces detected by a FaceDetector as an input, and output
     * an image with the aligned face rendered within it.
     * 
     * OpenIMAJ contains a number of face alignement implementations, which include:
     *      
     *      (See tutorial for information on each)
     * 
     *      - AffineAligner
     *      - CLMAligner
     *      - IdentityAligner
     *      - MeshWarpAligner
     *      - RotateScaleAligner
     *      - ScalingAligner
     */

    // FACIAL FEATURE EXTRACTION //

    /**
     * Once a face as been detected and possibly aligned, we need to extract a feature
     * which can then be used for recognition or similarity comparison.
     * 
     * OpenIMAJ contains a number of different implementations of FacialFeatureExtractors
     * which produce FacialFeatures as output together with methods for comparing pairs
     * of FacialFeatures in order to get a similarity measurement.
     * 
     * Types of OpenIMAJ FacialFeatures (each one has a corresponding extractor):
     * 
     *      (See tutorial for information on each)
     * 
     *      - CLMPoseFeature
     *      - CLMPoseShapeFeature
     *      - CLMShapeFeature
     *      - DoGSIFTFeature
     *      - EigenFaceFeature
     *      - FaceImageFeature
     *      - FacePatchFeature
     *      - FisherFaceFeature
     *      - LocalLBPHistogram
     *      - LtpDtFeature
     */

    // FACE RECOGNITION & CLASSIFICATION //

    /**
     * After the FacialFeatures have been extracted, the next stage is to perform
     * face recognition (i.e., determining who's face it is) or classification (
     * determining some characteristics about the face - e.g., man/woman, glasses/no-glasses, etc).
     * 
     * All recognisers/classifers are instances of FaceRecogniser.
     * 
     * There are some default implementations - the most common is AnnotatorFaceRecogniser, which
     * can use any form of IncrementalAnnotator to perform the classification.
     * 
     * All face recognisers are capable of serializing/de-serializing their internal state to a disk.
     * 
     * All face recognisers are capable of incremental learning.
     * 
     *      - There are implementations of IncrementalAnnotator that implement common machine learning
     *      algorithms including k-nearest-neighbour and native-bayes.
     *      -  Batch annotators (BatchAnnotators), such as a Support Vector Machine annotator can also 
     *      be used by using an adaptor to convert the BatchAnnotator into an IncrementalAnnotator 
     *      (for example a InstanceCachingIncrementalBatchAnnotator).
     */

    // FACE SIMILARITY //

    /**
     * The FaceSimilarityEngine class provides methods for assessing the similarity of faces 
     * by comparing FacialFeatures using appropriate comparators.
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

        /**
         * Here, before looking at the arrat of tools OpenIMAJ offers for face 
         * recognition, we will consider one of the earliest face recognition
         * algorithms - EigenFaces.
         * 
         * The basic idea - we use PCA to compress the face images into a lower
         * dimensional space within which they can be compared easily.
         * 
         * Fundamentally, the projection of the images to the lower dimensional
         * space is a form of feature extraction.
         * 
         * Unlike feature extraction seen before, the feature extractors must be 
         * "learnt" through PCA (i.e., we must calculate the covariance matrix,
         * use this to calculate the eigenvectors and eigen values, etc).
         * 
         * Once the features have been extracted, classification can be performed
         * using any standard technique - 1-nearest neighbour classification is the 
         * standard choice for the Eigenfaces algorithm.
         * 
         * Eigenfaces will really only work well on (near) full-frontal face images. 
         * In addition, because of the way Eigenfaces works, the face images we use 
         * must all be the same size, and must be aligned (typically such that the eyes 
         * of each subject must be in the same pixel locations).
         */

        //////////////////////////////////////
        // EIGENFACES ALGORITHM IN OPENIMAJ //
        //////////////////////////////////////

        /**
         * The EigenFaces algorithm is implemented in OpenIMAJ using the EigenImages class.
         * 
         * The EigenImages class automaticallty deals with steps of preparing for and applying
         * PCA.
         */

        /////////////////
        // THE DATASET //
        /////////////////

        /**
         * For this tutorial, we will use a dataset of approximatley aligned face images from the
         * AT&T "The Database of Faces".
         */

        VFSGroupDataset<FImage> dataset = new VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);

        /**
         * We need to split the data into two sub-sets - one for training and one for testing.
         */

        int nTraining = 5;
        int nTesting = 5;
        GroupedRandomSplitter<String, FImage> splits = new GroupedRandomSplitter<String, FImage>(dataset, nTraining, 0, nTesting);
        GroupedDataset<String, ListDataset<FImage>, FImage> training = splits.getTrainingDataset();
        GroupedDataset<String, ListDataset<FImage>, FImage> testing = splits.getTestDataset();

        ////////////////////////////
        // LEARNING THE PCA BASIS //
        ////////////////////////////

        /**
         * We need to use the training images to learn the PCA basis which will be used
         * to project the images into features in a lower dimensional space.
         */

        /**
         * The EigenImages class simply needs a list of images from which to learn the
         * basis as well as how many dimensions we want our features to have (i.e., the
         * number of eigenvectors to retain).
         */

        List<FImage> basisImages = DatasetAdaptors.asList(training);
        int nFeatures = 100;
        EigenImages eigen = new EigenImages(nFeatures);
        eigen.train(basisImages);

        //////////////////////////
        // DATABASE OF FEATURES //
        //////////////////////////

        /**
         * We must now use the learnt PCA to build a database of features for all of
         * our training images.
         * 
         * We will use a map of strings to an array of features (corresponding to 
         * all the features of the training instances of the respective person).
         * 
         * That is, this database will map a person's name to an array of features (this
         * persons face images compressed using PCA).
         */

        Map<String, DoubleFV[]> features = new HashMap<String, DoubleFV[]>();
        for (final String person : training.getGroups()) {
            final DoubleFV[] fvs = new DoubleFV[nTraining];
            for (int i = 0; i < nTraining; i++) {
                final FImage face = training.get(person).get(i);
                fvs[i] = eigen.extractFeature(face);
            }
            features.put(person, fvs);
        }

        ////////////////////////////
        // CLASSIFYING NEW IMAGES //
        ////////////////////////////

        /**
         * Now that we have a database of labelled features (compressed images), in order
         * to classify a new image, we can just use any standard classification algorithm.
         * 
         * We will use 1-Nearest-Neighbour.
         */

        // 1-Nearest Neighbour //

        /**
         * To classify a new face with 1-nearest-neighbour, all we need to do is take the new 
         * image, gather it's features (compressed version) using the learnt PCA, and find the
         * known features (compressed image) that is closest to it within the feature space.
         * 
         * Through this method, we can loop over the testing images and estimate which person
         * they belong to.
         * 
         * In addition, as we know the true identity of these people, we can estimate the accuracy
         * of this method.
         */

        double correct = 0, incorrect = 0;
        for (String truePerson : testing.getGroups()) {
            for (FImage face : testing.get(truePerson)) {
                DoubleFV testFeature = eigen.extractFeature(face);
                String bestPerson = null;
                double minDistance = Double.MAX_VALUE;
                for (final String person : features.keySet()) {
                    for (final DoubleFV fv : features.get(person)) {
                        double distance = fv.compare(testFeature, DoubleFVComparison.EUCLIDEAN);
                        if (distance < minDistance) {
                            minDistance = distance;
                            bestPerson = person;
                        }
                    }
                }
                System.out.println("Actual: " + truePerson + "\tguess: " + bestPerson);
                if (truePerson.equals(bestPerson))
                    correct++;
                else
                    incorrect++;
            }
        }
        System.out.println("Accuracy: " + (correct / (correct + incorrect)));

        /**
         * The result of this code is that the algorithm is able to predict the identuty of a person
         * within the testing data to ~95% accuracy (variation due to random split of data each 
         * the PCA is learnt).
         */
    }
}