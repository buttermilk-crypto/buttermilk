package com.cryptoregistry.workbench;

import javax.swing.JDialog;

public class CreateKeyDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private CreateKeyPanel panel;

	public CreateKeyDialog(WorkbenchGUI gui) {
		super(gui.getFrame(), "Simple Key Creation Dialog");
		panel = new CreateKeyPanel(this, gui.getProps());
		getContentPane().add(panel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
	//	setVisible(true);
	}
	
	public void open() {
		this.setVisible(true);
		panel.reinitialize();
	}
	
	public boolean addCreateKeyListener(CreateKeyListener e) {
		return panel.addCreateKeyListener(e);
	}

	public CreateKeyPanel getPanel() {
		return panel;
	}

}
