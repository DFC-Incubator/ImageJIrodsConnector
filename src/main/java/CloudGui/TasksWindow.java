package CloudGui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import file_transfer.ExecutorOperations;
import CloudGui.TransferProgressTable.UpdatableTableModel;

public class TasksWindow {
	private JPanel panel;
	private JRadioButton uploadRadioButton, downloadRadioButton;
	private JTextField sourcePath;
	JLabel lblSrc, targetLbl;
	private JTextField destinationPath;
	// button to open file chooser for the source file
	private JButton btnFileChooser1;
	// button to open the file chooser for the target file
	private JButton btnFileChooser2;
	// button to start download/upload
	private JButton btnStart;
	// msgs will be used for displaying task related information to user
	private Logger logger;
	// table with file transfer progress bars
	private TransferProgressTable progressTable;
	
	JPanel rPanel7;

	public void draw() {
		// main panel is composed of five little panels
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		JPanel rPanel1 = new JPanel(new FlowLayout());
		JPanel rPanel2 = new JPanel(new FlowLayout());
		JPanel rPanel3 = new JPanel(new FlowLayout());
		JPanel rPanel4 = new JPanel(new FlowLayout());
		JPanel rPanel5 = new JPanel(new FlowLayout());
		JPanel rPanel6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		rPanel7 = new JPanel(new GridLayout(1,1));
		
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
		lblSrc = new JLabel("Source: ");
		sourcePath = new JTextField("", 25);
		sourcePath.setEditable(false);
		btnFileChooser1 = new JButton("Browse");
		rPanel2.add(lblSrc);
		rPanel2.add(sourcePath);
		rPanel2.add(btnFileChooser1);

		// destination path components
		targetLbl = new JLabel("Target: ");
		destinationPath = new JTextField("", 25);
		destinationPath.setEditable(false);
		btnFileChooser2 = new JButton("Browse");
		rPanel3.add(targetLbl);
		rPanel3.add(destinationPath);
		rPanel3.add(btnFileChooser2);

		// button for starting the upload/download
		btnStart = new JButton("Start");
		rPanel4.add(btnStart);
		
		rPanel5.setPreferredSize(new Dimension(1, 10));

		// area for displaying the status of the upload/download
		JLabel lblTableTitle = new JLabel("Transfer Statistics");
		lblTableTitle.setForeground(Color.BLUE);
		rPanel6.add(lblTableTitle);
		
		// draw the table with progress bars
		rPanel7.setPreferredSize(new Dimension(650, 280));
		progressTable = new TransferProgressTable();
		progressTable.draw();
		rPanel7.add(new JScrollPane(progressTable.getProgressTable()));
		
		// add all the components in the main panel
		panel.add(rPanel1);
		panel.add(rPanel2);
		panel.add(rPanel3);
		panel.add(rPanel4);
		panel.add(rPanel5);
		panel.add(rPanel6);
		panel.add(rPanel7);
	}

	public void reset() {
		GuiUtils.enableComponentsFromContainer(panel, false);
		resetSelectionPaths();
		progressTable.getProgressTable().getTableHeader().setForeground(Color.BLACK);
		UIDefaults defaults = UIManager.getLookAndFeelDefaults();
		progressTable.getProgressTable().getTableHeader().setFont(defaults.getFont("TextField.font"));
	}

	public void enable() {
		GuiUtils.enableComponentsFromContainer(panel, true);
		lblSrc.setForeground(Color.BLUE);
		targetLbl.setForeground(Color.BLUE);
		progressTable.getProgressTable().getTableHeader().setForeground(Color.BLACK);
		progressTable.getProgressTable().getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));;
	}

	public void resetSelectionPaths() {
		sourcePath.setText("");
		destinationPath.setText("");
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

	public Logger getLogger() {
		return logger;
	}

	public void disableDestinationPath() {
		destinationPath.setEditable(false);
	}

	public UpdatableTableModel getProgressTableModel() {
		return progressTable.getProgressTableModel();
	}
	
	public void setDownloadExecutor(ExecutorOperations downloadExecutor) {
		progressTable.setDownloadOperations(downloadExecutor);
	}
	
	public void setUploadExecutor(ExecutorOperations uploadExecutor) {
		progressTable.setUploadOperations(uploadExecutor);
	}
	
	public void setDeleteExecutor(ExecutorOperations deleteExecutor) {
		progressTable.setDeleteOperations(deleteExecutor);
	}
	
	public void setNewFolderExecutor(ExecutorOperations newFolderExecutor) {
		progressTable.setNewFolderOperations(newFolderExecutor);
	}
}
