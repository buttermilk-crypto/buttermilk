package com.cryptoregistry.workbench.action;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;


public class CloseFileAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private JFileChooser fc;
	private JTabbedPane tabs;
	@SuppressWarnings("unused")
	private boolean saveTo;
	
	public CloseFileAction(JTabbedPane tabs, JFileChooser fc) {
		super();
		this.fc = fc;
		this.tabs = tabs;
		this.putValue(Action.NAME, "Close Selected Tab");
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		int index = tabs.getSelectedIndex();
		tabs.remove(index);
	}

}
