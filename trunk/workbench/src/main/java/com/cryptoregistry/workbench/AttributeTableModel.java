package com.cryptoregistry.workbench;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.table.AbstractTableModel;

import asia.redact.bracket.properties.Properties;

import com.cryptoregistry.CryptoContact;
import com.cryptoregistry.MapData;


public class AttributeTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private List<Pair> list;
    private String[] columnNames;
    private String id;

    /**
     * Creates a new instance of MapTableModel.
     */
    public AttributeTableModel() {
        super();
        list = new ArrayList<Pair>();
        this.columnNames = new String[2];
        this.columnNames[0] = "Attribute";
        this.columnNames[1] = "Value";
        id = UUID.randomUUID().toString();
    }

    /**
     * Creates a new instance of MapTableModel; non-destructive on the map
     */
    public AttributeTableModel(String uuid, Map<String,String> map) {
        this(uuid, map,"Attribute","Value");
        
    }

    /**
     * Creates a new instance of MapTableModel; non-destructive on the map
     */
    public AttributeTableModel(String uuid, Map<String,String> map, String keyName, String valueName) {
        this();
        mapToList(map);
        setColumnNames(keyName,valueName);
        id = uuid;
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
        return 2;
    }

    /**
     * Returns the value at row,column
     */
    public String getValueAt(int row, int column) {
    	if(column == 0) return list.get(row).key;
    	else if(column == 1) return list.get(row).value;
    	else throw new RuntimeException("Column out of bounds: "+column);
    }

    /**
     * Returns the column name.
     */
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * Sets the column names.
     */
    public void setColumnNames(String keyName, String valueName) {
        String[] names={keyName,valueName};
        columnNames=names;
    }

    
    public boolean isCellEditable(int row, int col){ return true; }
    
    public void setValueAt(Object value, int row, int col) {
    	
    	if(col == 0){
    		Pair p = list.get(row);
    		p.key = String.valueOf(value);
    		this.fireTableCellUpdated(row, col);
    		return;
    	}
    	
    	if(col == 1){
    		Pair p = list.get(row);
    		p.value = String.valueOf(value);
    		this.fireTableCellUpdated(row, col);
    		return;
    	}
    	
    	throw new RuntimeException("Unexpected column: "+col);
    }
    
    public void deleteRow(int rowIndex){
    	list.remove(rowIndex);
    	this.fireTableRowsDeleted(rowIndex, rowIndex);
    }
    
    public void clear() {
    	list.clear();
    	this.fireTableDataChanged();
    }
    
    public void update(Properties props) {
    	list.clear();
    	Map<String,String> map = props.getFlattenedMap();
    	Iterator<String> iter = map.keySet().iterator();
    	while(iter.hasNext()){
    		String key =iter.next();
    		String value = map.get(key);
    		list.add(new Pair(key,value));
    	}
    	this.fireTableDataChanged();
    }
    
    public void addRow(int index, String key, String value){
    	list.add(index, new Pair(key,value));
    	this.fireTableRowsInserted(index, index);
    }
    
    public MapData toMapData() {
    	
    	MapData data = new MapData(id);
    	for(Pair p: list){
    		data.put(p.key, p.value);
    	}
    	
    	return data;
    }
    
 public CryptoContact toCryptoContact() {
    	
    	CryptoContact data = new CryptoContact(id);
    	for(Pair p: list){
    		data.put(p.key, p.value);
    	}
    	
    	return data;
    }
    
    private static class Pair {
    	
    	public String key;
    	public String value;
    	
		public Pair(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}
    }
    
    private List<Pair> mapToList(Map<String, String> map){
    	ArrayList<Pair> list = new ArrayList<Pair>();
    	Iterator<String> iter = map.keySet().iterator();
    	while(iter.hasNext()){
    		String key = iter.next();
    		String value = map.get(key);
    		Pair p = new Pair(key,value);
    		list.add(p);
    	}
    	return list;
    }

} 
