package com.cryptoregistry.app.workbench;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JTextArea;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Scan a root path for Buttermilk-formatted files and show a graphic display
 * 
 * Reg handle
 * 		keys
 * 			uuid - alg - locked
 * 
 * etc
 * 
 * Has concept of current or selected 
 * 
 * Monitor on file status for updates
 * 
 * @author Dave
 *
 */
public class KeyMaterialsPanel extends JPanel implements TreeSelectionListener {

	private static final long serialVersionUID = 1L;
	private JTree tree;
	JScrollPane scrollPane;
	private String rootPath;

	public KeyMaterialsPanel(String rootPath) {
		super();
		this.rootPath = rootPath;
		
		scrollPane = new JScrollPane();
		
		JTextArea textArea = new JTextArea();
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 295, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(textArea, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(textArea, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
						.addComponent(scrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE))
					.addContainerGap())
		);
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Files: "+rootPath);
		tree = new JTree(root);
		tree.addTreeSelectionListener(this);
		scrollPane.setViewportView(tree);
		createNodes(root);
		setLayout(groupLayout);
	}
	
	private void createNodes(DefaultMutableTreeNode root){
		try {
			FindKeyMaterials f = new FindKeyMaterials();
			Files.walkFileTree(new File(rootPath).toPath(), f);
			for(String path : f.getPaths()){
				System.err.println("Looking at: "+path);
				f.iterate(path, root);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	  public static void main(String[] args) {
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            //	InputStream in = Thread.currentThread()
	            //			.getContextClassLoader().getResourceAsStream("regwizard.properties");
	            //	Properties props = Properties.Factory.getInstance(in);
	                JFrame frame = new JFrame("cryptoregistry.com - Registration Key Materials Wizard");
	    	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    	        
	    	        
	    	        frame.getContentPane().add(new KeyMaterialsPanel("./km"));
	    	        
	    	        //Display the window.
	    	        frame.pack();
	    	        frame.setVisible(true);
	            }
	        });
	    }

	@Override
	public void valueChanged(TreeSelectionEvent evt) {
		System.err.println(evt.getPath());
		System.err.println(evt.getNewLeadSelectionPath());
		System.err.println(evt.getOldLeadSelectionPath());
		
	}
}
