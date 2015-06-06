package CloudGui;

import general.JTextFieldLimit;
import ij.plugin.PlugIn;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import javax.swing.JPasswordField;
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
	/**
	 * enabled if dbxLoginRadioButton is selected contains components for
	 * entering Dbx credentials
	 */
	private JPanel lPanelDbSpecific;

	/**
	 * instructions: displayed in the Dbx screen
	 */
	private String displayInstructions = "";
	private String heading = "  Instructions : \n \n";
	private String step1 = "  1. Click the \"Access Dropbox !\" button below. It will open the Dropbox app URL in the default browser.\n \n";
	private String step2 = "  2. Next, Sign-in to the Dropbox account and allow the MyCloudJ App.\n \n";
	private String step3 = "  3. On clicking the \"Allow\" button, Dropbox will generate an access code.\n \n";
	private String step4 = "  4. Copy the \"access code\" and paste it in the text field below.\n \n";
	private String step5 = "  5. Click the \"Connect !\" button. You can now access Dropbox.\n \n";
	private String note1 = "  Note: Enter the correct access code!";

	/**
	 * This is used to open the Application Url in the default browser.
	 */
	private JButton accessDbxButton;

	/**
	 * this is where user has to paste the Dbx Access Code. Plugin can only be
	 * connected if user enters the correct "Access Code" and clicks "Connect"
	 * button
	 * 
	 * Note : Initially disabled.
	 */
	private JTextField dbxAccessCodeTextField;

	/**
	 * authorizeUrl : stores the Application Url
	 */
	private String dbxAccessCode;

	/**
	 * "Connect to Dropbox button". Users need to click this button, once they
	 * paste the access code in the textfield
	 * 
	 * Note : Intially disabled.
	 */
	private JButton btnConnect;
	
	/**
	 * This label will display the connection status of the plugin along with
	 * username (if connected) Status Format : Connected as <username> or Not
	 * Connected!
	 * 
	 * Note : Initial status "Not Connected!"
	 */
	private JLabel dbxLblConnectionStatus;

	/**
	 * User's Dropbox information: user name + user country + user quota (GB)
	 */
	private JTextArea userInfo;
	private String userName = "", country = "", userQuota = "";

	/**
	 * Dbx specific titles for topPanel1 and topPanel2
	 */
	private TitledBorder title1, title3;
	// ------------------------------------------------------------------------
	// fields specific to RODS functionality and GUI
	// ------------------------------------------------------------------------
	/**
	 * enabled if rodsLoginRadioButton is selected contains components for
	 * entering Dbx credentials
	 */
	private JPanel lPanelRodsSpecific;

	/**
	 * rods credentials
	 */
	private JTextField user, rodsPassword, rodsHost, rodsHostPort, rodsZone,
			rodsRes;
	
	/**
	 * This label will display the connection status of the plugin along with
	 * username (if connected) Status Format : Connected as <username> or Not
	 * Connected!
	 * 
	 * Note : Initial status "Not Connected!"
	 */
	private JLabel rodsLblConnectionStatus;

	/**
	 * button to start the login process
	 */
	private JButton loginRodsButton;

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
		loginRodsButton.addActionListener(new BtnConnectRodsListener());
		btnConnect.addActionListener(new BtnDbxConnectListener());
		accessDbxButton.addActionListener(new BtnDbxAccessListener());
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
		 * lPanelDbSpecific : This panel contains Dropbox specific elements
		 * contained in the next five labels lPanel1 : To display Instructions
		 * lPanel2 : Access Dropbox Button lPanel3 : Access code label, Access
		 * Code JTextField and Connect button lPanel4 : User Status Label:
		 * Connected as <username> or Not Connected lPanel5 : To display dropbox
		 * related information: <username>, <country> and <user quota(in GBs)>
		 * 
		 * Note : lPanelDbSpecific will be added into topPanel1(left side of the
		 * mainFrame)
		 */
		lPanelDbSpecific = new JPanel(new FlowLayout());
		JPanel lPanel1 = new JPanel(new FlowLayout());
		JPanel lPanel2 = new JPanel(new FlowLayout());
		JPanel lPanel3 = new JPanel(new FlowLayout());
		JPanel lPanel4 = new JPanel(new FlowLayout());
		JPanel lPanel5 = new JPanel(new FlowLayout());

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

		/*
		 * Add JTextArea : This text area is used to display instructions for
		 * the new users.
		 * 
		 * Added onto panel1
		 */
		displayInstructions = heading + step1 + step2 + step3 + step4 + step5
				+ note1;
		JTextArea instructions = new JTextArea(displayInstructions);
		instructions.setEditable(false);
		lPanel1.add(instructions);

		accessDbxButton = new JButton("Access Dropbox  !");
		lPanel2.add(accessDbxButton);

		/*
		 * Add JLabel : "Access Code".
		 * 
		 * Added onto panel3
		 */
		JLabel lbl1;
		lbl1 = new JLabel("Dropbox Access Code: ");
		lPanel3.add(lbl1);

		dbxAccessCodeTextField = new JTextField(25);
		dbxAccessCodeTextField.setText(null);
		dbxAccessCodeTextField.setEnabled(false);
		lPanel3.add(dbxAccessCodeTextField);

		btnConnect = new JButton("Connect !");
		btnConnect.setEnabled(false);
		lPanel3.add(btnConnect);

		/*
		 * Add JLabel for user status -> connected or not connected. This label
		 * will display the connection status of the plugin along with username
		 * (if connected) Status Format : Connected as <username> or Not
		 * Connected!
		 * 
		 * Added onto panel4
		 * 
		 * Note : Intial status "Not Connected !"
		 */
		dbxLblConnectionStatus = new JLabel("Not Connected!");
		lPanel4.add(dbxLblConnectionStatus);

		userInfo = new JTextArea("\n\n");
		userInfo.setEditable(false);
		lPanel5.add(userInfo);

		/*
		 * Event Handling for btnConnect. This handles the complete set set of
		 * events that has to be executed after user presses the "Connect"
		 * button.
		 */

		/*
		 * Added all the components related to connection in the topPanel1(Left
		 * side of the mainFrame).
		 */
		mainLeftPanel.add(lPanel0);
		mainLeftPanel.add(lPanelAlign);
		lPanelDbSpecific.add(lPanel1);
		lPanelDbSpecific.add(lPanel2);
		lPanelDbSpecific.add(lPanel3);
		lPanelDbSpecific.add(lPanel4);
		lPanelDbSpecific.add(lPanel5);
		lPanelDbSpecific.setLayout(new BoxLayout(lPanelDbSpecific,
				BoxLayout.Y_AXIS));
		mainLeftPanel.add(lPanelDbSpecific);

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

		// Initially all the components of topPanel2 are disabled. It is enabled
		// after successful connection with user's dropbox account
		setEnabledAll(mainRightPanel, false);

		/*
		 * lPanelRodsSpecific : This panel contains Dropbox specific elements
		 * contained in the next five labels lPanel6 : iRODS user lPanel7 :
		 * iRODS password lPanel8 : iRODS Host lPanel9 : iRODS Port lPanel10 :
		 * iRODS Zone lPanel11 : iRODS default resource
		 * 
		 * Note : lPanelRodsSpecific will be added into topPanel1(left side of
		 * the mainFrame)
		 */
		lPanelRodsSpecific = new JPanel(new FlowLayout());
		JPanel lPanel6 = new JPanel(new FlowLayout());
		JPanel lPanel7 = new JPanel(new FlowLayout());
		JPanel lPanel8 = new JPanel(new FlowLayout());
		JPanel lPanel9 = new JPanel(new FlowLayout());
		JPanel lPanel10 = new JPanel(new FlowLayout());
		JPanel lPanel11 = new JPanel(new FlowLayout());
		JPanel lPanel12 = new JPanel(new FlowLayout());
		JPanel lPanel13 = new JPanel(new FlowLayout());

		int maxCharsTextField = 25;
		int maxColumnsTextField = 19;
		
		JLabel labelRodsUser;
		labelRodsUser = new JLabel("iRods User:  ");
		lPanel6.add(labelRodsUser);
		user = new JTextField(maxColumnsTextField);
		user.setDocument(new JTextFieldLimit(maxCharsTextField));
		user.setText(null);
		lPanel6.add(user);

		JLabel labelRodsPassword;
		labelRodsPassword = new JLabel("iRods Pass:  ");
		lPanel7.add(labelRodsPassword);
		rodsPassword = new JPasswordField(maxColumnsTextField);
		rodsPassword.setDocument(new JTextFieldLimit(maxCharsTextField));
		rodsPassword.setText(null);
		lPanel7.add(rodsPassword);

		JLabel labelRodsHost;
		labelRodsHost = new JLabel("iRods Host:   ");
		lPanel8.add(labelRodsHost);
		rodsHost = new JTextField(maxColumnsTextField);
		rodsHost.setDocument(new JTextFieldLimit(maxCharsTextField));
		rodsHost.setText(null);
		lPanel8.add(rodsHost);

		JLabel labelRodsPort;
		labelRodsPort = new JLabel("iRods Port:    ");
		lPanel9.add(labelRodsPort);
		rodsHostPort = new JTextField(maxColumnsTextField);
		rodsHostPort.setDocument(new JTextFieldLimit(maxCharsTextField));
		rodsHostPort.setText(null);
		lPanel9.add(rodsHostPort);

		JLabel labelRodsZone;
		labelRodsZone = new JLabel("iRods Zone:   ");
		lPanel10.add(labelRodsZone);
		rodsZone = new JTextField(maxColumnsTextField);
		rodsZone.setDocument(new JTextFieldLimit(maxCharsTextField));
		rodsZone.setText(null);
		lPanel10.add(rodsZone);

		JLabel labelRodsRes;
		labelRodsRes = new JLabel("Resource:      ");
		lPanel11.add(labelRodsRes);
		rodsRes = new JTextField(maxColumnsTextField);
		rodsRes.setDocument(new JTextFieldLimit(maxCharsTextField));
		rodsRes.setText(null);
		lPanel11.add(rodsRes);

		loginRodsButton = new JButton("Access iRODS!");
		lPanel12.add(loginRodsButton);
		
		rodsLblConnectionStatus = new JLabel("Not Connected!");
		lPanel13.add(rodsLblConnectionStatus);

		lPanel6.setLayout(new FlowLayout(FlowLayout.CENTER));
		lPanel7.setLayout(new FlowLayout(FlowLayout.CENTER));
		lPanel8.setLayout(new FlowLayout(FlowLayout.CENTER));
		lPanel9.setLayout(new FlowLayout(FlowLayout.CENTER));
		lPanel10.setLayout(new FlowLayout(FlowLayout.CENTER));
		lPanel11.setLayout(new FlowLayout(FlowLayout.CENTER));
		lPanel12.setLayout(new FlowLayout(FlowLayout.CENTER));

		lPanelRodsSpecific.add(lPanel6);
		lPanelRodsSpecific.add(lPanel7);
		lPanelRodsSpecific.add(lPanel8);
		lPanelRodsSpecific.add(lPanel9);
		lPanelRodsSpecific.add(lPanel10);
		lPanelRodsSpecific.add(lPanel11);
		lPanelRodsSpecific.add(lPanel12);
		lPanelRodsSpecific.add(lPanel13);
		mainLeftPanel.add(lPanelRodsSpecific);
		lPanelRodsSpecific.setLayout(new BoxLayout(lPanelRodsSpecific,
				BoxLayout.Y_AXIS));
		lPanelRodsSpecific.setVisible(false);

		/*
		 * Add the topPanel1(Left side) and topPanel2(Right side) to mainFrame.
		 * Also set mainFrame visible
		 */
		lPanelDbSpecific.setPreferredSize(new Dimension(700, 360));
		lPanelRodsSpecific.setPreferredSize(new Dimension(700, 320));
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
			lPanelRodsSpecific.setVisible(false);
			lPanelDbSpecific.setVisible(true);
			mainRightPanel.setBorder(title3);
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
			lPanelRodsSpecific.setVisible(true);
			lPanelDbSpecific.setVisible(false);
			mainRightPanel.setBorder(title4);
		}
	}
	
	private void disableRodsGUI() {
		rodsLblConnectionStatus.setText("Not Connected!");
	}
	
	private void disableDbxGUI() {
		dbxAccessCodeTextField.setText("");
		dbxAccessCodeTextField.setEnabled(false);
		btnConnect.setEnabled(false);
		dbxLblConnectionStatus.setText("");
		userInfo.setText("");
	}
	
	private void disableJTreeGUI() {
		JFrame enclosingFrame = cloudFileTree.getEnclosingFrame();
		if (enclosingFrame != null)
			enclosingFrame.dispose();
		
		srcTxt.setText("");
		targetTxt.setText("");
		logMessages.setText("");
		
		setEnabledAll(mainRightPanel, false);		
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
					dbxAccessCodeTextField.setEnabled(true);
					btnConnect.setEnabled(true);
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
				dbxAccessCode = dbxAccessCodeTextField.getText();

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
					dbxLblConnectionStatus.setText("Connected as " + userName);
					userInfo.setText("Username: " + userName + "\nCountry: "
							+ country + "\nQuota: " + userQuota + " GB");

					cloudHomeDirectoryPath = cloudHandler.getHomeDirectory();
					buildFileSelectionTrees(cloudHomeDirectoryPath,
							LOCAL_HOME_DIRECTORY_PATH);

					/*
					 * Disable the access code textfield and enable the the
					 * right panel(which contains the tasks section) after the
					 * user is connected.
					 */
					dbxAccessCodeTextField.setEnabled(false);
					// All the components of topPanel2 are enabled after
					// successful connection with user's dropbox account
					setEnabledAll(mainRightPanel, true);
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
		@Override
		public void actionPerformed(ActionEvent e) {
			RodsOperations rodsUtilsObj = (RodsOperations) cloudHandler;

			/*
			 * rodsUtilsObj.setUsername(user.getText());
			 * rodsUtilsObj.setIrodsPassword(rodsPassword.getText());
			 * rodsUtilsObj.setHost(rodsHost.getText());
			 * rodsUtilsObj.setPort(Integer.parseInt(rodsHostPort.getText()));
			 * rodsUtilsObj.setZone(rodsZone.getText());
			 * rodsUtilsObj.setRes(rodsRes.getText());
			 */

			/*
			 * TESTING -temporary solution for not entering the credentials for
			 * every run
			 */
			rodsUtilsObj.setUsername("rods");
			rodsUtilsObj.setIrodsPassword("rods");
			rodsUtilsObj.setHost("192.168.0.103");
			rodsUtilsObj.setPort(1247);
			rodsUtilsObj.setZone("BragadiruZone");
			rodsUtilsObj.setRes("test1-resc");

			try {
				rodsUtilsObj.login();
				userIsConnected = true;

				cloudHomeDirectoryPath = cloudHandler.getHomeDirectory();
				buildFileSelectionTrees(cloudHomeDirectoryPath,
						LOCAL_HOME_DIRECTORY_PATH);
			} catch (CloudException e1) {
				rodsLblConnectionStatus.setText("Error connecting to iRODS!");
				e1.printStackTrace();
				return;
			}
			rodsLblConnectionStatus.setText("Connected to iRODS");
			setEnabledAll(mainRightPanel, true);
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
	
	/*
	 * Function to enable/disable components inside a container(works for Nested
	 * containers)
	 * 
	 * Parameters: Container container : container which you have to
	 * disable/enable enabled : boolean value, true(enable) or false(disable)
	 */
	public void setEnabledAll(Container container, boolean enabled) {
		Component[] components = container.getComponents();
		if (components.length > 0) {
			for (Component component : components) {
				component.setEnabled(enabled);
				if (component instanceof Container) { // has to be a container
														// to contain components
					setEnabledAll((Container) component, enabled); // the
																	// recursive
																	// call
				}
			}
		}
	}
}