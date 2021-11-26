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
import org.openimaj.feature.DiskCachingFeatureExtractor;
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
import org.openimaj.image.feature.local.aggregate.PyramidSpatialAggregator;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator.Mode;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;
import org.openimaj.ml.kernel.HomogeneousKernelMap;
import org.openimaj.ml.kernel.HomogeneousKernelMap.WindowType;
import org.openimaj.ml.kernel.HomogeneousKernelMap.KernelType;
import org.openimaj.util.pair.IntFloatPair;

import de.bwaldvogel.liblinear.SolverType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 12 - Classification with Caltech 101
 *
 * Exercises
 *
 * @author Charles Powell
 */
public class Ch12Exercises {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{
        // Exercise 1 - Apply Homogeneous Kernel Map
        //exercise1();

        // Exercise 2 - Feature Caching
        exercise2();

        // Exercise 3 - The Whole Dataset
        exercise3();
    }

    ////////////////
    // EXERCISE 1 //
    ////////////////

    /**
     * Exercise 1 - Apply a Homogeneous Kernel Map
     *
     * A Homogeneous Kernel Map transforms data into a compact linear representation 
     * such that applying a linear classifier approximates, to a high degree of accuracy, 
     * the application of a non-linear classifier over the original data. Try using 
     * the HomogeneousKernelMap class with a KernelType.Chi2 kernel and WindowType.Rectangular 
     * window on top of thePHOWExtractor feature extractor. 
     * 
     * What effect does this have on performance?
     */
    public static void exercise1() throws Exception{

        /**
         * Gathering dataset
         */

        GroupedDataset<String, VFSListDataset<Record<FImage>>, Record<FImage>> allData = Caltech101.getData(ImageUtilities.FIMAGE_READER);

        GroupedDataset<String, ListDataset<Record<FImage>>, Record<FImage>> data = GroupSampler.sample(allData, 5, false);

        GroupedRandomSplitter<String, Record<FImage>> splits = new GroupedRandomSplitter<String, Record<FImage>>(data, 15, 0, 15);

        /**
         * Constructing SIFT feature extractor
         */

        DenseSIFT dsift = new DenseSIFT(5, 7);
        PyramidDenseSIFT<FImage> pdsift = new PyramidDenseSIFT<FImage>(dsift, 6f, 7);
          
        /**
         * Constructing HardAssigner for image features
         */

        HardAssigner<byte[], float[], IntFloatPair> assigner = trainQuantiser(GroupedUniformRandomisedSampler.sample(splits.getTrainingDataset(), 30), pdsift, 300);

        /**
         * Constructing Homogeneous Kernel Map feature extractor
         */

        HomogeneousKernelMap hkm = new HomogeneousKernelMap(KernelType.Chi2, WindowType.Rectangular);
        FeatureExtractor<DoubleFV, Record<FImage>> extractor = hkm.createWrappedExtractor(new PHOWExtractor(pdsift, assigner));

        /**
         * Constructing image classifier
         */

        LiblinearAnnotator<Record<FImage>, String> ann = new LiblinearAnnotator<Record<FImage>, String>(extractor, 
                                                                                                        Mode.MULTICLASS, 
                                                                                                        SolverType.L2R_L2LOSS_SVC, 
                                                                                                        1.0, 
                                                                                                        0.00001);
        ann.train(splits.getTrainingDataset());

        /**
         * Evaluating classifier
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

        /**
         * Conclusion.
         */

        /**
         * Performance WITHOUT Homogenous Kernel Map : Accuracy ~ 0.613, Error Rate ~ 0.317
         * 
         * Performance WITH Homogenous Kernel Map : Accuracy ~ 0.8, Error Rate ~ 0.2
         * 
         * Therefore, the classifier that uses the Homogeneous Kernel Map feature extractor
         * achieves better classification results.
         */
    }

    ///////////////////////
    // END OF EXERCISE 1 //
    ///////////////////////

    ////////////////
    // EXERCISE 2 //
    ////////////////

    /**
     * Exercise 3 - Feature Caching
     * 
     * The DiskCachingFeatureExtractor class can be used to cache features extracted 
     * by a FeatureExtractor to disk. It will generate and save features if they don’t 
     * exist, or read from disk if they do. Try to incorporate the DiskCachingFeatureExtractor 
     * into your code. You’ll also need to save the HardAssigner using IOUtils.writeToFile and 
     * load it using IOUtils.readFromFile because the features must be kept with the same 
     * HardAssigner that created them.
     */
    public static void exercise2() throws Exception{
        /**
         * Gathering dataset
         */

        GroupedDataset<String, VFSListDataset<Record<FImage>>, Record<FImage>> allData = Caltech101.getData(ImageUtilities.FIMAGE_READER);

        GroupedDataset<String, ListDataset<Record<FImage>>, Record<FImage>> data = GroupSampler.sample(allData, 5, false);

        GroupedRandomSplitter<String, Record<FImage>> splits = new GroupedRandomSplitter<String, Record<FImage>>(data, 15, 0, 15);

        /**
         * Constructing SIFT feature extractor
         */

        DenseSIFT dsift = new DenseSIFT(5, 7);
        PyramidDenseSIFT<FImage> pdsift = new PyramidDenseSIFT<FImage>(dsift, 6f, 7);
          
        /**
         * Constructing HardAssigner for image features
         * 
         * Reads from file if it exists, creates new one if it doesnt
         */

        File assignerFile = new File("src/main/resources/feature-cache/assigner");
        HardAssigner<byte[], float[], IntFloatPair> assigner;

        if(assignerFile.exists()){
            assigner = IOUtils.readFromFile(assignerFile);
        }
        else{
            assigner = trainQuantiser(GroupedUniformRandomisedSampler.sample(splits.getTrainingDataset(), 30), pdsift, 300);
            IOUtils.writeToFile(assigner, assignerFile);
        }

        /**
         * Constructing FeatureExtractor and DiskCachingFeatureExtractor
         */

        FeatureExtractor<DoubleFV, Record<FImage>> extractor = new PHOWExtractor(pdsift, assigner);

        DiskCachingFeatureExtractor<DoubleFV, Record<FImage>> diskCachingExtractor = new DiskCachingFeatureExtractor<DoubleFV, Record<FImage>>(new File("src/main/resources/feature-cache/"), extractor);
        

        /**
         * Constructing image classifier
         */

        LiblinearAnnotator<Record<FImage>, String> ann = new LiblinearAnnotator<Record<FImage>, String>(diskCachingExtractor, 
                                                                                                        Mode.MULTICLASS, 
                                                                                                        SolverType.L2R_L2LOSS_SVC, 
                                                                                                        1.0, 
                                                                                                        0.00001);
        ann.train(splits.getTrainingDataset());

        /**
         * Evaluating classifier
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

    ///////////////////////
    // END OF EXERCISE 2 //
    ///////////////////////

    ////////////////
    // EXERCISE 3 //
    ////////////////

    /**
     * Exercise 3 - The Whole Dataset
     * 
     * Try running the code over all the classes in the Caltech 101 dataset. Also 
     * try increasing the number of visual words to 600, addingextra scales to the 
     * PyramidDenseSIFT (try [4, 6, 8, 10] and reduce the step-size of the DenseSIFT 
     * to 3), and instead of using the BlockSpatialAggregator, try the 
     * PyramidSpatialAggregator with [2, 4] blocks. 
     * 
     * What level of classifier performancedoes this achieve?
     */
    public static void exercise3() throws Exception{
        /**
         * Gathering dataset
         */

        GroupedDataset<String, VFSListDataset<Record<FImage>>, Record<FImage>> allData = Caltech101.getData(ImageUtilities.FIMAGE_READER);

        GroupedRandomSplitter<String, Record<FImage>> splits = new GroupedRandomSplitter<String, Record<FImage>>(allData, 15, 0, 15);

        /**
         * Constructing SIFT feature extractor
         * 
         * DenseSIFT step size reduced to 3
         */

        DenseSIFT dsift = new DenseSIFT(3, 7);
        PyramidDenseSIFT<FImage> pdsift = new PyramidDenseSIFT<FImage>(dsift, 6f, 4,6,8,10);
          
        /**
         * Constructing HardAssigner for image features
         */

        HardAssigner<byte[], float[], IntFloatPair> assigner = trainQuantiser(GroupedUniformRandomisedSampler.sample(splits.getTrainingDataset(), 30), pdsift, 600);

        /**
         * Constructing feature extractor
         */

        FeatureExtractor<DoubleFV, Record<FImage>> extractor = new PHOWExtractoPyramidSpatialAggregator(pdsift, assigner);

        /**
         * Constructing image classifier
         */

        LiblinearAnnotator<Record<FImage>, String> ann = new LiblinearAnnotator<Record<FImage>, String>(extractor, 
                                                                                                        Mode.MULTICLASS, 
                                                                                                        SolverType.L2R_L2LOSS_SVC, 
                                                                                                        1.0, 
                                                                                                        0.00001);
        ann.train(splits.getTrainingDataset());

        /**
         * Evaluating classifier
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

        /**
         * Conclusion
         */

        /**
         * Standard Performance : Accuracy ~ 0.613, Error Rate ~ 0.317
         * 
         * Performance with adjustments : Accuracy ~ ?, Error Rate ~ ?
         */
    }

    ///////////////////////
    // END OF EXERCISE 3 //
    ///////////////////////


    //////////////////////////////
    // HELPER METHODS & CLASSES //
    //////////////////////////////

    /**
     * Method to perform K-Means clustering on SIFT features extracted from a set of images.
     * 
     * @param sample The sample set of images to perform 
     * @param pdsift The PyramidSIFT object.
     * @return A hard assigner for the groups of SIFT features within the sample images.
     */
    static HardAssigner<byte[], float[], IntFloatPair> trainQuantiser(Dataset<Record<FImage>> sample, PyramidDenseSIFT<FImage> pdsift, int numVisWords)
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
            
        ByteKMeans km = ByteKMeans.createKDTreeEnsemble(numVisWords);
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
         * Extracts a single feature from the image.
         */
        public DoubleFV extractFeature(Record<FImage> object) {
            FImage image = object.getImage();
            pdsift.analyseImage(image);
            BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<byte[]>(assigner);
            BlockSpatialAggregator<byte[], SparseIntFV> spatial = new BlockSpatialAggregator<byte[], SparseIntFV>(bovw, 2, 2);
            return spatial.aggregate(pdsift.getByteKeypoints(0.015f), image.getBounds()).normaliseFV();
        }
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
    static class PHOWExtractoPyramidSpatialAggregator implements FeatureExtractor<DoubleFV, Record<FImage>> {

        // member variables
        PyramidDenseSIFT<FImage> pdsift;
        HardAssigner<byte[], float[], IntFloatPair> assigner;
    
        /**
         * Class constructor.
         * 
         * @param pdsift
         * @param assigner
         */
        public PHOWExtractoPyramidSpatialAggregator(PyramidDenseSIFT<FImage> pdsift, HardAssigner<byte[], float[], IntFloatPair> assigner){
            this.pdsift = pdsift;
            this.assigner = assigner;
        }
    
        /**
         * Extracts a single feature from the image.
         */
        public DoubleFV extractFeature(Record<FImage> object) {
            FImage image = object.getImage();
            pdsift.analyseImage(image);
            BagOfVisualWords<byte[]> bovw = new BagOfVisualWords<byte[]>(assigner);
            PyramidSpatialAggregator<byte[], SparseIntFV> spatial = new PyramidSpatialAggregator<byte[], SparseIntFV>(bovw, 2, 4);
            return spatial.aggregate(pdsift.getByteKeypoints(0.015f), image.getBounds()).normaliseFV();
        }
    }
}
