package CloudGui;

import general.GeneralUtility;
import ij.io.Opener;

import java.io.File;

import javax.swing.JTextArea;

import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;

public class UploadThread extends Thread {
	private CloudOperations cloudHandler;
	private String sourcePath;
	private String destinationPath;
	private boolean isFileUpload;
	private CloudFileTree cloudFileTree;
	// TODO: check parallel access to logMessages
	private JTextArea logMessages;

	public UploadThread(CloudOperations cloudHandler,  CloudFileTree cloudFileTree, JTextArea logMessages) {
		this.cloudHandler = cloudHandler;
		this.logMessages = logMessages;
		this.cloudFileTree = cloudFileTree;
	}

	public void prepareForUpload(String sourcePath, String destinationPath) {
		File file = new File(sourcePath);

		if (file.isFile()) {
			this.isFileUpload = true;
			String fileName = GeneralUtility.getLastComponentFromPath(
					sourcePath, "/");

			// Append the filename at the end of the destination Path
			destinationPath += ("/" + fileName);
		}
		this.sourcePath = sourcePath;
		this.destinationPath = destinationPath;
		
		logMessages.append("Uploading " + sourcePath
				+ " to cloud path: " + destinationPath + "\n\n");
	}

	public void run() {
		if (isFileUpload) {
			try {
				cloudHandler.uploadFile(sourcePath, destinationPath);
			} catch (CloudException e) {
				logMessages.append("Error uploading file " + e.getCloudError()
						+ "!\n\n");

				e.printStackTrace();
				return;
			}
		} else {
			try {
				cloudHandler.uploadFolder(sourcePath, destinationPath);
				// TODO: add the freshly uploaded folder in the selected browsing tree
			} catch (CloudException e) {
				logMessages.append("Error uploading folder "
						+ e.getCloudError() + "!\n\n");
				e.printStackTrace();
				return;
			}
		}

		logMessages.append("Uploading of " + sourcePath + " Complete !\n\n");
		
		// update the file browsing tree with the new node
		cloudFileTree.updateTrees(destinationPath, true);
		
		// Open in the default application
		Opener openfile = new Opener();
		openfile.open(sourcePath);
	}
}
