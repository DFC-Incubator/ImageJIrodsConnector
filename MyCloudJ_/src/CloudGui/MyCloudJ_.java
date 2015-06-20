package CloudGui;

import ij.plugin.PlugIn;

import java.awt.Color;
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
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import cloud_interfaces.CloudException;
import cloud_interfaces.CloudOperations;
import rods_backend.RodsOperations;
import dropbox_backend.DropboxOperations;
import file_transfer_backend.DownloadThread;
import file_transfer_backend.UploadThread;

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
		drawGUI();
		assignActionListeners();
	}

	private void drawGUI() {
		// mainFrame = loginWindow + tasksWindow
		mainFrame = new JFrame();
		mainFrame.setLayout(new FlowLayout());
		mainFrame.setTitle("CloudConnect - MyCloudJ");
		mainFrame.setSize(1200, 450);
		mainFrame.setResizable(false);
		mainFrame.setLocationRelativeTo(null); // center the mainFrame

		Border blackline = BorderFactory.createLineBorder(Color.black);
		title1 = BorderFactory.createTitledBorder(blackline, "Dropbox Connect");
		title2 = BorderFactory.createTitledBorder(blackline, "iRODS Connect");
		title3 = BorderFactory.createTitledBorder(blackline, "Dropbox Tasks");
		title4 = BorderFactory.createTitledBorder(blackline, "iRODS Tasks");

		// left panel of the mainFrame
		loginWindow = new JPanel();
		loginWindow.setLayout(new BoxLayout(loginWindow,
				BoxLayout.PAGE_AXIS));
		loginWindow.setBorder(title1);

		// right panel of the mainFrame
		tasksWindow = new TasksWindow();
		tasksWindow.draw();
		tasksWindow.setTitle(title3);
		tasksWindow.resetAndDisable();
		
		//Radio buttons for selecting the service to connect.
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

	private void buildFileSelectionTrees(String cloudHomeDirectoryPath,
			String localHomeDirectoryPath) throws CloudException {

		// build file browsing trees for cloud
		cloudFileTree = new CloudFileTree(cloudHomeDirectoryPath, cloudHandler);
		// build file browsing trees for local files
		localFileTree = new LocalFileTree(localHomeDirectoryPath);
	}

	private void assignActionListeners() {
		tasksWindow.getBtnFileChooser1().addActionListener(
				new BtnFileChooser1Listener());
		dbxLoginRadioButton.addActionListener(new BtnDbxLoginRadioListener());
		irodsLoginRadioButton
				.addActionListener(new BtnRodsLoginRadioListener());
		rodsLoginForm.getLoginRodsButton().addActionListener(
				new BtnConnectRodsListener());
		dropboxLoginForm.getBtnConnect().addActionListener(
				new BtnDbxConnectListener());
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

	// A lot of Action Listeners designed as inside classes
	class BtnDbxLoginRadioListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (userIsConnected) {
				Object[] message = { "Are you sure you want to connect to Dropbox?\nYou will be disconnected from iRODS" };

				int option = JOptionPane.showConfirmDialog(null, message,
						"Confirm", JOptionPane.OK_CANCEL_OPTION);

				// switch to Dropbox
				if (option == JOptionPane.OK_OPTION) {
					userIsConnected = false;

					disableRodsGUI();
					disableJTreeGUI();
					tasksWindow.resetAndDisable();
					freeCloudResources();
					// cancel the switch to Dropboxs
				} else {
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

		private void disableRodsGUI() {
			rodsLoginForm.resetAndEnable();
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

				// switch to iRODS
				if (option == JOptionPane.OK_OPTION) {
					userIsConnected = false;

					disableDbxGUI();
					disableJTreeGUI();
					tasksWindow.resetAndDisable();
					freeCloudResources();
					// cancel the switch to iRODS
				} else {
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

		private void disableDbxGUI() {
			dropboxLoginForm.disable();
		}
	}

	private void disableJTreeGUI() {
		JFrame enclosingFrame = cloudFileTree.getEnclosingFrame();
		if (enclosingFrame != null)
			enclosingFrame.dispose();
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

	class BtnDbxAccessListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// if user if not connected, then execute if block
			if (!userIsConnected) {
				try {
					cloudHandler.login();
					dropboxLoginForm.setEnabledAccessCodeField(true);
					dropboxLoginForm.getBtnConnect().setEnabled(true);
				} catch (CloudException e4) {
					JOptionPane.showMessageDialog(mainFrame, e4.getMessage(),
							"MyCLoudJ - Access Error",
							JOptionPane.ERROR_MESSAGE);
					e4.printStackTrace();
				}
			}
			// If userStatus=1 (is already connected), no need to use connect
			// connect button, warning for user
			else {
				JOptionPane.showMessageDialog(mainFrame, "Already connected !",
						"MyCLoudJ - Already Connected",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	class BtnDbxConnectListener implements ActionListener {
		String userName, country, userQuota;
		String dbxAccessCode;

		@Override
		public void actionPerformed(ActionEvent e) {
			DropboxOperations dbxUtility = (DropboxOperations) cloudHandler;

			try {
				// retrieve the access code from textfield
				dbxAccessCode = dropboxLoginForm.getAccesssCode();

				// if user is previously not connected and access code is not
				// empty then connect it
				if (!userIsConnected && !dbxAccessCode.equals("")) {
					// connect user to dropbox
					dbxUtility.DbxLinkUser(dbxAccessCode);

					// user status changed to 1(i.e., connected)
					userIsConnected = true;

					/*
					 * Retrieve username, country and quota from dropbox account
					 * info API and print it in the text area for the user
					 */
					userName = dbxUtility.getUserName();
					country = dbxUtility.getCountry();
					userQuota = dbxUtility.getUserQuota();
					dropboxLoginForm.setStatus("Connected as " + userName);
					dropboxLoginForm.setUserInfo("Username: " + userName
							+ "\nCountry: " + country + "\nQuota: " + userQuota
							+ " GB");

					cloudHomeDirectoryPath = cloudHandler.getHomeDirectory();
					buildFileSelectionTrees(cloudHomeDirectoryPath,
							LOCAL_HOME_DIRECTORY_PATH);
					/*
					 * Disable the access code textfield and enable the the
					 * right panel(which contains the tasks section) after the
					 * user is connected.
					 */
					dropboxLoginForm.setEnabledAccessCodeField(false);
					
					// All the components of right window are enabled after
					// successful connection with user's dropbox account
					tasksWindow.enable();
				}
				// If user is already connected userStatus=1, warning for user
				else if (userIsConnected == true)
					JOptionPane.showMessageDialog(mainFrame,
							"Already connected !",
							"MyCLoudJ - Already Connected",
							JOptionPane.WARNING_MESSAGE);
				// If user is not connected but there is no access code,
				// information for user
				else if (!userIsConnected && dbxAccessCode.equals(""))
					JOptionPane.showMessageDialog(mainFrame,
							"Enter Access Code !",
							"MyCLoudJ - Enter Access code",
							JOptionPane.WARNING_MESSAGE);
			} catch (CloudException e1) {
				JOptionPane.showMessageDialog(mainFrame, e1.getCloudError(),
						"MyCLoudJ - Access Error", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
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
			// rodsUtilsObj.setUsername("rods");
			// rodsUtilsObj.setIrodsPassword("rods");
			// rodsUtilsObj.setHost("192.168.0.104");
			// rodsUtilsObj.setPort(1247);
			// rodsUtilsObj.setZone("BragadiruZone");
			// rodsUtilsObj.setRes("test1-resc");

			try {
				checkLoginCredentials();
				rodsUtilsObj.setCredentials(user, password, host, port, zone,
						resource);

				rodsUtilsObj.login();
				userIsConnected = true;
				disableRodsLoginForm();

				cloudHomeDirectoryPath = cloudHandler.getHomeDirectory();
				buildFileSelectionTrees(cloudHomeDirectoryPath,
						LOCAL_HOME_DIRECTORY_PATH);
			} catch (CloudException e1) {
				rodsLoginForm.setStatus("Error connecting to iRODS!");
				e1.printStackTrace();
				JOptionPane.showMessageDialog(mainFrame, e1.getCloudError(),
						"Login error", JOptionPane.WARNING_MESSAGE);
				return;
			}
			rodsLoginForm.setStatus("Connected to iRODS");
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
					messages = messages.concat("- the iRODS port value is wrong\n");
				}
			if (user.length() == 0)
				messages = messages.concat("- iRods user cannot be empty\n");
			if (password.length() == 0)
				messages = messages.concat("- iRods Pass cannot be empty\n");

			if (messages.length() > 0)
				throw (new CloudException(
						messages = "The login failed because:\n\n".concat(messages)));
		}
	}

	class BtnDownloadRadioListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			tasksWindow.resetSelectionPaths();
		}
	}

	class BtnUploadRadioListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			tasksWindow.resetSelectionPaths();
		}
	}

	class BtnFileChooser1Listener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// upload file to cloud
			if (tasksWindow.getUploadRadioButton().isSelected()) {
				localFileTree.openSelectionGUI(false);
				tasksWindow.setSourcePath(localFileTree.getSelectedFilePath());
			}
			// download file from cloud
			else if (tasksWindow.getDownloadRadioButton().isSelected()) {
				cloudFileTree.createEnclosingFrameDownload();
				cloudFileTree.getExpandButton().addActionListener(
						new BtnExpandDownloadTreeListener());
				cloudFileTree.getSelectButton().addActionListener(
						new BtnSelectListener());
				cloudFileTree.getCancelButton().addActionListener(
						new BtnCancelListener());
			}
		}
	}

	class BtnFileChooser2Listener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// upload file to cloud
			if (tasksWindow.getUploadRadioButton().isSelected()) {
				cloudFileTree.createEnclosingFrameUpload();
				cloudFileTree.getExpandButton().addActionListener(
						new BtnExpandUploadTreeListener());
				cloudFileTree.getSelectButton().addActionListener(
						new BtnSelect2Listener());
				cloudFileTree.getCancelButton().addActionListener(
						new BtnCancelListener());
				// download file from cloud
			} else if (tasksWindow.getDownloadRadioButton().isSelected()) {
				localFileTree.openSelectionGUI(true);
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
		}
	}

	class BtnSelect2Listener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// Get the latest node selected
			String selectedNodePath = cloudFileTree
					.getSelectedNodePathUploadTree();
			tasksWindow.setDestinationPath(selectedNodePath);
			cloudFileTree.getEnclosingFrame().dispose();
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
				tasksWindow.getLogger().writeLog("Error: Select the files/folder to upload/download\n\n");
				return;
			}

			if (tasksWindow.getUploadRadioButton().isSelected()) {
				UploadThread uploadThread = new UploadThread(cloudHandler, cloudFileTree, tasksWindow.getLogger());
				uploadThread.prepareForUpload(sourcePath, destinationPath);
				uploadThread.start();
			} else if (tasksWindow.getDownloadRadioButton().isSelected()) {
				DownloadThread downloadThread = new DownloadThread(cloudHandler, tasksWindow.getLogger());
				downloadThread.prepareForDownload(sourcePath, destinationPath);
				downloadThread.start();
			}
		}
	}
}