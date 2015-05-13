package generalUtils;

import java.io.IOException;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import CloudConnect.CloudFile;

import com.dropbox.core.DbxException;

public interface CloudOperations {
	public void login() throws CloudException;

	public void downloadFile(String FileDbxPath, String TargetLocalPath)
			throws CloudException;

	public void downloadFolder(String FolderDbxPath, String TargetLocalPath)
			throws CloudException;

	public void uploadFile(String FileLocalPath, String TargetDbxPath)
			throws CloudException;

	public void uploadFolder(String FolderLocalPath, String TargetDbxPath)
			throws CloudException;

	public boolean isFileDownload(String name) throws CloudException;
	
	public List<CloudFile> listFiles(String directoryPath)
			throws CloudException;
	
	public String getHomeDirectory() throws CloudException;
}