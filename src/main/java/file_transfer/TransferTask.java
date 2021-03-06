package file_transfer;

public class TransferTask {
	private String sourcePath;
	private String destPath;
	private boolean isDownloadTask;
	private TransferTaskCallback callback;
	
	public TransferTask(String sourcePath, String destPath) {
		this.sourcePath = sourcePath;
		this.destPath = destPath;
	}
	
	public String getSourcePath() {
		return sourcePath;
	}
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}	
	public String getDestinationPath() {
		return destPath;
	}
	public void setDestinationPath(String destinationPath) {
		this.destPath = destinationPath;
	}
	public boolean isDownloadTask() {
		return isDownloadTask;
	}
	public void setDownloadTask(boolean isDownloadTask) {
		this.isDownloadTask = isDownloadTask;
	}

	public TransferTaskCallback getCallback() {
		return callback;
	}

	public void setCallback(TransferTaskCallback callback) {
		this.callback = callback;
	}
}
