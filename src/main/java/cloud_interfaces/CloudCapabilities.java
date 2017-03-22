package cloud_interfaces;

public interface CloudCapabilities {
	boolean isDownloadSupported();
	boolean isUploadSupported();
	boolean isDeleteSupported();
	boolean isMkDirSupported();
	boolean isRenameSupported();
}
