package CloudGui;

import general.GeneralUtility;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
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
	private static final String CLOUD_DELIMITER = "/";
	private String homeDirectoryPath;
	
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
		this.cloudHandler = cloudHandler;
		this.homeDirectoryPath = homeDirectoryPath;

		downloadRoot = new DefaultMutableTreeNode(homeDirectoryPath);
		downloadTreeModel = new DefaultTreeModel(downloadRoot);
		downloadTree = new JTree(downloadTreeModel);

		getDownloadTree().getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		downloadTreeModel.reload(downloadRoot);

		uploadRoot = new DefaultMutableTreeNode(homeDirectoryPath);
		uploadTreeModel = new DefaultTreeModel(uploadRoot);
		uploadTree = new JTree(uploadTreeModel);
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
		DefaultMutableTreeNode node;
		TreePath currNodePath;
		boolean currNodeisExpanded;
		
		// try to expand the upload tree
		node = getNodeFromPath(uploadRoot, path);
		if (node == null) 
			System.out.println("Error expanding tree!");
		else {
			currNodePath = new TreePath(node.getPath());
			currNodeisExpanded = uploadTree.isExpanded(currNodePath);
			/* expand only if the folder/parent of the folder where 
			 * we've done the upload is expanded.
			 */
			if (currNodeisExpanded || isParentExpanded(node, uploadTree))
				try {
					expandTree(uploadTree, uploadTreeModel, true, path);
				} catch (CloudException e) {
					System.out.println("Error expanding upload tree!");
					e.printStackTrace();
				}
		}
		
		// try to expand the download tree
		node = getNodeFromPath(downloadRoot, path);
		if (node == null) {
			System.out.println("Error expanding tree!");
		}
		else { 
			try {
				currNodePath = new TreePath(node.getPath());
				currNodeisExpanded = downloadTree.isExpanded(currNodePath);
				/* expand only if: 
				 * - the folder where've done the upload is expanded 
				 * - the folder where we've done the upload contains only the 
				 * uploaded file and the parent of the folder where we've done 
				 * the upload is expanded
				 */
				if (currNodeisExpanded || (isParentExpanded(node, downloadTree) &&
					cloudHandler.listFiles(path).size() == 1))
					expandTree(downloadTree, downloadTreeModel, false, path);
			} catch (CloudException e) {
				System.out.println("Error expanding download tree!");
				e.printStackTrace();
			}
		}
	}
	
	private boolean isParentExpanded(DefaultMutableTreeNode node, JTree tree) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
		TreePath parentTreePath;
		
		if (root.toString() == homeDirectoryPath)
			return true;
		
		parentTreePath = new TreePath(parent.getPath());
		if (tree.isExpanded(parentTreePath)) 
			return true;
		
		return false;
	}
	
	private DefaultMutableTreeNode getNodeFromPath (DefaultMutableTreeNode root, String path) {
		if (path == null || path.startsWith(homeDirectoryPath) == false)
			return null;
		
		if (path.equals(root.toString()) == true)
			return root;
		
		path = path.substring(homeDirectoryPath.length() + 1, path.length());
		List<String> pathComponents = new ArrayList<String>(Arrays.asList(path.split(CLOUD_DELIMITER)));
		
		return getNodeFromPath(root, pathComponents);
	}
	
	private DefaultMutableTreeNode getNodeFromPath(DefaultMutableTreeNode node, List<String> pathComponents) {
		boolean notFound = true;
		if (pathComponents.size() == 0) 
			return node;
	
		String searchedNodeName = pathComponents.get(0);
		pathComponents.remove(0);
		
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> children = node.children();
		while (children.hasMoreElements() && notFound) {
			DefaultMutableTreeNode currNode = children.nextElement();
			if (currNode.toString().equals(searchedNodeName) == true) {
					notFound = false;
					return getNodeFromPath(currNode, pathComponents);
			}
		}
		return null;
	}

	private String getSelectedNodePath(JTree tree) {
		String componentPath, selectedNodePath = "";
		int componentNo;

		TreePath parentPath = tree.getSelectionPath();
		if (parentPath == null)
			return null;
		
		componentNo = parentPath.getPathCount();

		for (int i = 0; i < componentNo; i++) {
			componentPath = parentPath.getPathComponent(i).toString();

			// do not add cloud delimiter to the last component of the path
			if ((componentPath.endsWith(CLOUD_DELIMITER) == false)
					&& (i < (componentNo - 1)))
				componentPath = componentPath.concat(CLOUD_DELIMITER);

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