package com.cryptoregistry.workbench.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import com.cryptoregistry.util.Lf2SpacesIndenter;
import com.cryptoregistry.workbench.UUIDTextPane;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Indenter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FormatJSONAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabs;

	public FormatJSONAction(JTabbedPane tabs) {
		this.tabs = tabs;
		this.putValue(Action.NAME, "Format JSON");
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Component comp = (Component) evt.getSource();
		int index = tabs.getSelectedIndex();
		if (index == -1)
			return; // fail because no tabs found
		UUIDTextPane pane = (UUIDTextPane) ((JScrollPane) tabs
				.getComponentAt(index)).getViewport().getView();
		String text = pane.getText();
		
		String input = null;
		boolean selection = false;
		if(selectionFound(pane)){
			input = pane.getSelectedText(); // do only selection
			selection = true;
		}else{
			input = pane.getText();
			selection = false;
		}
		
		if(input == null) return;
		
		if (!isValidJSON(input)) {
			JOptionPane.showMessageDialog(comp,
					"Not valid JSON input, try validation first", 
					"Formatting Results",
					JOptionPane.ERROR_MESSAGE);
			return;
			
		} else {
			
			String output = format(input);
			if(output != null){
				if(selection) {
					pane.replaceSelection(output);
				}else{
					pane.setText(output);
				}
				pane.requestFocusInWindow();
			}else{
				JOptionPane.showMessageDialog(comp,
						"Sorry, formatting error of some kind...", 
						"Formatting Results",
						JOptionPane.ERROR_MESSAGE);
			}
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
			valid = false;
		} catch (IOException e) {
			valid = false;
		}

		return valid;
	}
	
	public String format(final String input) {
	
		ObjectMapper mapper = new ObjectMapper();
		DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
		Indenter indenter = new Lf2SpacesIndenter();
		printer.indentObjectsWith(indenter);
		printer.indentArraysWith(indenter);
		
		Object json;
		try {
			json = mapper.readValue(input, Object.class);
			return mapper.writer(printer).writeValueAsString(json);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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


}
