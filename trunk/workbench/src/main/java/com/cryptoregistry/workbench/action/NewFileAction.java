package com.cryptoregistry.workbench.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.cryptoregistry.workbench.ButtonTabComponent;
import com.cryptoregistry.workbench.UUIDTextPane;
import com.cryptoregistry.workbench.WorkbenchGUI;

public class NewFileAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private JFileChooser fc;

	private JTabbedPane tabs;
	private File file;

	public NewFileAction(JTabbedPane tabs, JFileChooser fc) {
		this.tabs = tabs;
		this.fc = fc;
		this.putValue(Action.NAME, "New File...");
	}

	@Override
	public void actionPerformed(ActionEvent evt) {

		Component comp = (Component) evt.getSource();

		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"JSON and TXT files", "json", "js", "txt");
		fc.setFileFilter(filter);

		int returnVal = fc.showOpenDialog(comp);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			String title = file.getName();
			UUIDTextPane pane = new UUIDTextPane(file);
			pane.setFont(WorkbenchGUI.plainTextFont);
			pane.setTargetFile(file);
			JScrollPane scroll = new JScrollPane(pane);
			tabs.add(title,scroll);
        	tabs.setTabComponentAt(tabs.indexOfComponent(scroll), new ButtonTabComponent(tabs));
			pane.requestFocusInWindow();
		}
	}

}
