package CloudGui;

import general.GeneralUtility;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import cloud_interfaces.CloudException;
import cloud_interfaces.CloudFile;
import cloud_interfaces.CloudOperations;

public class CloudFileTree {
	/**
	 * @downloadTree: stores the complete metadata(path of folders) of cloud
	 *                account. Used to display when user browses to select the
	 *                files/folders to Download from.
	 * 
	 *                Note : Each node in downloadTree represents a file/folder
	 */
	private JTree downloadTree;
	private DefaultTreeModel downloadTreeModel;
	private DefaultMutableTreeNode downloadRoot;
	private JFrame treeFrame;

	/**
	 * @uploadTree: Stores the complete metadata(path of folders) of cloud
	 *              account. Used to display when user browses to select the
	 *              folders to Upload into.
	 * 
	 *              Note : Each node in uploadTree represents a folder
	 */
	private JTree uploadTree;
	private DefaultTreeModel uploadTreeModel;
	private DefaultMutableTreeNode uploadRoot;
	
	private CloudOperations cloudHandler;

	public CloudFileTree(String homeDirectoryPath, CloudOperations cloudHandler)
			throws CloudException {
		List<CloudFile> rootFiles;
		this.cloudHandler = cloudHandler;

		downloadRoot = new DefaultMutableTreeNode(homeDirectoryPath);
		downloadTree = new JTree(downloadRoot);
		downloadTreeModel = new DefaultTreeModel(downloadRoot);

		rootFiles = cloudHandler.listFiles(homeDirectoryPath);
		addChildren(downloadRoot, downloadTreeModel, rootFiles);
		getDownloadTree().getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		downloadTreeModel.reload(downloadRoot);

		/*
		 * Create the JTree for browsing(to select path for uploading the
		 * file/folder) Only added subfolders of the Dropbox root folder i.e.
		 * "/" Will add new nodes on demand of the user in form of "Expand"
		 * clicks
		 */
		uploadRoot = new DefaultMutableTreeNode(homeDirectoryPath);
		uploadTree = new JTree(uploadRoot);
		uploadTreeModel = new DefaultTreeModel(uploadRoot);
		addChildrenFolder(downloadRoot, downloadTreeModel, rootFiles);
		getUploadTree().getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		uploadTreeModel.reload(uploadRoot);
	}

	/*
	 * Function to add nodes to the JTree
	 * 
	 * This function is called when user selects a parent node and clicks Expand
	 * button
	 * 
	 * Parameters: TODO Javadoc: TODO
	 */
	private void addChildren(DefaultMutableTreeNode node,
			DefaultTreeModel Treemodel, List<CloudFile> cloudFiles) {

		for (int i = 0; i < cloudFiles.size(); i++) {
			CloudFile child = cloudFiles.get(i);
			DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(
					child.getPath());
			GeneralUtility.addUniqueNode(node, nodeChild, Treemodel);
		}
	}

	/*
	 * Function to add nodes to the JTree
	 * 
	 * This function is called when user selects a parent node and clicks Expand
	 * button
	 * 
	 * Parameters: TODO Javadoc: TODO
	 */
	private void addChildrenFolder(DefaultMutableTreeNode node,
			DefaultTreeModel Treemodel, List<CloudFile> cloudFiles) {

		for (int i = 0; i < cloudFiles.size(); i++) {
			CloudFile child = cloudFiles.get(i);
			if (!child.isFile()) {
				DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(
						child.getPath());
				GeneralUtility.addUniqueNode(node, nodeChild, Treemodel);
			}
		}
	}

	public void expandUploadTree() throws CloudException {
		expandTree(getUploadTree(), uploadTreeModel, true);
	}

	public void expandDownloadTree() throws CloudException {
		expandTree(getDownloadTree(), downloadTreeModel, false);
	}

	private void expandTree(JTree tree, DefaultTreeModel treeModel,
			boolean onlyFolders) throws CloudException {
		String selectedFilePath = "";
		// parent node of the currently selected node
		DefaultMutableTreeNode parentNode = null;

		TreePath parentPath = tree.getSelectionPath();
		parentNode = (DefaultMutableTreeNode) (parentPath
				.getLastPathComponent());

		// get the subfiles of the currently selected node
		selectedFilePath = getSelectedNodePath(tree);
		List<CloudFile> cloudFiles = cloudHandler.listFiles(selectedFilePath);

		if (onlyFolders)
			addChildrenFolder(parentNode, treeModel, cloudFiles);
		else
			addChildren(parentNode, treeModel, cloudFiles);

		tree.expandPath(new TreePath(parentNode.getPath()));
	}

	private String getSelectedNodePath(JTree tree) {
		String componentPath, selectedNodePath = "";
		int componentNo;

		TreePath parentPath = tree.getSelectionPath();
		componentNo = parentPath.getPathCount();

		for (int i = 0; i < componentNo; i++) {
			componentPath = parentPath.getPathComponent(i).toString();

			// do not add "/" to the last component of the path
			if ((componentPath.endsWith("/") == false)
					&& (i < (componentNo - 1)))
				componentPath = componentPath.concat("/");

			selectedNodePath = selectedNodePath.concat(componentPath);
		}

		return selectedNodePath;
	}

	public void setTreeFrame(final JFrame treeFrame) {
		this.treeFrame = treeFrame;
		uploadTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				treeFrame.pack();
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				treeFrame.pack();
			}
		});

		downloadTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				treeFrame.pack();
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				treeFrame.pack();
			}
		});
	}

	public String getSelectedNodePathDownloadTree() {
		return getSelectedNodePath(getDownloadTree());
	}

	public String getSelectedNodePathUploadTree() {
		return getSelectedNodePath(getUploadTree());
	}

	public JTree getDownloadTree() {
		return downloadTree;
	}

	public JTree getUploadTree() {
		return uploadTree;
	}

	public JFrame getTreeFrame() {
		return treeFrame;
	}
}
