package CloudGui;

import general.GeneralUtility;

import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

	/*
	 * enclosing frame of browsing trees
	 */
	private JFrame enclosingFrame;
	private JButton expandButton;
	private JButton selectButton;
	private JButton cancelButton;

	private CloudOperations cloudHandler;

	public void createEnclosingFrameDownload() {
		createEnclosingFrame(downloadTree);
	}

	public void createEnclosingFrameUpload() {
		createEnclosingFrame(uploadTree);
	}

	private void createEnclosingFrame(JTree fileTree) {
		JPanel fileTreePanel = new JPanel();
		JPanel buttonsPanel = new JPanel(new FlowLayout());
		enclosingFrame = new JFrame();
		BoxLayout boxLayout = new BoxLayout(enclosingFrame.getContentPane(),
				BoxLayout.Y_AXIS);

		// create a panel with 3 buttons
		expandButton = new JButton("Expand");
		selectButton = new JButton("Select");
		cancelButton = new JButton("Cancel");
		buttonsPanel.add(selectButton);
		buttonsPanel.add(expandButton);
		buttonsPanel.add(cancelButton);

		// add the file tree in the enclosing frame
		fileTreePanel.add(fileTree);
		enclosingFrame.add(new JScrollPane(fileTreePanel));

		// add the buttons in the enclosing Frame
		enclosingFrame.add(buttonsPanel);

		// set the properties for the enclosing frame
		enclosingFrame.setVisible(true);
		enclosingFrame.setLayout(boxLayout);

		// set expansion listeners for the file trees
		setExpansionListeners();

		// This will position the JFrame in the center of the screen
		enclosingFrame.setLocationRelativeTo(null);
		enclosingFrame.setTitle("Cloud Browse");
		enclosingFrame.setSize(350, 200);
		enclosingFrame.setResizable(true);
		enclosingFrame.pack();
	}

	public CloudFileTree(String homeDirectoryPath, CloudOperations cloudHandler)
			throws CloudException {
		List<CloudFile> rootFiles;
		this.cloudHandler = cloudHandler;

		downloadRoot = new DefaultMutableTreeNode(homeDirectoryPath);
		downloadTreeModel = new DefaultTreeModel(downloadRoot);
		downloadTree = new JTree(downloadTreeModel);

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
		uploadTreeModel = new DefaultTreeModel(uploadRoot);
		uploadTree = new JTree(uploadTreeModel);
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
		expandTree(getUploadTree(), uploadTreeModel, true,
				getSelectedNodePath(uploadTree));
	}

	public void expandDownloadTree() throws CloudException {
		expandTree(getDownloadTree(), downloadTreeModel, false,
				getSelectedNodePath(downloadTree));
	}

	private void expandTree(JTree tree, DefaultTreeModel treeModel,
			boolean onlyFolders, String selectedFilePath) throws CloudException {
		// parent node of the currently selected node
		DefaultMutableTreeNode parentNode = null;

		TreePath parentPath = tree.getSelectionPath();
		if (parentPath == null)
			return;
		parentNode = (DefaultMutableTreeNode) (parentPath
				.getLastPathComponent());

		List<CloudFile> cloudFiles = cloudHandler.listFiles(selectedFilePath);

		if (onlyFolders)
			addChildrenFolder(parentNode, treeModel, cloudFiles);
		else
			addChildren(parentNode, treeModel, cloudFiles);

		tree.expandPath(new TreePath(parentNode.getPath()));
	}

	public synchronized void updateTrees(String path, boolean onlyFolders) {
		String selectedFilePath = "";

		try {
			// update the upload tree
			selectedFilePath = getSelectedNodePath(uploadTree);
			if (selectedFilePath.startsWith(path) == true)
				expandTree(uploadTree, uploadTreeModel, false, selectedFilePath);

			// update the download tree
			selectedFilePath = getSelectedNodePath(downloadTree);
			if (selectedFilePath.startsWith(path) == true)
				expandTree(downloadTree, downloadTreeModel, false, selectedFilePath);
		} catch (CloudException e) {
			e.printStackTrace();
		}
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

	private void setExpansionListeners() {
		uploadTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				getEnclosingFrame().pack();
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				getEnclosingFrame().pack();
			}
		});

		downloadTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				getEnclosingFrame().pack();
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				getEnclosingFrame().pack();
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
		return getEnclosingFrame();
	}

	public JButton getExpandButton() {
		return expandButton;
	}

	public JButton getSelectButton() {
		return selectButton;
	}

	public JFrame getEnclosingFrame() {
		return enclosingFrame;
	}

	public JButton getCancelButton() {
		return cancelButton;
	}
}
