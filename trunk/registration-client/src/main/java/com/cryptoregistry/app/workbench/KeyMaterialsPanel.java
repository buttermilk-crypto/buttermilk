package com.cryptoregistry.app.workbench;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JTextArea;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Panel has tree view of file system based on rootPath. 
 *  - Incoming - if rootPath changes, must re-initialize. 
 *  - Outgoing - if valueChanged() on treeSelectionListener, then propagate that change to WorkbookGUI. 
 * 
 * @author Dave
 *
 */
public class KeyMaterialsPanel extends JPanel  {

	private static final long serialVersionUID = 1L;
	private JTree tree;
	JScrollPane treeScrollPane;
	JScrollPane textScrollPane;
	private String rootPath;
	private DefaultMutableTreeNode root;

	public KeyMaterialsPanel(String rootPath) {
		super();
		this.rootPath = rootPath;
		
		treeScrollPane = new JScrollPane();
		textScrollPane = new JScrollPane();
		
		JTextArea textArea = new JTextArea();
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(treeScrollPane, GroupLayout.PREFERRED_SIZE, 295, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(textScrollPane, GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(textScrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
						.addComponent(treeScrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE))
					.addContainerGap())
		);
		
		root = new DefaultMutableTreeNode("Files");
		tree = new JTree(root);
		treeScrollPane.setViewportView(tree);
		textScrollPane.setViewportView(textArea);
		createNodes(root);
		setLayout(groupLayout);
	}
	
	
	
	private void createNodes(DefaultMutableTreeNode root){
		try {
			
			if(rootPath == null) {
				
				return;
			}
			FindKeyMaterials f = new FindKeyMaterials(new File(rootPath).toPath());
			Files.walkFileTree(new File(rootPath).toPath(), f);
			for(String path : f.getPaths()){
				System.err.println("Looking at: "+path);
				f.iterate(path, root);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateNodes(){
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
		node.removeAllChildren();
		createNodes(node);
	}

	/**
	 * NOTE: Should only be called from the event-dispatch thread
	 * @param rootPath
	 */
	public void setRootPath(String rootPath) {
		if(rootPath == null) return;
		File test = new File(rootPath);
		if(test.exists() && test.isDirectory()){
			this.rootPath = rootPath;
		}else{
			 JOptionPane.showMessageDialog(null, "Path does not look valid", "Message", JOptionPane.WARNING_MESSAGE);
		}
		
		updateNodes();
		
		// expand the root node
		TreeNode[] items = new TreeNode[1];
		items[0] = root;
		TreePath tp = new TreePath(items);
		tree.expandPath(tp);
	}
	
	/**
	 * simple accessor
	 * 
	 * @return
	 */
	public String getRootPath() {
		return this.rootPath;
	}

	public void addTreeSelectionListener(TreeSelectionListener listener){
		this.tree.addTreeSelectionListener(listener);
	}
	
}
