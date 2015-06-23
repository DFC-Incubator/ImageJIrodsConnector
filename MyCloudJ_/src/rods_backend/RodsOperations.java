package rods_backend;

import general.GeneralUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;

import cloud_interfaces.CloudException;
import cloud_interfaces.CloudFile;
import cloud_interfaces.CloudOperations;

public class RodsOperations implements CloudOperations {
	private IRODSFileFactory irodsFileFactory;
	private DataTransferOperations dataTransferOperations;
	private IRODSSession session;
	private String user;
	private String password;
	private String host;
	private String zone;
	private String res;
	private int port;
	private String homeDirectoryPath;
	private boolean userIsLogged;
	private static final String RODS_DELIMITER = "/";

	private void buildHomePath() {
		StringBuilder homeBuilder = new StringBuilder();
		homeBuilder.append(RODS_DELIMITER);
		homeBuilder.append(zone);
		homeBuilder.append(RODS_DELIMITER);
		homeBuilder.append("home");
		homeBuilder.append(RODS_DELIMITER);
		homeBuilder.append(user);
		homeDirectoryPath = homeBuilder.toString();
	}

	public void login() throws CloudException {
		String error;
		IRODSProtocolManager connectionManager;
		IRODSAccount account;
		IRODSAccessObjectFactory accessObjectFactory;

		try {
			connectionManager = IRODSSimpleProtocolManager.instance();
			session = IRODSSession.instance(connectionManager);
			account = new IRODSAccount(host, port, user, password,
					homeDirectoryPath, zone, res);
			accessObjectFactory = IRODSAccessObjectFactoryImpl
					.instance(session);
			irodsFileFactory = accessObjectFactory.getIRODSFileFactory(account);
			dataTransferOperations = accessObjectFactory.getDataTransferOperations(account);
			
			buildHomePath();
			userIsLogged = true;
		} catch (JargonException e) {
			e.printStackTrace();
			error = "Error login to iRODS: ";
			throw (new CloudException(error.concat(e.getMessage())));
		}
	}

	@Override
	public void disconnect() throws CloudException {
		String error;
		// TODO: check for other resources to be closed
		if (session != null)
			try {
				session.closeSession();
			} catch (JargonException e) {
				e.printStackTrace();
				error = "Error closing the session with the iRODS server: ";
				throw (new CloudException(error.concat(e.getMessage())));
			}
	}

	public String getHomeDirectory() throws CloudException {
		String error = "User is not logged!";

		if (userIsLogged == false)
			throw (new CloudException(error));

		return homeDirectoryPath;
	}

	@Override
	public void downloadFile(String cloudPath, String localPath)
			throws CloudException {
		String error = "";
		long fileSizeKB;
		
		checkPaths(localPath, cloudPath);
		
		try {
			// access the local filesystem
			File localFile = new File(localPath);
			
			// access the file on cloud
			IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(cloudPath);
			
			fileSizeKB = irodsFile.length() / 1024;
			System.out.println("File Size: " + fileSizeKB + " KB");
			
			long start = System.nanoTime();
			dataTransferOperations.getOperation(irodsFile, localFile, null, null);
			long end = System.nanoTime();
			
			long elapsedTime = end - start;
			double seconds = (double)elapsedTime / 1000000000.0;
			System.out.println("Elapsed time: " + seconds + " seconds");
			System.out.println("Speed: " + fileSizeKB/seconds + " KB/s");
		} catch (JargonException e) {
			e.printStackTrace();
			throw (new CloudException(error.concat(e.getMessage())));
		}
	}

	@Override
	public void downloadFolder(String cloudPath, String localPath)
			throws CloudException {
	}

	@Override
	public void uploadFile(String localPath, String cloudPath)
			throws CloudException {
		String error = "";
		long fileSizeKB;
		
		checkPaths(localPath, cloudPath);
		
		try {
			// access the local filesystem
			File localFile = new File(localPath);
			
			// access the file on cloud
			IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(cloudPath);
			
			fileSizeKB = irodsFile.length() / 1024;
			System.out.println("File Size: " + fileSizeKB + " KB");
			
			long start = System.nanoTime();
			dataTransferOperations.putOperation(localFile, irodsFile, null, null);
			long end = System.nanoTime();
			
			long elapsedTime = end - start;
			double seconds = (double)elapsedTime / 1000000000.0;
			System.out.println("Elapsed time: " + seconds + " seconds");
			System.out.println("Speed: " + fileSizeKB/seconds + " KB/s");
		} catch (JargonException e) {
			e.printStackTrace();
			throw (new CloudException(error.concat(e.getMessage())));
		}
	}

	@Override
	public void uploadFolder(String localPath, String cloudPath)
			throws CloudException {
		String error;
		String folderName;
		File inputFolder;

		checkPaths(localPath, cloudPath);

		folderName = GeneralUtility.getLastComponentFromPath(localPath,
				GeneralUtility.getSystemSeparator());
		inputFolder = new File(localPath);

		if (inputFolder.isDirectory()) {
			try {
				IRODSFile irodsFile = irodsFileFactory
						.instanceIRODSFile(cloudPath + RODS_DELIMITER
								+ folderName);
				irodsFile.mkdir();
			} catch (JargonException e) {
				e.printStackTrace();
				error = "iRODS error creating folder:";
				throw (new CloudException(error.concat(error)));
			}

			String[] files = inputFolder.list();
			for (int i = 0; i < files.length; i++)
				uploadFolder(localPath + GeneralUtility.getSystemSeparator()
						+ files[i], cloudPath + RODS_DELIMITER + folderName);
		} else if (inputFolder.isFile()) {
			uploadFile(localPath, cloudPath + RODS_DELIMITER + folderName);
		}
	}

	@Override
	public List<CloudFile> listFiles(String cloudDirectoryPath)
			throws CloudException {
		List<CloudFile> fileList;
		IRODSFile irodsFile;

		checkCloudPath(cloudDirectoryPath);
		fileList = new ArrayList<CloudFile>();

		// add "/" at the end of the path, otherwise Jargon API complains
		if (!cloudDirectoryPath.endsWith(RODS_DELIMITER))
			cloudDirectoryPath = cloudDirectoryPath.concat(RODS_DELIMITER);

		irodsFile = accessFile(cloudDirectoryPath);
		File[] children = irodsFile.listFiles();
		for (File child : children)
			fileList.add(new CloudFile(child.getName(), child.isFile()));

		return fileList;
	}

	private IRODSFile accessFile(String filePath) throws CloudException {
		IRODSFile irodsFile;

		try {
			irodsFile = irodsFileFactory.instanceIRODSFile(filePath);
		} catch (JargonException e) {
			String error = "iRODS: Could not acces " + filePath + ": ";
			throw (new CloudException(error.concat(e.getMessage())));
		}
		return irodsFile;
	}

	@Override
	public boolean isFile(String filePath) throws CloudException {
		checkCloudPath(filePath);

		IRODSFile irodsFile = accessFile(filePath);
		if (irodsFile.isFile())
			return true;

		return false;
	}

	private void checkCloudPath(String path) throws CloudException {
		String error;

		if (path == null || path.contains(RODS_DELIMITER) == false) {
			error = "Invalid Cloud Path";
			throw (new CloudException(error));
		}
	}

	private void checkPaths(String localPath, String cloudPath)
			throws CloudException {
		String error;

		checkCloudPath(cloudPath);
		try {
			GeneralUtility.checkLocalPath(localPath);
		} catch (Exception e1) {
			e1.printStackTrace();
			error = "Invalid local path: ";
			throw (new CloudException(error.concat(e1.getMessage())));
		}
	}

	public String getUsername() {
		return user;
	}

	public void setCredentials(String user, String password, String host,
			int port, String zone, String resource) {
		setUsername(user);
		setIrodsPassword(password);
		setHost(host);
		setPort(port);
		setZone(zone);
		setRes(resource);
	}

	public void setUsername(String username) {
		this.user = username;
	}

	public String getIrodsPassword() {
		return password;
	}

	public void setIrodsPassword(String irodsPassword) {
		this.password = irodsPassword;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public String getRes() {
		return res;
	}

	public void setRes(String res) {
		this.res = res;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
