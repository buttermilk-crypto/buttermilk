package com.cryptoregistry.workbench.action;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import asia.redact.bracket.properties.Properties;

import com.cryptoregistry.workbench.ExceptionHolder;
import com.cryptoregistry.workbench.RegistrationSender;
import com.cryptoregistry.workbench.UUIDTextPane;

public class RegisterAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabs;
	private Properties props;
	private JLabel statusLabel;

	private RegistrationSender sender;
	private ExceptionHolder exception;

	public RegisterAction(JTabbedPane tabs, Properties props, JLabel statusLabel) {
		this.tabs = tabs;
		this.putValue(Action.NAME, "Register");
		this.props = props;
		this.statusLabel = statusLabel;
		exception = new ExceptionHolder();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		final Component comp = (Component) evt.getSource();
		int index = tabs.getSelectedIndex();
		if (index == -1)
			return; // fail because no tabs found
		final UUIDTextPane pane = (UUIDTextPane) ((JScrollPane) tabs
				.getComponentAt(index)).getViewport().getView();
		final String regJSON = pane.getText();

		statusLabel.setText("Using "
				+ props.get("registration.services.hostname") + "...");
		sender = new RegistrationSender(props);

		SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {

			@Override
			protected Boolean doInBackground() throws Exception {
				try {
					sender.request(regJSON);
					return sender.isSuccess();
				} catch (RuntimeException x) {
					exception.ex = x;
					return false;
				}
			}

			@Override
			public void done() {
				try {
					statusLabel.setText("Sent.");

					if (get()) {
						JOptionPane.showMessageDialog(comp, "Success!",
								"Registration Results",
								JOptionPane.INFORMATION_MESSAGE);
						return;
					} else {
						if (exception.hasException()) {
							String msg = exception.ex.getMessage();
							JOptionPane.showMessageDialog(comp, "Problem: "
									+ msg, "Registration Results",
									JOptionPane.ERROR_MESSAGE);

						} else {
							JOptionPane.showMessageDialog(
									comp,
									"Sorry, registration failed: "
											+ sender.getResponseBody(),
									"Registration Results",
									JOptionPane.ERROR_MESSAGE);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		worker.execute();
	}

}
