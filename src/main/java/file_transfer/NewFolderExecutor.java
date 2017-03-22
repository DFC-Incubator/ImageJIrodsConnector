package file_transfer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import CloudGui.CloudFileTree;
import CloudGui.TransferProgressTable.UpdatableTableModel;
import cloud_interfaces.CloudOperations;

public class NewFolderExecutor implements ExecutorOperations {
	private static final int MAX_THREADS = 1;
	private CloudOperations cloudHandler;
	private CloudFileTree cloudFileTree;
	private ExecutorService executor;
	private UpdatableTableModel model;
	int transferId;
	TransferTask task;
	NewFolderThread newFolderTask;

	public NewFolderExecutor (CloudOperations cloudHandler,
			CloudFileTree cloudFileTree, UpdatableTableModel model) {
		this.cloudHandler = cloudHandler;
		this.cloudFileTree = cloudFileTree;
		executor = Executors.newFixedThreadPool(MAX_THREADS);
		this.model = model;
	}

	@Override
	public synchronized void addTask(TransferTask task) throws FileTransferException {
		if (newFolderTask != null && newFolderTask.isDone() == false) 
			throw (new FileTransferException ("Another folder creation is already running \n"));
		
		transferId = model.addTransfer(task.getSourcePath(), "", Transfer.NEW_FOLDER);
		newFolderTask = new NewFolderThread(task, cloudHandler,
				cloudFileTree, model, transferId);
		executor.execute(newFolderTask);
	}

	@Override
	public void terminateAllTransfers() {
		// cancel this transfer
		if (newFolderTask != null && newFolderTask.isDone() == false)
			newFolderTask.cancel(true);
		
		// update the GUI
		model.cancelAllTransfers();
	}

	@Override
	public boolean terminateTransfer(int transferId)
			throws FileTransferException {
		if (newFolderTask != null && newFolderTask.isDone() == false)
			return newFolderTask.cancel(true);
		return true;
	}
}
