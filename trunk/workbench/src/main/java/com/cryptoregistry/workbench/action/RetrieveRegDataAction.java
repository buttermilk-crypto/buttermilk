package com.cryptoregistry.workbench.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

import asia.redact.bracket.properties.Properties;

import com.cryptoregistry.workbench.RegHandleDataRetriever;
import com.cryptoregistry.workbench.UUIDTextPane;

public class RetrieveRegDataAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabs;
	private Properties props;
	private JLabel statusLabel;
	
	private RegHandleDataRetriever retriever;
	private ExceptionHolder exception;

	public RetrieveRegDataAction(JTabbedPane tabs, Properties props, JLabel statusLabel) {
		this.tabs = tabs;
		this.putValue(Action.NAME, "Retrieve Registration Data");
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
			input = pane.getSelectedText(); // do only selection
		}else{
			JOptionPane.showMessageDialog(comp,
				    "You must select some text for this to work",
				    "Data Retrieval Results",
				    JOptionPane.INFORMATION_MESSAGE);
			
			return;
		}
		
		statusLabel.setText("Searching on "+props.get("registration.services.hostname")+"...");
		retriever = new RegHandleDataRetriever(props);
		
		final String handle = quoteRemover(input.trim());
		
		SwingWorker<Boolean,String> worker = new SwingWorker<Boolean,String>() {
			
			String val;
			
			@Override
			protected Boolean doInBackground() throws Exception {
				try {
				  val = retriever.retrieve(handle);
				  return val != null;
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
							pane.getDocument().insertString(end, "\n"+val, null);
						}else{
							if(exception.hasException()){
								String s = exception.ex.getMessage();
								pane.getDocument().insertString(end, " "+s, null);
								
							}else{
								pane.getDocument().insertString(end, "\n"+val, null);
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

}
