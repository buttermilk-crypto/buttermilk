package com.cryptoregistry.workbench;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;

import com.cryptoregistry.MapData;
import com.cryptoregistry.formats.JSONGenericReader;
import com.cryptoregistry.formats.JSONReader;
import com.cryptoregistry.formats.MapDataFormatter;
import com.cryptoregistry.formats.SignatureFormatter;
import com.cryptoregistry.signature.CryptoSignature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UUIDTextPane extends JTextPane implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static final Pattern DoubleQuotesPat = Pattern.compile("\"([^\"]*)\"");
	private static final String [] topLevelTokens =  {"Contacts", "Data", "Local", "Keys", "Macs", "Signatures"};
	
	public final String identifier;
	public File targetFile;
	public UndoManager manager = new UndoManager();
	JPopupMenu popup;
	String message;
	private Point currentClickPoint;

	public UUIDTextPane() {
		super();
		identifier = UUID.randomUUID().toString();
		this.getDocument().addUndoableEditListener(manager);
		createPopup();
	}
	
	public UUIDTextPane(String uuid) {
		super();
		identifier = uuid;
		this.getDocument().addUndoableEditListener(manager);
		createPopup();
	}
	
	public UUIDTextPane(File file) {
		this();
		this.targetFile = file;
	}

	public UUIDTextPane(StyledDocument arg0) {
		super(arg0);
		identifier = UUID.randomUUID().toString();
		this.getDocument().addUndoableEditListener(manager);
		createPopup();
	}
	
	private void createPopup() {
		 popup = new JPopupMenu();
		 
		 JMenuItem attribItem = new JMenuItem("Attribute Editor...");
		 attribItem.addActionListener(this);
		 attribItem.setActionCommand("edit-attributes");
		 popup.add(attribItem);
		 
		 JMenuItem menuItem = new JMenuItem("Validate JSON");
		 menuItem.setActionCommand("validate");
		 menuItem.addActionListener(this);
		 popup.add(menuItem);
		
		// Build the edit menu.
			JMenu editMenu = new JMenu("Edit");
			popup.add(editMenu);
			JMenuItem cut = new JMenuItem("Cut");
			editMenu.add(cut);
			cut.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
						cut();
				}
			});
			JMenuItem copy = new JMenuItem("Copy");
			editMenu.add(copy);
			copy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
						copy();
				}
			});
			JMenuItem paste = new JMenuItem("Paste");
			editMenu.add(paste);
			paste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
						paste();
				}
			});
			editMenu.addSeparator();
			JMenuItem selectAll = new JMenuItem("Select All");
			editMenu.add(selectAll);
			selectAll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
						selectAll();
				}
			});
			editMenu.addSeparator();
			editMenu.add(UndoManagerHelper.getUndoAction(manager));
			editMenu.add(UndoManagerHelper.getRedoAction(manager));
			editMenu.addSeparator();
			
		   
		 MouseListener popupListener = new PopupListener(this);
		 this.addMouseListener(popupListener);
		  
	}

	public String getIdentifier() {
		return identifier;
	}

	public File getTargetFile() {
		return targetFile;
	}

	public void setTargetFile(File targetFile) {
		this.targetFile = targetFile;
	}
	
	class PopupListener extends MouseAdapter {
		
		UUIDTextPane pane;
		
		public PopupListener(UUIDTextPane pane){
			this.pane = pane;
		}
		
		public void mouseClicked(MouseEvent e) {
		    pane.currentClickPoint = e.getPoint();
		}
		
	    public void mousePressed(MouseEvent e) {
	    //	pane.currentClickPoint = e.getPoint();
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(), e.getX(), e.getY());
	        }
	    }
	}

	// for the popup menus, check by action command
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Component comp = (Component)e.getSource();
		switch(cmd){
			case "validate" : {
				if(isValidJSON(getText())){
					JOptionPane.showMessageDialog(comp,
						    "Valid JSON",
						    "Validation Results",
						    JOptionPane.INFORMATION_MESSAGE);
				}else{
					 JFrame frame = (JFrame) SwingUtilities.getRoot(comp);
					new ValidationDialog(frame, "Validation Error", message);
				}
				break;
			}
			case "edit-attributes": {
				
				int pos = this.viewToModel(this.currentClickPoint);
				String text = this.getText().substring(0, pos+20); // HACK - but should contain what we want
				// make a list of all the quoted items
				ArrayList<Match> list = new ArrayList<Match>();
				Matcher matcher = DoubleQuotesPat.matcher(text);
				while(matcher.find()){
					list.add(new Match(matcher.start(1), matcher.end(1), matcher.group(1)));
				}
				check(1,list);
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
			message = e.getMessage();
			valid = false;
		} catch (IOException e) {
			message = e.getMessage();
			valid = false;
		}
		   return valid;
	}
	
	/**
	 * Return true if this pane contains text which looks like a secure key
	 * @return
	 */
	public boolean paneContainsAtLeastOneSecureKey() {
		return getText().contains("KeyData.EncryptedData");
	}
	
	private static class Match {
		//public final int start, end;
		public final String text;
		public Match(int start, int end, String text) {
			super();
		//	this.start = start;
		//	this.end = end;
			this.text = text;
		}
		
		public String toString() {
			return text;
		}
	}
	
	private void check(int count, List<Match> list){
		if(list.size()-count < 0) return;
		Match last = list.get(list.size()-count);
		boolean topLevel = false;
		for(String s: topLevelTokens){
			if(last.text.equals(s)){
				topLevel = true;
				break;
			}
		}
		if(topLevel){
			System.err.println("open editor for top level: "+last.text);
			JFrame frame = (JFrame) SwingUtilities.getRoot(this);
			EditAttributeDialog dialog = new EditAttributeDialog(frame);
			dialog.open();
			MapData data = dialog.toMapData();
			insert(data);
			return;
		}else if(last.text.length() == 36 || last.text.length() == 38){
				System.err.println("open editor for: "+find(last.text));
				return;
		}else {
			check(count+1, list);
		}
	}
	
	private MapData find(String uuid){
		JSONReader reader = new JSONReader(new StringReader(this.getText()));
		JSONGenericReader gen = reader.genericReader();
		List<MapData> contacts = gen.contacts();
		for(MapData data: contacts){
			if(uuid.contains(data.uuid)) {
				return data;
			}
		}
		List<MapData> local = gen.local();
		for(MapData data: local){
			if(uuid.contains(data.uuid)) {
				return data;
			}
		}
		List<MapData> keys = gen.keys();
		for(MapData data: keys){
			if(uuid.contains(data.uuid)) {
				return data;
			}
		}
		List<MapData> macs = gen.macs();
		for(MapData data: macs){
			if(uuid.contains(data.uuid)) {
				return data;
			}
		}
		List<MapData> sigs = gen.signatures();
		for(MapData data: sigs){
			if(uuid.contains(data.uuid)) {
				return data;
			}
		}
		
		return null;
	}
	
	public void insert(MapData data){
		MapDataFormatter formatter = new MapDataFormatter();
		formatter.add(data);
		String s = formatter.formatAsFragment();
		try {
			this.getDocument().insertString(this.getCaretPosition(), s, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public void insert(CryptoSignature sig){
		SignatureFormatter f = new SignatureFormatter(sig);
		String result = f.format();
		result = result.substring(1, result.length()-1);
		
		try {
			this.getDocument().insertString(this.getCaretPosition(), result, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
}
