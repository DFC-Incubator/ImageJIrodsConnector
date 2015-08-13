package CloudGui;

import file_transfer.ExecutorOperations;
import file_transfer.FileTransferException;
import file_transfer.TransferTask;
import file_transfer.TransferTaskCallback;
import general.GeneralUtility;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
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
	private JTree tree;
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
	private ExecutorOperations deleteExecutor;
	private ExecutorOperations newFolderExecutor;

	public void createEnclosingFrameDownload() {
		createEnclosingFrame(tree);
	}

	public void createEnclosingFrameUpload() {
		createEnclosingFrame(uploadTree);
	}

	public CloudFileTree(String homeDirectoryPath, CloudOperations cloudHandler)
			throws CloudException {
		this.cloudHandler = cloudHandler;
		this.homeDirectoryPath = homeDirectoryPath;

		downloadRoot = new DefaultMutableTreeNode(new Folder(homeDirectoryPath));
		downloadTreeModel = new DefaultTreeModel(downloadRoot);
		tree = new JTree(downloadTreeModel);

		getDownloadTree().getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		downloadTreeModel.reload(downloadRoot);

		uploadRoot = new DefaultMutableTreeNode(new Folder(homeDirectoryPath));
		uploadTreeModel = new DefaultTreeModel(uploadRoot);
		uploadTree = new JTree(uploadTreeModel);
		getUploadTree().getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		uploadTreeModel.reload(uploadRoot);

		// right click popup = rename + delete
		tree.addMouseListener(new RightClickAction(tree));
		uploadTree.addMouseListener(new RightClickAction(uploadTree));

		// renderer
		tree.setCellRenderer(new CustomTreeRenderer());
		uploadTree.setCellRenderer(new CustomTreeRenderer());
	}

	private void createEnclosingFrame(JTree fileTree) {
		JPanel fileTreePanel = new JPanel();
		JPanel buttonsPanel = new JPanel(new FlowLayout());
		enclosingFrame = new JFrame();
		enclosingFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				enclosingFrame = null;
			}
		});
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
		enclosingFrame.setTitle("Cloud Files");
		enclosingFrame.setSize(350, 200);
		enclosingFrame.setResizable(true);
		enclosingFrame.pack();
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
			DefaultMutableTreeNode nodeChild;
			if (child.isFile()) {
				nodeChild = new DefaultMutableTreeNode(
						new File(child.getPath()));
			} else {
				nodeChild = new DefaultMutableTreeNode(new Folder(
						child.getPath()));
			}
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
						new Folder(child.getPath()));
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
				getSelectedNodePath(tree));
	}

	private void expandTree(JTree tree, DefaultTreeModel treeModel,
			boolean onlyFolders, String selectedFilePath) throws CloudException {
		// parent node of the currently selected node
		DefaultMutableTreeNode parentNode = null;

		DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel
				.getRoot();
		parentNode = getNodeFromPath(root, selectedFilePath);
		if (parentNode == null)
			return;

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
		if (node != null) {
			currNodePath = new TreePath(node.getPath());
			currNodeisExpanded = uploadTree.isExpanded(currNodePath);
			/*
			 * expand only if the folder/parent of the folder where we've done
			 * the upload is expanded.
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
		if (node != null) {
			try {
				currNodePath = new TreePath(node.getPath());
				currNodeisExpanded = tree.isExpanded(currNodePath);
				/*
				 * expand only if: - the folder where've done the upload is
				 * expanded - the folder where we've done the upload contains
				 * only the uploaded file and the parent of the folder where
				 * we've done the upload is expanded
				 */
				if (currNodeisExpanded
						|| (isParentExpanded(node, tree) && cloudHandler
								.listFiles(path).size() == 1))
					expandTree(tree, downloadTreeModel, false, path);
			} catch (CloudException e) {
				System.out.println("Error expanding download tree!");
				e.printStackTrace();
			}
		}
	}

	private boolean isParentExpanded(DefaultMutableTreeNode node, JTree tree) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel()
				.getRoot();
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
				.getParent();
		TreePath parentTreePath;

		if (root.toString() == homeDirectoryPath)
			return true;

		parentTreePath = new TreePath(parent.getPath());
		if (tree.isExpanded(parentTreePath))
			return true;

		return false;
	}

	private DefaultMutableTreeNode getNodeFromPath(DefaultMutableTreeNode root,
			String path) {
		if (path == null || path.startsWith(homeDirectoryPath) == false)
			return null;

		if (path.equals(root.toString()) == true)
			return root;

		path = path.substring(homeDirectoryPath.length(), path.length());
		List<String> pathComponents = new ArrayList<String>(Arrays.asList(path
				.split(CLOUD_DELIMITER)));
		// TODO: cleanup the relation between file path and root
		if (pathComponents.get(0).length() == 0)
			pathComponents.remove(0);

		return getNodeFromPath(root, pathComponents);
	}

	private DefaultMutableTreeNode getNodeFromPath(DefaultMutableTreeNode node,
			List<String> pathComponents) {
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

		tree.addTreeExpansionListener(new TreeExpansionListener() {
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
		return tree;
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

	public void setEnclosingFrame(JFrame value) {
		enclosingFrame = value;
	}

	public JButton getCancelButton() {
		return cancelButton;
	}

	public void setDeleteExecutor(ExecutorOperations deleteExecutor) {
		this.deleteExecutor = deleteExecutor;
	}

	public ExecutorOperations getNewFolderExecutor() {
		return newFolderExecutor;
	}

	public void setNewFolderExecutor(ExecutorOperations newFolderExecutor) {
		this.newFolderExecutor = newFolderExecutor;
	}

	class RightClickAction extends MouseAdapter {
		private JTree tree;

		public RightClickAction(JTree tree) {
			this.tree = tree;
		}

		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				TreePath selectedPath = tree.getPathForLocation(e.getX(),
						e.getY());
				Rectangle pathBounds = tree.getUI().getPathBounds(tree,
						selectedPath);
				if (pathBounds != null
						&& pathBounds.contains(e.getX(), e.getY())) {
					// selected path is the right clicked node
					tree.setSelectionPath(selectedPath);

					// menu = rename + delete + mkdir
					JPopupMenu menu = new JPopupMenu();

					// add the rename option
					JMenuItem renameItem = new JMenuItem("Rename");
					ActionListener renameListener = new RenameListener(tree);
					renameItem.addActionListener(renameListener);
					menu.add(renameItem);

					// add the rename option
					JMenuItem deleteItem = new JMenuItem("Delete");
					ActionListener deleteListener = new DeleteListener(tree);
					deleteItem.addActionListener(deleteListener);
					menu.add(deleteItem);

					// only for folders
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree
							.getLastSelectedPathComponent();
					Object selectedNodeType = selectedNode.getUserObject();
					if (selectedNodeType instanceof Folder) { // add the mkdir option
						JMenuItem mkdirItem = new JMenuItem("New Folder");
						ActionListener mkdirListener = new MkDirListener(tree);
						mkdirItem.addActionListener(mkdirListener);
						menu.add(mkdirItem);
					}

					menu.show(tree, pathBounds.x, pathBounds.y
							+ pathBounds.height);
				}
			}

		}
	}

	private class CustomTreeRenderer extends DefaultTreeCellRenderer {
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);

			// decide what icons you want by examining the node
			if (value instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				if (node.getUserObject() instanceof Folder) {
					// your root node, since you just put a String as a user obj
					setIcon(UIManager.getIcon("FileView.directoryIcon"));
				} else if (node.getUserObject() instanceof File) {
					// decide based on some property of your Contact obj

					setIcon(UIManager.getIcon("FileView.fileIcon"));
				} else {
					setIcon(UIManager.getIcon("FileChooser.homeFolderIcon"));
				}
			}
			return this;
		}

	}

	class RenameListener implements ActionListener {
		private JTree tree;

		public RenameListener(JTree tree) {
			this.tree = tree;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (cloudHandler.getCloudCapabilities().isRenameSUpported() == false) {
				JOptionPane.showMessageDialog(null,
						"Operation not supported yet");
				return;
			}

			String selectedNodePath = getSelectedNodePath(tree);
			if (selectedNodePath == null)
				return;
		}
	}

	class DeleteListener implements ActionListener {
		private JTree tree;

		public DeleteListener(JTree tree) {
			this.tree = tree;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String selectedNodePath = getSelectedNodePath(tree);
			if (selectedNodePath == null)
				return;

			if (cloudHandler.getCloudCapabilities().isDeleteSupported() == false) {
				JOptionPane.showMessageDialog(null,
						"Operation not supported yet");
				return;
			}

			// popup for delete confirmation
			Object[] message = { "Are you sure you want to delete the file:\n"
					+ selectedNodePath + "\n" };
			int option = JOptionPane.showConfirmDialog(null, message,
					"Delete Confirm", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) { // delete is authorized
				TransferTask task = new TransferTask(selectedNodePath, "");
				task.setCallback(new DeleteTaskCallback(selectedNodePath));
				try {
					deleteExecutor.addTask(task);
				} catch (FileTransferException e2) {
					JOptionPane.showMessageDialog(null, e2.getError(), "Error",
							JOptionPane.ERROR_MESSAGE);
					e2.printStackTrace();
				}

			}
		}
	}

	class DeleteTaskCallback implements TransferTaskCallback {
		String selectedNodePath;

		public DeleteTaskCallback(String selectedNodePath) {
			this.selectedNodePath = selectedNodePath;
		}

		@Override
		public void updateGUI() {
			DefaultMutableTreeNode downloadNode = getNodeFromPath(downloadRoot,
					selectedNodePath);
			if (downloadNode != null) {
				downloadTreeModel.removeNodeFromParent(downloadNode);
				getEnclosingFrame().pack();
			}

			DefaultMutableTreeNode uploadNode = getNodeFromPath(uploadRoot,
					selectedNodePath);
			if (uploadNode != null) {
				uploadTreeModel.removeNodeFromParent(uploadNode);
				getEnclosingFrame().pack();
			}
		}
	}

	class MkDirListener implements ActionListener {
		private JTree tree;

		public MkDirListener(JTree tree) {
			this.tree = tree;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String selectedNodePath = getSelectedNodePath(tree);
			if (selectedNodePath == null)
				return;

			if (cloudHandler.getCloudCapabilities().isMkDirSupported() == false) {
				JOptionPane.showMessageDialog(null,
						"Operation not supported yet");
				return;
			}

			String dialogMessage = "New folder path: " + selectedNodePath
					+ "\n" + "Enter folder name:";
			String newFolderName = (String) JOptionPane.showInputDialog(null,
					dialogMessage, "Folder Name", JOptionPane.PLAIN_MESSAGE,
					UIManager.getIcon("FileView.directoryIcon"), null,
					"Folder Name");
			
			if (GeneralUtility.isValidFolderName(newFolderName, CLOUD_DELIMITER) == false) {
				JOptionPane.showMessageDialog(null, "Invalid folder name",
						"Input error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			

			TransferTask task = new TransferTask(selectedNodePath
					+ CLOUD_DELIMITER + newFolderName, "");
			task.setCallback(new MkDirTaskCallback(selectedNodePath,
					newFolderName));
			try {
				newFolderExecutor.addTask(task);
			} catch (FileTransferException e2) {
				JOptionPane.showMessageDialog(null, e2.getError(), "Error",
						JOptionPane.ERROR_MESSAGE);
				e2.printStackTrace();
			}
		}
	}

	class MkDirTaskCallback implements TransferTaskCallback {
		String selectedNodePath;
		String newFolderName;

		public MkDirTaskCallback(String selectedNodePath, String newFolderName) {
			this.selectedNodePath = selectedNodePath;
			this.newFolderName = newFolderName;
		}

		@Override
		public void updateGUI() {
			// refresh the download browsing tree
			DefaultMutableTreeNode downloadNode = getNodeFromPath(downloadRoot,
					selectedNodePath);
			if (downloadNode != null) {
				DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(
						new Folder(newFolderName));
				GeneralUtility.addUniqueNode(downloadNode, nodeChild,
						downloadTreeModel);

				TreePath parentPath = new TreePath(downloadNode.getPath());
				if (tree.isExpanded(parentPath) == false) {
					try {
						expandDownloadTree();
					} catch (CloudException e) {
						e.printStackTrace();
					}
				} else {
					tree.expandPath(new TreePath(downloadNode.getPath()));
					getEnclosingFrame().pack();
				}
			}

			// refresh the upload browsing tree
			DefaultMutableTreeNode uploadNode = getNodeFromPath(uploadRoot,
					selectedNodePath);
			if (uploadNode != null) {
				DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(
						new Folder(newFolderName));
				GeneralUtility.addUniqueNode(uploadNode, nodeChild,
						uploadTreeModel);
				getEnclosingFrame().pack();

				TreePath parentPath = new TreePath(uploadNode.getPath());
				if (uploadTree.isExpanded(parentPath) == false) {
					try {
						expandUploadTree();
					} catch (CloudException e) {
						e.printStackTrace();
					}
				} else {
					uploadTree.expandPath(new TreePath(uploadNode.getPath()));
					getEnclosingFrame().pack();
				}
			}
		}
	}
}