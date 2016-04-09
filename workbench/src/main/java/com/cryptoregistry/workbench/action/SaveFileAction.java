/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import com.cryptoregistry.workbench.UUIDTextPane;


public class SaveFileAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private JFileChooser fc;
	private JTabbedPane tabs;
	private boolean saveTo;
	
	private JLabel statusPane;
	
	public SaveFileAction(boolean saveTo, JTabbedPane tabs, JFileChooser fc, JLabel statusPane) {
		super();
		this.fc = fc;
		this.tabs = tabs;
		this.saveTo = saveTo;
		this.statusPane = statusPane;
		if(this.saveTo){
			this.putValue(Action.NAME, "Save...");
			this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		}else{
			this.putValue(Action.NAME, "Save");
			this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		int index = tabs.getSelectedIndex();
		UUIDTextPane pane = (UUIDTextPane) ((JScrollPane)tabs.getComponentAt(index)).getViewport().getView();
		File file = null;
		// if no file is set, we'll get one with the dialog. We'll also get one anyway if doing Save To...
		if(pane.getTargetFile() == null) {
			// file was not set, request where to save to using dialog
			Component comp = (Component) evt.getSource();
			int returnVal = fc.showSaveDialog(comp);
			  if (returnVal == JFileChooser.APPROVE_OPTION) {
		           file = fc.getSelectedFile();
		          String name = file.getName();
		          if(name.endsWith("json") || name.endsWith("txt")) {
		        	  //just dandy
		          }else{
		        	  //rename file
		        	  name = name+".json.txt";
		        	 file = new File(file.getParent(),name);
		          }
		           String title = file.getName();
		           try {
					Files.write(file.toPath(), pane.getText().getBytes(StandardCharsets.UTF_8));
					statusPane.setText("Saved: "+file.getCanonicalPath());
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(comp,
						    "I/O or other problem...",
						    "Write File Results",
						    JOptionPane.ERROR_MESSAGE);
					return;
				} 
		          tabs.setTitleAt(index, title);
		          pane.setTargetFile(file);
		          pane.requestFocusInWindow();
			  }
			
		}else{
			// found targetFile set
			 try {
					Files.write(pane.getTargetFile().toPath(), pane.getText().getBytes(StandardCharsets.UTF_8));
					statusPane.setText("Saved: "+pane.getTargetFile().getCanonicalPath());
			} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(pane,
						    "I/O or other problem...",
						    "Write File Results",
						    JOptionPane.ERROR_MESSAGE);
					return;
			} 
		    tabs.setTitleAt(index, pane.getTargetFile().getName());
		    pane.requestFocusInWindow();
		}
		
	}

}
