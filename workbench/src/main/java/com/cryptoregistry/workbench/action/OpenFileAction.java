package com.cryptoregistry.workbench.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.cryptoregistry.workbench.ButtonTabComponent;
import com.cryptoregistry.workbench.UUIDTextPane;
import com.cryptoregistry.workbench.WorkbenchGUI;

public class OpenFileAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private JFileChooser fc;
	
	private JTabbedPane tabs;
	private  File file;
	private boolean newTab;

	public OpenFileAction(boolean newTab, JTabbedPane tabs, JFileChooser fc) {
		this.tabs = tabs;
		this.fc = fc;
		this.newTab = newTab;
		if(newTab) {
			this.putValue(Action.NAME, "Open File...");
		}else{
			this.putValue(Action.NAME, "Open File In Current Tab...");
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Component comp = (Component) evt.getSource();
		
		  FileNameExtensionFilter filter = new FileNameExtensionFilter(
			   "JSON and TXT files", "json", "js", "txt");
		  fc.setFileFilter(filter);
		  
		int returnVal = fc.showOpenDialog(comp);
		   int currentIndex = 0;
		  if (returnVal == JFileChooser.APPROVE_OPTION) {
	            file = fc.getSelectedFile();
	            String title = file.getName();
	            UUIDTextPane pane = null;
	         
	            currentIndex = tabs.getSelectedIndex();
	            
	            if(newTab){
	            	pane = new UUIDTextPane(file);
					pane.setFont(WorkbenchGUI.plainTextFont);
	            	JScrollPane scroll= new JScrollPane(pane);
	            	tabs.add(title,scroll);
	            	tabs.setTabComponentAt(tabs.indexOfComponent(scroll), new ButtonTabComponent(tabs));
	            	tabs.setSelectedIndex(currentIndex+1);
	            	
	            }else{
	            	if(currentIndex == -1) return; // fail because no tabs found
	            	pane = (UUIDTextPane) ((JScrollPane)tabs.getComponentAt(currentIndex)).getViewport().getView();
	            	pane.setTargetFile(file);
	            	tabs.setTitleAt(currentIndex, file.getName());
	            	tabs.setSelectedIndex(currentIndex+1);
	            	
	            }
				
				try {
					String s = new String(Files.readAllBytes(file.toPath()),StandardCharsets.UTF_8);
					pane.setText(s);
					pane.requestFocusInWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
	      }
	}

}
