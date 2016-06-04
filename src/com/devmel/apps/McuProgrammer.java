package com.devmel.apps;

import javax.swing.ImageIcon;

import com.devmel.apps.mcuprogrammer.R;
import com.devmel.apps.mcuprogrammer.controller.BaseController;
import com.devmel.apps.mcuprogrammer.controller.CLIController;
import com.devmel.apps.mcuprogrammer.controller.GUIController;
import com.devmel.apps.mcuprogrammer.datas.DataArray;
import com.devmel.apps.mcuprogrammer.datas.TargetsConfig;
import com.devmel.apps.mcuprogrammer.view.swing.MainView;
import com.devmel.storage.java.UserPrefs;
import com.devmel.tools.CommandLineParser;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;


public class McuProgrammer {
	public final static String name = McuProgrammer.class.getSimpleName();
	private final static Locale systemLocale = Locale.getDefault();
	private final BaseController baseController;
	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		CommandLineParser cli = null;
		McuProgrammer program = null;
		try {
			if(args != null && args.length > 0){
				cli = new CommandLineParser(args);
			}
			program = new McuProgrammer(cli);
		} catch (Throwable e) {
			CLIController.help(System.out);
			System.exit(-1);
		}
		try {
			if(program != null){
				if(cli != null){
					System.exit(program.loadCLI());
				}else{
					program.loadGUI();
				}
			}
		} catch (Throwable e) {
			System.err.println("A fatal exception has occurred. Program will exit.");
			System.exit(-1);
		}
	}

	/**
	 * Create the application.
	 */
	public McuProgrammer(CommandLineParser cli) {
		loadRessources();
		//Build Model
		Preferences prefs = Preferences.userRoot().node(UserPrefs.class.getName());
		UserPrefs userPrefs = new UserPrefs(prefs);
//		userPrefs.clearAll();
		//Build data
		TargetsConfig targetsConfig = new TargetsConfig();
		if(cli !=null){
			//CLI
			baseController = new CLIController(userPrefs, targetsConfig, System.out, System.err, cli);
		}else{
			//Window
			MainView mainView = new MainView();
			baseController = new GUIController(userPrefs, targetsConfig, new DataArray(), mainView);
		}
	}
	
	public int loadCLI(){
		int ret = -1;
		if(baseController instanceof CLIController){
			CLIController controller = (CLIController) baseController;
			ret = controller.process();
		}
		return ret;
	}

	public int loadGUI(){
		int ret = -1;
		if(baseController instanceof GUIController){
			final GUIController controller = (GUIController) baseController;
			MainView mainView = controller.getGUI();
			URL resIcon = null;
			if(mainView != null){
				//Load Icon
				try {
					resIcon = getClass().getResource("/res/icon_app_32x32.png");
				} catch (Exception e) {
				}
				if(resIcon!=null){
					ImageIcon icon = new ImageIcon(resIcon);
					mainView.setIconImage(icon.getImage());
				}
				//Start controller
				mainView.setController(controller);
				controller.initialize();
				mainView.addWindowListener(new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent evt) {
						controller.exitClick();
					}
				});
				//Display Window
				mainView.setLocationRelativeTo(null);
				mainView.setVisible(true);
				ret = 0;
			}
		}
		return ret;
	}

	private void loadRessources(){
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
	}
}
