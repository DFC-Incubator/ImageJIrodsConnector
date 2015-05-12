package generalUtils;

import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

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

	public void addChildrenFolder(DefaultMutableTreeNode node,
			DefaultTreeModel Treemodel, String name) throws CloudException;

	public void addChildren(DefaultMutableTreeNode node,
			DefaultTreeModel Treemodel, String name) throws CloudException;
	
	public String getHomeDirectory() throws CloudException;
}