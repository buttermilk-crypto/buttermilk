package com.cryptoregistry.workbench.action;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

import asia.redact.bracket.properties.Properties;

import com.cryptoregistry.workbench.RegHandleChecker;
import com.cryptoregistry.workbench.UUIDTextPane;

public class CheckRegHandleAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabs;
	private Properties props;
	private JLabel statusLabel;
	
	private RegHandleChecker checker;
	private ExceptionHolder exception;

	public CheckRegHandleAction(JTabbedPane tabs, Properties props, JLabel statusLabel) {
		this.tabs = tabs;
		this.putValue(Action.NAME, "Check Handle Availability");
		this.props = props;
		this.statusLabel = statusLabel;
		exception = new ExceptionHolder();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Component comp = (Component) evt.getSource();
		int index = tabs.getSelectedIndex();
		if (index == -1) return; // fail because no tabs found
		final UUIDTextPane pane = (UUIDTextPane) ((JScrollPane) tabs.getComponentAt(index)).getViewport().getView();
		
		String input = null;

		if(selectionFound(pane)){
			input = pane.getSelectedText(); // do selection or clipboard only
		}else{
			
			// check clip board
			String clip = this.getClipboard();
			
			if(clip != null){ // data on the clipboard may be a handle
				input = clip;
			}else{
			   JOptionPane.showMessageDialog(comp,
				    "You must select some text put on clipboard for this to work",
				    "Registration Handle Check Results",
				    JOptionPane.INFORMATION_MESSAGE);
			}
			
			return;
		}
		
		statusLabel.setText("Checking...");
		checker = new RegHandleChecker(props);
		
		final String handle = quoteRemover(input.trim());
		
		SwingWorker<Boolean,String> worker = new SwingWorker<Boolean,String>() {
			
			@Override
			protected Boolean doInBackground() throws Exception {
				try {
				return checker.check(handle);
				}catch(RuntimeException x){
					exception.ex = x;
					return false;
				}
			}
			
			 @Override
			public void done() {
				 try {
					 statusLabel.setText("");
					 int end = pane.getSelectionEnd();
					 
						if(get()) {
							pane.getDocument().insertString(end, " Available!", null);
						}else{
							if(exception.hasException()){
								String s = exception.ex.getMessage();
								pane.getDocument().insertString(end, " "+s, null);
								
							}else{
								pane.getDocument().insertString(end, " Not Available, Sorry.", null);
							}
						}
						// deselects
						pane.setSelectionStart(end);
						pane.setSelectionEnd(end);
						
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		worker.execute();
	}
	
	/**
	 * Return true if select start and end are not equal
	 * 
	 * @param pane
	 * @return
	 */
	private boolean selectionFound(JTextPane pane) {
		final int selectionStart = pane.getSelectionStart();
		final int selectionEnd = pane.getSelectionEnd();
		return selectionStart != selectionEnd;
	}
	
	private static class ExceptionHolder{
		
		public Exception ex;

		public ExceptionHolder() {}
		
		public boolean hasException() {
			return ex != null;
		}
	}
	
	private String quoteRemover(String in){
		if(in.startsWith("\"") && in.endsWith("\"")){
			return in.substring(1, in.length()-1);
		}else return in;
	}
	
	private String getClipboard() {
		Clipboard c=Toolkit.getDefaultToolkit().getSystemClipboard();
		try {
			return String.valueOf( c.getData(DataFlavor.stringFlavor) );
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
