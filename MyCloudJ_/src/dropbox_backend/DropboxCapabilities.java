package dropbox_backend;

import cloud_interfaces.CloudCapabilities;

public class DropboxCapabilities implements CloudCapabilities{

	@Override
	public boolean isDownloadSupported() {
		return true;
	}

	@Override
	public boolean isUploadSupported() {
		return true;
	}

	@Override
	public boolean isRenameSUpported() {
		return false;
	}

	@Override
	public boolean isDeleteSupported() {
		return false;
	}

}
