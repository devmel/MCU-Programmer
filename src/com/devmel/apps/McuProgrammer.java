package com.devmel.apps;

import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.devmel.apps.mcuprogrammer.R;
import com.devmel.apps.mcuprogrammer.controller.MainController;
import com.devmel.apps.mcuprogrammer.datas.DataArray;
import com.devmel.apps.mcuprogrammer.datas.TargetsConfig;
import com.devmel.apps.mcuprogrammer.view.swing.MainView;
import com.devmel.storage.java.UserPrefs;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;


public class McuProgrammer {
	public final static String name = McuProgrammer.class.getSimpleName();
	private final static Locale systemLocale = Locale.getDefault();
	private final MainView mainView;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				//Load Ressource
				Locale systemLang = new Locale(systemLocale.getDisplayLanguage(), "");
				Locale defaultLang = new Locale("en", "");
				try {
					R.bundle = ResourceBundle.getBundle("res."+name, systemLocale);
				} catch (Exception e) {
					try {
						R.bundle = ResourceBundle.getBundle("res."+name, systemLang);
					} catch (Exception e1) {
						try {
							R.bundle = ResourceBundle.getBundle("res."+name, defaultLang);
						} catch (Exception e2) {
							System.exit(-1);
						}
					}
				}
				URL resIcon = null;
				try {
					resIcon = getClass().getResource("/res/icon_app_32x32.png");
					javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				} catch (Exception e) {
				}
				try {
					McuProgrammer window = new McuProgrammer();
					if(resIcon!=null){
						ImageIcon icon = new ImageIcon(resIcon);
						window.mainView.setIconImage(icon.getImage());
					}
					window.mainView.setLocationRelativeTo(null);
					window.mainView.setVisible(true);
				} catch (Throwable e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public McuProgrammer() {
		//Build Model
		UserPrefs userPrefs = new UserPrefs(null);
//		userPrefs.clearAll();
		//Splash screen
		if(userPrefs.getString("configStart") == null){
			String[] options = {"Computer", "LinkBus"};
			int ret = JOptionPane.showOptionDialog(null, R.bundle.getString("select_device_port"), R.bundle.getString("select_port"), JOptionPane.NO_OPTION, JOptionPane.DEFAULT_OPTION, null, options , options[0]);
			if(ret < options.length){
				userPrefs.saveString("configStart", options[ret]);
			}
		}
		DataArray tabData = new DataArray();
		final TargetsConfig devicesConfig = new TargetsConfig();
		//Start controller
		mainView = new MainView();
		final MainController controller = new MainController(userPrefs, tabData, devicesConfig, mainView);
		mainView.setController(controller);
		controller.initialize();
		mainView.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				controller.exitClick();
			}
		});
	}

}
