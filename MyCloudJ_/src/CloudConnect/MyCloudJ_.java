package CloudConnect;

import generalUtils.CloudException;
import generalUtils.CloudOperations;
import generalUtils.GeneralUtility;
import ij.io.Opener;
import ij.plugin.PlugIn;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import rodsUtils.RodsUtility;

import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxPath;

import dbxUtils.DbxUtility;

/**
 * @author Atin Mathur (mathuratin007@gmail.com) - Dropbox functionality
 * @author Doru-Cristian Gucea (gucea.doru@gmail.com) - iRODS functionality
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
	/**
	 * osType: windows or linux
	 */
	private String osType = GeneralUtility.getOS();

	/**
	 * cloudHandler : generic interface for cloud operations
	 */
	private CloudOperations cloudHandler = new DbxUtility();

	/**
	 * userIsConnected: true if user connected to cloud, false otherwise
	 */
	private int userIsConnected;

	/**
	 * after login, initialized to user home directory
	 */
	private String userHomeDirectoryPath;

	/**
	 * isFileDownload: true if it's a file download, false otherwise
	 */
	private boolean isFileDownload = false;

	/**
	 * Variables used during Download process:
	 * 
	 * @targetLocalPath : stores the "Target Local Machine Path" where users
	 *                  wants to download data from Dropbox
	 * @fileCloudPath : stores the "Source Dropbox File Path" which has to be
	 *                downloaded
	 * @folderCloudPath : stores the "Source Dropbox Folder Path" which has to
	 *                  be downloaded. Initialized to "/"
	 * 
	 *                  Note: During a download process, either FileDbxPath or
	 *                  FolderDbxPath is used (Depending on what has to be
	 *                  downloaded). This is just for easy understanding
	 * 
	 */
	private String targetLocalPath = ".", fileCloudPath = "",
			folderCloudPath = "/";

	/**
	 * Variables used during Download process:
	 * 
	 * @targetCloudPath : stores the "Target Dropbox Path" where users wants to
	 *                  upload data from local machine
	 * @fileLocalPath : stores the "Source Local Machine File Path" which has to
	 *                be uploaded
	 * @folderLocalPath : stores the "Source Local Machine Folder Path" which
	 *                  has to be uploaded
	 */
	private String targetCloudPath = "/", fileLocalPath = "",
			folderLocalPath = ".";

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
	 * This label will display the connection status of the plugin along with
	 * username (if connected) Status Format : Connected as <username> or Not
	 * Connected!
	 * 
	 * Note : Initial status "Not Connected!"
	 */
	private JLabel lblConnectionStatus;

	/**
	 * two radio buttons inside topPanel2
	 * 
	 * @uploadRadioButton: user will upload a file/folder to cloud
	 * @downloadRadioButton: user will download a file/folder from cloud
	 */
	private JRadioButton uploadRadioButton, downloadRadioButton;

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

	/**
	 * this JFrame contains the tree for selecting the files/folder
	 */
	private JFrame treeFrame = new JFrame();

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
	private JTextArea msgs;

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

		msgs = new JTextArea();
		msgs.setLineWrap(true);
		msgs.setWrapStyleWord(true);

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
		lblConnectionStatus = new JLabel("Not Connected !");
		lPanel4.add(lblConnectionStatus);

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

		/*
		 * Jlabel : "Target"
		 * 
		 * Added onto rPanel3
		 */
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
		JScrollPane msgsScrollPane = new JScrollPane(msgs);
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

		JLabel labelRodsUser;
		labelRodsUser = new JLabel("iRods User:           ");
		lPanel6.add(labelRodsUser);
		user = new JTextField(25);
		user.setText(null);
		lPanel6.add(user);

		JLabel labelRodsPassword;
		labelRodsPassword = new JLabel("iRods Password:  ");
		lPanel7.add(labelRodsPassword);
		rodsPassword = new JTextField(25);
		rodsPassword.setText(null);
		lPanel7.add(rodsPassword);

		JLabel labelRodsHost;
		labelRodsHost = new JLabel("iRods Host:           ");
		lPanel8.add(labelRodsHost);
		rodsHost = new JTextField(25);
		rodsHost.setText(null);
		lPanel8.add(rodsHost);

		JLabel labelRodsPort;
		labelRodsPort = new JLabel("iRods Port:            ");
		lPanel9.add(labelRodsPort);
		rodsHostPort = new JTextField(25);
		rodsHostPort.setText(null);
		lPanel9.add(rodsHostPort);

		JLabel labelRodsZone;
		labelRodsZone = new JLabel("iRods Zone:          ");
		lPanel10.add(labelRodsZone);
		rodsZone = new JTextField(25);
		rodsZone.setText(null);
		lPanel10.add(rodsZone);

		JLabel labelRodsRes;
		labelRodsRes = new JLabel("Default Resource:");
		lPanel11.add(labelRodsRes);
		rodsRes = new JTextField(25);
		rodsRes.setText(null);
		lPanel11.add(rodsRes);

		loginRodsButton = new JButton("Access iRODS!");
		lPanel12.add(loginRodsButton);

		lPanel13.add(lblConnectionStatus);

		lPanel6.setLayout(new FlowLayout(FlowLayout.LEFT));
		lPanel7.setLayout(new FlowLayout(FlowLayout.LEFT));
		lPanel8.setLayout(new FlowLayout(FlowLayout.LEFT));
		lPanel9.setLayout(new FlowLayout(FlowLayout.LEFT));
		lPanel10.setLayout(new FlowLayout(FlowLayout.LEFT));
		lPanel11.setLayout(new FlowLayout(FlowLayout.LEFT));
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
		lPanelDbSpecific.setPreferredSize(new Dimension(700, 340));
		lPanelRodsSpecific.setPreferredSize(new Dimension(700, 320));
		mainFrame.add(mainLeftPanel);
		mainFrame.add(mainRightPanel);
		mainFrame.setVisible(true);

	}

	class BtnDbxConnectListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			DbxUtility dbxUtility = (DbxUtility) cloudHandler;

			try {
				// retrieve the access code from textfield
				dbxAccessCode = dbxAccessCodeTextField.getText();

				// if user is previously not connected and access code is not
				// empty then connect it
				if (userIsConnected == 0 && !dbxAccessCode.equals("")) {
					// connect user to dropbox
					dbxUtility.DbxLinkUser(dbxAccessCode);

					// user status changed to 1(i.e., connected)
					userIsConnected = 1;

					/*
					 * Retrieve username, country and quota from dropbox account
					 * info API and print it in the text area for the user
					 */
					userName = dbxUtility.getUserName();
					country = dbxUtility.getCountry();
					userQuota = dbxUtility.getUserQuota();
					lblConnectionStatus.setText("Connected as " + userName);
					userInfo.setText("Username: " + userName + "\nCountry: "
							+ country + "\nQuota: " + userQuota + " GB");

					userHomeDirectoryPath = cloudHandler.getHomeDirectory();
					buildSelectionTrees(userHomeDirectoryPath);

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
				else if (userIsConnected == 1)
					JOptionPane.showMessageDialog(mainFrame,
							"Already connected !",
							"MyCLoudJ - Already Connected",
							JOptionPane.WARNING_MESSAGE);
				// If user is not connected but there is no access code,
				// information for user
				else if (userIsConnected == 0 && dbxAccessCode.equals(""))
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

	class BtnDbxAccessListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// if user if not connected, then execute if block
			if (userIsConnected == 0) {
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

	class BtnExpandListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String filePath = "";
			String filePathComponent;

			// Parent node is initially null
			DefaultMutableTreeNode parentNode = null;

			// Parent path of the currently selected node
			TreePath parentPath = downloadTree.getSelectionPath();

			// get the parent node(one in which we have to add children)
			parentNode = (DefaultMutableTreeNode) (parentPath
					.getLastPathComponent());

			for (int i = 0; i < parentPath.getPathCount(); i++) {
				filePathComponent = parentPath.getPathComponent(i).toString();

				if (filePathComponent.equals(userHomeDirectoryPath) == true
						&& parentPath.getPathCount() == 1) {
					filePath = userHomeDirectoryPath;
					break;
				}

				if (filePathComponent.endsWith("/") == false)
					filePathComponent = filePathComponent.concat("/");

				filePath = filePath.concat(filePathComponent);
			}

			// Add child nodes to this node(files and subfolders11)
			try {
				addChildren(parentNode, downloadTreeModel,
						cloudHandler.listFiles(filePath));
			} catch (CloudException e1) {
				// TODO: Display a error for the user inside the browse box
				e1.printStackTrace();
				return;
			}

			downloadTree.expandPath(new TreePath(parentNode.getPath()));

			treeFrame.pack();
		}
	}

	class BtnExpand2Listener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String filePath = "";
			String filePathComponent;

			// Parent node is initially null
			DefaultMutableTreeNode parentNode = null;

			// Parent path of the currently selected node
			TreePath parentPath = uploadTree.getSelectionPath();

			// get the parent node(one in which we have to add children)
			parentNode = (DefaultMutableTreeNode) (parentPath
					.getLastPathComponent());

			for (int i = 0; i < parentPath.getPathCount(); i++) {
				filePathComponent = parentPath.getPathComponent(i).toString();

				if (filePathComponent.equals(userHomeDirectoryPath) == true
						&& parentPath.getPathCount() == 1) {
					filePath = userHomeDirectoryPath;
					break;
				}

				if (filePathComponent.endsWith("/") == false)
					filePathComponent = filePathComponent.concat("/");

				filePath = filePath.concat(filePathComponent);
			}

			// Add child nodes to this node(files and subfolders)
			try {
				addChildrenFolder(parentNode, downloadTreeModel,
						cloudHandler.listFiles(filePath));
			} catch (CloudException e1) {
				JOptionPane
						.showMessageDialog(mainFrame, e1.getCloudError(),
								"MyCLoudJ - Expanding Error",
								JOptionPane.ERROR_MESSAGE);
				return;
			}

			uploadTree.expandPath(new TreePath(parentNode.getPath()));

			treeFrame.pack();
		}
	}

	class BtnSelectListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Get the latest node selected
			node = downloadTree.getLastSelectedPathComponent();
			parentNode = ((DefaultMutableTreeNode) node).getParent();

			// Extract the name from the node and set the source address with
			// its path
			String name;
			name = (node == null) ? "NONE" : node.toString();

			String suffix;
			suffix = (parentNode == null) ? "NONE" : parentNode.toString();

			if (name.startsWith("/")) {
				srcTxt.setText(name);
			} else {
				if (suffix.endsWith("/"))
					name = suffix + name;
				else
					name = suffix + "/" + name;
				srcTxt.setText(name);
			}

			try {
				isFileDownload = cloudHandler.isFileDownload(name);
			} catch (CloudException e1) {
				msgs.append(e1.getCloudError() + "\n\n");
				e1.printStackTrace();
			}

			// close the treeFrame
			treeFrame.dispose();
		}
	}

	class BtnFileChooser1Listener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// If user wants to Upload(Upload Radio button is selected), then
			// source is the local machine, browse from Local Machine
			if (uploadRadioButton.isSelected()) {
				// JFileChooser opens in current directory(imagej plugins/)
				JFileChooser chooser = new JFileChooser(new File("."));

				// Both Files and Directories are allowed
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int choice = chooser.showOpenDialog(chooser);
				if (choice != JFileChooser.APPROVE_OPTION)
					return;
				// File selected
				File chosenFile = chooser.getSelectedFile();

				// The path of the selected file is set in the source textfield
				srcTxt.setText(chosenFile.getAbsolutePath());
			}
			// If user wants to Download(Download Radio button is selected),
			// then source is the Dropbox, browse from Dropbox
			else if (downloadRadioButton.isSelected()) {
				treeFrame = new JFrame();
				BoxLayout boxLayout = new BoxLayout(treeFrame.getContentPane(),
						BoxLayout.Y_AXIS);
				treeFrame.setLayout(boxLayout);

				/*
				 * // Expand the JTree for (int i = 0; i <
				 * DbxTree1.getRowCount(); i++) { DbxTree1.expandRow(i); }
				 */

				/*
				 * JPanel for browsing frame
				 * 
				 * Scroll bar added
				 */
				JPanel treePanel = new JPanel();
				JScrollPane scroll = new JScrollPane(treePanel);

				/*
				 * JButton
				 * 
				 * Expand : expand the folders into subfolders and files.
				 * 
				 * Added onto panel2
				 */
				JPanel panel2 = new JPanel(new FlowLayout());
				JButton Expand = new JButton("Expand");
				panel2.add(Expand);
				Expand.addActionListener(new BtnExpandListener());

				/*
				 * JButton
				 * 
				 * Select : Select the folder/file and set the source textfield
				 * with the dropbox path of selected file/folder
				 * 
				 * Added onto panel2
				 */
				JButton Select = new JButton("Select");
				panel2.add(Select);
				Select.addActionListener(new BtnSelectListener());

				/*
				 * JButton
				 * 
				 * Cancel : No action, just close the treeFrame
				 * 
				 * Added onto panel2
				 */
				JButton Cancel = new JButton("Cancel");
				panel2.add(Cancel);
				Cancel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// Close the treeFrame
						treeFrame.dispose();
					}
				});

				downloadTree
						.addTreeExpansionListener(new TreeExpansionListener() {
							@Override
							public void treeExpanded(TreeExpansionEvent event) {
								treeFrame.pack();
							}

							@Override
							public void treeCollapsed(TreeExpansionEvent event) {
								treeFrame.pack();
							}
						});

				// This will position the JFrame in the center of the screen
				treeFrame.setLocationRelativeTo(null);
				treeFrame.setTitle("Dropbox - Browse!");
				treeFrame.setSize(350, 200);
				treeFrame.setResizable(true);
				// treeFrame.setMaximumSize(new Dimension(500,350));

				// Add DbxTree1(JTree) to this panel and in turn in treeFrame
				treePanel.add(downloadTree);
				treeFrame.add(scroll);
				treeFrame.add(panel2);
				treeFrame.setVisible(true);
				treeFrame.pack();
			}
		}
	}

	class BtnFileChooser2Listener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// If user wants to Upload(Upload Radio button is selected), then
			// target is the Dropbox, browse the Dropbox
			if (uploadRadioButton.isSelected()) {
				/*
				 * This JFrame contains Dropbox Jtree for selecting the folder
				 * for upload
				 */
				final JFrame treeFrame = new JFrame();
				BoxLayout boxLayout = new BoxLayout(treeFrame.getContentPane(),
						BoxLayout.Y_AXIS);
				treeFrame.setLayout(boxLayout);

				/*
				 * // Expand the JTree for (int i = 0; i <
				 * DbxTree2.getRowCount(); i++) { DbxTree2.expandRow(i); }
				 */

				/*
				 * JPanel for browsing frame
				 * 
				 * Scroll bar added
				 */
				JPanel treePanel = new JPanel();
				JScrollPane scroll = new JScrollPane(treePanel);

				/*
				 * JButton
				 * 
				 * Expand : expand the folders into subfolders and files.
				 * 
				 * Added onto panel2
				 */
				JPanel panel2 = new JPanel(new FlowLayout());
				JButton Expand = new JButton("Expand");
				panel2.add(Expand);
				Expand.addActionListener(new BtnExpand2Listener());

				/*
				 * JButton
				 * 
				 * Select : Select the folder and set the source textfield with
				 * its dropbox path
				 * 
				 * Added onto panel2
				 */
				JButton Select = new JButton("Select");
				panel2.add(Select);
				Select.addActionListener(new BtnSelect2Listener(treeFrame));

				/*
				 * JButton
				 * 
				 * Cancel : No action, just close the treeFrame
				 * 
				 * Added onto panel2
				 */
				JButton Cancel = new JButton("Cancel");
				panel2.add(Cancel);
				Cancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// Close the treeFrame
						treeFrame.dispose();
					}
				});

				uploadTree
						.addTreeExpansionListener(new TreeExpansionListener() {
							@Override
							public void treeExpanded(TreeExpansionEvent event) {
								treeFrame.pack();
							}

							@Override
							public void treeCollapsed(TreeExpansionEvent event) {
								treeFrame.pack();
							}
						});

				// This will position the JFrame in the center of the screen
				treeFrame.setLocationRelativeTo(null);
				treeFrame.setTitle("Dropbox - Browse!");
				treeFrame.setSize(350, 200);
				treeFrame.setResizable(true);
				// treeFrame.setMaximumSize(new Dimension(500,350));

				// Add DbxTree2(JTree) to this panel and in turn in treeFrame
				treePanel.add(uploadTree);
				treeFrame.add(scroll);
				treeFrame.add(panel2);
				treeFrame.setVisible(true);
				treeFrame.pack();
			}
			// If user wants to Download(Download Radio button is selected),
			// then target is the local machine, browse from local machine
			else if (downloadRadioButton.isSelected()) {
				// JFileChooser opens in current directory(imagej plugins/)
				JFileChooser chooser = new JFileChooser(new File("."));

				// Only Directories are allowed, files can only be downloaded
				// into Directories
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int choice = chooser.showOpenDialog(chooser);
				if (choice != JFileChooser.APPROVE_OPTION)
					return;
				// File selected
				File chosenFile = chooser.getSelectedFile();

				// The path of the selected file is set in the target textfield
				// and set to non-editable
				targetTxt.setText(chosenFile.getAbsolutePath());
				targetTxt.setEditable(false);
			}
		}

		class BtnSelect2Listener implements ActionListener {
			private JFrame treeFrame;

			public BtnSelect2Listener(JFrame treeFrame) {
				this.treeFrame = treeFrame;
			}

			public void actionPerformed(ActionEvent e) {
				// Get the latest node selected
				node = uploadTree.getLastSelectedPathComponent();

				// Extract the name from the node and set the source address
				// with its path
				String name;
				name = (node == null) ? "NONE" : node.toString();
				targetTxt.setText(name);
				System.out.println("close");
				this.treeFrame.dispose();
			}
		}
	}

	class BtnStartListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			/* Extract the source address and target address beforehand */
			final String source = srcTxt.getText();
			final String target = targetTxt.getText();

			// If source or target is empty, take no action and inform the user
			// about the same
			if (source.equals("") || target.equals("")) {
				msgs.append("Error: Select the files/folder to upload/download\n\n");
				return;
			}

			// If user wants to upload, this will be executed othewise else
			// block will execute
			if (uploadRadioButton.isSelected()) {
				// open the file selected by the user
				File file = new File(source);

				// Print the uploading information for the user in the text area
				msgs.append("Message: Uploading " + source
						+ " to Dropbox path: " + target + "\n\n");

				// Checks if selected path is of a file or a folder, if file,
				// then execute IF block
				if (file.isFile()) {
					// Store the file path
					fileLocalPath = source;

					// if windows OS, then change the separator from \\ to /
					String newFileLocalPath = fileLocalPath.replace('\\', '/');

					// Retrieve the filename from the path
					String fileName = newFileLocalPath
							.substring(newFileLocalPath.lastIndexOf("/"));

					// Target path in Dropbox folder
					targetCloudPath = target;

					// Append the filename at the end of the target path
					targetCloudPath += fileName;

					/*
					 * Call the upload function of DbxUtility class which in
					 * turn calls Dropbox API for upload function New thread is
					 * spawn as this may take a long time and GUI becomes
					 * unresponsive if there is a single thread
					 */
					Thread thread = new Thread("New Upload Thread") {
						public void run() {
							String localSource = source;
							try {
								cloudHandler.uploadFile(fileLocalPath,
										targetCloudPath);
							} catch (CloudException e) {
								msgs.append("Error uploading file "
										+ e.getCloudError() + "!\n\n"); // Message
																		// once
																		// the
																		// upload
																		// is
																		// complete
								e.printStackTrace();
							}
							msgs.append("Uploading of " + localSource
									+ " Complete !\n\n"); // Message once the
															// upload is
															// complete

							// Code for Opening the file/folder after upload in
							// default application
							Opener openfile = new Opener();
							openfile.open(localSource);
						}
					};
					thread.start();
				}
				// If selected path is a directory, execute ELSE-IF block
				else if (file.isDirectory()) {
					// Store the Local folder's path
					folderLocalPath = source;

					// Store the Target Dropbox's path
					targetCloudPath = target;

					/*
					 * Call the upload function New thread is spawn as this may
					 * take a long time and GUI becomes unresponsive if there is
					 * a single thread
					 */
					Thread thread = new Thread("New Upload Thread") {
						public void run() {
							String localSource = source;
							try {
								cloudHandler.uploadFolder(folderLocalPath,
										targetCloudPath);
								addChildrenFolder(downloadRoot,
										downloadTreeModel,
										cloudHandler
												.listFiles(cloudHandler.getHomeDirectory()));
							} catch (CloudException e) {
								msgs.append("Error uploading folder "
										+ e.getCloudError() + "!\n\n"); // Message
																		// once
																		// the
																		// upload
																		// is
																		// complete
								e.printStackTrace();
								return;
							}
							msgs.append("Uploading of " + localSource
									+ " Complete !\n\n"); // Message once the
															// upload is
															// complete

							// Code for Opening the file/folder after upload in
							// default application
							Opener openfile = new Opener();
							openfile.open(localSource);
						}
					};
					thread.start();
				}
			}
			// If user wants to download, ELSE block will be executed
			else if (downloadRadioButton.isSelected()) {
				// Stores the target path on Local machine
				targetLocalPath = target;

				// Print the downloading information for the user in the text
				// area
				msgs.append("Message: Downloading " + source
						+ " from Dropbox to Local Path: " + target + "\n\n");

				// If the path is file, then execute this
				if (isFileDownload) {
					// Store the Dropbox file path
					fileCloudPath = source;

					/*
					 * Call the download function of the DbxUtility class New
					 * thread is spawn as this may take a long time and GUI
					 * becomes unresponsive if there is a single thread
					 */
					Thread thread = new Thread("New Download Thread") {
						public void run() {
							String localSource = source;
							String localTarget = target;
							try {
								cloudHandler.downloadFile(fileCloudPath,
										targetLocalPath);
							} catch (CloudException e) {
								msgs.append("Error uploading folder "
										+ e.getCloudError() + "!\n\n"); // Message
																		// once
																		// the
																		// upload
																		// is
																		// complete
								e.printStackTrace();
								return;
							}
							msgs.append("Downloading of " + localSource
									+ " Complete !\n\n"); // Message once the
															// upload is
															// complete

							/*
							 * To open the file/folder which is downloaded from
							 * Dropbox
							 * 
							 * DbxPath.getName(path) : Returns just the last
							 * component of the path. For Ex: getName("/")
							 * returns "/" getName("/Photos") returns "Photos"
							 * getName("/Photos/Home.jpeg") returns "Home.jpeg"
							 */
							String lastPart = DbxPath.getName(localSource);

							// If OS is windows, the path separator is '\' else
							// '/'
							if (osType.contains("windows")) {
								lastPart = "\\" + lastPart;
							} else {
								lastPart = "/" + lastPart;
							}

							// Append the filename to Target local path
							String finalSource = localTarget + lastPart;

							// Code for Opening the file/folder after upload in
							// default application
							Opener openfile = new Opener();
							openfile.open(finalSource);
						}
					};
					thread.start();
				}
				// If the path is a directory. execute this
				else if (isFileDownload == false) {
					// Store the Dropbox folder path
					folderCloudPath = source;

					/*
					 * Call the download folder function of the DbXUtility class
					 * New thread is spawn as this may take a long time and GUI
					 * becomes unresponsive if there is a single thread
					 */
					Thread thread = new Thread("New Download Thread") {
						public void run() {
							String localSource = source;
							String localTarget = target;
							try {
								cloudHandler.downloadFolder(folderCloudPath,
										targetLocalPath);
							} catch (CloudException e) {
								msgs.append("Error downloading folder "
										+ e.getMessage() + "!\n\n"); // Message
																		// once
																		// the
																		// upload
																		// is
																		// complete
								e.printStackTrace();
								return;
							}
							msgs.append("Downloading of " + localSource
									+ " Complete !\n\n"); // Message once the
															// upload is
															// complete

							/*
							 * To open the file/folder which is downloaded from
							 * Dropbox
							 * 
							 * DbxPath.getName(path) : Returns just the last
							 * component of the path. For Ex: getName("/")
							 * returns "/" getName("/Photos") returns "Photos"
							 * getName("/Photos/Home.jpeg") returns "Home.jpeg"
							 */
							String lastPart = DbxPath.getName(localSource);

							// If OS is windows, the path separator is '\' else
							// '/'
							if (osType.contains("windows")) {
								lastPart = "\\" + lastPart;
							} else {
								lastPart = "/" + lastPart;
							}

							// Append the filename to Target local path
							String finalSource = localTarget + lastPart;

							// Code for Opening the file/folder after upload in
							// default application
							Opener openfile = new Opener();
							openfile.open(finalSource);
						}
					};
					thread.start();
				}
			}
		}
	}

	class BtnConnectRodsListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			RodsUtility rodsUtilsObj = (RodsUtility) cloudHandler;

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
			rodsUtilsObj.setHost("192.168.0.102");
			rodsUtilsObj.setPort(1247);
			rodsUtilsObj.setZone("BragadiruZone");
			rodsUtilsObj.setRes("test1-resc");

			try {
				rodsUtilsObj.initializeRods();
				rodsUtilsObj.login();

				userHomeDirectoryPath = cloudHandler.getHomeDirectory();
				buildSelectionTrees(userHomeDirectoryPath);
			} catch (CloudException e1) {
				lblConnectionStatus.setText("Error connecting to iRODS!");
				e1.printStackTrace();
				return;
			}
			lblConnectionStatus.setText("Connected to iRODS");
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

	class BtnDbxLoginRadioListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
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
			cloudHandler = new RodsUtility();
			mainLeftPanel.setBorder(title2);
			lPanelRodsSpecific.setVisible(true);
			lPanelDbSpecific.setVisible(false);
			mainRightPanel.setBorder(title4);
		}
	}

	private void buildSelectionTrees(String homeDirectoryPath)
			throws CloudException {
		downloadRoot = new DefaultMutableTreeNode(homeDirectoryPath);
		downloadTree = new JTree(downloadRoot);
		downloadTreeModel = new DefaultTreeModel(downloadRoot);
		addChildren(downloadRoot, downloadTreeModel,
				cloudHandler.listFiles(homeDirectoryPath));
		downloadTree.getSelectionModel().setSelectionMode(
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
		addChildrenFolder(downloadRoot, downloadTreeModel,
				cloudHandler.listFiles(homeDirectoryPath));
		uploadTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		uploadTreeModel.reload(uploadRoot);

	}

	/*
	 * Function to add nodes to the JTree
	 * 
	 * This function is called when user selects a parent node and clicks Expand
	 * button
	 * 
	 * Parameters: TODO
	 * Javadoc: TODO
	 */
	public void addChildren(DefaultMutableTreeNode node,
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
	 * Parameters: TODO
	 * Javadoc: TODO
	 */
	public void addChildrenFolder(DefaultMutableTreeNode node,
			DefaultTreeModel Treemodel, List<CloudFile> cloudFiles) {

		for (int i = 0; i < cloudFiles.size(); i++) {
			CloudFile child = cloudFiles.get(i);
			if (!child.isFile()) {
				DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(child.getPath());
				GeneralUtility.addUniqueNode(node, nodeChild, Treemodel);
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
		// End of setEnabledAll() method
	}

	// End of the MyCloudJ_ class
}