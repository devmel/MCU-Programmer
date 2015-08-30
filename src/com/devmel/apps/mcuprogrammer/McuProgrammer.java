package com.devmel.apps.mcuprogrammer;

import java.awt.EventQueue;

import javax.swing.ImageIcon;

import com.devmel.apps.mcuprogrammer.controller.MainController;
import com.devmel.apps.mcuprogrammer.datas.DataArray;
import com.devmel.apps.mcuprogrammer.datas.TargetsConfig;
import com.devmel.apps.mcuprogrammer.lang.Language;
import com.devmel.apps.mcuprogrammer.view.swing.MainView;
import com.devmel.storage.java.UserPrefs;

import java.net.URL;
import java.util.Locale;


public class McuProgrammer {
	private final MainController mainController;
	private final MainView mainFrame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//Load Language
					Locale systemLocale = Locale.getDefault();
					Language.setLanguage(systemLocale.getLanguage());
					try {
						javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					} catch (Exception ex) {
						// Handle Exception
					}
					McuProgrammer window = new McuProgrammer();
					URL url_image = getClass().getResource("/res/icon32.png");
					ImageIcon devmelIcon = new ImageIcon(url_image);
					window.mainFrame.setIconImage(devmelIcon.getImage());
					window.mainFrame.setLocationRelativeTo(null);
					window.mainFrame.setVisible(true);
				} catch (Throwable e) {
					e.printStackTrace();
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
		DataArray tabData = new DataArray();
		TargetsConfig devicesConfig = new TargetsConfig();
		//Start controller
		mainFrame = new MainView();
		mainController = new MainController(userPrefs, tabData, devicesConfig, mainFrame);
		mainFrame.setController(mainController);
		mainController.initialize();
	}

}
