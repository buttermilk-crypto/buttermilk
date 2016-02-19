package com.cryptoregistry.workbench;

import java.awt.Component;
import java.awt.Insets;
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
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

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
	
	public String identifier;
	public File targetFile;
	JPopupMenu popup;
	String message;
	private Point currentClickPoint;
	
	private UndoManager undoManager=new UndoManager();

	public UUIDTextPane() {
		super();
		getDocument().addUndoableEditListener(undoManager);
		identifier = UUID.randomUUID().toString();
		setCaretPosition(0);
	    setMargin(new Insets(5,5,5,5));
		createPopup();
	}
	
	public UUIDTextPane(File file) {
		this();
		this.targetFile = file;
	}
	
	public UUIDTextPane(String uuid) {
		this();
		this.identifier = uuid;
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
			JMenuItem undo = new JMenuItem("Undo");
			editMenu.add(undo);
			undo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
						undoManager.undo();
				}
			});
			
			JMenuItem redo = new JMenuItem("Redo");
			editMenu.add(redo);
			redo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
						undoManager.redo();
				}
			});
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
	
	public boolean paneContainsAtLeastOneSignature() {
		int length = this.getDocument().getLength();
		String t;
		try {
			t = this.getDocument().getText(0, length);
			return t.contains("SignedWith") && t.contains("SignedBy");
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return false;
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
			//System.err.println("open editor for top level: "+last.text);
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
	
	// Undo support - taken from http://java-sl.com/tip_merge_undo_edits.html
	
	 class MyCompoundEdit extends CompoundEdit {
	       
		private static final long serialVersionUID = 1L;
		
			boolean isUnDone=false;
	        public int getLength() {
	            return edits.size();
	        }
	 
	        public void undo() throws CannotUndoException {
	            super.undo();
	            isUnDone=true;
	        }
	        public void redo() throws CannotUndoException {
	            super.redo();
	            isUnDone=false;
	        }
	        public boolean canUndo() {
	            return edits.size()>0 && !isUnDone;
	        }

	        public boolean canRedo() {
	            return edits.size()>0 && isUnDone;
	        }
	 
	    }
	 
	    class UndoManager extends AbstractUndoableEdit implements UndoableEditListener {
	    	
			private static final long serialVersionUID = 1L;
			
			String lastEditName=null;
	        ArrayList<MyCompoundEdit> edits=new ArrayList<MyCompoundEdit>();
	        MyCompoundEdit current;
	        int pointer=-1;
	 
	        public void undoableEditHappened(UndoableEditEvent e) {
	            UndoableEdit edit=e.getEdit();
	            if (edit instanceof AbstractDocument.DefaultDocumentEvent) {
	                try {
	                    AbstractDocument.DefaultDocumentEvent event=(AbstractDocument.DefaultDocumentEvent)edit;
	                    int start=event.getOffset();
	                    int len=event.getLength();
	                    String text=event.getDocument().getText(start, len);
	                    boolean isNeedStart=false;
	                    if (current==null) {
	                        isNeedStart=true;
	                    }
	                    else if (text.contains("\n")) {
	                        isNeedStart=true;
	                    }
	                    else if (lastEditName==null || !lastEditName.equals(edit.getPresentationName())) {
	                        isNeedStart=true;
	                    }
	 
	                    while (pointer<edits.size()-1) {
	                        edits.remove(edits.size()-1);
	                        isNeedStart=true;
	                    }
	                    if (isNeedStart) {
	                        createCompoundEdit();
	                    }
	 
	                    current.addEdit(edit);
	                    lastEditName=edit.getPresentationName();
	 
	                } catch (BadLocationException e1) {
	                    e1.printStackTrace();
	                }
	            }
	        }
	 
	        public void createCompoundEdit() {
	            if (current==null) {
	                current= new MyCompoundEdit();
	            }
	            else if (current.getLength()>0) {
	                current= new MyCompoundEdit();
	            }
	 
	            edits.add(current);
	            pointer++;
	        }
	 
	        public void undo() throws CannotUndoException {
	            if (!canUndo()) {
	                throw new CannotUndoException();
	            }
	 
	            MyCompoundEdit u=edits.get(pointer);
	            u.undo();
	            pointer--;
	 
	        }
	 
	        public void redo() throws CannotUndoException {
	            if (!canRedo()) {
	                throw new CannotUndoException();
	            }
	 
	            pointer++;
	            MyCompoundEdit u=edits.get(pointer);
	            u.redo();
	 
	        }
	 
	        public boolean canUndo() {
	            return pointer>=0;
	        }

	        public boolean canRedo() {
	            return edits.size()>0 && pointer<edits.size()-1;
	        }
	 
	    }

		public UndoManager getUndoManager() {
			return undoManager;
		}
	    
	    
}
