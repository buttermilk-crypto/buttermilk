/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import asia.redact.bracket.properties.Properties;
import asia.redact.bracket.properties.PropertiesImpl;

import com.cryptoregistry.MapData;

import javax.swing.JComboBox;

public class EditAttributePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JTable table;
	JComboBox<Template> comboBox;
	JLabel uuidText;
	int rowIndex;
	List<Template> templates; 
	
	EditAttributeDialog owner;
	boolean OK = false;
	
	public EditAttributePanel() {
		this(new JFrame(), "...", new HashMap<String,String>());
	}
	
	public EditAttributePanel(final JFrame frame, String id, Map<String,String> map){
		this(frame);
		uuidText.setText(id);
		AttributeTableModel model = (AttributeTableModel) table.getModel();
		model.load(id, map);
	}

	public EditAttributePanel(final JFrame frame) {
		templates = new ArrayList<Template>();
		loadTemplates();
		JLabel lblUuid = new JLabel("UUID: ");
		
		uuidText = new JLabel("...");
		
		JPanel panel = new JPanel();
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				owner.setVisible(false);
				owner.dispose();
				OK = true;
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				owner.setVisible(false);
				owner.dispose();
				OK = false;
			}
		});
		
		JButton btnAddRow = new JButton("+");
		btnAddRow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AttributeTableModel model = (AttributeTableModel)table.getModel();
				if(rowIndex != -1) {
					model.addRow(rowIndex, "", "");
					table.getSelectionModel().setSelectionInterval(rowIndex, rowIndex); // doesn't seem to work
				}
				rowIndex = -1;
				
			}
		});
		
		JButton btnDeleteRow = new JButton("-");
		btnDeleteRow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AttributeTableModel model = (AttributeTableModel)table.getModel();
				if(rowIndex != -1) {
					model.deleteRow(rowIndex);
					table.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);// doesn't seem to work
				}
				rowIndex = -1;
			}
		});
		
		JButton btnRevert = new JButton("Revert");
		btnRevert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AttributeTableModel model = (AttributeTableModel)table.getModel();
				model.revert();
			}
		});
		
		comboBox = new JComboBox<Template>();
		DefaultComboBoxModel<Template> model = (DefaultComboBoxModel<Template>) comboBox.getModel();
		for(Template t: templates) {
			model.addElement(t);
		}
		
		JLabel lblTemplates = new JLabel("Templates:");
		
		JButton btnUse = new JButton("Use");
		btnUse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			      Template t = (Template)comboBox.getSelectedItem();
			      AttributeTableModel atModel = (AttributeTableModel)table.getModel();
			      atModel.update(t.props);
			}
		});
		
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(panel, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(btnRevert)
									.addPreferredGap(ComponentPlacement.RELATED, 145, Short.MAX_VALUE)
									.addComponent(btnDeleteRow)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnAddRow)
									.addGap(18)
									.addComponent(btnCancel)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnOk))))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(68)
							.addComponent(lblTemplates)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboBox, 0, 258, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnUse))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblUuid)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(uuidText)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblUuid)
						.addComponent(uuidText))
					.addGap(11)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 211, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnUse)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblTemplates))
					.addPreferredGap(ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnOk)
						.addComponent(btnCancel)
						.addComponent(btnAddRow)
						.addComponent(btnDeleteRow)
						.addComponent(btnRevert))
					.addContainerGap())
		);
		
		//table = new JTable();
		
		table = new JTable();
		
		table.setCellSelectionEnabled(true);
		final AttributeTableModel atModel = new AttributeTableModel();
		table.setModel(atModel);
		//table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
				rowIndex = table.getSelectedRow();
			}
		});
		JScrollPane scroll = new JScrollPane(table);
		panel.setLayout(new GridLayout(1, 0, 0, 0));
		panel.add(scroll);
		setLayout(groupLayout);
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(400);
		this.setPreferredSize(new Dimension(600,330));
		frame.getRootPane().setDefaultButton(btnOk);
	}
	
	public void init(){
		AttributeTableModel model = (AttributeTableModel) table.getModel();
		model.clear();
		this.comboBox.setSelectedIndex(0);
	}
	
	@SuppressWarnings("rawtypes")
	private void loadTemplates() {
		templates.add(new Template("Available Templates...", new PropertiesImpl()));
		List list = ClassSearchUtils.searchClassPath("template", ".properties");
		if(list == null || list.size() == 0){
			System.err.println("Warning, template list size is zero.");
		}
		for(Object obj:list){
			String s = String.valueOf(obj);
			InputStream in = this.getClass().getResourceAsStream(String.valueOf(obj));
			Properties props = Properties.Factory.getInstance(in);
			templates.add(new Template(s,props));
		}
	}
	
	public MapData toMapData() {
		AttributeTableModel model = (AttributeTableModel) table.getModel();
		return model.toMapData();
	}
	
	private static class Template {
		
		final String name; // from file name
		final Properties props;
		
		public Template(String name, Properties props){
			this.name = name;
			this.props = props;
		}
		
		public String toString() {
			if(name.endsWith(".properties")){
				return name.substring(1, name.length()-11);
			}else{
				return name;
			}
		}
		
	}
	
	public EditAttributeDialog getOwner() {
		return owner;
	}

	public void setOwner(EditAttributeDialog owner) {
		this.owner = owner;
	}

	public static final void main(String [] str){
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	InputStream in = Thread.currentThread().getContextClassLoader()
							.getResourceAsStream("regwizard.properties");
					Properties props = Properties.Factory.getInstance(in);
					JFrame frame = new JFrame();
					frame.getContentPane().setLayout(new BorderLayout());
					MapData data = new MapData(UUID.randomUUID().toString(),props.getFlattenedMap());
					frame.getContentPane().add(new EditAttributePanel(frame, data.uuid,data.data), BorderLayout.CENTER);
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.pack();
					frame.setVisible(true);
	            }
	        });
		
	}
}
