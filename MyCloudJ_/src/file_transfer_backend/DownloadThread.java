package file_transfer_backend;

import general.GeneralUtility;
import ij.io.Opener;
import CloudGui.Logger;
import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;

public class DownloadThread extends Thread {
	private CloudOperations cloudHandler;
	private String sourcePath;
	private String destPath;
	private Logger logger;

	public DownloadThread(CloudOperations cloudHandler, Logger logger) {
		this.cloudHandler = cloudHandler;
		this.logger = logger;
	}

	public void prepareForDownload(String sourcePath, String destinationPath) {
		this.sourcePath = sourcePath;
		this.destPath = destinationPath;

		logger.writeLog("Downloading from cloud path " + sourcePath
				+ " to local path " + destinationPath + "\n\n");
	}

	public void run() {
		String fileName;
		boolean isFileDownload = false;
		// file/folder
		String downloadType = "";

		try {
			isFileDownload = cloudHandler.isFile(sourcePath);
			downloadType = isFileDownload ? "file" : "folder";

			if (isFileDownload)
				cloudHandler.downloadFile(sourcePath, destPath);
			else
				cloudHandler.downloadFolder(sourcePath, destPath);
		} catch (CloudException e) {
			logger.writeLog("Error downloading " + downloadType + " "
					+ sourcePath + ". " + e.getCloudError() + "\n\n");
			e.printStackTrace();
			return;
		}

		logger.writeLog("Downloading of " + sourcePath + " complete \n\n");

		fileName = GeneralUtility.getLastComponentFromPath(sourcePath, "/");
		fileName = GeneralUtility.getSystemSeparator() + fileName;
		Opener openfile = new Opener();
		openfile.open(destPath + fileName);
	}
}
