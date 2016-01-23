package com.cryptoregistry.workbench.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.text.BadLocationException;

import net.iharder.Base64;

import com.cryptoregistry.workbench.UUIDTextPane;

public class Base64EncodeAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabs;
	private boolean encode;

	public Base64EncodeAction(JTabbedPane tabs, boolean encode, String title) {
		this.tabs = tabs;
		this.putValue(Action.NAME, title);
		this.encode = encode;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		//Component comp = (Component) evt.getSource();
		int index = tabs.getSelectedIndex();
		if (index == -1) return; // fail because no tabs found
		
		UUIDTextPane pane = (UUIDTextPane) ((JScrollPane) tabs.getComponentAt(index)).getViewport().getView();
		String selected = pane.getSelectedText();
		if(selected == null || selected.length() < 1){
			
		}
		
		String output = null;
		
		if(encode) {
			output = Base64.encodeBytes(selected.trim().getBytes(StandardCharsets.UTF_8));
		}else{
			try {
				output = new String(Base64.decode(selected),StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		int end = pane.getSelectionEnd();
		try {
			pane.getDocument().insertString(end, " "+output, null);
		
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

}
