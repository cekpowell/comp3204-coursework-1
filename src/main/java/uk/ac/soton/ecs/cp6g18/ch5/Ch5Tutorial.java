package uk.ac.soton.ecs.cp6g18.ch5;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.model.fit.RANSAC;

import java.net.URL;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 5 - SIFT and Feature Matching
 *
 * Tutorial Code
 *
 * @author Charles Powell
 */
public class Ch5Tutorial {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{

        ///////////////////////////
        // TUTORIAL INTRODUCTION //
        ///////////////////////////

        /**
         * Here, we will look at how to compare images to eachother using local
         * feature descriptors.
         * 
         * We will use a local feature extractor called SIFT to extract some
         * 'interesting points' from images and describe them in a standard way.
         * 
         * Once we have all of these local features and their descriptions, we can
         * match local features from different images together and therefore
         * compare images to eachother, or find a query image within a target image.
         * 
         * We will be using the local features to find a query image within a target image.
         */

        /**
         * We will start by loading some sample images - the query image and the target image.
         */

        MBFImage query = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/query.jpg"));
        MBFImage target = ImageUtilities.readMBF(new URL("http://static.openimaj.org/media/tutorial/target.jpg"));

        ////////////////////////
        // FEATURE EXTRACTION //
        ////////////////////////

        /**
         * We start by extracting the features from both the query and target images.
         * 
         * We will use the difference of Gaussian feature detector which we describe with a SIFT
         * descriptor.
         * 
         * The features we find are described in a way that makes them invariance to scale,
         * position and rotation.
         * 
         * Such features are quite powerful and can be used in a variety of tasks.
         */

        // USING SIFT TO EXTRACT FEATURES //

        /**
         * The standard implementation of SIFT in OpenIMAJ is found in the DoGSIFTEngine class.
         */

        DoGSIFTEngine engine = new DoGSIFTEngine();

        /**
         * Once the engine is constructed, we can use it to extract 'KeyPoint' objects from
         * the query and target images
         */
        LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
        LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());

        /**
         * They KeyPoint class contains a public field called 'ivec' which, in the case of a 
         * standard SIFT descriptor is a 128 dimensional description of a patch of pixels
         * around a detected 'significant point'.
         * 
         * Various distance measures (e.g., Euclidean) can be used to compare KeyPoints to KeyPoints.
         */

        ////////////////////////////////////
        // COMPARING IMAGES VIA KEYPOINTS //
        ////////////////////////////////////

        /**
         * The challenge in comparing KeyPoints is to figure out which KeyPoints match between 
         * KeyPoints from some query image and KeyPoints in some Target image.
         * 
         * The basic approach is to take a given KeyPoint in the query image and find the KeyPoint
         * in the target image that is the best match.
         * 
         * On top of this, we can disregard those points that match well with MANY points in the target
         * image. Such points are considered non-descriptive.
         */

        /**
         * Matching between query and target images can be achieved in OpenIMAJ using the BasicMatcher class.
         */

        // constructing and setting up a matcher.
        LocalFeatureMatcher<Keypoint> matcher = new BasicMatcher<Keypoint>(80);
        matcher.setModelFeatures(queryKeypoints);

        // finding matches between query and target images.
        matcher.findMatches(targetKeypoints);

        /**
         * We can then draw the matches between the two images using the basic matcher and the
         * MatchingUtilities class - i.e., we produce an image that draws lines from the points in the
         * query image to the matching points in the target image.
         */

        MBFImage basicMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), RGBColour.RED);
        DisplayUtilities.display(basicMatches, "KeyPoint Matching: Basic Approach");

        /**
         * This approach finds the matches - but most of them are incorrect - i.e., it's not very good.
         */

        // IMPROVING THE COMPARISON WITH AFFINE TRANSFORMS //

        /**
         * A more advanced approach to compare the KeyPoints in two images is to filter the matches based
         * on a given geometric model.
         * 
         * One way of doing this in OpenIMAJ is to use a ConsistentLocalFeatureMatcher which, given an
         * internal matcher and a model filter configured to fit a geometric model, finds which matches
         * given by the internal matcher are consistent with the model and therefore likely to be correct.
         */

        /**
         * We will use a an algorithm called Random Sample Consensus (RANSAC) to fit a geometric model
         * called an Affine Transform to the initial set of matches.
         * 
         * This is achieved by iteratiley selecting a random set of matches, learning a model from this
         * random set and then testing the remaining matches against the model.
         */

        /**
         * We can set up a RANSAC model filter configured to find Affine Transforms and our consistent
         * matcher.
         */

        RobustAffineTransformEstimator modelFitter = new RobustAffineTransformEstimator(50.0, 
                                                                                        1500,
                                                                                        new RANSAC.PercentageInliersStoppingCondition(0.5));
        matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8), modelFitter);
        matcher.setModelFeatures(queryKeypoints);
        matcher.findMatches(targetKeypoints);
        MBFImage consistentMatches = MatchingUtilities.drawMatches(query, target, matcher.getMatches(), RGBColour.RED);

        DisplayUtilities.display(consistentMatches, "KeyPoint Matching: Affine Transform Approach");

        // FINDING QUERY IMAGE WITHIN TARGET IMAGE //

        /**
         * The AffineTransformModel class models a two-dimensional Affine transform in OpenIMAJ.
         * 
         * The RobustAffineTransformEstimator class provides a method getModel() which returns
         * the internal Affine Transform model whose parameters are optimsed during the fitting process
         * driven by the ConsistentLocalFeatureMatcher2d.
         * 
         * A property of using ConsistentLocalFeatureMatcher2d is that the AffineTransformModel returned 
         * by getModel() contains the best transformation matric to go from the query to the target image.
         * 
         * We can take advantage of this by transforming the boundaing box of the query image with the transform
         * estimated by the AffineTransformModel. This allows us to draw a polygon around the estimated location of 
         * the query image within the target image.
         */

        /**
         * Drawing a polygon on the target image for the match of the query image.
         */

        target.drawShape(query.getBounds().transform(modelFitter.getModel().getTransform().inverse()), 3,RGBColour.BLUE);
        DisplayUtilities.display(target, "KeyPoint Matching: Matched Query Image in Target Image"); 
    }
}