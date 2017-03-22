package CloudGui;

import java.io.File;

import javax.swing.JFileChooser;

public class LocalFileTree {
	private JFileChooser chooser;
	private String lastOpened;

	public LocalFileTree(String homeDirectoryPath) {
		chooser = new JFileChooser(homeDirectoryPath);
		lastOpened = homeDirectoryPath;
	}

	public void openSelectionGUI(boolean directoriesOnly) {	
		if (directoriesOnly)
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		else
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int choice = chooser.showOpenDialog(chooser);
		if (choice != JFileChooser.APPROVE_OPTION)
			return;
	}
	
	public void openSelectionGUI(boolean directoriesOnly, String path) {
		String openPath = path;
		
		if (path == null || path.length() == 0)
				openPath = lastOpened;
		
		chooser = new JFileChooser(openPath);

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
		if (chosenFile == null)
			return null;
		
		return (lastOpened = chosenFile.getAbsolutePath());
	}
}
