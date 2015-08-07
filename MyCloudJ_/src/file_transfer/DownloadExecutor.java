package file_transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import CloudGui.TransferProgressTable.UpdatableTableModel;
import cloud_interfaces.CloudOperations;

public class DownloadExecutor implements ExecutorOperations {
	private static final int MAX_DOWNLOADS_QUEUED = 10;
	private static final int MAX_THREADS = 1;
	private CloudOperations cloudHandler;
	private ExecutorService executor;
	List<DownloadThread> transfers;
	private UpdatableTableModel model;

	public DownloadExecutor(CloudOperations cloudHandler,
			UpdatableTableModel model) {
		this.cloudHandler = cloudHandler;
		executor = Executors.newFixedThreadPool(MAX_THREADS);
		transfers = new ArrayList<DownloadThread>(MAX_DOWNLOADS_QUEUED);
		for (int i = 0; i < MAX_DOWNLOADS_QUEUED; i++)
			transfers.add(null);
		this.model = model;
	}

	@Override
	public void addTask(TransferTask task) throws FileTransferException {
		int transferId;

		for (int i = 0; i < MAX_DOWNLOADS_QUEUED - 1; i++) {
			DownloadThread futureTask = transfers.get(i);

			// search for an empty/finished future task
			if (futureTask == null || futureTask.isDone()) {

				// create a new future task
				transferId = this.model.addTransfer(task.getSourcePath(),
						task.getDestinationPath(), true);
				DownloadThread downloadTask = new DownloadThread(task,
						cloudHandler, model, transferId);
				transfers.set(i, downloadTask);

				// submit to execution the new future task
				executor.execute(downloadTask);
				return;
			}
		}

		throw (new FileTransferException(
				"Maximum number of pending downloads reached\n"));
	}

	@Override
	public boolean terminateTransfer(int transferId) {
		for (int i = 0; i < MAX_DOWNLOADS_QUEUED - 1; i++) {
			DownloadThread futureTask = transfers.get(i);
			if (futureTask.getTransferId() == transferId) {
				return futureTask.cancel(true);
			}
		}
		return false;
	}

	@Override
	public void terminateAllTransfers() {
		// terminate all the running transfers
		for (int i = 0; i < transfers.size(); i++) {
			DownloadThread futureTask = transfers.get(i);
			if (futureTask != null && futureTask.isDone() == false) {
				futureTask.cancel(true);
			}
		}
		
		// update the GUI
		model.cancelAllTransfers();
	}
}
