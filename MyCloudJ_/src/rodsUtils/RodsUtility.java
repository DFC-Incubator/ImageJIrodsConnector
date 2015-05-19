package rodsUtils;

import generalUtils.CloudException;
import generalUtils.CloudOperations;
import generalUtils.GeneralUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSProtocolManager;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;

import CloudConnect.CloudFile;

public class RodsUtility implements CloudOperations {
	private IRODSSession session;
	private IRODSAccount account;
	private IRODSProtocolManager connectionManager;
	IRODSAccessObjectFactory accessObjectFactory;
	IRODSFileFactory irodsFileFactory;
	private String user;
	private String password;
	private String host;
	private String zone;
	private String res;
	private int port;
	private String homePath;
	private String homeDirectoryPath = "";
	private boolean userIsLogged;

	public void initializeRods() throws CloudException {
		String error;

		connectionManager = IRODSSimpleProtocolManager.instance();
		try {
			session = IRODSSession.instance(connectionManager);
		} catch (JargonException e) {
			error = "Error initializing iRODS";
			throw (new CloudException(error));
		}
	}

	private void buildHomePath() {
		StringBuilder homeBuilder = new StringBuilder();
		homeBuilder.append('/');
		homeBuilder.append(zone);
		homeBuilder.append('/');
		homeBuilder.append("home");
		homeBuilder.append('/');
		homeBuilder.append(user);
		homePath = homeBuilder.toString();
	}

	public void login() throws CloudException {
		String error;

		buildHomePath();
		account = new IRODSAccount(host, port, user, password, homePath, zone,
				res);
		try {
			accessObjectFactory = IRODSAccessObjectFactoryImpl
					.instance(session);
			irodsFileFactory = accessObjectFactory.getIRODSFileFactory(account);
			userIsLogged = true;
		} catch (JargonException e) {
			error = "Jargon Exception " + e.getMessage();
			throw (new CloudException(error));
		}

		homeDirectoryPath = homeDirectoryPath.concat("/").concat(zone)
				.concat("/home/").concat(user).concat("/");
	}

	@Override
	public void downloadFile(String cloudPath, String localPath)
			throws CloudException {

		FileOutputStream fos = null;
		String error = "";
		String fileName;

		// extract the name of the file from full path
		fileName = cloudPath.substring(cloudPath.lastIndexOf("/"));
		localPath += fileName;

		try {
			// access the file on cloud
			IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(cloudPath);
			IRODSFileInputStream irodsFileInputStream = irodsFileFactory
					.instanceIRODSFileInputStream(irodsFile);

			// save the file as a byte stream
			byte[] saveAsFileByteStream = new byte[(int) irodsFile.length()];
			irodsFileInputStream.read(saveAsFileByteStream, 0,
					(int) irodsFile.length());

			// write the byte stream to disk
			fos = new FileOutputStream(localPath);
			fos.write(saveAsFileByteStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			error = "File System Error";
			throw (new CloudException(error));
		} catch (IOException e) {
			e.printStackTrace();
			error = "File System Error.";
			throw (new CloudException(error));
		} catch (JargonException e) {
			e.printStackTrace();
			error = "File was not found on iRODS server";
			throw (new CloudException(error));
		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
				error = "File System Error";
				throw (new CloudException(error));
			}
		}
	}

	@Override
	public void downloadFolder(String cloudPath, String localPath)
			throws CloudException {
		String error;
		IRODSFile irodsFile = null;

		/*
		 * Create a folder on the local machine with last part of the of the
		 * cloudPath
		 */
		// TODO: fix this method of extracting the last path from path
		// TODO: maybe use a method from GeneralUtility
		String folderName = cloudPath.substring(cloudPath.lastIndexOf("/"));
		localPath += folderName;

		// creates the directory on disk
		boolean newFolder = new File(localPath).mkdirs();
		if (!newFolder) {
			error = "Local File System error.";
			throw (new CloudException(error));
		}

		try {
			irodsFile = irodsFileFactory.instanceIRODSFile(cloudPath);
			File[] files = irodsFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory())
					downloadFolder(files[i].getPath(), localPath);
				else if (files[i].isFile())
					downloadFile(files[i].getPath(), localPath);
			}
		} catch (JargonException e) {
			e.printStackTrace();
			error = "Error accesing the file on cloud.";
			throw (new CloudException(error));
		}
	}

	@Override
	public void uploadFile(String localPath, String cloudPath)
			throws CloudException {
		String error;
		IRODSFile irodsFile;
		IRODSFileOutputStream irodsFileOutputStream = null;
		
		try {
			irodsFile = irodsFileFactory.instanceIRODSFile(cloudPath);
			irodsFileOutputStream = irodsFileFactory
					.instanceIRODSFileOutputStream(irodsFile);
			File localFile = new File(localPath);
			byte[] fileContent = Files.readAllBytes(localFile.toPath());
			fileContent = Files.readAllBytes(localFile.toPath());
			irodsFileOutputStream.write(fileContent, 0, fileContent.length);
		} catch (JargonException e) {
			e.printStackTrace();
			error = "iRODS internal error";
			throw (new CloudException(error));
		} catch (IOException e) {
			e.printStackTrace();
			error = "Local file system error";
			throw (new CloudException(error));
		} finally {
			try {
				if (irodsFileOutputStream != null)
					irodsFileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				error = "Error closing iRODS file";
				throw (new CloudException(error));
			}
		}
	}

	@Override
	public void uploadFolder(String FolderLocalPath, String TargetDbxPath)
			throws CloudException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<CloudFile> listFiles(String directoryPath)
			throws CloudException {
		List<CloudFile> fileList = new ArrayList<CloudFile>();
		IRODSFile irodsFile;
		int directoryPathLength = (directoryPath != null ? directoryPath
				.length() : 0);

		// add "/" at the end of the path, otherwise Jargon API complains
		if ((directoryPathLength > 0) && (!directoryPath.endsWith("/")))
			directoryPath = directoryPath.concat("/");

		irodsFile = accessFile(directoryPath);
		File[] children = irodsFile.listFiles();
		for (File child : children)
			fileList.add(new CloudFile(child.getName(), child.isFile()));

		return fileList;
	}

	private IRODSFile accessFile(String name) throws CloudException {
		IRODSFile irodsFile;

		try {
			irodsFile = irodsFileFactory.instanceIRODSFile(name);
		} catch (JargonException e) {
			String error = "iRODS: Could not acces " + name;
			throw (new CloudException(error));
		}
		return irodsFile;
	}

	public String getUsername() {
		return user;
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
	public boolean isFileDownload(String name) throws CloudException {
		IRODSFile irodsFile = accessFile(name);
		if (irodsFile.isFile())
			return true;

		return false;
	}

	public String getHomeDirectory() throws CloudException {
		String error = "User is not logged!";

		if (userIsLogged == false)
			throw (new CloudException(error));

		return homeDirectoryPath;
	}
}
