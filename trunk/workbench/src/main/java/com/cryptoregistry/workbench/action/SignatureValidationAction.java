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

import asia.redact.bracket.properties.Properties;

import com.cryptoregistry.workbench.ExceptionHolder;

import com.cryptoregistry.workbench.UUIDTextPane;

/**
 * We expect the signature UUID to be validated is highlighted on the current pane. We
 * also want the current pane to contain all the bits required
 * @author Dave
 *
 */
public class SignatureValidationAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabs;
	private Properties props;
	private JLabel statusLabel;
	private ExceptionHolder exception;

	public SignatureValidationAction(JTabbedPane tabs, Properties props, JLabel statusLabel) {
		this.tabs = tabs;
		this.putValue(Action.NAME, "Validate Signature");
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
				    "You must select a signature UUID for this to work",
				    "Signature Validation Results",
				    JOptionPane.INFORMATION_MESSAGE);
			
			return;
		}
		
		final String handle = quoteRemover(input.trim());
		
		
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
	
	private String quoteRemover(String in){
		if(in.startsWith("\"") && in.endsWith("\"")){
			return in.substring(1, in.length()-1);
		}else return in;
	}

}