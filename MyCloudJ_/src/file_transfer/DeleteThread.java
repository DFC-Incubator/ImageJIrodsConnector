package file_transfer;

import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import CloudGui.CloudFileTree;
import CloudGui.TransferProgressTable.UpdatableTableModel;
import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;
import cloud_interfaces.CloudTransferCallback;

public class DeleteThread extends SwingWorker<Void, Void> {
	private CloudOperations cloudHandler;
	private CloudFileTree cloudFileTree;
	private TransferTask task;
	private UpdatableTableModel model;
	private int transferId;
	
	public DeleteThread(TransferTask task, CloudOperations cloudHandler,
			CloudFileTree cloudFileTree, UpdatableTableModel model, int transferId) {
		this.cloudHandler = cloudHandler;
		this.cloudFileTree = cloudFileTree;
		this.task = task;
		this.model = model;
		this.transferId = transferId;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		try {
			model.updateTransferStatus(transferId, 1, "", false);
			boolean fileWasDeleted = cloudHandler.deleteFile(task.getSourcePath());
			if (fileWasDeleted) {
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
