package io.magnum.awscommons;

import io.magnum.awscommons.exec.Exec;
import io.magnum.awscommons.retry.AbortException;

import java.io.IOException;
import java.util.logging.Logger;

import com.amazonaws.services.s3.AmazonS3;

/**
 * An implementation of AwsS3Helper based on the command-line tool s3tools, see
 * <a href="http://s3tools.org">http://s3tools.org</a>
 * <p>
 * s3tools must be correctly installed in order to use this helper.
 * 
 * @author Yu Sun
 */
public class S3CmdBasedAwsS3Helper extends DefaultAwsS3Helper {

	private static final String S3CMD_ACL_SET_PUBLIC = "s3cmd setacl --acl-public";

	public S3CmdBasedAwsS3Helper(AmazonS3 s3Client) {
		super(s3Client);
	}

	/** The config to the path of the s3cmd credentials */
	private final static String S3CMD_CRED_CONFIG;

	static {
		String path = System.getProperty("s3cmd.cred.path");
		S3CMD_CRED_CONFIG = (path == null ? "" : "--config " + path);
	}

	private static final Logger log = Logger
			.getLogger(S3CmdBasedAwsS3Helper.class.getName());

	/** The command used to donwload a file from S3 */
	private static final String S3CMD_GET = "s3cmd get -f " + S3CMD_CRED_CONFIG;
	/** The command used to upload a file to S3 */
	private static final String S3CMD_PUT = "s3cmd put -f " + S3CMD_CRED_CONFIG;
	/** The command used to make files in S3 public */
	private static final String S3CMD_PUT_PUBLIC = "s3cmd put -f --acl-public " + S3CMD_CRED_CONFIG;
	/** The command used to delete files in S3 */
	private static final String S3CMD_DELETE = "s3cmd del " + S3CMD_CRED_CONFIG;
	/** The command used to sync files between S3 and local disk */
	private static final String S3CMD_SYNC = "s3cmd sync " + S3CMD_CRED_CONFIG;
	/** The command used to sync files between S3 and local disk */
    private static final String S3CMD_SYNC_PUBLIC = "s3cmd sync -P " + S3CMD_CRED_CONFIG;
	/** The timeout given to performance a sync operation */
	private static final int S3CMD_SYNC_TIMEOUT = 60 * 1000 * 10; // 10 mins

	private String s3CmdHome = null;

	@Override
	public void downloadFileFromS3(String bucket, String key,
			String localFilePath) throws AbortException {
		log.info("Downloading " + key + "...");
		String cmd = S3CMD_GET + " s3://" + bucket + "/" + key + " "
				+ localFilePath;
		
		if(this.s3CmdHome != null){
			cmd = this.s3CmdHome + "/" + cmd;
		}
		
		try {
			log.info("Run: " + cmd);
			new Exec(cmd).execute();
		} catch (IOException e) {
			log.warning("Failed to run: " + cmd);
			throw new AbortException("Failed to download the file from S3.", e);
		}
		log.info("Downloading " + key + " finished.");
	}
	
	@Override
	public void uploadFileToS3(String localFilePath, String bucket, String key)
			throws AbortException {
		uploadFileToS3(localFilePath, bucket, key, false);
	}

	@Override
	public void uploadFileToS3(String localFilePath, String bucket, String key, boolean ispublic)
			throws AbortException {
		log.info("Uploading " + key + "...");
		String cmd = ((ispublic)? S3CMD_PUT_PUBLIC : S3CMD_PUT) + " " + localFilePath + " s3://" + bucket + "/"
				+ key;
		
		if(this.s3CmdHome != null){
			cmd = this.s3CmdHome + "/" + cmd;
		}
		
		try {
			log.info("Run: " + cmd);
			new Exec(cmd).execute();
		} catch (IOException e) {
			log.warning("Failed to run: " + cmd);
			throw new AbortException("Failed to upload the file from S3.", e);
		}
		log.info("Uploading " + key + " finished.");
	}

	@Override
	public void syncLocalFilesToS3(String localPath, String bucket, String key)
			throws AbortException {
		log.info("Sync-ing files from local to S3...");
		if (!key.endsWith("/")) {
			key = key + "/";
		}
		if (!localPath.endsWith("/")) {
		    localPath = localPath + "/";
		}
		String cmd = S3CMD_SYNC + " " + localPath + " s3://" + bucket + "/"
				+ key;
		
		if(this.s3CmdHome != null){
			cmd = this.s3CmdHome + "/" + cmd;
		}
		
		try {
			log.info("Run: " + cmd);
			new Exec(cmd, 0, S3CMD_SYNC_TIMEOUT).execute();
		} catch (IOException e) {
			log.warning("Failed to run: " + cmd);
			throw new AbortException("Failed to sync files from local disk.", e);
		}
		log.info("Sync files from local to S3 finished");
	}
	
	@Override
    public void syncLocalFilesToS3Public(String localPath, String bucket, String key)
            throws AbortException {
        log.info("Sync-ing files from local to S3...");
        if (!key.endsWith("/")) {
            key = key + "/";
        }
        if (!localPath.endsWith("/")) {
            localPath = localPath + "/";
        }
        String cmd = S3CMD_SYNC_PUBLIC + " " + localPath + " s3://" + bucket + "/"
                + key;
        
        if(this.s3CmdHome != null){
            cmd = this.s3CmdHome + "/" + cmd;
        }
        
        try {
            log.info("Run: " + cmd);
            new Exec(cmd, 0, S3CMD_SYNC_TIMEOUT).execute();
        } catch (IOException e) {
            log.warning("Failed to run: " + cmd);
            throw new AbortException("Failed to sync files from local disk.", e);
        }
        log.info("Sync files from local to S3 finished");
    }
	
	@Override
	public void makeFilePublic(String bucket, String key)
			throws AbortException {
		log.info("Making s3://"+bucket+"/"+key+" public...");
		
		String cmd = S3CMD_ACL_SET_PUBLIC + " s3://" + bucket + "/"
				+ key;
		
		if(this.s3CmdHome != null){
			cmd = this.s3CmdHome + "/" + cmd;
		}
		
		try {
			log.info("Run: " + cmd);
			new Exec(cmd).execute();
		} catch (IOException e) {
			log.warning("Failed to run: " + cmd);
			throw new AbortException("Failed to sync files from local disk.", e);
		}
		log.info("Sync files from local to S3 finished");
		
	}	

	@Override
	public void syncS3FilesToLocal(String bucket, String key, String localPath)
			throws AbortException {
		log.info("Sync-ing files from S3 to local disk...");
		if (!key.contains(".") && !key.endsWith("/")) {
			key = key + "/";
		}
		String cmd = S3CMD_SYNC + " s3://" + bucket + "/" + key + " "
				+ localPath;
		
		if(this.s3CmdHome != null){
			cmd = this.s3CmdHome + "/" + cmd;
		}
		try {
			log.info("Run: " + cmd);
			new Exec(cmd, 0, S3CMD_SYNC_TIMEOUT).execute();
		} catch (IOException e) {
			log.warning("Failed to run: " + cmd);
			throw new AbortException("Failed to sync files from S3.", e);
		}
		log.info("Sync files from S3 to local finished.");
	}

	public String getS3CmdHome() {
		return this.s3CmdHome;
	}

	public void setS3CmdHome(String s3CmdHome) {
		this.s3CmdHome = s3CmdHome;
	}

}
