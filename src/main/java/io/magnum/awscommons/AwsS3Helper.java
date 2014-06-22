package io.magnum.awscommons;

import io.magnum.awscommons.retry.AbortException;

import java.io.InputStream;
import java.util.List;


/**
 * An interface whose implementations provide AWS S3 functionality.
 *
 * @author Yu Sun
 */
public interface AwsS3Helper {
	
	/**
	 * Copy a single file in S3 from a source location (bucket/key)
	 * to a target location (bucket/key)
	 */
	public void copyFileInS3(String srcBucket, String srcKey, String trgBucket, String trgKey);
	
	/**
	 * Copy the folder in S3 from one location to another recusively
	 * 
	 * @param srcBucket
	 * @param srcFolderKey
	 * @param trgBucket
	 * @param trgFolderKey
	 * @param extension the extension name used to filter the files to copy.
	 *                  if null, all the files will be copied.
	 * @throws AbortException
	 */
	public void copyFolderInS3(String srcBucket, String srcFolderKey, 
			String trgBucket, String trgFolderKey, String extension) throws AbortException;

    /**
     * Download the sepecified file in S3 to local disk.
     *
     * @param bucket the S3 bucket of the file to download
     * @param key the key of the file to download
     * @param localFilePath the localFilePath to store the downloaded file
     */
    public void downloadFileFromS3(String bucket, String key, String localFilePath)
            throws AbortException;
    
    /**
     * Upload the sepecified file in local disk to S3.
     *
     * @param bucket the S3 bucket of the file to upload
     * @param key the key of the file to upload
     * @param localFilePath the localFilePath 
     */
    public void uploadFileToS3(String localFilePath, String bucket, String key)
            throws AbortException;

    
    public void uploadFileToS3(String localFilePath, String bucket, String key, boolean ispublic)
			throws AbortException;
    
    public void makeFilePublic(String bucket, String key)
			throws AbortException;
    
    /**
     * Returns whether the given key exists in the S3 bucket
     *
     * @param bucket the given S3 bucket
     * @param key the key to search
     */
    public boolean isKeyExist(String bucket, String key);

    /**
     * Returns the list of s3 file keys with the given keyPrefix
     *
     * @param bucket the s3 bucket containing the files
     * @param keyPrefix the key prefix to match
     * @param extensionName used to filter out the files
     */
    public List<String> listS3Files(String bucket, String keyPrefix, String extensionName)
            throws AbortException;

    /**
     * Download a given list of files from S3 to local disk.
     * <p>
     * The downloaded files will be stored in the local disk with
     * the path being the key relative to the localRootPath. For example,
     * with the localRootPath "/tmp/demo/", a list of files from s3:
     * <p>
     *   s3://hd4ar-dev-us-east-1/phyicial_models/monte/images/image1.jpg <p>
     *   s3://hd4ar-dev-us-east-1/phyicial_models/monte/images/image2.jpg <p>
     *   s3://hd4ar-dev-us-east-1/cyber_models/monte/images/image1.jpg <p>
     * will be downloaded to local disk:
     * <p>
     *   /tmp/demo/phyicial_models/monte/images/image1.jpg <p>
     *   /tmp/demo/phyicial_models/monte/images/image2.jpg <p>
     *   /tmp/demo/cyber_models/monte/images/image1.jpg <p>
     *
     * @param bucket the S3 bucket of the file to download
     * @param keys the list of keys of the files to download
     * @param localRootPath the root path to store the downloaded files
     * @param listener the download listener, which can be null
     */
    public void downloadFilesFromS3(String bucket, List<String> keys, 
    		String localRootPath, DownloadListener listener) throws AbortException;

    /**
     * Download a list of files matching the given keyPrefix from S3 to local disk.
     * <p>
     * The downloaded files will be stored in the local disk with
     * the path being the key relative to the localRootPath. For example,
     * with the localRootPath "/tmp/demo/", a list of files from s3:
     *   <p>
     *   s3://hd4ar-dev-us-east-1/phyicial_models/monte/images/image1.jpg <p>
     *   s3://hd4ar-dev-us-east-1/phyicial_models/monte/images/image2.jpg <p>
     *   s3://hd4ar-dev-us-east-1/cyber_models/monte/images/image1.jpg <p>
     * will be downloaded to local disk:
     *   <p>
     *   /tmp/demo/phyicial_models/monte/images/image1.jpg <p>
     *   /tmp/demo/phyicial_models/monte/images/image2.jpg <p>
     *   /tmp/demo/cyber_models/monte/images/image1.jpg <p>
     *
     * @param bucket the S3 bucket of the file to download
     * @param keyPrefix the prefix of the keys to match for download
     * @param localRootPath the root path to store the downloaded files
     * @param listener the download listener, which can be null
     */
    public void downloadFilesFromS3(String bucket, String keyPrefix, 
    		String localRootPath, DownloadListener listener) throws AbortException;

    /**
     * Sync the local files to S3.
     * <p>
     * This operation checks the difference between local files and S3. Use local files
     * as the master, files in S3 will be updated (sync).
     * <p>
     * According to <a href="http://s3tools.org/s3cmd-sync">http://s3tools.org/s3cmd-sync</a>,
     * file size and md5 checksum will be used to determine the difference.
     * 
     * @param localPath the source local master files
     * @param bucket the bucket of the target files in S3 to update
     * @param key the key or keyprefix of the target files in S3 to udpate 
     */
    public void syncLocalFilesToS3(String localPath, String bucket, String key)
    		throws AbortException;
    
    public void syncLocalFilesToS3Public(String localPath, String bucket, String key)
            throws AbortException;
    		
    /**
     * Sync files in S3 to local disk.
     * <p>
     * This operation checks the difference between local files and S3. Use files in S3
     * as the master, files in local disk will be updated (sync).
     * <p>
     * According to <a href="http://s3tools.org/s3cmd-sync">http://s3tools.org/s3cmd-sync</a>,
     * file size and md5 checksum will be used to determine the difference.
     * 
     * @param bucket the bucket of the source master files in S3
     * @param key the key or keyprefix of the source master files in S3
     * @param localPath the target local files to update
     */
    public void syncS3FilesToLocal(String bucket, String key, String localPath)
			throws AbortException;    		

    /**
     * Delete files in S3
     * 
     * @param bucket
     * @param key
     * @throws AbortException
     */
    public void deleteFileInS3(String bucket, String key) throws AbortException;
    
    /** A S3 download listener */
    public interface DownloadListener {
        /**
         * Triggered when the download is completed.
         *
         * @param localFilePath the file path in local disk
         */
        public void onComplete(String localFilePath);
    }
    
    /**
     * Get the file input stream from S3
     * @param bucket
     * @param key
     * @return
     */
    public InputStream getFileInputStream(String bucket, String key);
}
