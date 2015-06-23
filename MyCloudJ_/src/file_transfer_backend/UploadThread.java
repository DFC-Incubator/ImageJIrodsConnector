package file_transfer_backend;

import ij.io.Opener;

import java.io.File;
import CloudGui.CloudFileTree;
import CloudGui.Logger;
import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;

public class UploadThread extends Thread {
	private CloudOperations cloudHandler;
	private String sourcePath;
	private String destPath;
	private boolean isFileUpload;
	private CloudFileTree cloudFileTree;
	private Logger logger;
	// file/folder
	private String uploadType;

	public UploadThread(CloudOperations cloudHandler,
			CloudFileTree cloudFileTree, Logger logger) {
		this.cloudHandler = cloudHandler;
		this.logger = logger;
		this.cloudFileTree = cloudFileTree;
	}

	public void prepareForUpload(String sourcePath, String destinationPath) {
		File file = new File(sourcePath);

		if (file.isFile()) {
			this.isFileUpload = true;
			uploadType = isFileUpload ? "file" : "folder";
		}
		this.sourcePath = sourcePath;
		this.destPath = destinationPath;

		logger.writeLog("Uploading " + sourcePath + " to cloud path: "
				+ destinationPath + "\n\n");
	}

	public void run() {
		try {
				cloudHandler.uploadFile(sourcePath, destPath);
		} catch (CloudException e) {
			logger.writeLog("Error uploading " + uploadType + " " + sourcePath
					+ ". " + e.getCloudError() + "\n\n");
			e.printStackTrace();
			return;
		}

		logger.writeLog("Uploading of " + sourcePath + " complete \n\n");

		// update the file browsing tree with the new node
		cloudFileTree.updateTrees(destPath, true);

		// Open in the default application
		Opener openfile = new Opener();
		openfile.open(sourcePath);
	}
}
