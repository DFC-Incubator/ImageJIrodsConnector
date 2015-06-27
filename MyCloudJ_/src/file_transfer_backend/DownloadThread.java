package file_transfer_backend;

import java.util.ArrayList;
import java.util.List;

import general.GeneralUtility;
import ij.io.Opener;
import CloudGui.Logger;
import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;

public class DownloadThread extends Thread {
	private static final int MAX_DOWNLOADS_QUEUE = 10;
	private CloudOperations cloudHandler;
	private Logger logger;
	private List<TransferTask> transfers;
	private volatile boolean keepRunning;

	public DownloadThread(CloudOperations cloudHandler, Logger logger) {
		this.cloudHandler = cloudHandler;
		this.logger = logger;
		transfers = new ArrayList<TransferTask>();
		keepRunning = true;
	}

	public void addTask(TransferTask task) throws FileTransferException {
		synchronized (transfers) {
			if (transfers.size() == MAX_DOWNLOADS_QUEUE)
				throw (new FileTransferException(
						"Maximum number of pending downloads reached\n"));

			logger.writeLog("Downloading of " + task.getSourcePath() + " to "
					+ task.getDestinationPath() + " is in progress...\n\n");
			transfers.add(task);
			transfers.notify();
		}
	}

	public void terminate() {
		synchronized (transfers) {
			keepRunning = false;
			transfers.notify();
		}
	}

	public void run() {
		TransferTask task;
		String sourcePath = "";
		String destPath = "";
		boolean isFileDownload = false;
		String downloadType = ""; // file/folder
		while (keepRunning) {
			try {
				synchronized (transfers) {
					// wait for a task
					if (transfers.size() == 0)
						transfers.wait();
					
					// finish processing if someone terminates the thread
					if (!keepRunning)
						break;

					task = transfers.get(transfers.size() - 1);
					transfers.remove(transfers.size() - 1);
				}
				
				// save task parameters
				sourcePath = task.getSourcePath();
				destPath = task.getDestinationPath();
				isFileDownload = cloudHandler.isFile(sourcePath);
				downloadType = isFileDownload ? "file" : "folder";

				// start the download
				cloudHandler.downloadFile(sourcePath, destPath);
				logger.writeLog("Downloading of " + sourcePath
						+ " complete\n\n");
				openFile(task);
			} catch (CloudException e) {
				logger.writeLog("Error downloading " + downloadType + " "
						+ sourcePath + ". " + e.getCloudError() + "\n\n");
				e.printStackTrace();
			} catch (InterruptedException e1) {
				logger.writeLog("Downloading thread error. Please restart ImageJ.\n");
				e1.printStackTrace();
			}
		}
	}

	private void openFile(TransferTask task) {
		String fileName;

		fileName = GeneralUtility.getLastComponentFromPath(
				task.getSourcePath(), "/");
		fileName = GeneralUtility.getSystemSeparator() + fileName;
		Opener openfile = new Opener();
		openfile.open(task.getDestinationPath() + fileName);
	}
}
