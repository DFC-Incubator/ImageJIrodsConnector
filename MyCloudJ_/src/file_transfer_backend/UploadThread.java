package file_transfer_backend;

import ij.io.Opener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import CloudGui.CloudFileTree;
import CloudGui.Logger;
import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;

public class UploadThread extends Thread {
	private static final int MAX_DOWNLOADS_QUEUE = 10;
	private CloudOperations cloudHandler;
	private Logger logger;
	private CloudFileTree cloudFileTree;
	private List<TransferTask> transfers;
	private volatile boolean keepRunning;

	public UploadThread(CloudOperations cloudHandler,
			CloudFileTree cloudFileTree, Logger logger) {
		this.cloudHandler = cloudHandler;
		this.logger = logger;
		this.cloudFileTree = cloudFileTree;
		transfers = new ArrayList<TransferTask>();
		keepRunning = true;
	}
	
	public void terminate() {
		synchronized (transfers) {
			keepRunning = false;
			transfers.notify();
		}
	}
	
	public void addTask(TransferTask task) throws FileTransferException {
		synchronized (transfers) {
			if (transfers.size() == MAX_DOWNLOADS_QUEUE)
				throw (new FileTransferException("Maximum number of pending uploads reached\n"));
			
			logger.writeLog("Uploading of" + task.getSourcePath() + " to "
					+ task.getDestinationPath() + " is in progress...\n\n");
			transfers.add(task);
			transfers.notify();
		}	
	}

	public void run() {
		TransferTask task;
		String sourcePath = "";
		String destPath = "";
		String uploadType = ""; // file/folder
		
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
				File file = new File(sourcePath);
				uploadType = file.isFile() ? "file" : "folder";
				
				// start the download
				cloudHandler.uploadFile(sourcePath, destPath);
				logger.writeLog("Uploading of " + sourcePath + " complete \n\n");
				
				if (file.isFile())
					openFile(task);
			} catch (CloudException e) {
				logger.writeLog("Error uploading " + uploadType + " " + sourcePath
						+ ". " + e.getCloudError() + "\n\n");
				e.printStackTrace();
				return;
			} catch (InterruptedException e1) {
				logger.writeLog("Uploading thread error. Please restart ImageJ.\n");
				e1.printStackTrace();
			}
	
			// update the file browsing tree with the new node
			cloudFileTree.updateTrees(destPath, true);
		}
	}
	
	private void openFile(TransferTask task) {
		// Open in the default application
		Opener openfile = new Opener();
		openfile.open(task.getSourcePath());
	}
}
