package CloudGui;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JTextField;

public class GuiUtils {
	/*
	 * Function to enable/disable components inside a container(works for Nested
	 * containers)
	 * 
	 * Parameters: Container container : container which you have to
	 * disable/enable enabled : boolean value, true(enable) or false(disable)
	 */
	public static void enableComponentsFromContainer(Container container, boolean enabled) {
		Component[] components = container.getComponents();
		if (components.length > 0) {
			for (Component component : components) {
				component.setEnabled(enabled);
				if (component instanceof Container) {
					enableComponentsFromContainer((Container) component, enabled);
				}
			}
		}
	}
	
	public static void enableTextFieldsFromContainer(Container container, boolean enabled) {
		Component[] components = container.getComponents();
		if (components.length > 0) {
			for (Component component : components) {
				if (component instanceof JTextField)
					component.setEnabled(enabled);
				if (component instanceof Container) {
					enableTextFieldsFromContainer((Container) component, enabled);
				}
			}
		}
	}
}
