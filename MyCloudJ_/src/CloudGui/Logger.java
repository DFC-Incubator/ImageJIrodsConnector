package CloudGui;

import javax.swing.JTextArea;

public class Logger {
	private JTextArea logMessages;
	
	public Logger () {
		logMessages = new JTextArea();
	}
	
	public synchronized void writeLog(String log) {
		logMessages.append(log);
	}
	
	public synchronized void reset() {
		logMessages.setText("");
	}
	
	public JTextArea getLogger() {
		return logMessages;
	}
}
