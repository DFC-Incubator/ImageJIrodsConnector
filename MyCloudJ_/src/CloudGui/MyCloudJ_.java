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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
 *               Node FileName : MyCloudJ_.java (package CloudConnect) ImageJ
 *               Plugin for CLoud Client.
 * @users : Image Processing Researchers (Neuroscientists etc.)
 * @motivation : To facilitate the sharing of datasets among ImageJ users
 * @technologies : Java, Dropbox Core APIs,Jargon API, Restful Web Services,
 *               Swing GUI Installation : Put the plugin/MyCloudJ_.jar to the
 *               plugins/ folder of the ImageJ. It will show up in the plugins
 *               when you run ImageJ. Requirements : ImageJ alongwith JRE 1.7 or
 *               later.
 */
public class MyCloudJ_ implements PlugIn {
	// ------------------------------------------------------------------------
	// commun functionality fields, specific both to Dbx and iRODS
	// ------------------------------------------------------------------------

	CloudFileTree cloudFileTree;
	LocalFileTree localFileTree;

	/**
	 * cloudHandler : generic interface for cloud operations
	 */
	private CloudOperations cloudHandler = new DropboxOperations();

	/**
	 * userIsConnected: true if user connected to cloud, false otherwise
	 */
	private boolean userIsConnected;

	/**
	 * after login, initialized to user home directory
	 */
	private String cloudHomeDirectoryPath;

	private final String LOCAL_HOME_DIRECTORY_PATH = ".";
	
	// ------------------------------------------------------------------------
	// commun GUI fields, specific both to Dbx and iRODS
	// ------------------------------------------------------------------------
	/**
	 * contains the whole GUI for the MyCloudJ_ plugin
	 */
	private JFrame mainFrame;

	/**
	 * @topPanel1: left side of the mainFrame, components used for connecting to
	 *             the cloud
	 * @topPanel2: right side of the mainFrame, components used for
	 *             downloading/uploading
	 */
	private JPanel mainLeftPanel, mainRightPanel;

	/**
	 * two radio buttons inside topPanel1:
	 * 
	 * @dbxLoginRadioButton: draw screen for Dbx Login
	 * @irodsLoginRadioButton: draw screen for RODS login
	 */
	private JRadioButton dbxLoginRadioButton, irodsLoginRadioButton;

	/**
	 * text for dbxLoginRadioButton
	 */
	private String dbLoginS = "Connect to Dropbox";

	/**
	 * text for irodsLoginRadioButton
	 */
	private String irodsLoginS = "Connect to iRODS";

	/**
	 * two radio buttons inside topPanel2
	 * 
	 * @uploadRadioButton: user will upload a file/folder to cloud
	 * @downloadRadioButton: user will download a file/folder from cloud
	 */
	private JRadioButton uploadRadioButton, downloadRadioButton;

	/**
	 * this holds the JTree node that is selected by the user for
	 * upload/download
	 */
	public Object node, parentNode;

	/**
	 * button to open file chooser for the source file
	 */
	private JButton btnFileChooser1;

	/**
	 * button to open the file chooser for the target file
	 */
	private JButton btnFileChooser2;

	/**
	 * source address
	 */
	private JTextField srcTxt;

	/**
	 * destination address
	 */
	private JTextField targetTxt;

	/**
	 * button to start download/upload
	 */
	private JButton btnStart;

	/**
	 * msgs will be used for displaying task related information to user
	 */
	private JTextArea logMessages;

	// ------------------------------------------------------------------------
	// fields specific to Dbx functionality and GUI
	// ------------------------------------------------------------------------
	
	DropboxLoginForm dropboxLoginForm;

	/**
	 * authorizeUrl : stores the Application Url
	 */
	private String dbxAccessCode;

	private String userName = "", country = "", userQuota = "";

	/**
	 * Dbx specific titles for topPanel1 and topPanel2
	 */
	private TitledBorder title1, title3;
	// ------------------------------------------------------------------------
	// fields specific to RODS functionality and GUI
	// ------------------------------------------------------------------------
	RodsLoginForm rodsLoginForm;
	
	/**
	 * RODS specific titles for topPanel1 and topPanel2
	 */
	private TitledBorder title2, title4;

	public void run(String arg) {
		drawGUI();
		assignActionListeners();
	}

	private void assignActionListeners() {
		btnFileChooser1.addActionListener(new BtnFileChooser1Listener());
		dbxLoginRadioButton.addActionListener(new BtnDbxLoginRadioListener());
		irodsLoginRadioButton
				.addActionListener(new BtnRodsLoginRadioListener());
		rodsLoginForm.getLoginRodsButton().addActionListener(new BtnConnectRodsListener());
		dropboxLoginForm.getBtnConnect().addActionListener(new BtnDbxConnectListener());
		dropboxLoginForm.getAccessDbxButton().addActionListener(new BtnDbxAccessListener());
		btnFileChooser2.addActionListener(new BtnFileChooser2Listener());
		/*
		 * Set source/target address to "" whenever radio button is changed from
		 * rButton1(upload) to rButtoon2(download) and vice versa.
		 */
		uploadRadioButton.addActionListener(new BtnUploadRadioListener());
		downloadRadioButton.addActionListener(new BtnDownloadRadioListener());
		btnStart.addActionListener(new BtnStartListener());
	}

	private void drawGUI() {
		mainFrame = new JFrame();
		mainFrame.setLayout(new FlowLayout());
		mainFrame.setTitle("CloudConnect - MyCloudJ");
		mainFrame.setSize(1200, 450);
		mainFrame.setResizable(false);
		// This will position the JFrame in the center of the screen
		mainFrame.setLocationRelativeTo(null);

		Border blackline = BorderFactory.createLineBorder(Color.black);
		title1 = BorderFactory.createTitledBorder(blackline, "Dropbox Connect");
		title2 = BorderFactory.createTitledBorder(blackline, "iRODS Connect");
		title3 = BorderFactory.createTitledBorder(blackline, "Dropbox Tasks");
		title4 = BorderFactory.createTitledBorder(blackline, "iRODS Tasks");

		// topPanel1
		mainLeftPanel = new JPanel();
		mainLeftPanel.setLayout(new BoxLayout(mainLeftPanel,
				BoxLayout.PAGE_AXIS));
		mainLeftPanel.setBorder(title1);

		// topPanel2
		mainRightPanel = new JPanel();
		mainRightPanel.setLayout(new BoxLayout(mainRightPanel,
				BoxLayout.PAGE_AXIS));
		mainRightPanel.setBorder(title3);

		logMessages = new JTextArea();
		logMessages.setLineWrap(true);
		logMessages.setWrapStyleWord(true);

		// First we'll add components to topPanel1. Then, we'll start with
		// topPanel2

		/*
		 * These panels will add into topPanel1 (Left side of the frame) lPanel0
		 * : To display connection option: Dropbox or iRODS
		 */
		JPanel lPanel0 = new JPanel(new FlowLayout());
		JPanel lPanelAlign = new JPanel(new FlowLayout());
		/*
		 * Add ButtonGroup : Radio buttons for selecting the service to connect.
		 * 
		 * Added onto panel0
		 */
		dbxLoginRadioButton = new JRadioButton(dbLoginS);
		dbxLoginRadioButton.setSelected(true);
		irodsLoginRadioButton = new JRadioButton(irodsLoginS);
		ButtonGroup serviceToLogin = new ButtonGroup();
		serviceToLogin.add(dbxLoginRadioButton);
		serviceToLogin.add(irodsLoginRadioButton);
		lPanel0.add(dbxLoginRadioButton);
		lPanel0.add(irodsLoginRadioButton);
		
		dropboxLoginForm = new DropboxLoginForm();
		dropboxLoginForm.draw();
		
		mainLeftPanel.add(lPanel0);
		mainLeftPanel.add(lPanelAlign);
		mainLeftPanel.add(dropboxLoginForm.getlPanelDbSpecific());
		
		/*
		 * Let's start working on topPanel2(Right side of the mainFrame)
		 */

		/*
		 * JPanels : To be added to topPanel2(Right side of mainFrame)
		 * 
		 * rPanel1 : Contains label "Tasks:" rPanel2 : Contains Radio buttons
		 * "Upload" and "Download".
		 * 
		 * Added to topPanel2
		 */
		JPanel rPanel1 = new JPanel(new FlowLayout());
		JPanel rPanel2 = new JPanel(new FlowLayout());
		JPanel rPanel3 = new JPanel(new FlowLayout());
		JPanel rPanel4 = new JPanel(new FlowLayout());
		JPanel rPanel5 = new JPanel(new FlowLayout());

		/*
		 * Add JLabel : "Tasks". Download/Upload
		 * 
		 * Added to rPanel1
		 */
		JLabel lblTasks;
		lblTasks = new JLabel("Tasks: ");
		rPanel1.add(lblTasks);

		uploadRadioButton = new JRadioButton("Upload");
		downloadRadioButton = new JRadioButton("Download", true);
		ButtonGroup group = new ButtonGroup();
		group.add(uploadRadioButton);
		group.add(downloadRadioButton);
		rPanel1.add(uploadRadioButton);
		rPanel1.add(downloadRadioButton);

		/*
		 * Add JLabel : "Source".
		 * 
		 * Added to rPanel2
		 */
		JLabel lblSrc;
		lblSrc = new JLabel("Source: ");
		rPanel2.add(lblSrc);

		srcTxt = new JTextField("", 25);
		srcTxt.setEditable(false);
		rPanel2.add(srcTxt);

		btnFileChooser1 = new JButton("Browse");
		rPanel2.add(btnFileChooser1);

		JLabel targetLbl = new JLabel("Target: ");
		rPanel3.add(targetLbl);

		targetTxt = new JTextField("", 25);
		targetTxt.setEditable(false);
		rPanel3.add(targetTxt);

		btnFileChooser2 = new JButton("Browse");
		rPanel3.add(btnFileChooser2);

		btnStart = new JButton("Start !");
		rPanel4.add(btnStart);

		/*
		 * JLabel : "Messages"
		 * 
		 * Added onto rPanel5
		 */
		JLabel lblMsg = new JLabel("Messages: ");
		rPanel5.add(lblMsg);

		/*
		 * JTextArea : For user's information (task related) Added onto rPanel5
		 * Added the scrollpane to text area
		 */
		JScrollPane msgsScrollPane = new JScrollPane(logMessages);
		msgsScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		msgsScrollPane.setPreferredSize(new Dimension(340, 220));
		rPanel5.add(msgsScrollPane);

		/*
		 * Add rPanels to topPanel2(right side)
		 */
		mainRightPanel.add(rPanel1);
		mainRightPanel.add(rPanel2);
		mainRightPanel.add(rPanel3);
		mainRightPanel.add(rPanel4);
		mainRightPanel.add(rPanel5);
		GuiUtils.enableComponentsFromContainer(mainRightPanel, false);
		
		rodsLoginForm = new RodsLoginForm();
		rodsLoginForm.draw();
		
		mainLeftPanel.add(rodsLoginForm.getlPanelRodsSpecific());
		mainFrame.add(mainLeftPanel);
		mainFrame.add(mainRightPanel);
		mainFrame.setVisible(true);

	}
	
	private void buildFileSelectionTrees(String cloudHomeDirectoryPath,
			String localHomeDirectoryPath) throws CloudException {

		// build file browsing trees for cloud
		cloudFileTree = new CloudFileTree(cloudHomeDirectoryPath, cloudHandler);
		// build file browsing trees for local files
		localFileTree = new LocalFileTree(localHomeDirectoryPath);
	}
	
	/*
	 * -------------------------
	 * A lot of Action Listeners
	 * -------------------------
	 */
	
	class BtnDbxLoginRadioListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (userIsConnected) {
				Object[] message = { "Are you sure you want to connect to Dropbox? \nYou will be disconnected from iRODS" };

				int option = JOptionPane.showConfirmDialog(null, message,
						"Confirm", JOptionPane.OK_CANCEL_OPTION);
				
				// switch to Dropbox
				if (option == JOptionPane.OK_OPTION) {
					userIsConnected = false;
					
					disableRodsGUI();
					disableJTreeGUI();
					freeCloudResources();
				// cancel the switch to Dropboxs
				} else {
					irodsLoginRadioButton.setSelected(true);
					return;
				}
			}

			cloudHandler = new DropboxOperations();
			mainLeftPanel.setBorder(title1);
			rodsLoginForm.setVisible(false);
			dropboxLoginForm.setVisible(true);
			mainRightPanel.setBorder(title3);
		}
		
		private void disableRodsGUI() {
			rodsLoginForm.restoreToOriginalState();
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
				Object[] message = { "Are you sure you want to connect to iRODS? \nYou will be disconnected from Dropbox" };
				int option = JOptionPane.showConfirmDialog(null, message,
						"Confirm", JOptionPane.OK_CANCEL_OPTION);
				
				// switch to iRODS
				if (option == JOptionPane.OK_OPTION) {
					userIsConnected = false;
					
					disableDbxGUI();
					disableJTreeGUI();
					freeCloudResources();
				// cancel the switch to iRODS
				} else {
					dbxLoginRadioButton.setSelected(true);
					return;
				}
			}
			
			cloudHandler = new RodsOperations();
			mainLeftPanel.setBorder(title2);
			rodsLoginForm.setVisible(true);
			dropboxLoginForm.setVisible(false);
			mainRightPanel.setBorder(title4);
		}
		
		private void disableDbxGUI() {
			dropboxLoginForm.disable();
		}
	}
	
	private void disableJTreeGUI() {
		JFrame enclosingFrame = cloudFileTree.getEnclosingFrame();
		if (enclosingFrame != null)
			enclosingFrame.dispose();
		
		srcTxt.setText("");
		targetTxt.setText("");
		logMessages.setText("");
		
		GuiUtils.enableComponentsFromContainer(mainRightPanel, false);		
	}
	
	private void freeCloudResources() {
		try {
			cloudHandler.disconnect();
		} catch (CloudException e1) {
			JOptionPane.showMessageDialog(mainFrame, "Error",
					e1.getCloudError(),
					JOptionPane.ERROR_MESSAGE);
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
					dropboxLoginForm.setUserInfo("Username: " + userName + "\nCountry: "
							+ country + "\nQuota: " + userQuota + " GB");

					cloudHomeDirectoryPath = cloudHandler.getHomeDirectory();
					buildFileSelectionTrees(cloudHomeDirectoryPath,
							LOCAL_HOME_DIRECTORY_PATH);
					/*
					 * Disable the access code textfield and enable the the
					 * right panel(which contains the tasks section) after the
					 * user is connected.
					 */
					dropboxLoginForm.setEnabledAccessCodeField(false);;
					// All the components of topPanel2 are enabled after
					// successful connection with user's dropbox account
					GuiUtils.enableComponentsFromContainer(mainRightPanel, true);
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
//			rodsUtilsObj.setUsername("rods");
//			rodsUtilsObj.setIrodsPassword("rods");
//			rodsUtilsObj.setHost("192.168.0.104");
//			rodsUtilsObj.setPort(1247);
//			rodsUtilsObj.setZone("BragadiruZone");
//			rodsUtilsObj.setRes("test1-resc");
			
			try {
				checkLoginCredentials();
				rodsUtilsObj.setCredentials(user, password, host, port, zone, resource);
				
				rodsUtilsObj.login();
				userIsConnected = true;
				disableRodsLoginForm();
			
				cloudHomeDirectoryPath = cloudHandler.getHomeDirectory();
				buildFileSelectionTrees(cloudHomeDirectoryPath,
						LOCAL_HOME_DIRECTORY_PATH);
			} catch (CloudException e1) {
				rodsLoginForm.setStatus("Error connecting to iRODS!");
				e1.printStackTrace();
				JOptionPane.showMessageDialog(mainFrame,
						e1.getCloudError(),
						"Login error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			rodsLoginForm.setStatus("Connected to iRODS");
			GuiUtils.enableComponentsFromContainer(mainRightPanel, true);
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
				messages = messages.concat("- iRods Host cannot be empty \n");
			if (zone.length() == 0)
				messages = messages.concat("- iRods Zone cannot be empty \n");
			if (portString.length() == 0) 
				messages = messages.concat("- iRods Port cannot be empty \n");
			else 
				try {
					port = Integer.parseInt(portString);
				} catch (NumberFormatException e) {
					messages = messages.concat("- the iRODS port value is wrong \n");
				}
			if (user.length() == 0)
				messages = messages.concat("- iRods user cannot be empty \n");
			if (password.length() == 0)
				messages = messages.concat("- iRods Pass cannot be empty \n");
			
			if (messages.length() > 0) 
				throw (new CloudException(messages = "The login failed because: \n\n".concat(messages)));
		}
	}
	
	class BtnDownloadRadioListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Reset source address to ""
			srcTxt.setText("");

			// Reset target address to ""
			targetTxt.setText("");
		}
	}

	class BtnUploadRadioListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Reset source address to ""
			srcTxt.setText("");

			// Reset target address to ""
			targetTxt.setText("");
		}
	}
	
	class BtnFileChooser1Listener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// upload file to cloud
			if (uploadRadioButton.isSelected()) {
				localFileTree.openSelectionGUI(false);
				srcTxt.setText(localFileTree.getSelectedFilePath());
			}
			// download file from cloud
			else if (downloadRadioButton.isSelected()) {
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
			if (uploadRadioButton.isSelected()) {
				cloudFileTree.createEnclosingFrameUpload();
				cloudFileTree.getExpandButton().addActionListener(
						new BtnExpandUploadTreeListener());
				cloudFileTree.getSelectButton().addActionListener(
						new BtnSelect2Listener());
				cloudFileTree.getCancelButton().addActionListener(
						new BtnCancelListener());
			//download file from cloud
			} else if (downloadRadioButton.isSelected()) {
				localFileTree.openSelectionGUI(true);
				targetTxt.setText(localFileTree.getSelectedFilePath());
				targetTxt.setEditable(false);
			}
		}
	}

	class BtnSelectListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ae) {
			String selectedNodePath = cloudFileTree
					.getSelectedNodePathDownloadTree();

			srcTxt.setText(selectedNodePath);
			cloudFileTree.getEnclosingFrame().dispose();
		}
	}
	
	class BtnSelect2Listener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// Get the latest node selected
			String selectedNodePath = cloudFileTree
					.getSelectedNodePathUploadTree();
			targetTxt.setText(selectedNodePath);
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
			final String sourcePath = srcTxt.getText();
			final String destinationPath = targetTxt.getText();

			if (sourcePath.equals("") || destinationPath.equals("")) {
				logMessages.append("Error: Select the files/folder to upload/download\n\n");
				return;
			}
			
			if (uploadRadioButton.isSelected()) {
				UploadThread uploadThread = new UploadThread(cloudHandler, cloudFileTree, logMessages);
				uploadThread.prepareForUpload(sourcePath, destinationPath);
				uploadThread.start();
			}
			else if (downloadRadioButton.isSelected()) {
				DownloadThread downloadThread = new DownloadThread(cloudHandler, logMessages);
				downloadThread.prepareForDownload(sourcePath, destinationPath);
				downloadThread.start();
			}
		}
	}
}