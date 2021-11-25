package uk.ac.soton.ecs.cp6g18.ch6;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

import java.util.Map.Entry;

/**
 * OpenIMAJ Tutorial.
 *
 * Chapter 6 - Image Datasets
 *
 * Exercises
 *
 * @author Charles Powell
 */
public class Ch6Exercises {

    /**
     * Main method.
     *
     * @param args System arguments.
     */
    public static void main( String[] args ) throws Exception{
        // Exercise 1 - Exploring Grouped Datasets
        exercise1();

        // Exercise 2 - Find out more about VFS datasets
        exercise2();

        // Exercise 3 - Try the BingImageDataset dataset
        exercise3();

        // Exercise 4 - Using MapBackedDataset
        exercise4();
    }

    ////////////////
    // EXERCISE 1 //
    ////////////////

    /**
     * Exercise 1 - Exploring Grouped Datasets
     *
     * Using the faces dataset available from http://datasets.openimaj.org/att_faces.zip, 
     * can you display an image that shows a randomly selected photo of each person in the 
     * dataset?
     */
    public static void exercise1() throws Exception{
        
        /**
         * Loading the dataset of faces into a GroupedDataset.
         */

        VFSGroupDataset<FImage> facesDataset = new VFSGroupDataset<FImage>("zip:http://datasets.openimaj.org/att_faces.zip",
                                                                           ImageUtilities.FIMAGE_READER);
        
                                                                        
        /**
         * Displaying a random photo of each persons face.
         */

        for (final Entry<String, VFSListDataset<FImage>> entry : facesDataset.entrySet()) {
            DisplayUtilities.display(entry.getValue().getRandomInstance(), "Random Instance from sub-dataset : " + entry.getKey());
        }
    }

    ///////////////////////
    // END OF EXERCISE 1 //
    ///////////////////////

    ////////////////
    // EXERCISE 2 //
    ////////////////

    /**
     * Exercise 2 - Find out more about VFS datasets
     *
     * VFSListDatasets and VFSGroupDatasets are based on a technology from the Apache 
     * Software Foundation called Commons Virtual File System (Commons VFS). Explore the 
     * documentation of the Commons VFS to see what other kinds of sources are supported for 
     * building datasets.
     */
    public static void exercise2() throws Exception{

        /**
         * Supported sources:
         *      - Local Files
         *      - Zip, Jar and Tar
         *      - Gzip and Bzip2
         *      - HDFS
         *      - HTTP and HTTPS
         *      - WebDAV
         *      - FTP
         *      - FTPS
         *      - SFTP
         *      - CIFS
         *      - Temporary Files
         *      - Resources (local Java resources)
         *      - RAM
         *      - MIME
         */
    }

    ///////////////////////
    // END OF EXERCISE 2 //
    ///////////////////////

    ////////////////
    // EXERCISE 3 //
    ////////////////

    /**
     * Exercise 3 - Try the BingImageDataset datset
     *
     * The BingImageDataset class allows you to create a dataset of images by performing a 
     * search using the Bing search engine. The BingImageDataset class works in a similar 
     * way to the FlickrImageDataset described above. Try it out!
     */
    public static void exercise3() throws Exception{
        /**
         * Not completed as the Bing API is not working.
         */
    }

    ///////////////////////
    // END OF EXERCISE 3 //
    ///////////////////////

    ////////////////
    // EXERCISE 4 //
    ////////////////

    /**
     * Exercise 4 - Using MapBackedDataset
     *
     * The MapBackedDataset class provides a concrete implementation of a GroupedDataset. See if 
     * you can use the static MapBackedDataset.of method to construct a grouped dataset of images 
     * of some famous people. Use a BingImageDataset to get the images of each person.
     */
    public static void exercise4() throws Exception{
        /**
         * Not completed as the Bing API is not working.
         */
    }

    ///////////////////////
    // END OF EXERCISE 4 //
    ///////////////////////
}
