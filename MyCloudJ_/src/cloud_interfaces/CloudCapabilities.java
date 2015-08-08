package cloud_interfaces;

public interface CloudCapabilities {
	boolean isDownloadSupported();
	boolean isUploadSupported();
	boolean isRenameSUpported();
	boolean isDeleteSupported();
}
