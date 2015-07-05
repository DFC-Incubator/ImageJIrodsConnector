package file_transfer_backend;

import general.GeneralUtility;
import ij.io.Opener;

import java.util.concurrent.Callable;

import CloudGui.Logger;
import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;

public class DownloadThread implements Callable<Void> {
	private CloudOperations cloudHandler;
	private Logger logger;
	private TransferTask task;

	public DownloadThread(TransferTask task, CloudOperations cloudHandler,
			Logger logger) {
		this.cloudHandler = cloudHandler;
		this.logger = logger;
		this.task = task;
	}

	@Override
	public Void call() throws CloudException {
		String sourcePath = "";
		String destPath = "";
		boolean isFileDownload = false;
		String downloadType = ""; // file/folder

		try {
			// save task parameters
			sourcePath = task.getSourcePath();
			destPath = task.getDestinationPath();
			isFileDownload = cloudHandler.isFile(sourcePath);
			downloadType = isFileDownload ? "file" : "folder";

			// start the download
			if (isFileDownload)
				cloudHandler.downloadFile(sourcePath, destPath);
			else
				cloudHandler.downloadFolder(sourcePath, destPath);
			logger.writeLog("Downloading of " + sourcePath + " complete\n\n");

			if (isFileDownload)
				openFile(task);
		} catch (CloudException e) {
			e.printStackTrace();
			logger.writeLog("Error downloading " + downloadType + " "
					+ sourcePath + ". " + e.getCloudError() + "\n\n");
			
		}

		// TODO: in the future we'll return the file transfer status
		return null;
	}

	private void openFile(TransferTask task) {
		String fileName;

		fileName = GeneralUtility.getLastComponentFromPath(
				task.getSourcePath(), "/");
		fileName = GeneralUtility.getSystemSeparator() + fileName;
		Opener openfile = new Opener();
		openfile.open(task.getDestinationPath() + fileName);
	}

	public TransferTask getTask() {
		return task;
	}

	public void setTask(TransferTask task) {
		this.task = task;
	}
}
