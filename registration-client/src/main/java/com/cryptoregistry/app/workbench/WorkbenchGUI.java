package com.cryptoregistry.app.workbench;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.cryptoregistry.CryptoKeyWrapper;
import com.cryptoregistry.KeyMaterials;
import com.cryptoregistry.app.SwingRegistrationWizardGUI;
import com.cryptoregistry.formats.JSONReader;
import com.cryptoregistry.passwords.Password;

import asia.redact.bracket.properties.Properties;

public class WorkbenchGUI implements TreeSelectionListener {
	
	private Properties props;
	private JFileChooser fc;
	private JFrame frame;
	private KeyMaterialsPanel kmPanel;
	private TreePath currentTreePath;

	public WorkbenchGUI(Properties props) {
		this.props = props;
	}
	
	private void unlock() {
		if(currentTreePath == null) {
			 JOptionPane.showMessageDialog(this.frame, 
					 "Please select a UUID for the key to unlock", "Message", JOptionPane.INFORMATION_MESSAGE);
			 return;
		}
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) currentTreePath.getLastPathComponent();
		String item = (String) node.getUserObject();
		if(!item.endsWith("-S")){
			 JOptionPane.showMessageDialog(this.frame, 
					 "Please select a UUID that ends in -S", "Message", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		TreeNode [] nodes = node.getPath();
		for(TreeNode n: nodes) {
			System.err.println(n);
		}
		String rootPath = kmPanel.getRootPath();
		File file = new File(rootPath, nodes[1].toString());
		if(!file.exists() || !file.isFile()){
			 JOptionPane.showMessageDialog(this.frame, 
					 "Seems to be a problem, file does not exist...", "Message", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		JSONReader reader = new JSONReader(file);
		KeyMaterials km = reader.parse();
		CryptoKeyWrapper encryptedWrapper = null;
		for(CryptoKeyWrapper wrapper : km.keys()) {
			if(item.equals(wrapper.distingushedHandle())) {
				encryptedWrapper = wrapper;
				break;
			}
		}
		
		PasswordInputDialog dialog = new PasswordInputDialog(null, "Enter Password", props);
		char [] pass = dialog.getPassword();
    	if(pass == null) {
    		
			return;
    	}else{
    		encryptedWrapper.unlock(new Password(pass));
    	}
		
	}
	
	private void initFileDialog() {
		 fc = new JFileChooser();
		 Path currentRelativePath = Paths.get("");
		 String cd = currentRelativePath.toAbsolutePath().toString();
		 fc.setCurrentDirectory(new File(cd));
		 fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

	private JMenuBar createMenuBar() {

		JMenuBar menuBar = new JMenuBar();

		// Build the first menu.
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		JMenuItem setWorkFolderItem = new JMenuItem("Set Root Folder ...");
		fileMenu.add(setWorkFolderItem);
		
		setWorkFolderItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//open file dialog
				 int returnVal = fc.showOpenDialog((Component)e.getSource());
		            if (returnVal == JFileChooser.APPROVE_OPTION) {
		                File file = fc.getSelectedFile();
		                try {
							kmPanel.setRootPath(file.getCanonicalPath());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
		            }
			}
		});

		// Build the first menu.
		JMenu editMenu = new JMenu("Edit");
		menuBar.add(editMenu);
		
		JMenu localMenu = new JMenu("Local");
		menuBar.add(localMenu);
		
		JMenuItem itemUnlock = new JMenuItem("Unlock...");
		localMenu.add(itemUnlock);
		itemUnlock.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// unlock selected key and hold in memory
				unlock();
			}
		});
		

		JMenu remoteMenu = new JMenu("Remote");
		menuBar.add(remoteMenu);

		JMenuItem itemReg = new JMenuItem("Create Registration...");
		remoteMenu.add(itemReg);
		itemReg.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						SwingRegistrationWizardGUI.createAndShowGUI(props);
					}
				});
			}
		});
		remoteMenu.addSeparator();
		JMenuItem itemSession = new JMenuItem("Create Session...");
		remoteMenu.add(itemSession);
		itemSession.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// JFrame myframe = new JFrame();
				// myframe.setIconImages(new IconLister().getIconList());
				// new SessionDialog(myframe, "Create Session", props);
			}
		});

		JMenuItem itemKey = new JMenuItem("Upload Key Materials");
		remoteMenu.add(itemKey);
		itemKey.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//item highlighted in GUI
			}
		});
		// itemKey.setEnabled(false);

		JMenu utilityMenu = new JMenu("Utility");
		menuBar.add(utilityMenu);
		JMenuItem item0 = new JMenuItem("Binary Entropy (Croll's Method)");
		utilityMenu.add(item0);
		item0.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// JFrame myframe = new JFrame();
				// myframe.setIconImages(new IconLister().getIconList());
				// new EntropyDialog(myframe, "Binary Entropy");
			}

		});

		menuBar.add(Box.createHorizontalGlue());

		JMenu aboutMenu = new JMenu("About");
		menuBar.add(aboutMenu);

		JMenuItem itemAbout = new JMenuItem("About This Application");
		aboutMenu.add(itemAbout);
		itemAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// new AboutFrame(myprops);
			}
		});

		return menuBar;
	}

	public void createGUI() {
		frame = new JFrame("cryptoregistry.com - Key Materials Workbench");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImages(new IconLister().getIconList());
		frame.setJMenuBar(createMenuBar());
		kmPanel = new KeyMaterialsPanel(null);
		kmPanel.addTreeSelectionListener(this);
		frame.getContentPane().add(kmPanel);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth()/2;
		double height = screenSize.getHeight()/2;
		
		frame.setPreferredSize(new Dimension((int)width,(int)height));
		
		initFileDialog();
		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	private static class IconLister {

		public IconLister() {
		}

		public final List<Image> getIconList() {

			List<Image> iconList = null;
			try {
				BufferedImage icon16 = ImageIO.read(this.getClass()
						.getResource("/hand16.png"));
				BufferedImage icon32 = ImageIO.read(this.getClass()
						.getResource("/hand32.png"));
				BufferedImage icon64 = ImageIO.read(this.getClass()
						.getResource("/hand64.png"));
				iconList = new ArrayList<Image>();
				iconList.add(icon16);
				iconList.add(icon32);
				iconList.add(icon64);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return iconList;
		}
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				InputStream in = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("regwizard.properties");
				Properties props = Properties.Factory.getInstance(in);
				WorkbenchGUI gui = new WorkbenchGUI(props);
				gui.createGUI();
			}
		});
	}

	@Override
	public void valueChanged(TreeSelectionEvent evt) {
		currentTreePath = evt.getPath();
	}

}
