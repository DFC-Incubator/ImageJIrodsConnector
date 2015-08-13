package file_transfer;

import javax.swing.SwingWorker;

import CloudGui.CloudFileTree;
import CloudGui.TransferProgressTable.UpdatableTableModel;
import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;

public class RenameThread extends SwingWorker<Void, Void> {
	private CloudOperations cloudHandler;
	private TransferTask task;
	private UpdatableTableModel model;
	private int transferId;
	
	public RenameThread(TransferTask task, CloudOperations cloudHandler,
			CloudFileTree cloudFileTree, UpdatableTableModel model, int transferId) {
		this.cloudHandler = cloudHandler;
		this.task = task;
		this.model = model;
		this.transferId = transferId;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		try {
			model.updateTransferStatus(transferId, 1, "", false);
			boolean isFolderRenamed = cloudHandler.rename(task.getSourcePath(), task.getDestinationPath());
			if (isFolderRenamed) {
				task.getCallback().updateGUI();
				/*
				 * TODO: for the moment, the errors are announced to the progress
				 * table using different values for the progress field. 
				 */
				model.updateTransferStatus(transferId, 100, "", false);
			} else 
				model.updateTransferStatus(transferId, 2, "", true);
		} catch (CloudException e1) {
			/*
			 * TODO: for the moment, the errors are announced to the progress
			 * table using different values for the progress field. 
			 */
			e1.printStackTrace();
			model.updateTransferStatus(transferId, 3, "", true);
		}
		return null;
	}
}
