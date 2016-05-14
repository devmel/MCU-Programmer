package com.devmel.apps.mcuprogrammer.view.swing;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.devmel.apps.mcuprogrammer.controller.MainController;
import com.devmel.apps.mcuprogrammer.R;
import com.devmel.apps.mcuprogrammer.view.swing.TargetToolsBar;
import com.devmel.apps.mcuprogrammer.view.swing.TargetSelectionBar;
import com.devmel.apps.mcuprogrammer.view.swing.DeviceSelectBar;
import com.devmel.apps.mcuprogrammer.view.swing.StatusBar;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainView extends JFrame{
	private static final long serialVersionUID = -7648450274854220447L;
	private final FileNameExtensionFilter filetype = new FileNameExtensionFilter("*.hex, *.bin", "hex", "bin");
	private final FileNameExtensionFilter filetypeTarget = new FileNameExtensionFilter("*.zip, *.jar", "zip", "jar");
	private MainController controller;
	public JTabbedPane tabbedPane;
	public JMenuItem mntmSave;
	public DeviceSelectBar deviceSelectBar;
	public TargetSelectionBar targetSelectionBar;
	public TargetToolsBar targetToolsBar;
	public StatusBar statusBar;

	public MainView(){
		this.setTitle(R.bundle.getString("MainView.3"));
		this.setBounds(100, 100, 630, 640);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				if(controller!=null){
					controller.exitClick();
				}
			}
		});

		
		JPanel toolsPanel = new JPanel();
		toolsPanel.setLayout(new BorderLayout(0, 0));
		this.getContentPane().add(toolsPanel, BorderLayout.NORTH);

		
		deviceSelectBar = new DeviceSelectBar();
		toolsPanel.add(deviceSelectBar, BorderLayout.NORTH);
		
		targetSelectionBar = new TargetSelectionBar();
		toolsPanel.add(targetSelectionBar, BorderLayout.CENTER);
		
		targetToolsBar = new TargetToolsBar();
		toolsPanel.add(targetToolsBar, BorderLayout.SOUTH);
		
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu(R.bundle.getString("MainView.4"));
		menuBar.add(mnFile);
		mnFile.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				if(controller!=null){
					controller.fileMenuClick();
				}
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});

		JMenuItem mntmOpen = new JMenuItem(R.bundle.getString("MainView.5"));
		mnFile.add(mntmOpen);
		mntmOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(controller!=null){
					controller.openClick();
				}
			}
		});

		JSeparator separator = new JSeparator();
		mnFile.add(separator);

		mntmSave = new JMenuItem(R.bundle.getString("MainView.6"));
		mnFile.add(mntmSave);
		mntmSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(controller!=null){
					controller.saveClick();
				}
			}
		});

		JMenuItem mntmSaveAs = new JMenuItem(R.bundle.getString("MainView.7"));
		mnFile.add(mntmSaveAs);
		mntmSaveAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(controller!=null){
					controller.saveAsClick();
				}
			}
		});

		JSeparator separator_1 = new JSeparator();
		mnFile.add(separator_1);

		JMenuItem mntmExit = new JMenuItem(R.bundle.getString("MainView.8"));
		mnFile.add(mntmExit);
		mntmExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(controller!=null){
					controller.exitClick();
				}
			}
		});

		JMenu mnSettings = new JMenu(R.bundle.getString("MainView.9"));
		menuBar.add(mnSettings);
		
		JMenuItem menuTargetDatabase = new JMenuItem(R.bundle.getString("MainView.10"));
		mnSettings.add(menuTargetDatabase);
		menuTargetDatabase.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(controller!=null){
					controller.targetDatabaseClick();
				}
			}
		});
		
		statusBar = new StatusBar();
		this.getContentPane().add(statusBar, BorderLayout.SOUTH);
	}

	
	
	public void setController(MainController controller) {
		this.controller = controller;
		deviceSelectBar.setController(controller);
		targetSelectionBar.setController(controller);
		targetToolsBar.setController(controller);
	}

	
	public void saveEnable(boolean enable){
		if (mntmSave != null) {
			mntmSave.setEnabled(enable);
		}
	}
	
	public void showTagetTools(){
		targetToolsBar.setVisible(true);
	}
	public void hideTargetTools(){
		targetToolsBar.setVisible(false);
	}
	
	public void showTargetDatabaseDialog(String defaultFilePath){
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.addChoosableFileFilter(filetypeTarget);
		if(defaultFilePath!=null){
			chooser.setCurrentDirectory(new File(defaultFilePath));
		}
		chooser.showOpenDialog(null);
		if(controller!=null){
			controller.openTargetDatabaseFile(chooser.getSelectedFile());
		}
	}
	public void showOpenDialog(String defaultFilePath){
		JFileChooser chooser = new JFileChooser();
		chooser.addChoosableFileFilter(filetype);
		if(defaultFilePath!=null){
			chooser.setCurrentDirectory(new File(defaultFilePath));
		}
		chooser.showOpenDialog(null);
		if(controller!=null){
			controller.openFile(chooser.getSelectedFile());
		}
	}
	public void showSaveDialog(String defaultFilePath){
		JFileChooser chooser = new JFileChooser();
		chooser.addChoosableFileFilter(filetype);
		if(defaultFilePath!=null){
			chooser.setCurrentDirectory(new File(defaultFilePath));
		}
		chooser.showSaveDialog(null);
		if(controller!=null){
			controller.saveFile(chooser.getSelectedFile());
		}
	}
}
