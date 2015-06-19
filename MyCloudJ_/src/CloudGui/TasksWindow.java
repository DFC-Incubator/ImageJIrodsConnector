package CloudGui;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class TasksWindow {
	private JPanel panel;
	private JRadioButton uploadRadioButton, downloadRadioButton;
	private JTextField sourcePath;
	private JTextField destinationPath;
	// button to open file chooser for the source file
	private JButton btnFileChooser1;
	// button to open the file chooser for the target file
	private JButton btnFileChooser2;
	// button to start download/upload
	private JButton btnStart;
	// msgs will be used for displaying task related information to user
	private JTextArea logMessages;

	public void draw() {
		// main panel is composed of five little panels
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		JPanel rPanel1 = new JPanel(new FlowLayout());
		JPanel rPanel2 = new JPanel(new FlowLayout());
		JPanel rPanel3 = new JPanel(new FlowLayout());
		JPanel rPanel4 = new JPanel(new FlowLayout());
		JPanel rPanel5 = new JPanel(new FlowLayout());

		// set a title for the main panel
		JLabel lblTasks = new JLabel("Tasks: ");
		rPanel1.add(lblTasks);

		// radio button for choosing between upload/download
		uploadRadioButton = new JRadioButton("Upload");
		downloadRadioButton = new JRadioButton("Download", true);
		ButtonGroup group = new ButtonGroup();
		group.add(uploadRadioButton);
		group.add(downloadRadioButton);
		rPanel1.add(uploadRadioButton);
		rPanel1.add(downloadRadioButton);

		// source path components
		JLabel lblSrc = new JLabel("Source: ");
		sourcePath = new JTextField("", 25);
		sourcePath.setEditable(false);
		btnFileChooser1 = new JButton("Browse");
		rPanel2.add(lblSrc);
		rPanel2.add(sourcePath);
		rPanel2.add(btnFileChooser1);

		// destination path components
		JLabel targetLbl = new JLabel("Target: ");
		destinationPath = new JTextField("", 25);
		destinationPath.setEditable(false);
		btnFileChooser2 = new JButton("Browse");
		rPanel3.add(targetLbl);
		rPanel3.add(destinationPath);
		rPanel3.add(btnFileChooser2);

		// button for starting the upload/download
		btnStart = new JButton("Start");
		rPanel4.add(btnStart);

		// area for displaying the status of the upload/download
		JLabel lblMsg = new JLabel("Messages: ");
		logMessages = new JTextArea();
		logMessages.setLineWrap(true);
		logMessages.setWrapStyleWord(true);
		JScrollPane msgsScrollPane = new JScrollPane(logMessages);
		msgsScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		msgsScrollPane.setPreferredSize(new Dimension(340, 220));
		rPanel5.add(lblMsg);
		rPanel5.add(msgsScrollPane);

		panel.add(rPanel1);
		panel.add(rPanel2);
		panel.add(rPanel3);
		panel.add(rPanel4);
		panel.add(rPanel5);
	}

	public void resetAndDisable() {
		GuiUtils.enableComponentsFromContainer(panel, false);
		resetSelectionPaths();
	}

	public void enable() {
		GuiUtils.enableComponentsFromContainer(panel, true);
	}

	public void resetSelectionPaths() {
		sourcePath.setText("");
		destinationPath.setText("");
		logMessages.setText("");
	}

	public void setTitle(TitledBorder title) {
		panel.setBorder(title);
	}

	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel mainRightPanel) {
		this.panel = mainRightPanel;
	}

	public JButton getBtnFileChooser1() {
		return btnFileChooser1;
	}

	public void setBtnFileChooser1(JButton btnFileChooser1) {
		this.btnFileChooser1 = btnFileChooser1;
	}

	public JButton getBtnFileChooser2() {
		return btnFileChooser2;
	}

	public void setBtnFileChooser2(JButton btnFileChooser2) {
		this.btnFileChooser2 = btnFileChooser2;
	}

	public JRadioButton getUploadRadioButton() {
		return uploadRadioButton;
	}

	public void setUploadRadioButton(JRadioButton uploadRadioButton) {
		this.uploadRadioButton = uploadRadioButton;
	}

	public JRadioButton getDownloadRadioButton() {
		return downloadRadioButton;
	}

	public void setDownloadRadioButton(JRadioButton downloadRadioButton) {
		this.downloadRadioButton = downloadRadioButton;
	}

	public JButton getBtnStart() {
		return btnStart;
	}

	public void setBtnStart(JButton btnStart) {
		this.btnStart = btnStart;
	}

	public String getSourcePath() {
		return sourcePath.getText();
	}

	public void setSourcePath(String srcTxt) {
		this.sourcePath.setText(srcTxt);
	}

	public String getDestinationPath() {
		return destinationPath.getText();
	}

	public void setDestinationPath(String targetTxt) {
		this.destinationPath.setText(targetTxt);
	}

	public JTextArea getLogArea() {
		return logMessages;
	}

	public void setLogArea(JTextArea logMessages) {
		this.logMessages = logMessages;
	}

	public void disableDestinationPath() {
		destinationPath.setEditable(false);
	}
}
