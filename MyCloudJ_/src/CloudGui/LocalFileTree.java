package CloudGui;

import java.io.File;

import javax.swing.JFileChooser;

public class LocalFileTree {
	private String homeDirectoryPath;
	private JFileChooser chooser;

	public LocalFileTree(String homeDirectoryPath) {
		this.homeDirectoryPath = homeDirectoryPath;
	}

	public void openSelectionGUI(boolean directoriesOnly) {	
		// JFileChooser opens in current directory (imagej plugins/)
		chooser = new JFileChooser(homeDirectoryPath);

		if (directoriesOnly)
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		else
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int choice = chooser.showOpenDialog(chooser);
		if (choice != JFileChooser.APPROVE_OPTION)
			return;
	}

	public String getSelectedFilePath() {
		File chosenFile = chooser.getSelectedFile();
		return chosenFile.getAbsolutePath();
	}
}
