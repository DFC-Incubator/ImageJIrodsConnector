package file_transfer_backend;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import CloudGui.CloudFileTree;
import CloudGui.Logger;
import cloud_interfaces.CloudOperations;

public class UploadExecutor extends Thread {
	private static final int MAX_UPLOADS_QUEUED = 10;
	private static final int MAX_THREADS = 1;
	private CloudOperations cloudHandler;
	private Logger logger;
	private CloudFileTree cloudFileTree;
	private ExecutorService executor;
	List<UploadThread> futureTasks;
	
	public UploadExecutor(CloudOperations cloudHandler,
			CloudFileTree cloudFileTree, Logger logger) {
		this.cloudHandler = cloudHandler;
		this.logger = logger;
		this.cloudFileTree = cloudFileTree;
		executor = Executors.newFixedThreadPool(MAX_THREADS);
		futureTasks = new ArrayList<UploadThread>(MAX_UPLOADS_QUEUED);
		for (int i = 0; i < MAX_UPLOADS_QUEUED - 1; i++)
			futureTasks.add(null);
	}

	public void addTask(TransferTask task) throws FileTransferException {
		for (int i = 0; i < MAX_UPLOADS_QUEUED - 1; i++) {
			UploadThread futureTask = futureTasks.get(i);

			// search for an empty/finished future task
			if (futureTask == null || futureTask.isDone()) {
				// create a new future task
				UploadThread uploadTask = new UploadThread(task, cloudHandler,
						cloudFileTree, logger);
				futureTasks.set(i, uploadTask);

				// submit to execution the new future task
				executor.execute(uploadTask);
				logger.writeLog("Uploading of" + task.getSourcePath() + " to "
						+ task.getDestinationPath() + " is in progress...\n\n");
				return;
			}
		}

		throw (new FileTransferException(
				"Maximum number of pending uploads reached\n"));
	}

	public void terminate() {
		for (int i = 0; i < futureTasks.size(); i++) {
			UploadThread futureTask = futureTasks.get(i);
			if (futureTask != null && futureTask.isDone() == false) {
				logger.writeLog("Canceled upload from"
						+ futureTask.getTask().getSourcePath() + " to "
						+ futureTask.getTask().getDestinationPath() + "!\n\n");
				futureTask.cancel(true);
			}
		}
	}
}
