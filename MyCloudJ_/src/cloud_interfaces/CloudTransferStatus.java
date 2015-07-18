package cloud_interfaces;

public class CloudTransferStatus {
	private long totalBytes;
	private long bytesTransferredSoFar;
	private int fraction;
	
	public long getTotalBytes() {
		return totalBytes;
	}
	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}
	public long getBytesTransferredSoFar() {
		return bytesTransferredSoFar;
	}
	public void setBytesTransferredSoFar(long bytesTransferredSoFar) {
		this.bytesTransferredSoFar = bytesTransferredSoFar;
	}
	public int getFraction() {
		return fraction;
	}
	public void setFraction(int fraction) {
		this.fraction = fraction;
	}
}
