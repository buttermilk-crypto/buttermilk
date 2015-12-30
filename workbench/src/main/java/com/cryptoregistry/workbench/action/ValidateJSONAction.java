package com.cryptoregistry.workbench.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.cryptoregistry.workbench.UUIDTextPane;
import com.cryptoregistry.workbench.ValidationDialog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ValidateJSONAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabs;
	
	private String message;

	public ValidateJSONAction(JTabbedPane tabs) {
		this.tabs = tabs;
		this.putValue(Action.NAME, "Validate JSON");
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Component comp = (Component) evt.getSource();
		int index = tabs.getSelectedIndex();
		if (index == -1) return; // fail because no tabs found
		UUIDTextPane pane = (UUIDTextPane) ((JScrollPane) tabs.getComponentAt(index)).getViewport().getView();
		if(isValidJSON(pane.getText())){
			JOptionPane.showMessageDialog(comp,
				    "Valid JSON",
				    "Validation Results",
				    JOptionPane.INFORMATION_MESSAGE);
		}else{
			 JFrame frame = (JFrame) SwingUtilities.getRoot(comp);
			new ValidationDialog(frame, "Validation Error", message);
		}
	}
	
	public boolean isValidJSON(final String json) {
		   boolean valid = false;
		   ObjectMapper objectMapper = new ObjectMapper();
		   objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
		   try {
			objectMapper.readTree(json);
			valid = true;
		} catch (JsonProcessingException e) {
			message = e.getMessage();
			valid = false;
		} catch (IOException e) {
			message = e.getMessage();
			valid = false;
		}

		   return valid;
		}

}
