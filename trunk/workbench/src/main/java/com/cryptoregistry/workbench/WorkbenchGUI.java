package com.cryptoregistry.workbench;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.passwords.Password;
import com.cryptoregistry.workbench.action.AddSkeletonAction;
import com.cryptoregistry.workbench.action.Base64EncodeAction;
import com.cryptoregistry.workbench.action.CheckRegHandleAction;
import com.cryptoregistry.workbench.action.CloseFileAction;
import com.cryptoregistry.workbench.action.FormatJSONAction;
import com.cryptoregistry.workbench.action.NewFileAction;
import com.cryptoregistry.workbench.action.OpenFileAction;
import com.cryptoregistry.workbench.action.OpenSignatureAction;
import com.cryptoregistry.workbench.action.PrintAction;
import com.cryptoregistry.workbench.action.RegisterAction;
import com.cryptoregistry.workbench.action.RegistrationType;
import com.cryptoregistry.workbench.action.RetrieveRegDataAction;
import com.cryptoregistry.workbench.action.SaveFileAction;
import com.cryptoregistry.workbench.action.SignatureValidationAction;
import com.cryptoregistry.workbench.action.UnlockKeysAction;
import com.cryptoregistry.workbench.action.ValidateJSONAction;

import asia.redact.bracket.properties.Properties;
import asia.redact.bracket.properties.mgmt.PropertiesReference;
import asia.redact.bracket.properties.mgmt.ReferenceType;

public class WorkbenchGUI 
implements ChangeListener, 
			PasswordListener, 
			RegHandleListener, 
			CreateKeyListener {

	public static Font sourceCodeFont; //orig TTF
	public static Font plainTextFont, plainTextFontLg; //derived
	
	JFrame frame;
	JTabbedPane tabs;
	private int counter = 0;
	private final JFileChooser fc = new JFileChooser();
	
	// Actions
	private NewFileAction newFileAction;
	private OpenFileAction openAction;
	private OpenFileAction openToFileAction;
	private SaveFileAction saveAction;
	private SaveFileAction saveToFileAction;
	private CloseFileAction closeFileAction;
	private PrintAction printAction;
	
	private JMenu editMenu;
	
	private ValidateJSONAction validateJSONAction;
	private FormatJSONAction formatJSONAction;
	private Base64EncodeAction base64EncodeAction;
	private Base64EncodeAction base64DecodeAction;
	private AddSkeletonAction addSkeletonAction0;
	private AddSkeletonAction addSkeletonAction1;
	private AddSkeletonAction addSkeletonAction2;
	private AddSkeletonAction addSkeletonAction3;
	private OpenSignatureAction openSignatureAction;
	
	private CheckRegHandleAction checkRegHandleAction;
	private RetrieveRegDataAction retrieveRegDataAction;
	private SignatureValidationAction signatureValidationAction;
	private RegisterAction registerAction;
	
	private JMenuItem createKeyItem;
	private UnlockKeysAction unlockKeysAction;
	
	private JMenuItem delWindowItem;
	
	private JLabel statusLabel;
	
	private Password password;
	private String regHandle;
	private String adminEmail;
	
	private ExternalPropsManager propsMgr;

	public WorkbenchGUI(Properties props) {
		propsMgr = new ExternalPropsManager(props);
		createSourceCodeFont();
		createGUI();
	}
	
	public JFrame getFrame() {
		return frame;
	}

	public JTabbedPane getTabs() {
		return tabs;
	}

	public JFileChooser getFc() {
		return fc;
	}

	public JLabel getStatusLabel() {
		return statusLabel;
	}

	public Password getPassword() {
		return password;
	}

	public String getRegHandle() {
		return regHandle;
	}

	public Properties getProps() {
		return propsMgr.getProps();
	}

	private void createSourceCodeFont() {
		InputStream in = this.getClass().getResourceAsStream("/TTF/SourceCodePro-Regular.ttf");
	    try {
			sourceCodeFont = Font.createFont(Font.TRUETYPE_FONT, in);
			plainTextFont = sourceCodeFont.deriveFont(Font.PLAIN, 11);
			plainTextFontLg = sourceCodeFont.deriveFont(Font.PLAIN, 14);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createGUI() {
		
		if(!propsMgr.hasDefaultKeyDirectoryLocation()) {
			InitialSetupDialog isd = new InitialSetupDialog(this.frame,"Initial Setup");
			if(isd.isSucceeded()){
				String path = isd.getRootDirTextField().getText();
				adminEmail = isd.getAdminEmailTextField().getText();
				propsMgr.put("default.key.directory",path);
				propsMgr.put("registration.email",adminEmail);
				propsMgr.write();
				File dir = new File(path);
				fc.setCurrentDirectory(dir);
			}
		}else{
			String path = propsMgr.get("default.key.directory");
			File dir = new File(path);
			fc.setCurrentDirectory(dir);
		}
		
		if(propsMgr.hasRegistrationHandleSerialized()){
			this.regHandle = propsMgr.get("registration.handle");
		}
		if(propsMgr.hasAdminEmailSerialized()){
			this.adminEmail = propsMgr.get("registration.email");
		}
		
		
		
		frame = new JFrame("cryptoregistry.com - Key Materials Workbench");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setIconImages(new IconLister().getIconList());
		tabs = new JTabbedPane(); // these need to be here to be available to the menu construction
		statusLabel = new JLabel("...");
		frame.setJMenuBar(createMenuBar());
		frame.getContentPane().add(tabs, BorderLayout.CENTER);
		tabs.addChangeListener(this);
		
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		frame.add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setPreferredSize(new Dimension(frame.getWidth(), 20));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusLabel.setPreferredSize(new Dimension(frame.getWidth()/8, 20));
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusLabel);

		// size the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth() / 2;
		double height = screenSize.getHeight() / 2;
		frame.setPreferredSize(new Dimension((int) width, (int) height));

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	private String createTabTitle() {
		String title = "New Tab - " + counter;
		counter++;
		return title;
	}

	private JMenuBar createMenuBar() {

		// final myself, makes "this" usable
		final WorkbenchGUI instance = this;
		
		JMenuBar menuBar = new JMenuBar();

		// Build the first menu.
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		newFileAction = new NewFileAction(tabs,fc);
		fileMenu.add(newFileAction);
		openAction = new OpenFileAction(false,tabs,fc);
		openToFileAction = new OpenFileAction(true,tabs,fc);
		fileMenu.add(openToFileAction);
		fileMenu.add(openAction);
		openAction.setEnabled(false);
		fileMenu.addSeparator();
		saveToFileAction = new SaveFileAction(true,tabs,fc,statusLabel);
		saveAction = new SaveFileAction(false,tabs,fc,statusLabel);
		fileMenu.add(saveToFileAction);
		fileMenu.add(saveAction);
		saveToFileAction.setEnabled(false);
		saveAction.setEnabled(false);
		fileMenu.addSeparator();
		closeFileAction = new CloseFileAction(tabs,fc);
		fileMenu.add(closeFileAction);
		closeFileAction.setEnabled(false);
		fileMenu.addSeparator();
		printAction = new PrintAction(tabs,fc);
		fileMenu.add(printAction);
		printAction.setEnabled(false);
		fileMenu.addSeparator();
		JMenuItem exitItem = new JMenuItem("Exit");
		fileMenu.add(exitItem);
		exitItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				frame.dispose();
				System.exit(0);
			}
			
		});
		

		// Build the edit menu.
		editMenu = new JMenu("Edit");
		menuBar.add(editMenu);
		JMenuItem cut = new JMenuItem("Cut");
		editMenu.add(cut);
		cut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UUIDTextPane pane = currentTextPane();
				if(pane != null) {
					pane.requestFocusInWindow();
					pane.cut();
				}
			}
		});
		JMenuItem copy = new JMenuItem("Copy");
		editMenu.add(copy);
		copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UUIDTextPane pane = currentTextPane();
				if(pane != null) {
					pane.requestFocusInWindow();
					pane.copy();
				}
			}
		});
		JMenuItem paste = new JMenuItem("Paste");
		editMenu.add(paste);
		paste.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UUIDTextPane pane = currentTextPane();
				if(pane != null) {
					pane.requestFocusInWindow();
					pane.paste();
				}
			}
		});
		editMenu.addSeparator();
		JMenuItem selectAll = new JMenuItem("Select All");
		editMenu.add(selectAll);
		selectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UUIDTextPane pane = currentTextPane();
				if(pane != null) {
					pane.requestFocusInWindow();
					pane.selectAll();
				}
			}
		});
		editMenu.addSeparator();
		

		// Build the source menu.
		JMenu sourceMenu = new JMenu("Source");
		menuBar.add(sourceMenu);
		validateJSONAction = new ValidateJSONAction(tabs);
		validateJSONAction.setEnabled(false);
		sourceMenu.add(validateJSONAction);
		formatJSONAction = new FormatJSONAction(tabs);
		formatJSONAction.setEnabled(false);
		sourceMenu.add(formatJSONAction);
		sourceMenu.addSeparator();
		
		JMenu templateSubmenu = new JMenu("Templates");
		sourceMenu.add(templateSubmenu);
		
		addSkeletonAction0 = new AddSkeletonAction(tabs,regHandle,adminEmail, RegistrationType.BASIC);
		addSkeletonAction0.setEnabled(false);
		templateSubmenu.add(addSkeletonAction0);
		
		addSkeletonAction1 = new AddSkeletonAction(tabs,regHandle,adminEmail, RegistrationType.INDIVIDUAL);
		addSkeletonAction1.setEnabled(false);
		templateSubmenu.add(addSkeletonAction1);
		
		addSkeletonAction2 = new AddSkeletonAction(tabs,regHandle,adminEmail, RegistrationType.BUSINESS);
		addSkeletonAction2.setEnabled(false);
		templateSubmenu.add(addSkeletonAction2);
		
		addSkeletonAction3 = new AddSkeletonAction(tabs,regHandle,adminEmail, RegistrationType.WEBSITE);
		addSkeletonAction3.setEnabled(false);
		templateSubmenu.add(addSkeletonAction3);
		
		templateSubmenu.addSeparator();
		
		base64EncodeAction = new Base64EncodeAction(tabs, true, "Base64 Encode");
		base64EncodeAction.setEnabled(false);
		sourceMenu.add(base64EncodeAction);
		
		base64DecodeAction = new Base64EncodeAction(tabs, false, "Base64 Decode");
		base64DecodeAction.setEnabled(false);
		sourceMenu.add(base64DecodeAction);
		sourceMenu.addSeparator();
		
		openSignatureAction = new OpenSignatureAction(tabs);
		openSignatureAction.setEnabled(false);
		sourceMenu.add(openSignatureAction);
		sourceMenu.addSeparator();
		
		// Dialogs instantiated here so they can have listeners added
		final RegHandleSearchDialog rhsd = new RegHandleSearchDialog(frame, "Set Registration Handle", propsMgr.getProps());
    	rhsd.addRegHandleListener(instance);
    	rhsd.addRegHandleListener(propsMgr);
		final UnlockedKeyDialog unlockedKeyDialog = new UnlockedKeyDialog(this);
		final CreateKeyDialog createKeyDialog = new CreateKeyDialog(this);
		createKeyDialog.addCreateKeyListener(unlockedKeyDialog.getPanel());
		createKeyDialog.addCreateKeyListener(this);
		rhsd.addRegHandleListener(createKeyDialog.getPanel());
		final SetDefaultPasswordDialog enterPasswordDialog = new SetDefaultPasswordDialog(frame, "Enter a Password");
    	enterPasswordDialog.addPasswordChangedListener(instance);
    	enterPasswordDialog.addPasswordChangedListener(createKeyDialog.getPanel());
    	rhsd.addRegHandleListener(addSkeletonAction0);
    	createKeyDialog.addCreateKeyListener(openSignatureAction);
    
    	unlockedKeyDialog.getPanel().addCryptoKeySelectionListener(this.openSignatureAction);
    	unlockedKeyDialog.getPanel().addCryptoKeySelectionListener(this.addSkeletonAction0);
    	unlockedKeyDialog.getPanel().addCryptoKeySelectionListener(this.addSkeletonAction1);
    	unlockedKeyDialog.getPanel().addCryptoKeySelectionListener(this.addSkeletonAction2);
    	unlockedKeyDialog.getPanel().addCryptoKeySelectionListener(this.addSkeletonAction3);
    	
		JMenu keysMenu = new JMenu("Key Materials");
		menuBar.add(keysMenu);
		keysMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				// if the adminEmail and regHandle and password are not set, don't enable
				if(password == null || regHandle == null || adminEmail == null){
					createKeyItem.setEnabled(false);
				}else{
					createKeyItem.setEnabled(true);
				}
			}
			@Override
			public void menuDeselected(MenuEvent e) {}
			@Override
			public void menuCanceled(MenuEvent e) {}
		});
		
		JMenuItem regHandleItem = new JMenuItem("Set Registration Handle");
		keysMenu.add(regHandleItem);
		
		regHandleItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 rhsd.open(); 
			}
		});
		
		JMenuItem currentPasswordItem = new JMenuItem("Set Default Password");
		keysMenu.add(currentPasswordItem);
		currentPasswordItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enterPasswordDialog.open();
			}
		});
		
		keysMenu.addSeparator();
		JMenu keysSubmenu = new JMenu("Keys");
		keysMenu.add(keysSubmenu);
		
		createKeyItem = new JMenuItem("Simple Key Creation Dialog...");
		keysSubmenu.add(createKeyItem);
		createKeyItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createKeyDialog.open();
			}
		});
		
		keysMenu.addSeparator();
		
		unlockKeysAction = new UnlockKeysAction(frame, tabs, statusLabel);
		unlockKeysAction.addUnlockKeyListener(unlockedKeyDialog.getPanel());
		unlockKeysAction.addUnlockKeyListener(openSignatureAction);
		enterPasswordDialog.addPasswordChangedListener(unlockKeysAction);
		keysMenu.add(unlockKeysAction);
		unlockKeysAction.setEnabled(false);
		
		keysMenu.addSeparator();
		
		JMenu sigSubmenu = new JMenu("Signatures");
		keysMenu.add(sigSubmenu);
		
		signatureValidationAction = new SignatureValidationAction(tabs, propsMgr.getProps(),statusLabel);
		sigSubmenu.add(signatureValidationAction);
		
		JMenu remoteMenu = new JMenu("Remote/Registry");
		menuBar.add(remoteMenu);
		checkRegHandleAction = new CheckRegHandleAction(tabs,propsMgr.getProps(),statusLabel);
		remoteMenu.add(checkRegHandleAction);
		checkRegHandleAction.setEnabled(false);
		
		retrieveRegDataAction = new RetrieveRegDataAction(tabs,propsMgr.getProps(),statusLabel);
		remoteMenu.add(retrieveRegDataAction);
		retrieveRegDataAction.setEnabled(false);
		
		remoteMenu.addSeparator();
		
		registerAction = new RegisterAction(tabs,propsMgr.getProps(),statusLabel);
		remoteMenu.add(registerAction);
		registerAction.setEnabled(false);
		

		// Build the Window menu.
		JMenu windowMenu = new JMenu("Window");
		menuBar.add(windowMenu);
		JMenuItem newWindowItem = new JMenuItem("New Tab");
		windowMenu.add(newWindowItem);
		newWindowItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				String title = createTabTitle();
				UUIDTextPane pane = new UUIDTextPane();
				pane.setFont(WorkbenchGUI.plainTextFont);
				String identifier = pane.identifier;
				JScrollPane scroll= new JScrollPane(pane);
				tabs.add(title,scroll);
            	tabs.setTabComponentAt(tabs.indexOfComponent(scroll), new ButtonTabComponent(tabs));
				int count = tabs.getTabCount();
		
				for(int i = 0; i<count;i++){
					JScrollPane sc = (JScrollPane) tabs.getComponentAt(i);
					UUIDTextPane editor = (UUIDTextPane) sc.getViewport().getView();
					if(editor.identifier.equals(identifier)){
						tabs.setSelectedIndex(i);
						editor.requestFocusInWindow();
					}
				}
			}
		});
		delWindowItem = new JMenuItem("Close Tab");
		delWindowItem.setEnabled(false);
		windowMenu.add(delWindowItem);
		delWindowItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if(tabs.getComponentCount() == 0) return;
				int count = tabs.getSelectedIndex();
				tabs.remove(count);
				
			}
		});
		windowMenu.addSeparator();

		JMenuItem keysDialogItem = new JMenuItem("Unlocked Keys Dialog");
		windowMenu.add(keysDialogItem);
		keysDialogItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				unlockedKeyDialog.open();
			}
		});
		
		windowMenu.addSeparator();
		

		menuBar.add(Box.createHorizontalGlue());

		JMenu aboutMenu = new JMenu("About");
		menuBar.add(aboutMenu);

		JMenuItem itemAbout = new JMenuItem("About This Application");
		aboutMenu.add(itemAbout);
		aboutMenu.addSeparator();
		itemAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// new AboutFrame(myprops);
			}
		});

		return menuBar;
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

	/**
	 * Listen to changes to the number of tabs and their contents
	 * 
	 */
	@Override
	public void stateChanged(ChangeEvent evt) {
		
		JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
		int index = sourceTabbedPane.getSelectedIndex();
		if(index == -1) {
			// no tabs, so many menu functions should be disabled
			this.openToFileAction.setEnabled(true);
			this.openAction.setEnabled(false);
			this.saveAction.setEnabled(false);
			this.saveToFileAction.setEnabled(false);
			this.closeFileAction.setEnabled(false);
			this.printAction.setEnabled(false);
			
			this.validateJSONAction.setEnabled(false);
			this.formatJSONAction.setEnabled(false);
			this.base64EncodeAction.setEnabled(false);
			this.base64DecodeAction.setEnabled(false);
			this.addSkeletonAction0.setEnabled(false);
			this.addSkeletonAction1.setEnabled(false);
			this.addSkeletonAction2.setEnabled(false);
			this.addSkeletonAction3.setEnabled(false);
			
			this.checkRegHandleAction.setEnabled(false);
			this.retrieveRegDataAction.setEnabled(false);
			
			this.unlockKeysAction.setEnabled(false);
			this.openSignatureAction.setEnabled(false);
			this.registerAction.setEnabled(false);
			
			delWindowItem.setEnabled(false);
			
			this.statusLabel.setText("...");
			return;
		}else{
			
			// has at least one tab
			UUIDTextPane pane = currentTextPane();
			
			// if the tab text relates to secure keys...
			if(pane.paneContainsAtLeastOneSecureKey()){
				this.unlockKeysAction.setEnabled(true);
			}else{
				this.unlockKeysAction.setEnabled(false);
			}
			
			
			// tab is a newly created one with no file backing
			if(pane.getTargetFile()==null){
				this.openToFileAction.setEnabled(true);
				this.openAction.setEnabled(true);
				this.saveAction.setEnabled(false);
				this.saveToFileAction.setEnabled(true);
				this.closeFileAction.setEnabled(false);
				this.printAction.setEnabled(false);
				
				this.validateJSONAction.setEnabled(true);
				this.formatJSONAction.setEnabled(true);
				this.addSkeletonAction0.setEnabled(true);
				this.addSkeletonAction1.setEnabled(true);
				this.addSkeletonAction2.setEnabled(true);
				this.addSkeletonAction3.setEnabled(true);
				this.base64EncodeAction.setEnabled(true);
				this.base64DecodeAction.setEnabled(true);
				this.openSignatureAction.setEnabled(true);
				
				this.checkRegHandleAction.setEnabled(true);
				this.retrieveRegDataAction.setEnabled(true);
				this.registerAction.setEnabled(true);
				
				delWindowItem.setEnabled(true);
				
				this.statusLabel.setText("...");
				return;
			}else{
				// selected tab has file backing
				this.openToFileAction.setEnabled(true);
				this.openAction.setEnabled(false);
				this.saveAction.setEnabled(true);
				this.saveToFileAction.setEnabled(true);
				this.closeFileAction.setEnabled(true);
				this.printAction.setEnabled(true);
				
				this.validateJSONAction.setEnabled(true);
				this.formatJSONAction.setEnabled(true);
				this.addSkeletonAction0.setEnabled(true);
				this.addSkeletonAction1.setEnabled(true);
				this.addSkeletonAction2.setEnabled(true);
				this.addSkeletonAction3.setEnabled(true);
				this.base64EncodeAction.setEnabled(true);
				this.base64DecodeAction.setEnabled(true);
				
				this.checkRegHandleAction.setEnabled(true);
				this.retrieveRegDataAction.setEnabled(true);
				
				this.unlockKeysAction.setEnabled(true);
				this.openSignatureAction.setEnabled(true);
				this.registerAction.setEnabled(true);
				
				delWindowItem.setEnabled(true);
				
				try {
					this.statusLabel.setText(pane.getTargetFile().getCanonicalPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private UUIDTextPane currentTextPane(){
		int index = tabs.getSelectedIndex();
		if(index == -1) {
			System.err.println("Warn, selected index was null...");
			return null;		
		}
		else {
			return  (UUIDTextPane) ((JScrollPane)tabs.getComponentAt(index)).getViewport().getView();
		}
	}

	// for the default password
	@Override
	public void passwordChanged(EventObject evt) {
		char [] pass = ((PasswordEvent)evt).getPasswordValue();
		password = new Password(pass);
		this.statusLabel.setText("Updated default password.");
	}

	// for reg handle being set
	@Override
	public void registrationHandleChanged(EventObject evt) {
		regHandle = ((RegHandleEvent)evt).getRegHandle();
		this.statusLabel.setText("Updated registration handle.");
	}
	
	@Override
	public void keyCreated(CreateKeyEvent evt) {
		//CryptoKey secureKey = evt.getKey();
		CryptoKey pkey = evt.getKeyForPublication();
		String title = pkey.getMetadata().getDistinguishedHandle()+" ("+pkey.getMetadata().getKeyAlgorithm()+")";
		UUIDTextPane pane = new UUIDTextPane(pkey.getMetadata().getHandle());
		pane.setFont(WorkbenchGUI.plainTextFont);
		pane.setText(evt.getTextualRepresentation());
		String identifier = pane.identifier;
		JScrollPane scroll= new JScrollPane(pane);
		tabs.add(title, scroll);
		int count = tabs.getTabCount();
		for(int i = 0; i<count;i++){
			JScrollPane sc = (JScrollPane) tabs.getComponentAt(i);
			UUIDTextPane editor = (UUIDTextPane) sc.getViewport().getView();
			if(editor.identifier.equals(identifier)){
				tabs.setSelectedIndex(i);
				editor.requestFocusInWindow();
			}
		}
	}
	
	public OpenSignatureAction getOpenSignatureAction() {
		return openSignatureAction;
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				List<PropertiesReference> refs = new ArrayList<PropertiesReference>();
			    refs.add(new PropertiesReference(ReferenceType.CLASSLOADED,"regwizard.properties"));
				try {
					File home = new File(System.getProperty("user.home"));
					File regwizExternal = new File(home,"regwizard.properties");
					refs.add(new PropertiesReference(ReferenceType.EXTERNAL,regwizExternal.getCanonicalPath()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			
			   Properties props = Properties.Factory.loadReferences(refs);
			   new WorkbenchGUI(props);
			}
		});
	}

}
