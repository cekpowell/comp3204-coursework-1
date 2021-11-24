package uk.ac.soton.ecs.cp6g18.ch6;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.dataset.FlickrImageDataset;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.FlickrAPIToken;

import java.net.URL;
import java.util.Map.Entry;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 6 - Image Datasets
 *
 * Tutorial Code
 *
 * @author Charles Powell
 */
public class Ch6Tutorial {

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
         * Datasets are an important concept in OpenIMAJ.
         * 
         * A dataset is a collection of data items.
         * 
         * OpenIMAJ supports two types of dataset: ListDatasets and GroupedDatasets.
         * 
         * ListDataset:
         *      - List of data items (extends List class).
         * 
         * GroupedDataset:
         *      - A Keyed map of Datasets (extension of Map interface).
         *      - That is, we map keys to sub-datasets.
         * 
         * The dataset classes are designed to provide a useful way of manipulating 
         * collections of items, and are particularly useful for applying 
         * machine-learning techniques to data.
         * 
         * This tutorial looks at the construction and manipulation of datasets of images.
         */

        //////////////////
        // LIST DATASET //
        //////////////////

        // FORMATION //

        /**
         * We will start by looking at how we can create a simple list dataset from a directory
         * of images on the local computer.
         */

        /**
         * We can create a list dataset by referencing the path to the directory of images.
         * 
         * Notice that the dataset we create is for greyscale images (FImages), meaning that the
         * dataset will contain greyscale versions of the images in the directory, regardless of if
         * they are actually greyscale.
         */

        VFSListDataset<FImage> localDataset = new VFSListDataset<FImage>("/Users/charlie/Desktop/", ImageUtilities.FIMAGE_READER);

        // MANIPULATION //

        /**
         * A list dataset extends List, and so we can do the same operations on it that can be done on a 
         * normal Java List object.
         * 
         * For example, we can get the number of images in the dataset.
         */

        System.out.println("Number of images in the datset : " + localDataset.size());

        /**
         * The Dataset interface allows for us to get a random item from the dataset.
         * 
         * As we're dealing with images, we can use this to display a random image.
         */

        DisplayUtilities.display(localDataset.getRandomInstance(), "Random Image");

        /**
         * As were dealing with a List of images, we can display them all in the same window.
         */

        DisplayUtilities.display("Local Dataset", localDataset);

        // OTHER DATA TYPES //

        /**
         * The VFSListDataset class is very powerful. It can be used to create datasets from any
         * kind of data given an appropriate ObjectReader implementation.
         * 
         * Beyond this, it is also able to create datasets from other sources, such as compressed
         * archives containing data items and even remote data that is not stored on the local]
         * disk.
         */

        /**
         * For example, we can create an image datast from a zip file that is hosted on a web-server.
         */

        VFSListDataset<FImage> webDataset = new VFSListDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip",
                                                                       ImageUtilities.FIMAGE_READER);
        DisplayUtilities.display("Web Dataset", webDataset);

        /////////////////////
        // GROUPED DATASET //
        /////////////////////

        /**
         * A GroupedDataset maps a set of keys to sub-datasets.
         * 
         * GroupedDatasets are useful in things like machine learning where you
         * want to train classifiers to distinguish between groups.
         */

        // FORMATION //

        /**
         * Essentially, GroupedDatasets are formed by directories that have sub-directories
         * of images.
         * 
         * Each sub-directory name becomes a key, and the images within the directory become
         * the mapped sub-dataset.
         */

        /**
         * The dataset loaded from the web in the above example, the images are actually grouped
         * into sub-directories, with all of the images for a single individual stored in the same
         * directory.
         * 
         * When this dataset was loaded into a ListDataset object from the zip file, we lost the 
         * association between images of each individual (i.e., which sub-directory each 
         * individual belonged to).
         * 
         * If we were to use a VFSGroupedDataset, we can maintain this association.
         */

        VFSGroupDataset<FImage> groupedWebDataset = new VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip",
                                                                                ImageUtilities.FIMAGE_READER);

        // MANIPULATION //

        /**
         * We can iterate through the keys of this grouped dataset and display each all of the images
         * from each group in an individual window.
         */

        for (final Entry<String, VFSListDataset<FImage>> entry : groupedWebDataset.entrySet()) {
            DisplayUtilities.display(entry.getKey(), entry.getValue());
        }

        // DYNAMIC DATASET FORMATION USING FLICKR//

        /**
         * Somtimes, it can be useful to be able to dynamically create a dataset of images
         * from the web.
         */

        /**
         * In the image analysis community, FLickr is often used as a source of tagged images for
         * performing activities such as training classifiers.
         * 
         * The FlickrImageDataset class makes it easy to dynamically construct a dataset of images
         * from a Flickr search.
         */

        /**
         * Note, at the time of writing this code, the Flickr daraset cannot be loaded because
         * you are not able to make a new account through the Flickr website, and thus cannot 
         * gather an API key.
         */

        FlickrAPIToken flickrToken = DefaultTokenFactory.get(FlickrAPIToken.class);
        FlickrImageDataset<FImage> cats = FlickrImageDataset.create(ImageUtilities.FIMAGE_READER, flickrToken, "cat", 10);
        DisplayUtilities.display("Cats", cats);
    }
}