package file_transfer;

public interface ExecutorOperations {
	public void addTask(TransferTask task) throws FileTransferException;
	public void terminateAllTransfers();
	public boolean terminateTransfer(int transferId) throws FileTransferException;
}
