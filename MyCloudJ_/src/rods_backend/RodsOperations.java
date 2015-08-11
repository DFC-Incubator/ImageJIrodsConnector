package rods_backend;

import general.GeneralUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.transfer.TransferControlBlock;

import cloud_interfaces.CloudCapabilities;
import cloud_interfaces.CloudException;
import cloud_interfaces.CloudFile;
import cloud_interfaces.CloudOperations;
import cloud_interfaces.CloudTransferCallback;

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
			dataTransferOperations = accessObjectFactory
					.getDataTransferOperations(account);

			buildHomePath();
			userIsLogged = true;
		} catch (JargonException e) {
			e.printStackTrace();
			error = "Error login to iRODS: ";
			throw (new CloudException(error.concat(e.getMessage())));
		}
	}
	
	@Override
	public CloudCapabilities getCloudCapabilities() {
		return new RodsCapabilities();
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
	public void downloadFile(String cloudPath, String localPath,
			CloudTransferCallback callback) throws CloudException {
		String error = "";
		long fileSizeKB;

		GeneralUtility.checkPaths(localPath, cloudPath, RODS_DELIMITER);

		try {
			// access the local filesystem
			File localFile = new File(localPath);

			// access the file on cloud
			IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(cloudPath);

			fileSizeKB = irodsFile.length() / 1024;
			System.out.println("File Size: " + fileSizeKB + " KB");

			// set the callbacks
			long start = System.nanoTime();
			TransferStatusCallback tsc = new TransferStatusCallback();
			tsc.setCloudCallback(callback);

			// set the transfer options: just overwrite, for now
			TransferControlBlock tcb = IRODSFileSystem.instance()
					.getIRODSAccessObjectFactory()
					.buildDefaultTransferControlBlockBasedOnJargonProperties();
			tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);

			dataTransferOperations.getOperation(irodsFile, localFile, tsc, tcb);
			long end = System.nanoTime();

			long elapsedTime = end - start;
			double seconds = (double) elapsedTime / 1000000000.0;
			System.out.println("Elapsed time: " + seconds + " seconds");
			System.out.println("Speed: " + fileSizeKB / seconds + " KB/s");
		} catch (JargonException e) {
			e.printStackTrace();
			throw (new CloudException(error.concat(e.getMessage())));
		}
	}

	@Override
	public void downloadFolder(String cloudPath, String localPath,
			CloudTransferCallback callback) throws CloudException {
		downloadFile(cloudPath, localPath, callback);
	}

	@Override
	public void uploadFile(String localPath, String cloudPath,
			CloudTransferCallback callback) throws CloudException {
		String error = "";
		long fileSizeKB;

		GeneralUtility.checkPaths(localPath, cloudPath, RODS_DELIMITER);

		try {
			// access the local filesystem
			File localFile = new File(localPath);

			// access the file on cloud
			IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(cloudPath);

			fileSizeKB = irodsFile.length() / 1024;
			System.out.println("File Size: " + fileSizeKB + " KB");

			long start = System.nanoTime();
			TransferStatusCallback tsc = new TransferStatusCallback();
			tsc.setCloudCallback(callback);

			// set the transfer options: just overwrite, for now
			TransferControlBlock tcb = IRODSFileSystem.instance()
					.getIRODSAccessObjectFactory()
					.buildDefaultTransferControlBlockBasedOnJargonProperties();
			tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);

			dataTransferOperations
					.putOperation(localFile, irodsFile, tsc, null);
			long end = System.nanoTime();

			long elapsedTime = end - start;
			double seconds = (double) elapsedTime / 1000000000.0;
			System.out.println("Elapsed time: " + seconds + " seconds");
			System.out.println("Speed: " + fileSizeKB / seconds + " KB/s");
		} catch (JargonException e) {
			e.printStackTrace();
			throw (new CloudException(error.concat(e.getMessage())));
		}
	}

	@Override
	public void uploadFolder(String localPath, String cloudPath,
			CloudTransferCallback callback) throws CloudException {
		uploadFile(localPath, cloudPath, callback);
	}

	@Override
	public List<CloudFile> listFiles(String cloudDirectoryPath)
			throws CloudException {
		List<CloudFile> fileList;
		IRODSFile irodsFile;

		GeneralUtility.checkCloudPath(cloudDirectoryPath, RODS_DELIMITER);
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
		GeneralUtility.checkCloudPath(filePath, RODS_DELIMITER);

		IRODSFile irodsFile = accessFile(filePath);
		if (irodsFile.isFile())
			return true;

		return false;
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

	@Override
	public boolean deleteFile(String cloudPath) throws CloudException {
		String error = "";
		boolean fileWasDeleted;

		GeneralUtility.checkCloudPath(cloudPath, RODS_DELIMITER);

		try {
			// access the file on cloud
			IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(cloudPath);

			// set the callbacks
			long start = System.nanoTime();
			fileWasDeleted = irodsFile.deleteWithForceOption();
			long end = System.nanoTime();

			long elapsedTime = end - start;
			double seconds = (double) elapsedTime / 1000000000.0;
			System.out.println("Deletion time: " + seconds + " seconds");
		} catch (Exception e) {
			e.printStackTrace();
			error = "iRODS: Could not acces " + cloudPath + ": ";
			throw (new CloudException(error.concat(e.getMessage())));
		}
		return fileWasDeleted;
	}

	@Override
	public boolean mkdir(String cloudPath) throws CloudException {
		String error = "";
		boolean fileWasDeleted;

		GeneralUtility.checkCloudPath(cloudPath, RODS_DELIMITER);

		try {
			// access the file on cloud
			IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(cloudPath);

			// set the callbacks
			long start = System.nanoTime();
			fileWasDeleted = irodsFile.mkdir();
			long end = System.nanoTime();

			long elapsedTime = end - start;
			double seconds = (double) elapsedTime / 1000000000.0;
			System.out.println("Mkdir time: " + seconds + " seconds");
		} catch (Exception e) {
			e.printStackTrace();
			error = "iRODS: Could not acces " + cloudPath + ": ";
			throw (new CloudException(error.concat(e.getMessage())));
		}
		return fileWasDeleted;
	}
}
