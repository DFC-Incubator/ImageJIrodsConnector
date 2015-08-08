package file_transfer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import CloudGui.CloudFileTree;
import CloudGui.TransferProgressTable.UpdatableTableModel;
import cloud_interfaces.CloudOperations;

public class DeleteExecutor implements ExecutorOperations {
	private static final int MAX_THREADS = 1;
	private CloudOperations cloudHandler;
	private CloudFileTree cloudFileTree;
	private ExecutorService executor;
	private UpdatableTableModel model;
	int transferId;
	TransferTask task;
	DeleteThread deleteTask;

	public DeleteExecutor(CloudOperations cloudHandler,
			CloudFileTree cloudFileTree, UpdatableTableModel model) {
		this.cloudHandler = cloudHandler;
		this.cloudFileTree = cloudFileTree;
		executor = Executors.newFixedThreadPool(MAX_THREADS);
		this.model = model;
	}

	@Override
	public synchronized void addTask(TransferTask task) throws FileTransferException {
		if (deleteTask != null && deleteTask.isDone() == false) 
			throw (new FileTransferException ("Another delete is already running \n"));
		
		transferId = model.addTransfer(task.getSourcePath(), "", Transfer.DELETE);
		deleteTask = new DeleteThread(task, cloudHandler,
				cloudFileTree, model, transferId);
		executor.execute(deleteTask);
	}

	@Override
	public void terminateAllTransfers() {
		// cancel this transfer
		if (deleteTask != null && deleteTask.isDone() == false)
			deleteTask.cancel(true);
		
		// update the GUI
		model.cancelAllTransfers();
	}

	@Override
	public boolean terminateTransfer(int transferId)
			throws FileTransferException {
		if (deleteTask != null && deleteTask.isDone() == false)
			return deleteTask.cancel(true);
		return true;
	}
	
}
