package dropbox_backend;

import cloud_interfaces.CloudCapabilities;
import cloud_interfaces.CloudException;
import cloud_interfaces.CloudFile;
import cloud_interfaces.CloudOperations;
import cloud_interfaces.CloudTransferCallback;
import cloud_interfaces.CloudTransferStatus;

import com.dropbox.core.*;

import general.GeneralUtility;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class DropboxOperations implements CloudOperations {
	private static final String DBX_DELIMITER = "/";
	private static final String OS_DELIMITER = GeneralUtility
			.getSystemSeparator();

	// user info
	private String userName = "", country = "", userQuota = "";

	private String homeDirectoryPath = "/";
	private boolean userIsLogged;

	// Till JavaDoc, enjoy:
	// https://www.dropbox.com/developers/core/start/java
	private final String APP_KEY = "5jysg1bzg0ulli3";
	private final String APP_SECRET = "t0ln07k26pctonw";
	public DbxClient client;
	private DbxWebAuthNoRedirect webAuth;
	private DbxRequestConfig config;
	private DbxAppInfo appInfo;
	private DbxAuthFinish authFinish;
	private String authorizeUrl;
	private String accessToken;

	public void login() throws CloudException {
		appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
		config = new DbxRequestConfig("JavaTutorial/1.0", Locale.getDefault()
				.toString());
		webAuth = new DbxWebAuthNoRedirect(config, appInfo);
		authorizeUrl = webAuth.start();

		GeneralUtility.openDefaultBrowser(authorizeUrl);
	}
	
	@Override
	public CloudCapabilities getCloudCapabilities() {
		return new DropboxCapabilities();
	}

	// Function to accept the access code and link the account of the user
	public void DbxLinkUser(String authCode) throws CloudException {
		String error;

		try {
			authFinish = webAuth.finish(authCode);
			userIsLogged = true;
		} catch (DbxException e) {
			error = "Access code error - Re-enter the correct access code "
					+ e.getMessage();
			throw new CloudException(error);
		}
		accessToken = authFinish.accessToken;
		client = new DbxClient(config, accessToken);

		try {
			setUserName(client.getAccountInfo().displayName);
			setCountry(client.getAccountInfo().country);
			setUserQuota(getUserQuota()
					+ (double) client.getAccountInfo().quota.total
					/ (1024 * 1024 * 1024));
		} catch (DbxException e) {
			error = "Error getting user information " + e.getMessage();
			throw new CloudException(error);
		}

	}
	
	@Override
	public void disconnect() throws CloudException {
		// TODO: close resources
	}

	public boolean isFile(String filePath) throws CloudException {
		DbxEntry metaData = null;
		String error;

		GeneralUtility.checkCloudPath(filePath, DBX_DELIMITER);
		filePath = formatPathForDbx(filePath);

		try {
			metaData = client.getMetadata(filePath);
		} catch (DbxException e) {
			error = "Error getting metadata: " + e.getMessage();
			throw (new CloudException(error));
		}

		if (metaData.isFile())
			return true;

		return false;
	}

	@Override
	public void uploadFile(String localPath, String cloudPath, CloudTransferCallback callback)
			throws CloudException {
		CloudTransferStatus cloudTransferStatus = new CloudTransferStatus();
		File inputFile = new File(localPath);
		InputStream inputStream = null;
		String error, fileName;
		
		GeneralUtility.checkPaths(localPath, cloudPath, DBX_DELIMITER);
		cloudPath = formatPathForDbx(cloudPath);

		fileName = GeneralUtility.getLastComponentFromPath(localPath,
				OS_DELIMITER);
		cloudPath = cloudPath.concat(DBX_DELIMITER).concat(fileName);

		try {
			inputStream = new FileInputStream(inputFile);
			
			/*
			 *  Future Sync: Dropbox API doesn't offer a callback for file
			 *  transfer status. 
			 *  Temporary solution: Call the upper callback once at the start
			 *  of the file transfer and once at completion of file transfer.
			 */
			cloudTransferStatus.setFraction(1);
			cloudTransferStatus.setCurrFile(fileName);
			callback.statusCallback(cloudTransferStatus);
			client.uploadFile(cloudPath, DbxWriteMode.add(),
					inputFile.length(), inputStream);
			cloudTransferStatus.setFraction(100);
			callback.statusCallback(cloudTransferStatus);
			
		} catch (DbxException e) {
			error = "Error uploading on Dropbox: " + e.getMessage();
			throw (new CloudException(error));
		} catch (FileNotFoundException e) {
			error = "Local file not found: " + e.getMessage();
			throw (new CloudException(error));
		} catch (IOException e) {
			error = "IOException: " + e.getMessage();
			throw (new CloudException(error));
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				error = "Error closing inputStream: " + e.getMessage();
				throw (new CloudException(error));
			}
		}
	}

	@Override
	public void uploadFolder(String localPath, String cloudPath, CloudTransferCallback callback)
			throws CloudException {
		String error;
		File inputFolder;
		String folderName;

		GeneralUtility.checkPaths(localPath, cloudPath, DBX_DELIMITER);
		cloudPath = formatPathForDbx(cloudPath);
			
		inputFolder = new File(localPath);

		if (inputFolder.isDirectory()) {
			try {
				folderName = GeneralUtility.getLastComponentFromPath(localPath,
						OS_DELIMITER);
				cloudPath = cloudPath.concat(DBX_DELIMITER).concat(folderName);
				client.createFolder(cloudPath);
			} catch (DbxException e) {
				error = "Error creating remote Dropbox folder: "
						+ e.getMessage();
				throw (new CloudException(error));
			}

			// List of files inside the folder
			String[] files = inputFolder.list();
			for (String file : files) {
				uploadFolder(localPath.concat(OS_DELIMITER).concat(file), cloudPath, callback);
			}
		} else if (inputFolder.isFile()) {
			uploadFile(localPath, cloudPath, callback);
		}
	}

	@Override
	public void downloadFile(String cloudPath, String localPath, CloudTransferCallback callback)
			throws CloudException {
		CloudTransferStatus cloudTransferStatus = new CloudTransferStatus();
		OutputStream outputStream = null;
		String fileName;
		String error;

		GeneralUtility.checkPaths(localPath, cloudPath, DBX_DELIMITER);
		cloudPath = formatPathForDbx(cloudPath);

		fileName = GeneralUtility.getLastComponentFromPath(cloudPath,
				DBX_DELIMITER);
		localPath = localPath.concat(OS_DELIMITER).concat(fileName);

		try {
			File localFile = new File(localPath);
			outputStream = new FileOutputStream(localFile);
			
			/*
			 *  Future Sync: Dropbox API doesn't offer a callback for file
			 *  transfer status. 
			 *  Temporary solution: Call the upper callback once at the start
			 *  of the file transfer and once at completion of file transfer.
			 */
			cloudTransferStatus.setFraction(1);
			cloudTransferStatus.setCurrFile(fileName);
			callback.statusCallback(cloudTransferStatus);
			client.getFile(cloudPath, null, outputStream);
			cloudTransferStatus.setFraction(100);
			callback.statusCallback(cloudTransferStatus);
		} catch (DbxException e) {
			error = "Error downloading on Dropbox: " + e.getMessage();
			throw (new CloudException(error));
		} catch (FileNotFoundException e) {
			error = "File not found: " + e.getMessage();
			throw (new CloudException(error));
		} catch (IOException e) {
			error = "IOException: " + e.getMessage();
			throw (new CloudException(error));
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				error = "Error closing inputStream: " + e.getMessage();
				throw (new CloudException(error));
			}
		}
	}

	@Override
	public void downloadFolder(String cloudPath, String localPath, CloudTransferCallback callback)
			throws CloudException {
		String error;
		String folderName;
		DbxEntry.WithChildren folderInfo;

		GeneralUtility.checkPaths(localPath, cloudPath, DBX_DELIMITER);
		cloudPath = formatPathForDbx(cloudPath);

		folderName = GeneralUtility.getLastComponentFromPath(cloudPath,
				DBX_DELIMITER);
		localPath = localPath.concat(OS_DELIMITER).concat(folderName);

		if (!(new File(localPath).mkdirs())) {
			error = "Error creating local folder";
			throw (new CloudException(error));
		}

		try {
			folderInfo = client.getMetadataWithChildren(cloudPath);
			if (folderInfo == null) {
				error = "Metadata not found for the folder";
				throw (new CloudException(error));
			}
		} catch (DbxException e) {
			error = "Error downloading folder from Dropbox: " + e.getMessage();
			throw (new CloudException(error));
		}

		Iterator<DbxEntry> iterChildren;
		DbxEntry child;
		iterChildren = folderInfo.children.iterator();
		while (iterChildren.hasNext()) {
			child = iterChildren.next();
			if (child.isFolder())
				downloadFolder(child.path, localPath, callback);
			else if (child.isFile())
				downloadFile(child.path, localPath, callback);
		}
	}

	public List<CloudFile> listFiles(String cloudDirectoryPath)
			throws CloudException {
		String error;
		DbxEntry.WithChildren folderInfo;
		List<CloudFile> fileList = new ArrayList<CloudFile>();

		GeneralUtility.checkCloudPath(cloudDirectoryPath, DBX_DELIMITER);

		try {
			folderInfo = client
					.getMetadataWithChildren(formatPathForDbx(cloudDirectoryPath));
			if (folderInfo == null) {
				error = "Metadata not found for the folder";
				throw (new CloudException(error));
			}
		} catch (DbxException e) {
			error = "Error getting metadata for the folder " + e.getMessage();
			throw (new CloudException(error));
		}

		Iterator<DbxEntry> iterChildren;
		DbxEntry child;
		if (folderInfo.children != null) {
			iterChildren = folderInfo.children.iterator();
			while (iterChildren.hasNext()) {
				child = iterChildren.next();
				fileList.add(new CloudFile(child.name, child.isFile()));
			}
		}

		return fileList;
	}
	
	@Override
	public boolean deleteFile(String cloudPath) throws CloudException {
		// TODO Auto-generated method stub
		return false;
	}

	private String formatPathForDbx(String path) {
		int pathLength = (path != null ? path.length() : 0);

		// remove the "/" from the end of the path, otherwise Dbx API complains
		if ((pathLength > 0) && (!path.equals(homeDirectoryPath))
				&& (path.endsWith("/")))
			path = path.substring(0, pathLength - 1);

		return path;
	}

	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	public void setAuthorizeUrl(String authorizeUrl) {
		this.authorizeUrl = authorizeUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getUserQuota() {
		return userQuota;
	}

	public void setUserQuota(String userQuota) {
		this.userQuota = userQuota;
	}

	public String getHomeDirectory() throws CloudException {
		String error = "User is not logged!";

		if (userIsLogged == false)
			throw (new CloudException(error));

		return homeDirectoryPath;
	}

	@Override
	public boolean mkdir(String cloudPath) throws CloudException {
		return false;
	}

	@Override
	public boolean rename(String cloudPath, String newName)
			throws CloudException {
		return false;
	}
}