package CloudGui;

import ij.plugin.PlugIn;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;
import rods_backend.RodsOperations;
import dropbox_backend.DropboxOperations;
import file_transfer.DeleteExecutor;
import file_transfer.DownloadExecutor;
import file_transfer.ExecutorOperations;
import file_transfer.FileTransferException;
import file_transfer.TransferTask;
import file_transfer.UploadExecutor;

/**
 * @author Atin Mathur (mathuratin007@gmail.com) - Dropbox functionality
 * @author Doru-Cristian Gucea (gucea.doru@gmail.com) - iRODS functionality +
 *         Refactoring
 * @Mentor : Dimiter Prodanov (dimiterpp@gmail.com)
 * 
 * @description : Dropbox functionality: Google Summer of Code 2014
 * @project iRODS functionality: Google Summer of Code 2015 Project
 * @organization : International Neuroinformatics Coordinating Facility, Belgian
 *               Node
 */
public class MyCloudJ_ implements PlugIn {
	// ------------------------------------------------------------------------
	// commun functionality fields, specific both to Dbx and iRODS
	// ------------------------------------------------------------------------

	CloudFileTree cloudFileTree;
	LocalFileTree localFileTree;

	// generic interface for cloud operations
	private CloudOperations cloudHandler = new DropboxOperations();

	// true if user connected to cloud, false otherwise
	private boolean userIsConnected;

	// after login, initialized to user home directory
	private String cloudHomeDirectoryPath;

	private final String LOCAL_HOME_DIRECTORY_PATH = ".";

	// thread used for downloading tasks
	ExecutorOperations downloadExecutor;

	// thread used for downloading tasks
	ExecutorOperations uploadExecutor;

	// thread used for delete tasks
	ExecutorOperations deleteExecutor;

	// ------------------------------------------------------------------------
	// commun GUI fields, specific both to Dbx and iRODS
	// ------------------------------------------------------------------------

	// contains the whole GUI for the MyCloudJ_ plugin
	private JFrame mainFrame;

	// left side of the mainFrame, components used for connecting to the cloud
	private JPanel loginWindow;

	// right side of the mainFrame
	TasksWindow tasksWindow;

	/*
	 * two radio buttons inside topPanel1: - dbxLoginRadioButton: draw screen
	 * for Dbx Login - irodsLoginRadioButton: draw screen for RODS login
	 */
	private JRadioButton dbxLoginRadioButton, irodsLoginRadioButton;

	// ------------------------------------------------------------------------
	// fields specific to Dbx functionality and GUI
	// ------------------------------------------------------------------------
	DropboxLoginForm dropboxLoginForm;

	// Dbx specific titles for topPanel1 and topPanel2
	private TitledBorder title1, title3;

	// ------------------------------------------------------------------------
	// fields specific to RODS functionality and GUI
	// ------------------------------------------------------------------------
	RodsLoginForm rodsLoginForm;

	// specific titles for topPanel1 and topPanel2
	private TitledBorder title2, title4;

	public void run(String arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				System.out.println(javax.swing.SwingUtilities
						.isEventDispatchThread());
				drawGUI();
				assignActionListeners();
			}
		});
	}

	private void drawGUI() {
		// mainFrame = loginWindow + tasksWindow
		mainFrame = new JFrame();
		mainFrame.setLayout(new FlowLayout());
		mainFrame.setTitle("CloudConnect - MyCloudJ");
		mainFrame.setSize(1300, 520);
		mainFrame.setPreferredSize(new Dimension(1300, 500));
		mainFrame.setResizable(false);
		mainFrame.setLocationRelativeTo(null); // center the mainFrame

		Border blackline = BorderFactory.createLineBorder(Color.black);
		title1 = BorderFactory.createTitledBorder(blackline, "Dropbox Connect");
		title2 = BorderFactory.createTitledBorder(blackline, "iRODS Connect");
		title3 = BorderFactory.createTitledBorder(blackline, "Dropbox Tasks");
		title4 = BorderFactory.createTitledBorder(blackline, "iRODS Tasks");

		// left panel of the mainFrame
		loginWindow = new JPanel();
		loginWindow.setLayout(new BoxLayout(loginWindow, BoxLayout.PAGE_AXIS));
		loginWindow.setBorder(title1);

		// right panel of the mainFrame
		tasksWindow = new TasksWindow();
		tasksWindow.draw();
		tasksWindow.setTitle(title3);
		tasksWindow.reset();

		// Radio buttons for selecting the service to connect.
		dbxLoginRadioButton = new JRadioButton("Connect to Dropbox");
		dbxLoginRadioButton.setSelected(true);
		irodsLoginRadioButton = new JRadioButton("Connect to iRODS");
		ButtonGroup serviceToLogin = new ButtonGroup();
		serviceToLogin.add(dbxLoginRadioButton);
		serviceToLogin.add(irodsLoginRadioButton);
		// place the radio buttons inside a JPanel
		JPanel lPanel0 = new JPanel(new FlowLayout());
		lPanel0.add(dbxLoginRadioButton);
		lPanel0.add(irodsLoginRadioButton);
		loginWindow.add(lPanel0);

		// add the dropbox login form to the mainFrame
		dropboxLoginForm = new DropboxLoginForm();
		dropboxLoginForm.draw();
		loginWindow.add(new JPanel(new FlowLayout()));
		loginWindow.add(dropboxLoginForm.getlPanelDbSpecific());

		// add the iRODS login form to the mainFrame
		rodsLoginForm = new RodsLoginForm();
		rodsLoginForm.draw();
		loginWindow.add(rodsLoginForm.getlPanelRodsSpecific());

		mainFrame.add(loginWindow);
		mainFrame.add(tasksWindow.getPanel());
		mainFrame.setVisible(true);
	}

	private void assignActionListeners() {
		tasksWindow.getBtnFileChooser1().addActionListener(
				new BtnFileChooser1Listener());
		dbxLoginRadioButton.addActionListener(new BtnDbxLoginRadioListener());
		irodsLoginRadioButton
				.addActionListener(new BtnRodsLoginRadioListener());
		rodsLoginForm.getLoginRodsButton().addActionListener(
				new BtnConnectRodsListener());
		rodsLoginForm.getDisconnectButton().addActionListener(
				new BtnDisConnectRodsListener());
		dropboxLoginForm.getBtnConnect().addActionListener(
				new BtnDbxConnectListener());
		dropboxLoginForm.getDisconnectButton().addActionListener(
				new BtnDisConnectDbxListener());
		dropboxLoginForm.getAccessDbxButton().addActionListener(
				new BtnDbxAccessListener());
		tasksWindow.getBtnFileChooser2().addActionListener(
				new BtnFileChooser2Listener());
		tasksWindow.getUploadRadioButton().addActionListener(
				new BtnUploadRadioListener());
		tasksWindow.getDownloadRadioButton().addActionListener(
				new BtnDownloadRadioListener());
		tasksWindow.getBtnStart().addActionListener(new BtnStartListener());
	}

	private void buildFileSelectionTrees(String cloudHomeDirectoryPath,
			String localHomeDirectoryPath) throws CloudException {

		// build file browsing trees for cloud
		cloudFileTree = new CloudFileTree(cloudHomeDirectoryPath, cloudHandler);
		// build file browsing trees for local files
		localFileTree = new LocalFileTree(localHomeDirectoryPath);
	}

	public void initializeTransferThreads(CloudOperations cloudHandler,
			TasksWindow tasksWindow) {
		// start the thread for downloading
		downloadExecutor = new DownloadExecutor(cloudHandler,
				tasksWindow.getProgressTableModel());

		// start the thread for uploading
		uploadExecutor = new UploadExecutor(cloudHandler, cloudFileTree,
				tasksWindow.getProgressTableModel());

		deleteExecutor = new DeleteExecutor(cloudHandler, cloudFileTree,
				tasksWindow.getProgressTableModel());

		// TODO: solve this ugly dependancy between tasksWindow and executors
		tasksWindow.setDownloadExecutor(downloadExecutor);
		tasksWindow.setUploadExecutor(uploadExecutor);
		tasksWindow.setDeleteExecutor(deleteExecutor);
		cloudFileTree.setDeleteExecutor(deleteExecutor);
	}

	public void genericCloudDisconnect() {
		// free resources
		terminateTransferThreads();
		freeCloudResources();

		// reset right panel components
		disableJTreeGUI();
		tasksWindow.reset();

		userIsConnected = false;
	}

	public void terminateTransferThreads() {
		downloadExecutor.terminateAllTransfers();
		uploadExecutor.terminateAllTransfers();
		deleteExecutor.terminateAllTransfers();
	}

	private void freeCloudResources() {
		try {
			cloudHandler.disconnect();
		} catch (CloudException e1) {
			JOptionPane.showMessageDialog(mainFrame, "Error",
					e1.getCloudError(), JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}

	private void disableJTreeGUI() {
		JFrame enclosingFrame = cloudFileTree.getEnclosingFrame();
		if (enclosingFrame != null)
			enclosingFrame.dispose();
	}

	// A lot of Action Listeners designed as inside classes
	class BtnDbxLoginRadioListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (userIsConnected) {
				Object[] message = { "Are you sure you want to connect to Dropbox?\nYou will be disconnected from iRODS" };

				int option = JOptionPane.showConfirmDialog(null, message,
						"Confirm", JOptionPane.OK_CANCEL_OPTION);

				if (option == JOptionPane.OK_OPTION) { // switch to Dropbox
					genericCloudDisconnect();
					rodsLoginForm.reset();
				} else { // cancel the switch to Dropbox
					irodsLoginRadioButton.setSelected(true);
					return;
				}
			}

			cloudHandler = new DropboxOperations();
			loginWindow.setBorder(title1);
			rodsLoginForm.setVisible(false);
			dropboxLoginForm.setVisible(true);
			tasksWindow.setTitle(title3);
		}
	}

	class BtnRodsLoginRadioListener implements ActionListener {
		/**
		 * ActionListener for the "Connect to iRODS" button - initialize the
		 * cloud handler with an iRODS object - enable the screen for entering
		 * the credentials for the iRODS
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (userIsConnected) {
				Object[] message = { "Are you sure you want to connect to iRODS?\nYou will be disconnected from Dropbox" };
				int option = JOptionPane.showConfirmDialog(null, message,
						"Confirm", JOptionPane.OK_CANCEL_OPTION);

				if (option == JOptionPane.OK_OPTION) { // switch to iRODS
					// reset left panel components
					genericCloudDisconnect();
					dropboxLoginForm.reset();
				} else { // cancel the switch to iRODS
					dbxLoginRadioButton.setSelected(true);
					return;
				}
			}

			cloudHandler = new RodsOperations();
			loginWindow.setBorder(title2);
			rodsLoginForm.setVisible(true);
			dropboxLoginForm.setVisible(false);
			tasksWindow.setTitle(title4);
		}
	}

	class BtnDbxAccessListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				cloudHandler.login();
				dropboxLoginForm.prepareToConnect();
			} catch (CloudException e4) {
				JOptionPane.showMessageDialog(mainFrame, e4.getMessage(),
						"MyCLoudJ - Access Error", JOptionPane.ERROR_MESSAGE);
				e4.printStackTrace();
			}
		}
	}

	class BtnDbxConnectListener implements ActionListener {
		String dbxAccessCode;

		@Override
		public void actionPerformed(ActionEvent e) {
			DropboxOperations dbxUtility = (DropboxOperations) cloudHandler;

			try {
				dbxAccessCode = dropboxLoginForm.getAccesssCode();

				if (!userIsConnected && !dbxAccessCode.equals("")) {
					// connect user to dropbox
					dbxUtility.DbxLinkUser(dbxAccessCode);
					userIsConnected = true;
					dropboxLoginForm.setConnected();

					// display user info in a text area
					displayUserInfo(dbxUtility);

					// prepare the file browsing trees
					cloudHomeDirectoryPath = cloudHandler.getHomeDirectory();
					buildFileSelectionTrees(cloudHomeDirectoryPath,
							LOCAL_HOME_DIRECTORY_PATH);

					// start transfer threads
					initializeTransferThreads(cloudHandler, tasksWindow);

					// enable the right panel
					tasksWindow.enable();
				} else if (!userIsConnected && dbxAccessCode.equals(""))
					JOptionPane.showMessageDialog(mainFrame,
							"Enter Access Code !",
							"MyCLoudJ - Enter Access code",
							JOptionPane.WARNING_MESSAGE);
			} catch (CloudException e1) {
				dropboxLoginForm.setErrorStatus(e1.getCloudError());
				e1.printStackTrace();
			}
		}
	}

	public void displayUserInfo(DropboxOperations dbxUtility) {
		String userName, country, userQuota;
		userName = dbxUtility.getUserName();
		country = dbxUtility.getCountry();
		userQuota = dbxUtility.getUserQuota();
		dropboxLoginForm.setConnected("Connected as " + userName);
		dropboxLoginForm.setUserInfo("Username: " + userName + "\nCountry: "
				+ country + "\nQuota: " + userQuota + " GB");
	}

	class BtnDisConnectDbxListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			genericCloudDisconnect();
			dropboxLoginForm.reset();
		}
	}

	class BtnConnectRodsListener implements ActionListener {
		private String host, zone, resource, user, password;
		int port;

		@Override
		public void actionPerformed(ActionEvent e) {
			RodsOperations rodsUtilsObj = (RodsOperations) cloudHandler;

			/*
			 * TESTING -temporary solution for not entering the credentials for
			 * every run
			 */
			rodsUtilsObj.setUsername("rods");
			rodsUtilsObj.setIrodsPassword("rods");
			rodsUtilsObj.setHost("irods-dev.incf.org");
			rodsUtilsObj.setPort(1247);
			rodsUtilsObj.setZone("BragadiruZone");
			rodsUtilsObj.setRes("");

			try {
				// checkLoginCredentials();
				// rodsUtilsObj.setCredentials(user, password, host, port, zone,
				// resource);

				rodsUtilsObj.login();
				userIsConnected = true;
				disableRodsLoginForm();

				cloudHomeDirectoryPath = cloudHandler.getHomeDirectory();
				buildFileSelectionTrees(cloudHomeDirectoryPath,
						LOCAL_HOME_DIRECTORY_PATH);
			} catch (CloudException e1) {
				rodsLoginForm.setErrorStatus("Error connecting to iRODS!");
				e1.printStackTrace();
				JOptionPane.showMessageDialog(mainFrame, e1.getCloudError(),
						"Login error", JOptionPane.WARNING_MESSAGE);
				return;
			}
			initializeTransferThreads(cloudHandler, tasksWindow);

			rodsLoginForm.setConnected(true);
			tasksWindow.enable();
		}

		private void disableRodsLoginForm() {
			rodsLoginForm.disable();
		}

		public void checkLoginCredentials() throws CloudException {
			String messages = "";

			host = rodsLoginForm.getRodsHost();
			zone = rodsLoginForm.getRodsZone();
			resource = rodsLoginForm.getRodsRes();
			user = rodsLoginForm.getUserName();
			password = rodsLoginForm.getRodsPassword();
			String portString = rodsLoginForm.getRodsHostPort();

			if (host.length() == 0)
				messages = messages.concat("- iRods Host cannot be empty\n");
			if (zone.length() == 0)
				messages = messages.concat("- iRods Zone cannot be empty\n");
			if (portString.length() == 0)
				messages = messages.concat("- iRods Port cannot be empty\n");
			else
				try {
					port = Integer.parseInt(portString);
				} catch (NumberFormatException e) {
					messages = messages
							.concat("- the iRODS port value is wrong\n");
				}
			if (user.length() == 0)
				messages = messages.concat("- iRods user cannot be empty\n");
			if (password.length() == 0)
				messages = messages.concat("- iRods Pass cannot be empty\n");

			if (messages.length() > 0)
				throw (new CloudException(
						messages = "The login failed because:\n\n"
								.concat(messages)));
		}
	}

	class BtnDisConnectRodsListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			genericCloudDisconnect();
			rodsLoginForm.reset();
		}
	}

	class BtnDownloadRadioListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			tasksWindow.resetSelectionPaths();
			if (cloudFileTree.getEnclosingFrame() != null) {
				cloudFileTree.getEnclosingFrame().dispose();
				// TODO: instead of setting the frame to null
				// overwrite the dispose method
				cloudFileTree.setEnclosingFrame(null);
			}
		}
	}

	class BtnUploadRadioListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			tasksWindow.resetSelectionPaths();
			if (cloudFileTree.getEnclosingFrame() != null) {
				cloudFileTree.getEnclosingFrame().dispose();
				// TODO: instead of setting the frame to null
				// overwrite the dispose method
				cloudFileTree.setEnclosingFrame(null);
			}
		}
	}

	class BtnFileChooser1Listener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// upload file to cloud
			if (tasksWindow.getUploadRadioButton().isSelected()) {
				localFileTree.openSelectionGUI(false,
						localFileTree.getSelectedFilePath());
				tasksWindow.setSourcePath(localFileTree.getSelectedFilePath());
			}
			// download file from cloud
			else if (tasksWindow.getDownloadRadioButton().isSelected()) {
				if (cloudFileTree.getEnclosingFrame() == null) {
					cloudFileTree.createEnclosingFrameDownload();
					cloudFileTree.getExpandButton().addActionListener(
							new BtnExpandDownloadTreeListener());
					cloudFileTree.getSelectButton().addActionListener(
							new BtnSelectListener());
					cloudFileTree.getCancelButton().addActionListener(
							new BtnCancelListener());
				} else {
					cloudFileTree.getEnclosingFrame().toFront();
				}
			}
		}
	}

	class BtnFileChooser2Listener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// upload file to cloud
			if (tasksWindow.getUploadRadioButton().isSelected()) {
				// check if the frame is already openened
				if (cloudFileTree.getEnclosingFrame() == null) {
					cloudFileTree.createEnclosingFrameUpload();
					cloudFileTree.getExpandButton().addActionListener(
							new BtnExpandUploadTreeListener());
					cloudFileTree.getSelectButton().addActionListener(
							new BtnSelect2Listener());
					cloudFileTree.getCancelButton().addActionListener(
							new BtnCancelListener());
				} else {
					cloudFileTree.getEnclosingFrame().toFront();
				}
				// download file from cloud
			} else if (tasksWindow.getDownloadRadioButton().isSelected()) {
				localFileTree.openSelectionGUI(true,
						localFileTree.getSelectedFilePath());
				tasksWindow.setDestinationPath(localFileTree
						.getSelectedFilePath());
				tasksWindow.disableDestinationPath();
			}
		}
	}

	class BtnSelectListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ae) {
			String selectedNodePath = cloudFileTree
					.getSelectedNodePathDownloadTree();

			tasksWindow.setSourcePath(selectedNodePath);
			cloudFileTree.getEnclosingFrame().dispose();
			cloudFileTree.setEnclosingFrame(null);
		}
	}

	class BtnSelect2Listener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// Get the latest node selected
			String selectedNodePath = cloudFileTree
					.getSelectedNodePathUploadTree();
			tasksWindow.setDestinationPath(selectedNodePath);
			cloudFileTree.getEnclosingFrame().dispose();
			cloudFileTree.setEnclosingFrame(null);
		}
	}

	class BtnExpandDownloadTreeListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent aE) {
			try {
				cloudFileTree.expandDownloadTree();
			} catch (CloudException e) {
				// TODO: Display a error for the user inside the browse box
				e.printStackTrace();
				return;
			}
			cloudFileTree.getEnclosingFrame().pack();
		}
	}

	class BtnExpandUploadTreeListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ae) {
			try {
				cloudFileTree.expandUploadTree();
			} catch (CloudException e) {
				// TODO: Display a error for the user inside the browse box
				e.printStackTrace();
				return;
			}
			cloudFileTree.getEnclosingFrame().pack();
		}
	}

	class BtnCancelListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			cloudFileTree.getEnclosingFrame().dispose();
		}
	}

	class BtnStartListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String sourcePath = tasksWindow.getSourcePath();
			String destinationPath = tasksWindow.getDestinationPath();

			if (sourcePath.equals("") || destinationPath.equals("")) {
				JOptionPane.showMessageDialog(mainFrame,
						"Select the files/folder to upload/download", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			try {
				if (tasksWindow.getUploadRadioButton().isSelected()) {
					TransferTask task = new TransferTask(sourcePath,
							destinationPath);
					uploadExecutor.addTask(task);
				} else if (tasksWindow.getDownloadRadioButton().isSelected()) {
					TransferTask task = new TransferTask(sourcePath,
							destinationPath);
					downloadExecutor.addTask(task);
				}
			} catch (FileTransferException e1) {
				JOptionPane.showMessageDialog(mainFrame, e1.getError(),
						"Limit reached", JOptionPane.WARNING_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
}