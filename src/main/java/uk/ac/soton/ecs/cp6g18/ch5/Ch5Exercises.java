package uk.ac.soton.ecs.cp6g18.ch5;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.feature.local.matcher.BasicTwoWayMatcher;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.FastEuclideanKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.MultipleMatchesMatcher;
import org.openimaj.feature.local.matcher.VotingKeypointMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
import org.openimaj.math.model.fit.RANSAC;

import java.net.URL;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 5 - SIFT and Feature Matching
 *
 * Exercises
 *
 * @author Charles Powell
 */
public class Ch5Exercises {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{
        // Exercise 1 - Different Matchers
        //exercise1();

        // Exercise 2 - Different Models
        exercise2();
    }

    ////////////////
    // EXERCISE 1 //
    ////////////////

    /**
     * Exercise 1 - Different Matchers
     *
     * Experiment with different matchers; try the BasicTwoWayMatcher for example.
     */
    public static void exercise1() throws Exception{
        /**
         * Loading images.
         */

        MBFImage query = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/query.jpg"));
        MBFImage target = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/target.jpg"));

        /**
         * Extracting KeyPoints
         */

        DoGSIFTEngine engine = new DoGSIFTEngine();

        LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
        LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());

        /**
         * Finding matches between two images using different matchers.
         */

        // BasicMatcher
        LocalFeatureMatcher<Keypoint> basicMatcher = new BasicMatcher<Keypoint>(80);
        basicMatcher.setModelFeatures(queryKeypoints);
        basicMatcher.findMatches(targetKeypoints);

        // BasicTwoWayMatcher
        LocalFeatureMatcher<Keypoint> basicTwoWayMatcher = new BasicTwoWayMatcher<Keypoint>();
        basicTwoWayMatcher.setModelFeatures(queryKeypoints);
        basicTwoWayMatcher.findMatches(targetKeypoints);

        // FastBasicKeypointMatcher
        LocalFeatureMatcher<Keypoint> fastBasicKeypointMatcher = new FastBasicKeypointMatcher<Keypoint>(80);
        fastBasicKeypointMatcher.setModelFeatures(queryKeypoints);
        fastBasicKeypointMatcher.findMatches(targetKeypoints);

        // FastEuclideanKeypointMatcher
        LocalFeatureMatcher<Keypoint> fastEuclideanKeypointMatcher = new FastEuclideanKeypointMatcher<Keypoint>(80);
        fastEuclideanKeypointMatcher.setModelFeatures(queryKeypoints);
        fastEuclideanKeypointMatcher.findMatches(targetKeypoints);

        // MultipleMatchesMatcher
        LocalFeatureMatcher<Keypoint> multipleMatchesMatcher = new MultipleMatchesMatcher<Keypoint>(5, 80);
        multipleMatchesMatcher.setModelFeatures(queryKeypoints);
        multipleMatchesMatcher.findMatches(targetKeypoints);

        // VotingKeypointMatcher
        LocalFeatureMatcher<Keypoint> votingKeypointMatcher = new VotingKeypointMatcher<Keypoint>(80);
        votingKeypointMatcher.setModelFeatures(queryKeypoints);
        votingKeypointMatcher.findMatches(targetKeypoints);

        /**
         * Displaying Matches.
         */

        // BasicMatcher
        MBFImage basicMatches = MatchingUtilities.drawMatches(query, target, basicMatcher.getMatches(), RGBColour.RED);
        DisplayUtilities.display(basicMatches, "KeyPoint Matching: Basic Matches");

        // BasicTwoWayMatches
        MBFImage basicTwoWayMatches = MatchingUtilities.drawMatches(query, target, basicTwoWayMatcher.getMatches(), RGBColour.RED);
        DisplayUtilities.display(basicTwoWayMatches, "KeyPoint Matching: Basic Two Way Matches");

        // FastBasicKeypointMatches
        MBFImage fastBasicKeypointMatches = MatchingUtilities.drawMatches(query, target, fastBasicKeypointMatcher.getMatches(), RGBColour.RED);
        DisplayUtilities.display(fastBasicKeypointMatches, "KeyPoint Matching: Fast Basic KeyPoint Matches");

        // FastEuclideanKeypointMatches
        MBFImage fastEuclideanKeypointMatches = MatchingUtilities.drawMatches(query, target, fastEuclideanKeypointMatcher.getMatches(), RGBColour.RED);
        DisplayUtilities.display(fastEuclideanKeypointMatches, "KeyPoint Matching: Fast Euclidean Keypoint Matches");

        // MultipleMatchesMatches
        MBFImage multipleMatchesMatches = MatchingUtilities.drawMatches(query, target, multipleMatchesMatcher.getMatches(), RGBColour.RED);
        DisplayUtilities.display(multipleMatchesMatches, "KeyPoint Matching: Multiple Matches Matches");

        // VotingKeypointMatches
        MBFImage votingKeypointMatches = MatchingUtilities.drawMatches(query, target, votingKeypointMatcher.getMatches(), RGBColour.RED);
        DisplayUtilities.display(votingKeypointMatches, "KeyPoint Matching: Voting Keypoint Matches");
    }

    ///////////////////////
    // END OF EXERCISE 1 //
    ///////////////////////

    ////////////////
    // EXERCISE 2 //
    ////////////////

    /**
     * Exercise 2 - Different Models
     *
     * Experiment with different models (such as a HomographyModel) in the consistent matcher. 
     * The RobustHomographyEstimator helper class can be used to construct an object that fits 
     * the HomographyModel model. You can also experiment with an alternative robust fitting 
     * algorithm to RANSAC called Least Median of Squares (LMedS) through the 
     * RobustHomographyEstimator.
     */
    public static void exercise2() throws Exception{

        /**
         * Loading images.
         */

        MBFImage query = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/query.jpg"));
        MBFImage target = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/target.jpg"));

        /**
         * Extracting KeyPoints
         */

        DoGSIFTEngine engine = new DoGSIFTEngine();

        LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
        LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());

        /**
         * Finding consistent matches.
         */

        // RobustAffineTransformEstimator
        RobustAffineTransformEstimator affineModelFitter = new RobustAffineTransformEstimator(50.0, 
                                                                                        1500,
                                                                                        new RANSAC.PercentageInliersStoppingCondition(0.5));
        LocalFeatureMatcher<Keypoint> affineMatcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8), affineModelFitter);
        affineMatcher.setModelFeatures(queryKeypoints);
        affineMatcher.findMatches(targetKeypoints);
        MBFImage affineConsistentMatches = MatchingUtilities.drawMatches(query, target, affineMatcher.getMatches(), RGBColour.RED);

        // RobustHomographyEstimator
        RobustHomographyEstimator homographyModelFitter = new RobustHomographyEstimator(50.0, 
                                                                              1500,
                                                                              new RANSAC.PercentageInliersStoppingCondition(0.5),
                                                                              HomographyRefinement.SYMMETRIC_TRANSFER);
        LocalFeatureMatcher<Keypoint> homographyMatcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8), homographyModelFitter);
        homographyMatcher.setModelFeatures(queryKeypoints);
        homographyMatcher.findMatches(targetKeypoints);
        MBFImage homographyConsistentMatches = MatchingUtilities.drawMatches(query, target, homographyMatcher.getMatches(), RGBColour.RED);

        /**
         * Displaying consistent matches.
        */

        // Affine Transform Approach
        DisplayUtilities.display(affineConsistentMatches, "KeyPoint Matching: Affine Transform Approach");

        // Homography Model Approach
        DisplayUtilities.display(homographyConsistentMatches, "KeyPoint Matching: Robust Homography Approach");

        /**
         * Locating query image within target image.
         */

        // Affine Transform Approach
        target.drawShape(query.getBounds().transform(affineModelFitter.getModel().getTransform().inverse()), 3,RGBColour.BLUE);
        DisplayUtilities.display(target, "Locating Query Image: Affine Transform Approach"); 


        // Homography Model Approach
        target.drawShape(query.getBounds().transform(homographyModelFitter.getModel().getTransform().inverse()), 3,RGBColour.BLUE);
        DisplayUtilities.display(target, "Locating Query Image: Homography Model Approach"); 
    }

    ///////////////////////
    // END OF EXERCISE 2 //
    ///////////////////////
}
