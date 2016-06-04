package com.devmel.apps.mcuprogrammer.view.swing;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.devmel.apps.mcuprogrammer.controller.GUIController;
import com.devmel.apps.mcuprogrammer.R;
import com.devmel.apps.mcuprogrammer.view.swing.TargetToolsBar;
import com.devmel.apps.mcuprogrammer.view.swing.TargetSelectionBar;
import com.devmel.apps.mcuprogrammer.view.swing.DeviceSelectBar;
import com.devmel.apps.mcuprogrammer.view.swing.StatusBar;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;

import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;

public class MainView extends JFrame{
	private static final long serialVersionUID = -7648450274854220447L;
	private final FilterFileNameExtension filetype = new FilterFileNameExtension("Data (*.hex, *.bin)", "hex", "bin");
	private final FilterFileNameExtension filetypeTarget = new FilterFileNameExtension("Archive (*.zip, *.jar)", "zip", "jar");
	private GUIController controller;
	public JTabbedPane tabbedPane;
	public JMenuItem mntmSave;
	public DeviceSelectBar deviceSelectBar;
	public TargetSelectionBar targetSelectionBar;
	public TargetToolsBar targetToolsBar;
	public StatusBar statusBar;

	public MainView(){
		try {
			javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
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

	
	
	public void setController(GUIController controller) {
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
	
	public final class FilterFileNameExtension extends FileFilter
	{

	    public FilterFileNameExtension(String s, String... as)
	    {
	        if(as == null || as.length == 0)
	            throw new IllegalArgumentException("Extensions must be non-null and not empty");
	        description = s;
	        extensions = new String[as.length];
	        lowerCaseExtensions = new String[as.length];
	        for(int i = 0; i < as.length; i++)
	        {
	            if(as[i] == null || as[i].length() == 0)
	                throw new IllegalArgumentException("Each extension must be non-null and not empty");
	            extensions[i] = as[i];
	            lowerCaseExtensions[i] = as[i].toLowerCase(Locale.ENGLISH);
	        }

	    }

	    public boolean accept(File file)
	    {
	        if(file != null)
	        {
	            if(file.isDirectory())
	                return true;
	            String s = file.getName();
	            int i = s.lastIndexOf('.');
	            if(i > 0 && i < s.length() - 1)
	            {
	                String s1 = s.substring(i + 1).toLowerCase(Locale.ENGLISH);
	                String as[] = lowerCaseExtensions;
	                int j = as.length;
	                for(int k = 0; k < j; k++)
	                {
	                    String s2 = as[k];
	                    if(s1.equals(s2))
	                        return true;
	                }

	            }
	        }
	        return false;
	    }

	    public String getDescription()
	    {
	        return description;
	    }

	    public String[] getExtensions()
	    {
	        String as[] = new String[extensions.length];
	        System.arraycopy(extensions, 0, as, 0, extensions.length);
	        return as;
	    }

	    private final String description;
	    private final String extensions[];
	    private final String lowerCaseExtensions[];
	}

}
