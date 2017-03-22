package rods_backend;

import general.GeneralUtility;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

import cloud_interfaces.CloudTransferCallback;
import cloud_interfaces.CloudTransferStatus;

public class TransferStatusCallback implements TransferStatusCallbackListener {

	private CloudTransferCallback cloudCallback;

	@Override
	public void overallStatusCallback(TransferStatus arg0)
			throws JargonException {
	}

	@Override
	public FileStatusCallbackResponse statusCallback(
			TransferStatus transferStatus) throws JargonException {
		CloudTransferStatus cloudTransferStatus = new CloudTransferStatus();
		int fraction;

		long bytesTransferred = transferStatus.getBytesTransfered();
		long totalBytes = transferStatus.getTotalSize();
		fraction = Math
				.round((long) ((float) bytesTransferred / totalBytes * 100));

		if (transferStatus.isIntraFileStatusReport()) {
			cloudTransferStatus.setFraction(fraction);
			cloudCallback.statusCallback(cloudTransferStatus);
		} else if (fraction == 0) {
			String source = GeneralUtility.getLastComponentFromPath(
					transferStatus.getSourceFileAbsolutePath(), "/");
			cloudTransferStatus.setCurrFile(source);
			cloudTransferStatus.setFraction(1);
			cloudCallback.statusCallback(cloudTransferStatus);
		}

		return FileStatusCallbackResponse.CONTINUE;
	}

	@Override
	public CallbackResponse transferAsksWhetherToForceOperation(String arg0,
			boolean arg1) {
		return null;
	}

	public CloudTransferCallback getCloudCallback() {
		return cloudCallback;
	}

	public void setCloudCallback(CloudTransferCallback cloudCallback) {
		this.cloudCallback = cloudCallback;
	}
}