package file_transfer_backend;

import general.GeneralUtility;
import ij.io.Opener;

import javax.swing.JTextArea;

import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;

public class DownloadThread extends Thread {
	private CloudOperations cloudHandler;
	private String sourcePath;
	private String destinationPath;
	// TODO: check parallel access to logMessages
	private JTextArea logMessages;

	public DownloadThread(CloudOperations cloudHandler, JTextArea logMessages) {
		this.cloudHandler = cloudHandler;
		this.logMessages = logMessages;
	}

	public void prepareForDownload(String sourcePath, String destinationPath) {
		this.sourcePath = sourcePath;
		this.destinationPath = destinationPath;
		
		logMessages.append("Downloading from cloud path: " + sourcePath
				+ " to local path: " + destinationPath + "\n\n");
	}

	public void run() {
		String fileName;
		boolean isFileDownload = false;
		
		try {
			isFileDownload = cloudHandler.isFile(sourcePath);
		} catch (CloudException e1) {
			logMessages.append(e1.getCloudError() + "\n\n");
			e1.printStackTrace();
		}

		if (isFileDownload) {
			try {
				cloudHandler.downloadFile(sourcePath, destinationPath);
			} catch (CloudException e) {
				logMessages.append("Error uploading folder "
						+ e.getCloudError() + "!\n\n");
				e.printStackTrace();
				return;
			}
			logMessages.append("Downloading of " + sourcePath
					+ " Complete !\n\n");
		} else {
			try {
				cloudHandler.downloadFolder(sourcePath, destinationPath);
			} catch (CloudException e) {
				logMessages.append("Error downloading folder " + e.getMessage()
						+ "!\n\n");
				e.printStackTrace();
				return;
			}
			logMessages.append("Downloading of " + sourcePath
					+ " Complete !\n\n");

		}

		// TODO: check for null return values
		fileName = GeneralUtility.getLastComponentFromPath(sourcePath, "/");
		fileName = GeneralUtility.getSystemSeparator() + fileName;
		Opener openfile = new Opener();
		openfile.open(destinationPath + fileName);
	}
}
