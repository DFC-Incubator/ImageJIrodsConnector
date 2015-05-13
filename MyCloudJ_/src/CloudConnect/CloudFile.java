package CloudConnect;

public class CloudFile {

	private String path;
	private boolean isFile;

	public CloudFile(String path, boolean isFile) {
		this.setPath(path);
		this.setFile(isFile);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isFile() {
		return isFile;
	}

	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}
}
