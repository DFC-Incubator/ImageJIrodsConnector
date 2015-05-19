package generalUtils;

import java.util.List;

import CloudConnect.CloudFile;

public interface CloudOperations {
	public void login() throws CloudException;

	public void downloadFile(String cloudPath, String localPath)
			throws CloudException;

	public void downloadFolder(String cloudPath, String localPath)
			throws CloudException;

	public void uploadFile(String localPath, String cloudPath)
			throws CloudException;

	public void uploadFolder(String localPath, String cloudPath)
			throws CloudException;

	public boolean isFile(String name) throws CloudException;
	
	public List<CloudFile> listFiles(String directoryPath)
			throws CloudException;
	
	public String getHomeDirectory() throws CloudException;
}