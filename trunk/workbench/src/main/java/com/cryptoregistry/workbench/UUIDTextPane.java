package com.cryptoregistry.workbench;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UUIDTextPane extends JTextPane implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	public final String identifier;
	public File targetFile;
	public UndoManager manager = new UndoManager();
	JPopupMenu popup;
	
	String message;

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
		   
		 MouseListener popupListener = new PopupListener();
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
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
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
