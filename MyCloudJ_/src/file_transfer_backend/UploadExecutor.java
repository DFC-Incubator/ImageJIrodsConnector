package file_transfer_backend;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

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
	List<FutureTask<Void>> tasks;

	public UploadExecutor(CloudOperations cloudHandler,
			CloudFileTree cloudFileTree, Logger logger) {
		this.cloudHandler = cloudHandler;
		this.logger = logger;
		this.cloudFileTree = cloudFileTree;
		tasks = new ArrayList<FutureTask<Void>>(MAX_UPLOADS_QUEUED);
		executor = Executors.newFixedThreadPool(MAX_THREADS);
		for (int i = 0; i < MAX_UPLOADS_QUEUED - 1; i++)
			tasks.add(null);
	}

	public void addTask(TransferTask task) throws FileTransferException {
		for (int i = 0; i < MAX_UPLOADS_QUEUED - 1; i++) {
			FutureTask<Void> futureTask = tasks.get(i);

			// search for an empty/finished future task
			if (futureTask == null || futureTask.isDone()) {
				// create a new future task
				UploadThread uploadTask = new UploadThread(task, cloudHandler,
						cloudFileTree, logger);
				FutureTask<Void> newFutureTask = new FutureTask<Void>(uploadTask);
				tasks.set(i, newFutureTask);

				// submit to execution the new future task
				executor.execute(newFutureTask);
				logger.writeLog("Uploading of" + task.getSourcePath() + " to "
						+ task.getDestinationPath() + " is in progress...\n\n");
				return;
			}
		}

		throw (new FileTransferException(
				"Maximum number of pending uploads reached\n"));
	}

	public void terminate() {
		for (int i = 0; i < tasks.size(); i++) {
			FutureTask<Void> futureTask = tasks.get(i);
			if (futureTask != null && futureTask.isDone() == false) {
				futureTask.cancel(true);
			}
		}
	}
}
