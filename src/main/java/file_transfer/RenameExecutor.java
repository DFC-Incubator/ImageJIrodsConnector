package file_transfer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import CloudGui.CloudFileTree;
import CloudGui.TransferProgressTable.UpdatableTableModel;
import cloud_interfaces.CloudOperations;

public class RenameExecutor implements ExecutorOperations {
	private static final int MAX_THREADS = 1;
	private CloudOperations cloudHandler;
	private CloudFileTree cloudFileTree;
	private ExecutorService executor;
	private UpdatableTableModel model;
	int transferId;
	TransferTask task;
	RenameThread renameTask;

	public RenameExecutor (CloudOperations cloudHandler,
			CloudFileTree cloudFileTree, UpdatableTableModel model) {
		this.cloudHandler = cloudHandler;
		this.cloudFileTree = cloudFileTree;
		executor = Executors.newFixedThreadPool(MAX_THREADS);
		this.model = model;
	}

	@Override
	public synchronized void addTask(TransferTask task) throws FileTransferException {
		if (renameTask != null && renameTask.isDone() == false) 
			throw (new FileTransferException ("Another folder renaming is already running \n"));
		
		transferId = model.addTransfer(task.getSourcePath(), task.getDestinationPath(), Transfer.RENAME);
		renameTask = new RenameThread(task, cloudHandler,
				cloudFileTree, model, transferId);
		executor.execute(renameTask);
	}

	@Override
	public void terminateAllTransfers() {
		// cancel this transfer
		if (renameTask != null && renameTask.isDone() == false)
			renameTask.cancel(true);
		
		// update the GUI
		model.cancelAllTransfers();
	}

	@Override
	public boolean terminateTransfer(int transferId)
			throws FileTransferException {
		if (renameTask != null && renameTask.isDone() == false)
			return renameTask.cancel(true);
		return true;
	}
}
