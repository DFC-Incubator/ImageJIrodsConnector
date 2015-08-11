package rods_backend;

import cloud_interfaces.CloudCapabilities;

public class RodsCapabilities implements CloudCapabilities {

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
		return true;
	}

	@Override
	public boolean isMkDirSupported() {
		return true;
	}

}
