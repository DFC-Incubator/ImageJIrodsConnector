package CloudGui;

public class Folder {
	private String path;
	
	public Folder(String path) {
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
