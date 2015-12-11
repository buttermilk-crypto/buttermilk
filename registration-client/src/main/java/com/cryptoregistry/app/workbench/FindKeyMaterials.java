package com.cryptoregistry.app.workbench;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.nio.file.FileVisitResult.*;

public class FindKeyMaterials extends SimpleFileVisitor<Path> {
	
	private List<String> paths;
	private DefaultMutableTreeNode currentPathNode;
	private DefaultMutableTreeNode currentRegHandleNode;
	private DefaultMutableTreeNode currentCategoryNode;

	public FindKeyMaterials() {
		super();
		paths = new ArrayList<String>();
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
		if(file.getFileName().toString().contains("json")){
			paths.add(file.toString());
		}
		return CONTINUE;
	}
	
	public List<String> getPaths() {
		return paths;
	}
	
	public void iterate(String path, DefaultMutableTreeNode rootTreeNode) {
		try {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(new File(path));  
		currentPathNode = new DefaultMutableTreeNode(path);
		Iterator<Map.Entry<String,JsonNode>> fields = rootNode.fields();
		while (fields.hasNext()) {
		    Map.Entry<String,JsonNode> field = fields.next();
		    String key = field.getKey();
		    switch(key){
		    	case "RegHandle" : {
		    		String regHandle = field.getValue().toString();
		    		currentRegHandleNode = new DefaultMutableTreeNode(regHandle);
		    		currentPathNode.add(currentRegHandleNode);
		    		rootTreeNode.add(currentPathNode);
		    		break;
		    	}
		    	case "Contacts": {
		    		
		    		currentCategoryNode = new DefaultMutableTreeNode("Contacts");
		    		currentRegHandleNode.add(currentCategoryNode);
		    		iterNode(field,currentCategoryNode);
		    		break;
		    	}
		    	case "Data": {
		    		
		    		currentCategoryNode = new DefaultMutableTreeNode("Data");
		    		currentRegHandleNode.add(currentCategoryNode);
		    		iterDataNode(field,currentCategoryNode);
		    		break;
		    	}
		    	case "Keys": {
		    		
		    		currentCategoryNode = new DefaultMutableTreeNode("Keys");
		    		currentRegHandleNode.add(currentCategoryNode);
		    		iterNode(field,currentCategoryNode);
		    		break;
		    	}
		    	case "Signatures": {
		    	
		    		currentCategoryNode = new DefaultMutableTreeNode("Signatures");
		    		currentRegHandleNode.add(currentCategoryNode);
		    		iterNode(field,currentCategoryNode);
		    		break;
		    	}
		    }
		}
		}catch(Exception x){
			x.printStackTrace();
		}
	}
	
	private void iterDataNode( Map.Entry<String,JsonNode> field, DefaultMutableTreeNode currentCategoryNode){
		JsonNode node = field.getValue();
		Iterator<Map.Entry<String,JsonNode>> iter = node.fields();
		while(iter.hasNext()){
			Map.Entry<String,JsonNode> entry = iter.next();
			DefaultMutableTreeNode localNode = new DefaultMutableTreeNode("Local");
			currentCategoryNode.add(localNode);
			iterNode(entry,localNode);
		}
	}
	
	private void iterNode(Map.Entry<String,JsonNode> field, DefaultMutableTreeNode categoryTreeNode){
		JsonNode node = field.getValue();
		Iterator<Map.Entry<String,JsonNode>> uuids = node.fields();
		while (uuids.hasNext()) {
		    Map.Entry<String,JsonNode> uuidMapEntry = uuids.next();
		    String uuid = uuidMapEntry.getKey();
		    DefaultMutableTreeNode uuidNode = new DefaultMutableTreeNode(uuid);
		    categoryTreeNode.add(uuidNode);
		    JsonNode mapdata = uuidMapEntry.getValue();
    		Iterator<Map.Entry<String,JsonNode>> mapdataIter = mapdata.fields();
    		while (mapdataIter.hasNext()) {
    		    Map.Entry<String,JsonNode> dataEntry = mapdataIter.next();
    		    String dataKey = dataEntry.getKey();
    		    JsonNode dataValue = dataEntry.getValue();
    		    dataValue.asText();
    		    StringBuffer buf = new StringBuffer();
    		    buf.append(dataKey);
    		    buf.append("=");
    		    buf.append(dataValue);
    		    DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(buf.toString());
    		    uuidNode.add(keyNode);
    		}
		}
	}

	public static void main(String [] args){
		try {
			FindKeyMaterials f = new FindKeyMaterials();
			Files.walkFileTree(new File("./km").toPath(), f);
			for(String path : f.paths){
				f.iterate(path,new DefaultMutableTreeNode("Files"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
