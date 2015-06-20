package file_transfer_backend;

import general.GeneralUtility;
import ij.io.Opener;
import CloudGui.Logger;
import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;

public class DownloadThread extends Thread {
	private CloudOperations cloudHandler;
	private String sourcePath;
	private String destinationPath;
	private Logger logger;

	public DownloadThread(CloudOperations cloudHandler, Logger logger) {
		this.cloudHandler = cloudHandler;
		this.logger = logger;
	}

	public void prepareForDownload(String sourcePath, String destinationPath) {
		this.sourcePath = sourcePath;
		this.destinationPath = destinationPath;
		
		logger.writeLog("Downloading from cloud path: " + sourcePath
				+ " to local path: " + destinationPath + "\n\n");
	}

	public void run() {
		String fileName;
		boolean isFileDownload = false;
		
		try {
			isFileDownload = cloudHandler.isFile(sourcePath);
		} catch (CloudException e1) {
			logger.writeLog(e1.getCloudError() + "\n\n");
			e1.printStackTrace();
		}

		if (isFileDownload) {
			try {
				cloudHandler.downloadFile(sourcePath, destinationPath);
			} catch (CloudException e) {
				logger.writeLog("Error uploading folder " + e.getCloudError() + "!\n\n");
				e.printStackTrace();
				return;
			}
			logger.writeLog("Downloading of " + sourcePath + " complete !\n\n");
		} else {
			try {
				cloudHandler.downloadFolder(sourcePath, destinationPath);
			} catch (CloudException e) {
				logger.writeLog("Error downloading folder: " + e.getMessage() + "!\n\n");
				e.printStackTrace();
				return;
			}
			logger.writeLog("Downloading of " + sourcePath + " complete !\n\n");
		}

		// TODO: check for null return values
		fileName = GeneralUtility.getLastComponentFromPath(sourcePath, "/");
		fileName = GeneralUtility.getSystemSeparator() + fileName;
		Opener openfile = new Opener();
		openfile.open(destinationPath + fileName);
	}
}
