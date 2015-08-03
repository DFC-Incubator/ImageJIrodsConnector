package file_transfer;

public class FileTransferException extends Exception {
	private String error;
	
	public FileTransferException(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
