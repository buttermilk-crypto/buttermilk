/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.table.AbstractTableModel;


public class SignatureItemTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private List<SigElement> list;
    private String[] columnNames;
    private String id;

    /**
     * Creates a new instance of MapTableModel.
     */
    public SignatureItemTableModel() {
        super();
        list = new ArrayList<SigElement>();
        this.columnNames = new String[3];
        this.columnNames[0] = "Include";
        this.columnNames[1] = "Signature Element";
        this.columnNames[2] = "Value";
        id = UUID.randomUUID().toString();
    }

    /**
     * Creates a new instance of MapTableModel; non-destructive on the map
     */
    public SignatureItemTableModel(String uuid, Map<String,String> map) {
        this(uuid, map,"Signature Element","Value"); 
    }

    /**
     * Creates a new instance of MapTableModel; non-destructive on the map
     */
    public SignatureItemTableModel(String uuid, Map<String,String> map, String keyName, String valueName) {
        this();
        load(id, map);
        setColumnNames("Include", keyName, valueName);
    }
    
    public void load(String id, Map<String,String> map){
    	this.id = id;
    	list = mapToList(map);
    }

    /**
     * Returns the row count.
     */
    public int getRowCount() {
        return list.size();
    }

    /**
     * Returns the column count.
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Returns the value at row,column
     */
    public Object getValueAt(int row, int column) {
    	if(column == 0) return list.get(row).selected;
    	else if(column == 1) return list.get(row).key;
    	else if(column == 2) return list.get(row).value;
    	else throw new RuntimeException("Column out of bounds: "+column);
    }

    /**
     * Returns the column name.
     */
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    public Class<?> getColumnClass(int column){
    	 return (getValueAt(0, column).getClass());
    }

    /**
     * Sets the column names.
     */
    public void setColumnNames(String selectedName, String keyName, String valueName) {
        String[] names={selectedName,keyName,valueName};
        columnNames=names;
    }

    
    public boolean isCellEditable(int row, int col){ 
    	return col == 0;
    }
    
    public void setValueAt(Object value, int row, int col) {
    	
    	if(col == 0){
    		SigElement p = list.get(row);
    		p.selected = Boolean.valueOf(String.valueOf(value));
    		this.fireTableCellUpdated(row, col);
    		return;
    	}
    	
    	if(col == 1){
    		SigElement p = list.get(row);
    		p.key = String.valueOf(value);
    		this.fireTableCellUpdated(row, col);
    		return;
    	}
    	
    	if(col == 2){
    		SigElement p = list.get(row);
    		p.value = String.valueOf(value);
    		this.fireTableCellUpdated(row, col);
    		return;
    	}
    	
    	throw new RuntimeException("Unexpected column: "+col);
    }
    
    public void add(SigElement el){
    	list.add(el);
    }
    
    public static class SigElement {
    	
    	public Boolean selected;
    	public String key;
    	public String value;
    	
		public SigElement(Boolean selected, String key, String value) {
			super();
			this.selected = selected;
			this.key = key;
			this.value = value;
		}
		
		public SigElement clone() {
			return new SigElement(selected, key, value);
		}
    }
    
    private List<SigElement> mapToList(Map<String, String> map){
    	ArrayList<SigElement> list = new ArrayList<SigElement>();
    	Iterator<String> iter = map.keySet().iterator();
    	while(iter.hasNext()){
    		String key = iter.next();
    		String value = map.get(key);
    		SigElement p = new SigElement(false,key,value);
    		list.add(p);
    	}
    	return list;
    }

	public List<SigElement> getList() {
		return list;
	}

} 
