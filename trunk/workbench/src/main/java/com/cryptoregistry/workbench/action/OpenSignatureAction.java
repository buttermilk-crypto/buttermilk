package com.cryptoregistry.workbench.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.EventObject;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.util.Lf2SpacesIndenter;
import com.cryptoregistry.workbench.CreateKeyEvent;
import com.cryptoregistry.workbench.CreateKeyListener;
import com.cryptoregistry.workbench.KeyWrapper;
import com.cryptoregistry.workbench.SignatureItemSelectionDialog;
import com.cryptoregistry.workbench.UUIDTextPane;
import com.cryptoregistry.workbench.UnlockKeyEvent;
import com.cryptoregistry.workbench.UnlockKeyListener;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Indenter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenSignatureAction extends AbstractAction implements CreateKeyListener, UnlockKeyListener {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabs;
	private Set<KeyWrapper> keys;

	public OpenSignatureAction(JTabbedPane tabs) {
		this.tabs = tabs;
		this.putValue(Action.NAME, "Signature Dialog...");
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
		if (!isValidJSON(text)) {
			JOptionPane.showMessageDialog(comp,
					"Not valid, try validation first", 
					"Formatting Results",
					JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(comp);
			SignatureItemSelectionDialog dialog = new SignatureItemSelectionDialog(topFrame,text);
			System.err.println(dialog.isOK());
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

	@Override
	public void keyUnlocked(EventObject evt) {
		UnlockKeyEvent uevt = (UnlockKeyEvent)evt;
		CryptoKey key = uevt.getKey();
		KeyWrapper wrapper = new KeyWrapper(key);
		keys.add(wrapper);
	}

	@Override
	public void keyCreated(CreateKeyEvent evt) {
		KeyWrapper wrapper = new KeyWrapper(evt.getKey());
		keys.add(wrapper);
	}

}
