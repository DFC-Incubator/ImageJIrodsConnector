package CloudGui;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class RodsLoginForm {
	/**
	 * enabled if rodsLoginRadioButton is selected contains components for
	 * entering Dbx credentials
	 */
	private JPanel lPanelRodsSpecific;
	private JTextField user;
	private JPasswordField rodsPassword;
	private JTextField rodsHost;
	private JTextField rodsHostPort;
	private JTextField rodsZone;
	private JTextField rodsRes;
	private JButton loginRodsButton;
	private JButton disconnectButton;
	private JLabel rodsLblConnectionStatus;
	
	
	public void draw() {
		setlPanelRodsSpecific(new JPanel(new FlowLayout()));
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
		
		// user
		JLabel labelRodsUser;
		labelRodsUser = new JLabel("iRods User:  ");
		user = new JTextField(maxColumnsTextField);
		user.setDocument(new JTextFieldLimit(maxCharsTextField));
		user.setText(null);
		lPanel6.add(labelRodsUser);
		lPanel6.add(user);
		
		// password
		JLabel labelRodsPassword;
		labelRodsPassword = new JLabel("iRods Pass:  ");
		rodsPassword = new JPasswordField(maxColumnsTextField);
		rodsPassword.setDocument(new JTextFieldLimit(maxCharsTextField));
		rodsPassword.setText(null);
		lPanel7.add(labelRodsPassword);
		lPanel7.add(rodsPassword);
		
		// host
		JLabel labelRodsHost;
		labelRodsHost = new JLabel("iRods Host:   ");
		rodsHost = new JTextField(maxColumnsTextField);
		rodsHost.setDocument(new JTextFieldLimit(maxCharsTextField));
		rodsHost.setText(null);
		lPanel8.add(labelRodsHost);
		lPanel8.add(rodsHost);
		
		// port
		JLabel labelRodsPort;
		labelRodsPort = new JLabel("iRods Port:    ");
		rodsHostPort = new JTextField(maxColumnsTextField);
		rodsHostPort.setDocument(new JTextFieldLimit(maxCharsTextField));
		rodsHostPort.setText(null);
		lPanel9.add(labelRodsPort);
		lPanel9.add(rodsHostPort);
		
		// zone
		JLabel labelRodsZone;
		labelRodsZone = new JLabel("iRods Zone:   ");
		rodsZone = new JTextField(maxColumnsTextField);
		rodsZone.setDocument(new JTextFieldLimit(maxCharsTextField));
		rodsZone.setText(null);
		lPanel10.add(labelRodsZone);
		lPanel10.add(rodsZone);
		
		// resource
		JLabel labelRodsRes;
		labelRodsRes = new JLabel("Resource:      ");
		rodsRes = new JTextField(maxColumnsTextField);
		rodsRes.setDocument(new JTextFieldLimit(maxCharsTextField));
		rodsRes.setText(null);
		lPanel11.add(labelRodsRes);
		lPanel11.add(rodsRes);
		
		// button for connectiing to iRODS
		JLabel buttonsLabel;
		setLoginRodsButton(new JButton("Access iRODS"));
		disconnectButton = new JButton("Disconnect iRODS");
		disconnectButton.setVisible(false);
		lPanel12.add(getLoginRodsButton());
		lPanel12.add(disconnectButton);
		
		// connection status
		rodsLblConnectionStatus = new JLabel("Not Connected!");
		lPanel13.add(rodsLblConnectionStatus);

		lPanel6.setLayout(new FlowLayout(FlowLayout.CENTER));
		lPanel7.setLayout(new FlowLayout(FlowLayout.CENTER));
		lPanel8.setLayout(new FlowLayout(FlowLayout.CENTER));
		lPanel9.setLayout(new FlowLayout(FlowLayout.CENTER));
		lPanel10.setLayout(new FlowLayout(FlowLayout.CENTER));
		lPanel11.setLayout(new FlowLayout(FlowLayout.CENTER));
		lPanel12.setLayout(new FlowLayout(FlowLayout.CENTER));

		getlPanelRodsSpecific().add(lPanel6);
		getlPanelRodsSpecific().add(lPanel7);
		getlPanelRodsSpecific().add(lPanel8);
		getlPanelRodsSpecific().add(lPanel9);
		getlPanelRodsSpecific().add(lPanel10);
		getlPanelRodsSpecific().add(lPanel11);
		getlPanelRodsSpecific().add(lPanel12);
		getlPanelRodsSpecific().add(lPanel13);
		getlPanelRodsSpecific().setLayout(new BoxLayout(getlPanelRodsSpecific(),
				BoxLayout.Y_AXIS));
		getlPanelRodsSpecific().setVisible(false);
		getlPanelRodsSpecific().setPreferredSize(new Dimension(700, 320));
	}
	
	public void disable() {
		GuiUtils.enableTextFieldsFromContainer(lPanelRodsSpecific, false);
		getLoginRodsButton().setEnabled(false);
	}
	
	public void reset() {
		rodsLblConnectionStatus.setText("Not Connected");
		GuiUtils.enableTextFieldsFromContainer(lPanelRodsSpecific, true);
		getLoginRodsButton().setEnabled(true);
		loginRodsButton.setVisible(true);
		disconnectButton.setVisible(false);
		rodsLblConnectionStatus.setText("Not connected");
	}
	
	public void setVisible(boolean value) {
		lPanelRodsSpecific.setVisible(value);
	}
	
	public void setStatus(String status) {
		rodsLblConnectionStatus.setText(status);
	}

	public JPanel getlPanelRodsSpecific() {
		return lPanelRodsSpecific;
	}


	public void setlPanelRodsSpecific(JPanel lPanelRodsSpecific) {
		this.lPanelRodsSpecific = lPanelRodsSpecific;
	}

	public JButton getLoginRodsButton() {
		return loginRodsButton;
	}

	public void setLoginRodsButton(JButton loginRodsButton) {
		this.loginRodsButton = loginRodsButton;
	}

	public String getUserName() {
		return user.getText();
	}

	public String getRodsPassword() {
		return rodsPassword.getText();
	}

	public String getRodsHost() {
		return rodsHost.getText();
	}

	public String getRodsHostPort() {
		return rodsHostPort.getText();
	}

	public String getRodsZone() {
		return rodsZone.getText();
	}

	public String getRodsRes() {
		return rodsRes.getText();
	}

	public JButton getDisconnectButton() {
		return disconnectButton;
	}

	public void setDisconnectButton(JButton disconnectButton) {
		this.disconnectButton = disconnectButton;
	}
	
	public void setConnected(boolean value) {
		if (value) {
			loginRodsButton.setVisible(false);
			disconnectButton.setVisible(true);
			rodsLblConnectionStatus.setText("Connected to iRODS");
		}
		else {
			loginRodsButton.setVisible(true);
			disconnectButton.setVisible(false);
			rodsLblConnectionStatus.setText("Not connected");
		}
	}
}
