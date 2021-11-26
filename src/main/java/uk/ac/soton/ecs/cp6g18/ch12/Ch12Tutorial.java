package uk.ac.soton.ecs.cp6g18.ch12;

import org.openimaj.data.DataSource;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.dataset.sampling.GroupSampler;
import org.openimaj.experiment.dataset.sampling.GroupedUniformRandomisedSampler;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101;
import org.openimaj.image.annotation.evaluation.datasets.Caltech101.Record;
import org.openimaj.image.feature.dense.gradient.dsift.ByteDSIFTKeypoint;
import org.openimaj.image.feature.dense.gradient.dsift.DenseSIFT;
import org.openimaj.image.feature.dense.gradient.dsift.PyramidDenseSIFT;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator.Mode;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.util.pair.IntFloatPair;

import de.bwaldvogel.liblinear.SolverType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 12 - Classification with Caltech 101
 *
 * Tutorial Code.
 *
 * @author Charles Powell
 */
public class Ch12Tutorial {

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
         * Here, we will go through the steps of building and evaluating a 
         * state of the art image classifier.
         * 
         * For the purpose of this tutorial, we will be using features extracted
         * from images, but everything learnt here can be applied to features extracted
         * from other forms of media.
         */

        ///////////////////////////////////////
        // GATHERING THE CALTECH 101 DATASET //
        ///////////////////////////////////////

        /**
         * The first thing we need for this tutorial is a dataset of images with which we will 
         * work with.
         * 
         * We will be using a well known set of labelled images called the Caltech 101 dataset.
         * 
         * This dataset contains labelled images of 101 object classes together with a set of 
         * background images.
         */

        /**
         * OpenIMAJ has built in support for working with the Caltech101 dataset through the
         * 'Caltech101' class.
         * 
         * We can use this class to load the dataset into a GroupedDataset object.
         */

        GroupedDataset<String, VFSListDataset<Record<FImage>>, Record<FImage>> allData = Caltech101.getData(ImageUtilities.FIMAGE_READER);

        /**
         * For this tutorial, we will work with a subset of the classes in the grouped dataset to minimise 
         * the time taken to run programs.
         * 
         * We can create a subset using the GroupSampler class.
         * 
         * In this case, we will take only the first 5 classes/groups from within the dataset.
         */

        GroupedDataset<String, ListDataset<Record<FImage>>, Record<FImage>> data = GroupSampler.sample(allData, 5, false);

        /////////////////////////////////////////////////////
        // SPLITTING THE DATASET INTO TRAINING AND TESTING //
        /////////////////////////////////////////////////////

        /**
         * We need to split the dataset into training and testing images.
         * 
         * One approach is to chose a number of training and testing images for each class
         * of images in the dataset.
         * 
         * This can be achieved using the GroupedRandomSplitter class.
         */

        GroupedRandomSplitter<String, Record<FImage>> splits = new GroupedRandomSplitter<String, Record<FImage>>(data, 15, 0, 15);


        /**
         * The above code has split the dataset into a training dataset with 15 images
         * per group/class and a testing dataset with 15 images per group/class.
         * 
         * The 0 parameter references the number of validation images, which we will not use in
         * this tutorial.
         */

        ////////////////////
        // IMAGE FEATURES //
        ////////////////////

        /**
         * To build the classifier, we need to decide what image features we are going to extract 
         * from the image.
         */

        // PYRAMID HISTOGRAM OF WORDS //

        /**
         * For this tutorial, we are going to use a technique called the Pyramid Histogram of Words
         * (PHOW) to extract the image features.
         * 
         * PHOW is based on the idea of extracting Dense SIFT features, quantising the SIFT features
         * into visual words, and then building a spacial histogram of the visual word occurances.
         */

        /**
         * The idea of a visual word is as follows:
        *    - Rather than representing each SIFT feature as a 128 dimension feature vector, we 
        *      represent it by an identitifier.
        *    - Similar features (those that have similar but not necesarrily the same feature vectors)
        *      are given the same identifier.
        */

        /**
         * A common approach for assigning identifiers to features is to train a vector quantiser (fancy
         * name for a classifier) using k-means.
         */

        /**
         * To build a histogram of visual words (known as a Bag of Visual Words), all we have to do is 
         * count up how many times each identifier appears in an image and store the values in a 
         * histogram.
         */

        // FORMING THE PHOW EXTRACTOR //

        /**
         * To get started, we will define two objects.
         */

        DenseSIFT dsift = new DenseSIFT(5, 7);
        PyramidDenseSIFT<FImage> pdsift = new PyramidDenseSIFT<FImage>(dsift, 6f, 7);
          
        // CLUSTERING THE SIFT FEATURES //

        /**
         * The next stage is to write some code to perform K-Means Clustering on a sample
         * of SIFT features in order to build a HardAssigner that can assign features to
         * identifiers.
         * 
         * The 'trainQuantiser' method is defined to create such a HardAssigner, which we
         * make use of here.
         */

        HardAssigner<byte[], float[], IntFloatPair> assigner = trainQuantiser(GroupedUniformRandomisedSampler.sample(splits.getTrainingDataset(), 30), pdsift);

        // EXTRACTING IMAGE FEATURES //

        /**
         * Next, we need to write a Feature Extractor which can be used to train our classifier.
         * 
         * This code is contained within the PHOWExtractor class.
         */

        /**
         * We can now construct an instance of our PHOW extractor.
         */

        FeatureExtractor<DoubleFV, Record<FImage>> extractor = new PHOWExtractor(pdsift, assigner);


        //////////////////////
        // IMAGE CLASSIFIER //
        //////////////////////

        /**
         * We are now ready to construct and train our classifer.
         * 
         * We will use the LinearClassifier provided by the LiblinearAnnotator class.
         */

        LiblinearAnnotator<Record<FImage>, String> ann = new LiblinearAnnotator<Record<FImage>, String>(extractor, 
                                                                                                        Mode.MULTICLASS, 
                                                                                                        SolverType.L2R_L2LOSS_SVC, 
                                                                                                        1.0, 
                                                                                                        0.00001);
        ann.train(splits.getTrainingDataset());

        ///////////////////////////////////////
        // EVALUATING CLASSIFIER PERFORMANCE //
        ///////////////////////////////////////

        /**
         * We can use the OpenIMAJ evaluation framework to perform an automated evaluation of our
         * classifier's acuracy for us.
         */

        ClassificationEvaluator<CMResult<String>, String, Record<FImage>> eval = new ClassificationEvaluator<CMResult<String>, String, Record<FImage>>(ann, 
                                                                                                                                                       splits.getTestDataset(), 
                                                                                                                                                       new CMAnalyser<Record<FImage>, 
                                                                                                                                                       String>(CMAnalyser.Strategy.SINGLE));
       
        Map<Record<FImage>, ClassificationResult<String>> guesses = eval.evaluate();
        CMResult<String> result = eval.analyse(guesses);

        System.out.println();
        System.out.println(result.getDetailReport());
        System.out.println();
    }

    /**
     * Method to perform K-Means clustering on SIFT features extracted from a set of images.
     * 
     * @param sample The sample set of images to perform 
     * @param pdsift The PyramidSIFT object.
     * @return A hard assigner for the groups of SIFT features within the sample images.
     */
    static HardAssigner<byte[], float[], IntFloatPair> trainQuantiser(Dataset<Record<FImage>> sample, PyramidDenseSIFT<FImage> pdsift)
    {
        List<LocalFeatureList<ByteDSIFTKeypoint>> allkeys = new ArrayList<LocalFeatureList<ByteDSIFTKeypoint>>();
        for (Record<FImage> rec : sample) {
            FImage img = rec.getImage();
            pdsift.analyseImage(img);
            allkeys.add(pdsift.getByteKeypoints(0.005f));
        }
        if (allkeys.size() > 10000){
            allkeys = allkeys.subList(0, 10000);
        }
            
        ByteKMeans km = ByteKMeans.createKDTreeEnsemble(300);
        DataSource<byte[]> datasource = new LocalFeatureListDataSource<ByteDSIFTKeypoint, byte[]>(allkeys);
        ByteCentroidsResult result = km.cluster(datasource);
        return result.defaultHardAssigner();
    }

    /**
     * Feature extractor for the classifier.
     * 
     * This class uses a BlockSpatialAggregator together with a BagOfVisualWords to compute
     * 4 histograms across the image (2 horizontal and 2 vertical).
     * 
     * The BagOfVisualWords uses the given HardAssigner to assign each Dense SIFT feature
     * to a visual word and then compute the histogram.
     * 
     * The resultant spatial histograms are then appended together and normalised before
     * being returned.
     */
    static class PHOWExtractor implements FeatureExtractor<DoubleFV, Record<FImage>> {

        // member variables
        PyramidDenseSIFT<FImage> pdsift;
        HardAssigner<byte[], float[], IntFloatPair> assigner;
    
        /**
         * Class constructor.
         * 
         * @param pdsift
         * @param assigner
         */
        public PHOWExtractor(PyramidDenseSIFT<FImage> pdsift, HardAssigner<byte[], float[], IntFloatPair> assigner){
            this.pdsift = pdsift;
            this.assigner = assigner;
        }
    
        /**
         * Extraxts a single feature from the image.
         */
        public DoubleFV extractFeature(Record<FImage> object) {
            FImage image = object.getImage();
            pdsift.analyseImage(image);
            BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<byte[]>(assigner);
            BlockSpatialAggregator<byte[], SparseIntFV> spatial = new BlockSpatialAggregator<byte[], SparseIntFV>(bovw, 2, 2);
            return spatial.aggregate(pdsift.getByteKeypoints(0.015f), image.getBounds()).normaliseFV();
        }
    }
}