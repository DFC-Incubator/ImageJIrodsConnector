package file_transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import CloudGui.CloudFileTree;
import CloudGui.Logger;
import CloudGui.TransferProgressTable.UpdatableTableModel;
import cloud_interfaces.CloudOperations;

public class UploadExecutor  implements ExecutorOperations {
	private static final int MAX_UPLOADS_QUEUED = 10;
	private static final int MAX_THREADS = 1;
	private CloudOperations cloudHandler;
	private Logger logger;
	private CloudFileTree cloudFileTree;
	private ExecutorService executor;
	List<UploadThread> transfers;
	private UpdatableTableModel model;
	
	public UploadExecutor(CloudOperations cloudHandler,
			CloudFileTree cloudFileTree, Logger logger, UpdatableTableModel model) {
		this.cloudHandler = cloudHandler;
		this.logger = logger;
		this.cloudFileTree = cloudFileTree;
		executor = Executors.newFixedThreadPool(MAX_THREADS);
		transfers = new ArrayList<UploadThread>(MAX_UPLOADS_QUEUED);
		for (int i = 0; i < MAX_UPLOADS_QUEUED - 1; i++)
			transfers.add(null);
		this.model = model;
	}

	@Override
	public void addTask(TransferTask task) throws FileTransferException {
		int transferId;
		
		for (int i = 0; i < MAX_UPLOADS_QUEUED - 1; i++) {
			UploadThread futureTask = transfers.get(i);

			// search for an empty/finished future task
			if (futureTask == null || futureTask.isDone()) {
				// create a new future task
				transferId = this.model.addTransfer(task.getSourcePath(),
						task.getDestinationPath(), false);
				UploadThread uploadTask = new UploadThread(task, cloudHandler,
						cloudFileTree, logger, model, transferId);
				transfers.set(i, uploadTask);

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
	
	@Override
	public boolean terminateTransfer(int transferId) {
		for (int i = 0; i < MAX_UPLOADS_QUEUED - 1; i++) {
			UploadThread futureTask = transfers.get(i);
			if (futureTask.getTransferId() == transferId) {
				logger.writeLog("Canceled download from"
						+ futureTask.getTask().getSourcePath() + " to "
						+ futureTask.getTask().getDestinationPath() + "!\n\n");
				return futureTask.cancel(true);
			}
		}
		return false;
	}
	
	@Override
	public void terminateAllTransfers() {
		for (int i = 0; i < transfers.size(); i++) {
			UploadThread futureTask = transfers.get(i);
			if (futureTask != null && futureTask.isDone() == false) {
				logger.writeLog("Canceled upload from"
						+ futureTask.getTask().getSourcePath() + " to "
						+ futureTask.getTask().getDestinationPath() + "!\n\n");
				futureTask.cancel(true);
			}
		}
	}
}
