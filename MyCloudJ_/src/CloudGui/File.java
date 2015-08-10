package CloudGui;

public class File {
	private String path;
	
	public File(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	 @Override
        public String toString() {
            return path;
        }
}
