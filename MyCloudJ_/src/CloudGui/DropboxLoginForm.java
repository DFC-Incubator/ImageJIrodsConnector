package CloudGui;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class DropboxLoginForm {
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
	private JTextArea userInfo;
	
	public void draw() {
		/**
		 * enabled if dbxLoginRadioButton is selected contains components for
		 * entering Dbx credentials
		 */
		setlPanelDbSpecific(new JPanel(new FlowLayout()));
		JPanel lPanel1 = new JPanel(new FlowLayout());
		JPanel lPanel2 = new JPanel(new FlowLayout());
		JPanel lPanel3 = new JPanel(new FlowLayout());
		JPanel lPanel4 = new JPanel(new FlowLayout());
		JPanel lPanel5 = new JPanel(new FlowLayout());

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

		setAccessDbxButton(new JButton("Access Dropbox  !"));
		lPanel2.add(getAccessDbxButton());

		/*
		 * Add JLabel : "Access Code".
		 * 
		 * Added onto panel3
		 */
		JLabel lbl1;
		lbl1 = new JLabel("Dropbox Access Code: ");
		lPanel3.add(lbl1);

		setDbxAccessCodeTextField(new JTextField(25));
		getDbxAccessCodeTextField().setText(null);
		getDbxAccessCodeTextField().setEnabled(false);
		lPanel3.add(getDbxAccessCodeTextField());

		setBtnConnect(new JButton("Connect !"));
		getBtnConnect().setEnabled(false);
		lPanel3.add(getBtnConnect());

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
		getUserInfo().setEditable(false);
		lPanel5.add(getUserInfo());

		/*
		 * Event Handling for btnConnect. This handles the complete set set of
		 * events that has to be executed after user presses the "Connect"
		 * button.
		 */

		/*
		 * Added all the components related to connection in the topPanel1(Left
		 * side of the mainFrame).
		 */
		
		getlPanelDbSpecific().add(lPanel1);
		getlPanelDbSpecific().add(lPanel2);
		getlPanelDbSpecific().add(lPanel3);
		getlPanelDbSpecific().add(lPanel4);
		getlPanelDbSpecific().add(lPanel5);
		getlPanelDbSpecific().setLayout(new BoxLayout(getlPanelDbSpecific(),
				BoxLayout.Y_AXIS));
		lPanelDbSpecific.setPreferredSize(new Dimension(700, 360));
		
	}
	
	public void setVisible(boolean value) {
		lPanelDbSpecific.setVisible(value);
	}
	
	public void reset() {
		dbxAccessCodeTextField.setText("");
		dbxAccessCodeTextField.setEnabled(false);
		getBtnConnect().setEnabled(false);
		dbxLblConnectionStatus.setText("");
		getUserInfo().setText("");
	}
	
	public void setStatus(String status) {
		dbxLblConnectionStatus.setText(status);
	}
	
	public JPanel getlPanelDbSpecific() {
		return lPanelDbSpecific;
	}
	
	public void setEnabledAccessCodeField(boolean value) {
		dbxAccessCodeTextField.setEnabled(value);
	}
	
	public String getAccesssCode() {
		return dbxAccessCodeTextField.getText();
	}

	public void setlPanelDbSpecific(JPanel lPanelDbSpecific) {
		this.lPanelDbSpecific = lPanelDbSpecific;
	}

	public JButton getAccessDbxButton() {
		return accessDbxButton;
	}

	public void setAccessDbxButton(JButton accessDbxButton) {
		this.accessDbxButton = accessDbxButton;
	}

	public JTextField getDbxAccessCodeTextField() {
		return dbxAccessCodeTextField;
	}

	public void setDbxAccessCodeTextField(JTextField dbxAccessCodeTextField) {
		this.dbxAccessCodeTextField = dbxAccessCodeTextField;
	}

	public JButton getBtnConnect() {
		return btnConnect;
	}

	public void setBtnConnect(JButton btnConnect) {
		this.btnConnect = btnConnect;
	}

	public JTextArea getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(String info) {
		userInfo.setText(info);
	}
}
