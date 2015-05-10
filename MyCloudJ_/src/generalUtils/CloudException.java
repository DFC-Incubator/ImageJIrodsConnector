package generalUtils;

public class CloudException extends Exception {
	private String cloudError;
	
	public CloudException(String error) {
		this.cloudError = error;
	}

	public String getCloudError() {
		return cloudError;
	}

	public void setCloudError(String cloudError) {
		this.cloudError = cloudError;
	}
}
