package CloudGui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import file_transfer.ExecutorOperations;
import file_transfer.FileTransferException;
import file_transfer.Transfer;

public class TransferProgressTable {
	private JTable progressTable;
	private UpdatableTableModel progressTableModel;
	private ExecutorOperations downloadOperations;
	private ExecutorOperations uploadOperations;
	private ExecutorOperations deleteOperations;
	private ExecutorOperations newFolderOperations;

	public void draw() {
		progressTable = new JTable() {
			// tooltip for source, destination path and current file
			public String getToolTipText(MouseEvent e) {
				Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);

				if (colIndex == 0 || colIndex == 1 || colIndex == 2) {
					String toolTip = null;
					try {
						Object value = getValueAt(rowIndex, colIndex);
						if (value != null) 
							toolTip = value.toString();
					} catch (RuntimeException e1) {
						e1.printStackTrace();
					}
					return toolTip;
				}
				return null;
			}
		};

		// set the model
		progressTableModel = new UpdatableTableModel();
		progressTable.setModel(progressTableModel);

		// dimensions for correct drawing
		progressTable.getColumn("Source").setMinWidth(60);
		progressTable.getColumn("Destination").setMinWidth(90);
		progressTable.getColumn("Current File").setMinWidth(120);
		progressTable.getColumn("Progress").setMinWidth(190);
		progressTable.getColumn("Action").setMinWidth(90);

		// set the cell renders
		progressTable.getColumn("Progress").setCellRenderer(
				new ProgressCellRender(progressTableModel.getRows()));
		progressTable.getColumn("Action").setCellRenderer(
				new ButtonRenderer(progressTableModel.getRows()));

		// set the cell editors
		progressTable.getColumn("Action").setCellEditor(
				new ButtonEditor(new JCheckBox(), progressTable));

		// center alignment for cells
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		progressTable.getColumnModel().getColumn(0)
				.setCellRenderer(centerRenderer);
		progressTable.getColumnModel().getColumn(1)
				.setCellRenderer(centerRenderer);
		progressTable.getColumnModel().getColumn(2)
				.setCellRenderer(centerRenderer);
	}

	public void destroy() {
		// TODO
	}

	public JTable getProgressTable() {
		return progressTable;
	}

	public void setProgressTable(JTable progressTable) {
		this.progressTable = progressTable;
	}

	public UpdatableTableModel getProgressTableModel() {
		return progressTableModel;
	}

	public void setProgressTableModel(UpdatableTableModel progressTableModel) {
		this.progressTableModel = progressTableModel;
	}

	public ExecutorOperations getDownloadOperations() {
		return downloadOperations;
	}

	public void setDownloadOperations(ExecutorOperations downloadOperations) {
		this.downloadOperations = downloadOperations;
	}
	
	public void setDeleteOperations(ExecutorOperations deleteOperations) {
		this.deleteOperations = deleteOperations;
	}

	public ExecutorOperations getUploadOperations() {
		return uploadOperations;
	}

	public void setUploadOperations(ExecutorOperations uploadOperations) {
		this.uploadOperations = uploadOperations;
	}

	public ExecutorOperations getNewFolderOperations() {
		return newFolderOperations;
	}

	public void setNewFolderOperations(ExecutorOperations newFolderOperations) {
		this.newFolderOperations = newFolderOperations;
	}

	// Progress Bar Renderer
	class ProgressCellRender extends JProgressBar implements TableCellRenderer {
		List<RowData> rows;
		String progBarText;

		public ProgressCellRender(List<RowData> rows) {
			this.rows = rows;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			int progress = 0;
			String status = "", details;

			setStringPainted(true);
			// scale the progress to [0, 100]
			progress = Math.round(((Float) rows.get(row).getProgress()) * 100f);
			setValue(progress);

			// transfer status: Queued/Downloading/Uploading
			if (progress == 0)
				status = "Queued: ";
			else {
				int transferType = rows.get(row).getTransferType();
				if (transferType == Transfer.DOWNLOAD)
					status = "Downloading: ";
				else if (transferType == Transfer.UPLOAD)
					status = "Uploading: ";
				else if (transferType == Transfer.DELETE)
					status = "Delete: ";
				else if (transferType == Transfer.NEW_FOLDER)
					status = "New Folder: ";
			}

			// details for transfer status: Canceled/Progress in %
			if (rows.get(row).isCanceled)
				details = "Canceled";
			else
				details = progress + "%";
			setString(status + details);

			return this;
		}
	}

	// Action Renderer
	class ButtonRenderer extends JButton implements TableCellRenderer {
		List<RowData> rows;

		public ButtonRenderer(List<RowData> rows) {
			this.rows = rows;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			setText((value == null) ? "" : value.toString());

			// change the label of the button to Details if the progress is
			// canceled or complete
			if (rows.get(row).isCanceled || rows.get(row).getProgress() == 1)
				this.setText("Details");
			else
				this.setText("Cancel");
			return this;
		}
	}

	// Action Editor
	class ButtonEditor extends DefaultCellEditor {

		protected JButton button;
		private String label;
		private UpdatableTableModel model;

		public ButtonEditor(JCheckBox checkBox, final JTable table) {
			super(checkBox);
			button = new JButton();
			this.model = ((UpdatableTableModel) table.getModel());
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final int transferId = table.getSelectedRow();
					final List<RowData> rows = model.getRows();
					try {
						// user clicks the Details button => display transfer
						// details
						if (rows.get(transferId).isCanceled
								|| rows.get(transferId).progress == 1) {

							// TODO: check if we should really use a swing
							// worker
							SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
								@Override
								public Object doInBackground() {
									RowData row = rows.get(transferId);

									JPanel error = new JPanel(new GridLayout(
											7, 7));
									error.add(new JLabel("Source Path"));
									JTextField sourceField = new JTextField(row
											.getSource());
									sourceField.setEditable(false);
									error.setAlignmentX(Component.LEFT_ALIGNMENT);
									error.add(sourceField);

									error.add(new JLabel("Destination Path"));
									JTextField destField = new JTextField(row
											.getDestination());
									destField.setEditable(false);
									error.add(destField);

									error.add(new JLabel("Status:"));
									String statusText;
									if (row.isCanceled)
										statusText = "Canceled ";
									else
										statusText = "Successful ";
									
									
									int transferType = row.getTransferType();
									if (transferType == Transfer.DOWNLOAD)
										statusText += "Download";
									else if (transferType == Transfer.UPLOAD)
										statusText += "Upload";
									else if (transferType == Transfer.DELETE)
										statusText += "Delete";
									else if (transferType == Transfer.NEW_FOLDER)
										statusText += "new folder creation";

									int currProgress = Math.round(((Float) row
											.getProgress()) * 100f);
									if (currProgress == 0)
										statusText += " (while queued)";

									JTextField statusField = new JTextField(
											statusText);
									statusField.setEditable(false);
									error.add(statusField);

									SimpleDateFormat dateFormat = new SimpleDateFormat(
											"HH:mm:ss yyyy-MM-dd ");
									error.add(new JLabel(
											"Transfer queued date"));
									Date queueDate = row.getQueuedDate();
									String queuedDateString = "N/A";
									if (queueDate != null)
										queuedDateString = dateFormat
												.format(queueDate);
									JTextField queuedDateTextField = new JTextField(
											queuedDateString);
									queuedDateTextField.setEditable(false);
									error.add(queuedDateTextField);

									error.add(new JLabel(
											"Transfer start date"));
									Date startDate = row.getStartTransferDate();
									String startDateString = "N/A";
									if (startDate != null)
										startDateString = dateFormat
												.format(startDate);
									JTextField startDateTextField = new JTextField(
											startDateString);
									startDateTextField.setEditable(false);
									error.add(startDateTextField);

									error.add(new JLabel("Transfer end date"));
									Date endDate = row.getEndTransferDate();
									String endDateString = "N/A";
									if (endDate != null)
										endDateString = dateFormat
												.format(endDate);
									JTextField endDateTextField = new JTextField(
											endDateString);
									endDateTextField.setEditable(false);
									error.add(endDateTextField);
									
									error.add(new JLabel("Error"));
									JTextField errorField = new JTextField(row.getError());
									errorField.setEditable(false);
									error.add(errorField);

									final JComponent[] inputs = new JComponent[] { error };
									JOptionPane.showMessageDialog(null, inputs,
											"Transfer Details",
											JOptionPane.PLAIN_MESSAGE);
									return null;
								}
							};
							sw.execute();
							// user clicks the "Cancel" button during download
						} else if (rows.get(transferId).getTransferType() == Transfer.DOWNLOAD) {
							downloadOperations.terminateTransfer(transferId);
							rows.get(transferId).setCanceled(true);
							((UpdatableTableModel) table.getModel())
									.updateTransferStatus(transferId, 0, null,
											true);
							button.setText("Details");
							// user clicks the "Cancel" button during upload
						} else if (rows.get(transferId).getTransferType() == Transfer.UPLOAD) {
							uploadOperations.terminateTransfer(transferId);
							rows.get(transferId).setCanceled(true);
							((UpdatableTableModel) table.getModel())
									.updateTransferStatus(transferId, 0, null,
											true);
							button.setText("Details");

						} else if (rows.get(transferId).getTransferType() == Transfer.DELETE) {
							// TODO
							deleteOperations.terminateTransfer(transferId);
							rows.get(transferId).setCanceled(true);
							((UpdatableTableModel) table.getModel())
									.updateTransferStatus(transferId, 0, null,
											true);
							button.setText("Details");
						} else if (rows.get(transferId).getTransferType() == Transfer.NEW_FOLDER) {
							// TODO
							getNewFolderOperations().terminateTransfer(transferId);
							rows.get(transferId).setCanceled(true);
							((UpdatableTableModel) table.getModel())
									.updateTransferStatus(transferId, 0, null,
											true);
							button.setText("Details");
						}
						
						fireEditingStopped();
					} catch (FileTransferException e1) {
						e1.printStackTrace();
					}
				}
			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			List<RowData> rows = model.getRows();
			int selected = table.getSelectedRow();

			if (selected != -1
					&& (rows.get(selected).isCanceled || rows.get(selected).progress == 1))
				button.setText("Details");

			else {
				label = (value == null) ? "" : value.toString();
				button.setText(label);
			}
			return button;
		}
	}

	// data per table row
	public class RowData {
		private boolean isCanceled;
		private int transferType;
		private String source;
		private String destination;
		private String currentFile;
		private float progress;
		private String actionBtnLabel;
		private Date queuedDate;
		private Date startTransferDate;
		private Date endTransferDate;
		private int transferId;
		private String error = "N/A";

		public RowData(String source, String destination, int transferType, int transferId) {
			this.source = source;
			this.destination = destination;
			this.progress = 0f;
			this.transferType = transferType;
			this.actionBtnLabel = "Cancel";
			this.transferId = transferId;
		}

		public String getDestination() {
			return destination;
		}

		public void setDestination(String destination) {
			this.destination = destination;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public float getProgress() {
			return progress;
		}

		public void setProgress(float progress) {
			this.progress = progress;
		}

		public String getCurrentFile() {
			return currentFile;
		}

		public void setCurrentFile(String currentFile) {
			this.currentFile = currentFile;
		}

		public String getActionBtnLabel() {
			return actionBtnLabel;
		}

		public void setActionBtnLabel(String actionBtnLabel) {
			this.actionBtnLabel = actionBtnLabel;
		}

		public boolean isCanceled() {
			return isCanceled;
		}

		public void setCanceled(boolean isCanceled) {
			this.isCanceled = isCanceled;
		}

		public Date getStartTransferDate() {
			return startTransferDate;
		}

		public void setStartTransferDate(Date startTransferDate) {
			this.startTransferDate = startTransferDate;
		}

		public Date getEndTransferDate() {
			return endTransferDate;
		}

		public void setEndTransferDate(Date endTransferDate) {
			this.endTransferDate = endTransferDate;
		}

		public Date getQueuedDate() {
			return queuedDate;
		}

		public void setQueuedDate(Date queuedDate) {
			this.queuedDate = queuedDate;
		}

		public int getTransferId() {
			return transferId;
		}

		public void setTransferId(int transferId) {
			this.transferId = transferId;
		}

		public int getTransferType() {
			return transferType;
		}

		public void setTransferType(int transferType) {
			this.transferType = transferType;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}
	}

	public class UpdatableTableModel extends AbstractTableModel {
		private static final int MAX_ROWS = 10;
		private List<RowData> rows;

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return (columnIndex == 4);
		}

		public UpdatableTableModel() {
			rows = new ArrayList<>(MAX_ROWS);
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public String getColumnName(int column) {
			String name = "??";
			switch (column) {
			case 0:
				name = "Source";
				break;
			case 1:
				name = "Destination";
				break;
			case 2:
				name = "Current File";
				break;
			case 3:
				name = "Progress";
				break;
			case 4:
				name = "Action";
				break;
			}
			return name;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			RowData rowData = rows.get(rowIndex);
			Object value = null;
			switch (columnIndex) {
			case 0:
				value = rowData.getSource();
				break;
			case 1:
				value = rowData.getDestination();
				break;
			case 2:
				value = rowData.getCurrentFile();
				break;
			case 3:
				value = rowData.getProgress();
				break;
			case 4:
				value = rowData.getActionBtnLabel();
				break;
			}
			return value;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			RowData rowData = rows.get(rowIndex);
			if (aValue == null)
				return;

			switch (columnIndex) {
			case 2:
				String currFile = (String) aValue;
				rowData.setCurrentFile(currFile);
				break;
			case 3:
				int progress = (int) aValue;
				/*
				 * a trick that should be fixed: in order to trigger the
				 * listener of the progress bar every time the "Cancel" button
				 * is pushed we need to update progress value with a very small
				 * value. This is because a cancel operation doesn't update the
				 * progress value and the progress bar listener is not woken up.
				 */
				if (rowData.getProgress() == (float) progress)
					rowData.setProgress((float) (progress / 100f + 0.001));
				else
					rowData.setProgress((float) progress / 100f);
				break;
			}
		}

		public int addTransfer(String source, String dest, int transferType) {
			RowData rowData = new RowData(source, dest, transferType, rows.size());
			rowData.setQueuedDate(new Date());
			rows.add(rowData);
			fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
			return rows.size() - 1;
		}
		
		public void cancelAllTransfers() {
			for (int i = 0; i < rows.size(); i++) {
				RowData row = rows.get(i);
				if (row.isCanceled == false && row.getProgress() != 1.0) {
					updateTransferStatus(i, 0, null, true);
				}
			}
		}

		public void updateTransferStatus(int rowId, int progress,
				String currFile, boolean cancel) {
			RowData rowData = rows.get(rowId);
			if (rowData == null)
				return;

			// user canceled a file transfer or
			if (cancel) {
				/*
				 * trigger the above trick
				 */
				if (rowData.transferType == Transfer.DELETE) {
					if (progress == 2) 
						rowData.setError("Cloud Error: File was not deleted \n");
					else if (progress == 3)
						rowData.setError("Cloud Exception \n");
				} else if (rowData.transferType == Transfer.NEW_FOLDER) {
					if (progress == 2) 
						rowData.setError("Cloud Error: File was not created \n");
					else if (progress == 3)
						rowData.setError("Cloud Exception \n");
				}

				rowData.setCanceled(true);
				rowData.setEndTransferDate(new Date());
				setValueAt((int) (rowData.getProgress() * 100), rowId, 3);
				fireTableCellUpdated(rowId, 3);
				fireTableCellUpdated(rowId, 4);
			} else {
				// canceling will trigger the else branch after the if branch
				// in a subsequent call to updateTransferStatus => TODO
				// investigate
				if (progress > rowData.getProgress() * 100) {
					if (progress == 1)
						rowData.setStartTransferDate(new Date());
					else if (progress == 100)
						rowData.setEndTransferDate(new Date());

					setValueAt(currFile, rowId, 2);
					fireTableCellUpdated(rowId, 2);
					setValueAt(progress, rowId, 3);
					fireTableCellUpdated(rowId, 3);
					fireTableCellUpdated(rowId, 4);
				}
			}
		}

		public List<RowData> getRows() {
			return rows;
		}

		public void setRows(List<RowData> rows) {
			this.rows = rows;
		}
	}
}
