package file_transfer;

import ij.io.Opener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.SwingWorker;

import CloudGui.CloudFileTree;
import CloudGui.TransferProgressTable.UpdatableTableModel;
import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;
import cloud_interfaces.CloudTransferCallback;
import cloud_interfaces.CloudTransferStatus;

public class UploadThread extends SwingWorker<Void, Void> implements CloudTransferCallback {
	private CloudOperations cloudHandler;
	private CloudFileTree cloudFileTree;
	private TransferTask task;
	private UpdatableTableModel model;
	private int transferId;
	private String currFile;
	
	public UploadThread(TransferTask task, CloudOperations cloudHandler,
			CloudFileTree cloudFileTree, UpdatableTableModel model, int transferId) {
		this.cloudHandler = cloudHandler;
		this.cloudFileTree = cloudFileTree;
		this.task = task;
		this.model = model;
		this.transferId = transferId;
		
		this.addPropertyChangeListener(new PropertyChange());
	}
	
	class PropertyChange implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			  if (evt.getPropertyName().equals("progress")) {
	            	model.updateTransferStatus(getTransferId(), (int) evt.getNewValue(), currFile, false);
	            }
		}
	}
	
	@Override
	public void statusCallback(CloudTransferStatus transferStatus) {
		int fraction = transferStatus.getFraction();
		setProgress(fraction);
		currFile = transferStatus.getCurrFile();
	}

	@Override
	public Void doInBackground() throws CloudException {
		String sourcePath = "";
		String destPath = "";
		String uploadType = ""; // file/folder

		try {
			// save task parameters
			sourcePath = task.getSourcePath();
			destPath = task.getDestinationPath();
			File file = new File(sourcePath);
			boolean isFileUpload = file.isFile();
			uploadType = isFileUpload ? "file" : "folder";

			// start the upload
			if (isFileUpload)
				cloudHandler.uploadFile(sourcePath, destPath, this);
			else
				cloudHandler.uploadFolder(sourcePath, destPath, this);
			
			// TODO: open file from the progress table
			//if (file.isFile())
				//openFile(task);
		} catch (CloudException e) {
			e.printStackTrace();
			return null;
		}

		// update the file browsing tree with the new node
		cloudFileTree.updateTrees(destPath, true);

		// TODO: in the future we'll return the file transfer status
		return null;
	}

	private void openFile(TransferTask task) {
		// Open in the default application
		Opener openfile = new Opener();
		openfile.open(task.getSourcePath());
	}

	public TransferTask getTask() {
		return task;
	}

	public void setTask(TransferTask task) {
		this.task = task;
	}

	public int getTransferId() {
		return transferId;
	}

	public void setTransferId(int transferId) {
		this.transferId = transferId;
	}
}
