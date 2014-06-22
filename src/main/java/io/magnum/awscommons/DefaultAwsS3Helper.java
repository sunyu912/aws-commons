package io.magnum.awscommons;

import io.magnum.awscommons.retry.AbortException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * The default implement of AwsS3Helper using AWS S3 client.
 * 
 * @author Yu Sun
 */
public class DefaultAwsS3Helper implements AwsS3Helper {

	private static final Logger log = Logger.getLogger(DefaultAwsS3Helper.class
			.getName());

	/**
	 * The number of threads used to download the files from S3. Further tests
	 * need to be done to find out the most optimized number.
	 */
	private static final int NUM_DOWNLOAD_THREADS = 5;
	/**
	 * The executor service used to download the files from S3
	 */
	private final ExecutorService executor;

	private final AmazonS3 s3Client;

	public DefaultAwsS3Helper(AmazonS3 s3Client) {
		this.s3Client = s3Client;
		this.executor = Executors.newFixedThreadPool(NUM_DOWNLOAD_THREADS);
	}

	private InputStream getFromS3(String bucket, String key) {
		return s3Client.getObject(new GetObjectRequest(bucket, key))
				.getObjectContent();
	}

	@Override
	public void downloadFileFromS3(String bucket, String key,
			String localFilePath) throws AbortException {
		log.info("Downloading " + key + "...");
		InputStream reader = getFromS3(bucket, key);
		OutputStream writer = null;
		try {
			File file = new File(localFilePath);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			writer = new BufferedOutputStream(new FileOutputStream(file));
			int read = -1;
			while ((read = reader.read()) != -1) {
				writer.write(read);
			}
		} catch (IOException e) {
			throw new AbortException("Failed to download the file.", e);
		} finally {
			try {
				writer.flush();
				writer.close();
				reader.close();
			} catch (IOException e) {
				throw new AbortException(
						"Failed to close input/output streams.", e);
			}
		}
		log.info("Downloading " + key + " finished.");
	}

	@Override
	public void downloadFilesFromS3(final String bucket, List<String> keys,
			final String localRootPath, DownloadListener listener)
			throws AbortException {
		CompletionService<String> completionService = new ExecutorCompletionService<String>(
				executor);

		// submit all the download tasks
		for (final String key : keys) {
			completionService.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					String localFilePath = localRootPath + "/" + key;
					// in order to avoid conflicts of creating the same folder
					// during the mulitple-process download, we create the
					// folder
					// first before download. Java File.mkdirs() can tolerate
					// creating the existing folder.
					File localFileFolder = new File(localFilePath)
							.getParentFile();
					if (!localFileFolder.exists()) {
						localFileFolder.mkdirs();
					}
					downloadFileFromS3(bucket, key, localFilePath);
					return localFilePath;
				}
			});
		}
		// whenever a download is done, process it
		try {
			for (int i = 0; i < keys.size(); i++) {
				Future<String> f = completionService.take();
				String localFileName = f.get();
				log.fine(localFileName + " has been downloaded to local disk");
				if (listener != null) {
					listener.onComplete(localFileName);
				}
			}
		} catch (InterruptedException e) {
			throw new AbortException("Failed to download files", e);
		} catch (ExecutionException e) {
			throw new AbortException("Failed to download files", e);
		}
	}

	@Override
	public void downloadFilesFromS3(String bucket, String keyPrefix,
			String localRootPath, DownloadListener listener)
			throws AbortException {
		List<String> keys = listS3Files(bucket, keyPrefix, null);
		downloadFilesFromS3(bucket, keys, localRootPath, listener);
	}

	@Override
	public List<String> listS3Files(String bucket, String keyPrefix,
			String extensionName) throws AbortException {
		List<String> keyList = new LinkedList<String>();
		ObjectListing list = s3Client.listObjects(new ListObjectsRequest()
				.withBucketName(bucket).withPrefix(keyPrefix));
		while (true) {
			for (S3ObjectSummary obj : list.getObjectSummaries()) {
				// filter out the folders from the list
				// TODO: It does not filter out the sub-directories
				if (!obj.getKey().endsWith("/")) {
					if (extensionName != null && !extensionName.isEmpty()
							&& obj.getKey().endsWith(extensionName)) {
						keyList.add(obj.getKey());
					} else if (extensionName == null) {
						keyList.add(obj.getKey());
					}
				}
			}
			// the listObjects request may not return all the objects if there
			// are too many, we need to request again
			if (list.isTruncated()) {
				list = s3Client.listNextBatchOfObjects(list);
			} else {
				break;
			}
		}
		return keyList;
	}

	@Override
	public boolean isKeyExist(String bucket, String key) {
		ObjectListing list = s3Client.listObjects(new ListObjectsRequest()
				.withBucketName(bucket).withPrefix(key).withMaxKeys(1));

		if (list != null && list.getObjectSummaries().size() > 0) {
			return true;
		}
		return false;
	}

	@Override
	public void syncLocalFilesToS3(String localPath, String bucket, String key)
			throws AbortException {
		// TODO
	}

	@Override
	public void syncS3FilesToLocal(String bucket, String key, String localPath)
			throws AbortException {
		// TODO
	}

	@Override
	public void uploadFileToS3(String localFilePath, String bucket, String key)
			throws AbortException {
		// TODO Auto-generated method stub

	}

	@Override
	public void uploadFileToS3(String localFilePath, String bucket, String key,
			boolean ispublic) throws AbortException {
		// TODO Auto-generated method stub

	}

	@Override
	public InputStream getFileInputStream(String bucket, String key) {
		S3Object s3Obj = s3Client.getObject(bucket, key);
		return s3Obj.getObjectContent();
	}

	@Override
	public void makeFilePublic(String bucket, String key) throws AbortException {
		// TODO Auto-generated method stub

	}

	@Override
	public void copyFileInS3(String srcBucket, String srcKey, String trgBucket,
			String trgKey) {
		s3Client.copyObject(srcBucket, srcKey, trgBucket, trgKey);		
	}

	@Override
	public void copyFolderInS3(String srcBucket, String srcFolderKey,
		String trgBucket, String trgFolderKey, String extension) throws AbortException {
		List<String> keys = listS3Files(srcBucket, srcFolderKey, extension);
		for(String key : keys) {				
			String trgKey = key.replaceFirst(srcFolderKey, trgFolderKey);
			log.fine("Copying " + srcBucket + "/" + key + " to " + trgBucket + "/" + trgKey);
			s3Client.copyObject(srcBucket, key, trgBucket, trgKey);
		}		
	}

    @Override
    public void deleteFileInS3(String bucket, String key) throws AbortException {
        s3Client.deleteObject(bucket, key);        
    }

    @Override
    public void syncLocalFilesToS3Public(String localPath, String bucket,
            String key) throws AbortException {
        // TODO Auto-generated method stub
        
    }
}
