package com.cryptoregistry.workbench;

import javax.swing.JDialog;

public class UnlockedKeyDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private UnlockedKeyPanel panel;

	public UnlockedKeyDialog(WorkbenchGUI gui) {
		// model
		super(gui.getFrame(), "Unlocked Keys", true);
		panel = new UnlockedKeyPanel(this);
		getContentPane().add(panel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(false);
	}
	
	public void open() {
		setVisible(true);
	}

	// implements CreateKeyListener
	public UnlockedKeyPanel getPanel() {
		return panel;
	}
	
}
