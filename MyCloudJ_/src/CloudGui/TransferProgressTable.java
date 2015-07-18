package CloudGui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class TransferProgressTable {
	private JTable progressTable;
	private UpdatableTableModel progressTableModel;
	JFrame frame;

	public void draw() {
		progressTable = new JTable();
	
		progressTableModel = new UpdatableTableModel();
		progressTable.setModel(progressTableModel);
		progressTable.getColumn("Progress").setCellRenderer(
				new ProgressCellRender(progressTableModel.getRows()));
		
		// center alignment for cells
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		progressTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		progressTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		progressTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

		// TODO: temp solution
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(new JScrollPane(progressTable));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void destroy() {
		frame.dispose();
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

	public class ProgressCellRender extends JProgressBar implements
			TableCellRenderer {
		List<RowData> rows;
		
		public ProgressCellRender(List<RowData> rows) {
			this.rows = rows;			
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			int progress = 0;
			if (value instanceof Float) {
				progress = Math.round(((Float) value) * 100f);
			} else if (value instanceof Integer) {
				progress = (int) value;
			}
			this.setStringPainted(true);
			
			if (progress == 0) 
				this.setString("Queued");
			else {
				String progBarText = rows.get(row).isDownload ? "Downloading: " : "Uploading: ";
				this.setString(progBarText + progress + "%");
			}
			
			setValue(progress);
			return this;
		}
	}

	public class RowData {
		private boolean isDownload;
		private String source;
		private String destination;
		private String currentFile;
		private float progress;

		public RowData(String source, String destination, boolean isDownload) {
			this.source = source;
			this.destination = destination;
			this.progress = 0f;
			this.isDownload = isDownload;
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

		public boolean isDownload() {
			return isDownload;
		}

		public void setDownload(boolean isDownload) {
			this.isDownload = isDownload;
		}

		public String getCurrentFile() {
			return currentFile;
		}

		public void setCurrentFile(String currentFile) {
			this.currentFile = currentFile;
		}
	}

	public class UpdatableTableModel extends AbstractTableModel {
		private static final int MAX_ROWS = 10;
		private List<RowData> rows;

		public UpdatableTableModel() {
			rows = new ArrayList<>(MAX_ROWS);
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int column) {
			String name = "??";
			switch (column) {
			case 0:
				name = "Source Path";
				break;
			case 1:
				name = "Dest Path";
				break;
			case 2:
				name = "Current File";
				break;
			case 3:
				name = "Progress";
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
				rowData.setProgress((float) progress / 100f);
				break;
			}
		}

		public int addFile(String source, String dest, boolean isDownload) {
			RowData rowData = new RowData(source, dest, isDownload);
			rows.add(rowData);
			fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
			return rows.size() - 1;
		}

		public void updateStatus(int rowId, int progress, String currFile) {
			RowData rowData = rows.get(rowId);
			if (rowData != null) {
				setValueAt(currFile, rowId, 2);
				fireTableCellUpdated(rowId, 2);
				setValueAt(progress, rowId, 3);
				fireTableCellUpdated(rowId, 3);
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
