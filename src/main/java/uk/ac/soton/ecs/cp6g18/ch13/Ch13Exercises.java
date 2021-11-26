package uk.ac.soton.ecs.cp6g18.ch13;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.model.EigenImages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 13 - Face Recognition 101 - Eigenfaces
 *
 * Exercises
 *
 * @author Charles Powell
 */
public class Ch13Exercises {

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

        // Exercise 3 - Apply a Threshold
        exercise3();
    }

    ////////////////
    // EXERCISE 1 //
    ////////////////

    /**
     * Exercise 1 - Reconstructing Faces
     * 
     * An interesting property of the features extracted by the Eigenfaces 
     * algorithm (specifically from the PCA process) is that it's possibleto 
     * reconstruct an estimate of the original image from the feature. Try doing 
     * this by building a PCA basis as described above, and then extract the feature 
     * of a randomly selected face from the test-set. Use the EigenImages#reconstruct()
     * to convert the feature back into an image and display it. You will need to 
     * normalise the image (FImage#normalise()) to ensure it displays correctly as 
     * the reconstruction might give pixel values bigger than 1 or smaller than 0.
     */
    public static void exercise1() throws Exception{

        /**
         * Gathering the dataset
         */

        VFSGroupDataset<FImage> dataset = new VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);

        /**
         * Splitting the data
         */

        int nTraining = 5;
        int nTesting = 5;
        GroupedRandomSplitter<String, FImage> splits = new GroupedRandomSplitter<String, FImage>(dataset, nTraining, 0, nTesting);
        GroupedDataset<String, ListDataset<FImage>, FImage> training = splits.getTrainingDataset();
        GroupedDataset<String, ListDataset<FImage>, FImage> testing = splits.getTestDataset();

        /**
         * Learning the PCA basis
         */

        List<FImage> basisImages = DatasetAdaptors.asList(training);
        int nFeatures = 100;
        EigenImages eigen = new EigenImages(nFeatures);
        eigen.train(basisImages);

        /**
         * Forming the database of lower-dimensionality images (i.e., the features)
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

        /**
         * Reconstructing a sample image from it's features (lower-dimensionality version)
         */

        // getting random face from features database
        int personNumber = 0;
        DoubleFV randomFace = ((DoubleFV[]) features.values().toArray()[new Random().nextInt(features.values().size())])[personNumber];

        // reconstructing random face
        FImage reconstructedRandomFace = eigen.reconstruct(randomFace);

        // normalising reconstructed face
        reconstructedRandomFace = reconstructedRandomFace.normalise();

        // displaying the reconstructed face
        DisplayUtilities.display(reconstructedRandomFace, "Reconstructed Face");
    }

    ///////////////////////
    // END OF EXERCISE 1 //
    ///////////////////////

    ////////////////
    // EXERCISE 2 //
    ////////////////

    /**
     * Exercise 2 - Explore the Effect of Training Set Size
     * 
     * The number of images used for training can have a big effect 
     * in the performance of your recogniser. Try reducing the number 
     * of training images (keep the number of testing images fixed at 5). 
     * 
     * What do you observe?
     */
    public static void exercise2() throws Exception{

        /**
         * Forming datasets
         */

        VFSGroupDataset<FImage> dataset = new VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);

        int nTraining = 2;
        int nTesting = 5;
        GroupedRandomSplitter<String, FImage> splits = new GroupedRandomSplitter<String, FImage>(dataset, nTraining, 0, nTesting);
        GroupedDataset<String, ListDataset<FImage>, FImage> training = splits.getTrainingDataset();
        GroupedDataset<String, ListDataset<FImage>, FImage> testing = splits.getTestDataset();

        /**
         * Learning PCA basis
         */

        List<FImage> basisImages = DatasetAdaptors.asList(training);
        int nFeatures = 100;
        EigenImages eigen = new EigenImages(nFeatures);
        eigen.train(basisImages);

        /**
         * Forming database of features
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

        /**
         * Classifying new images
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
         * Conclusion
         */

        /**
         * Accuracy of classifier decreases as training size decreases.
         * 
         * e.g., 
         *      
         *      - nTraining = 5 : Classifier Accuracy ~95
         *      - nTraining = 2 : Classifier Accuracy ~81
         *      - ...
         */
    }

    ///////////////////////
    // END OF EXERCISE 2 //
    ///////////////////////

    ////////////////
    // EXERCISE 3 //
    ////////////////

    /**
     * Exercise 3 - Apply a Threshold
     * 
     * In the original Eigenfaces paper, a variant of nearest-neighbour 
     * classification was used that incorporated a distance threshold. If the 
     * distance between the query face and closest database face was greater 
     * than a threshold, then an unknown result would be returned, rather 
     * than just returning the label of the closest person. 
     * 
     * Can you alter your code to include such a threshold? 
     * 
     * What is a good value for the threshold?
     */
    public static void exercise3() throws Exception{
        /**
         * Forming datasets
         */

        VFSGroupDataset<FImage> dataset = new VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip", ImageUtilities.FIMAGE_READER);

        int nTraining = 2;
        int nTesting = 5;
        GroupedRandomSplitter<String, FImage> splits = new GroupedRandomSplitter<String, FImage>(dataset, nTraining, 0, nTesting);
        GroupedDataset<String, ListDataset<FImage>, FImage> training = splits.getTrainingDataset();
        GroupedDataset<String, ListDataset<FImage>, FImage> testing = splits.getTestDataset();

        /**
         * Learning PCA basis
         */

        List<FImage> basisImages = DatasetAdaptors.asList(training);
        int nFeatures = 100;
        EigenImages eigen = new EigenImages(nFeatures);
        eigen.train(basisImages);

        /**
         * Forming database of features
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

        /**
         * Classifying new images
         */

        double correct = 0, incorrect = 0;
        double threshold = Double.MAX_VALUE; /** Threshold value not calculated - only provided logic for implementing it */
        for (String truePerson : testing.getGroups()) {
            for (FImage face : testing.get(truePerson)) {
                DoubleFV testFeature = eigen.extractFeature(face);
                String bestPerson = null;
                double minDistance = Double.MAX_VALUE;
                for (final String person : features.keySet()) {
                    for (final DoubleFV fv : features.get(person)) {
                        double distance = fv.compare(testFeature, DoubleFVComparison.EUCLIDEAN);
                        if (distance < minDistance && distance < threshold) {
                            minDistance = distance;
                            bestPerson = person;
                        }
                    }
                }
                if(bestPerson != null){
                    System.out.println("Actual: " + truePerson + "\tGuess: " + bestPerson);
                    if (truePerson.equals(bestPerson))
                        correct++;
                    else
                        incorrect++;
                }
                else{
                    System.out.println("Actual: " + truePerson + "\tGuess: " + "Unknown (closest person above threshold distance).");
                }
                
            }
        }
        System.out.println("Accuracy: " + (correct / (correct + incorrect)));

        /**
         * Conclusion
         */

        /**
         * A threshold value was not given in the code (Double.MAX_VALUE was used), only the framework for 
         * making use of one was implemented.
         * 
         * A good value for threshold:
         * 
         *      - Mean spread of features within groups + some constant (e.g., standard deviation of spread 
         *      between features within groups 'times' some constant K).
         */
    }

    ///////////////////////
    // END OF EXERCISE 3 //
    ///////////////////////
}
