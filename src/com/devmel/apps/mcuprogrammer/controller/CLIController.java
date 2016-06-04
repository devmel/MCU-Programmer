package com.devmel.apps.mcuprogrammer.controller;

import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.devmel.apps.mcuprogrammer.datas.DataArray;
import com.devmel.apps.mcuprogrammer.datas.TargetsConfig;
import com.devmel.apps.mcuprogrammer.sections.MemoryHex;
import com.devmel.apps.mcuprogrammer.sections.Memory.StatusListener;
import com.devmel.apps.mcuprogrammer.tools.ProgressBar;
import com.devmel.apps.mcuprogrammer.tools.SearchQRCode;
import com.devmel.storage.IBase;
import com.devmel.tools.CommandLineParser;
import com.devmel.tools.Hexadecimal;

public class CLIController extends BaseController{
	private final CommandLineParser cli;
	private final PrintStream out;
	private final PrintStream err;

	public CLIController(IBase baseStorage, TargetsConfig targetsConfig, PrintStream consoleOut, PrintStream consoleErr, CommandLineParser cli) {
		super(baseStorage, targetsConfig);
		out = consoleOut;
		err = consoleErr;
		this.cli = cli;
	}
	
	public int process(){
		int ret = -1;
		out.println("------------- MCU Programmer -----------");
		if(cli == null || cli.hasOption("h") || cli.hasOption("?")){
			help();
		}else{
			String target = null;
			//Configuration file
			if(cli.hasOption("C")){
				String configFile = cli.getOptionValue("C");
				super.loadTargetConfig(configFile);
			}else{
				super.loadTargetConfig();
			}
			//Connection port
			if(cli.hasOption("P")){
				String port = cli.getOptionValue("P");
				if(port != null && port.equals("sp://")){
					//Scan QR code
					SearchQRCode search = null;
					try{
						search = new SearchQRCode();
						if(search.newScan()){
							out.println("QR Code scanning...");
							search.waitResult(10000);
							port = search.getResult();
						}
					}catch(Exception e){
						err.println("No webcam found...");
					}finally{
						if(search != null)
							search.close();
					}
				}
				super.selectDevice(port);
			}
			//Programmer name
			if(cli.hasOption("c")){
				String programmer = cli.getOptionValue("c");
				super.selectProgrammer(programmer);
			}
			//Target name
			if(cli.hasOption("p")){
				target = cli.getOptionValue("p");
			}
			//Programmer protocol and target detection
			if(deviceType==null || deviceName == null){
				errPort(super.getDeviceList());
			}else{
				//Test programmer
				String[] programmers = targetsConfig.programmers(target);
				String prog = containsCaseInsensitive(programmer, programmers);
				if(prog != null){
					programmer = prog;
					loadDevice();
					//Open and read id
					if(program!=null){
						out.println("Connection port : "+deviceName);
						//Open target
						try {
							out.println("Programmer : "+programmer);
							if(program.open()){
								byte[] targetID = program.read("DEVID",0,3);
								if(targetID!=null){
									out.println("Device ID = " + Hexadecimal.fromBytes(targetID));
									ret = 0;
									//Erase All
									if(cli.hasOption("e")){
										if(program.erase(null)){
											out.println("Target erased");
										}else{
											err.println("An error occured during erase...");
										}
									}
									//Actions
									int actions = cli.getOptionValueCount("U");
									if(actions > 0){
										//Get sections
										Object[] sections = targetsConfig.sections(target);
										for(int i = 0; i < actions && ret == 0; i++){
						                    Pattern pattern = Pattern.compile("^(.*):([rwv]):(.*)$");
						                    Matcher matcher = pattern.matcher(cli.getOptionValue("U", i));
						                    if (matcher.matches()) {
						                    	//Search memory in section
						                    	String memory = matcher.group(1);
						                    	MemoryHex memoryObj = null;
												for(int j=0;j<sections.length;j++){
													if(sections[j] instanceof MemoryHex){
														String name = ((MemoryHex)sections[j]).name;
							                    		String[] suffix = name.split("-");
							                    		if(suffix != null && suffix.length > 0){
							                    			name = suffix[0];
							                    		}
							                    		if(name != null && name.equalsIgnoreCase(memory)){
							                    			memoryObj = ((MemoryHex)sections[j]);
							                    		}
													}
												}
						                    	if(memoryObj != null){
						                    		String action = matcher.group(2);
					                    			//Decode file type
						                    		String file = matcher.group(3);
						                    		String codec = "a";
						                    		String[] fOpt = file.split(":");
						                    		if(fOpt != null && fOpt.length>1){
						                    			file = fOpt[0];
						                    			codec = fOpt[1];
						                    		}
						                    		//Action
						                    		ret = action(action.getBytes()[0], memoryObj, codec.getBytes()[0], file);
						                    	}else{
						                    		String[] memories = new String[sections.length];
													for(int j=0;j<sections.length;j++){
														if(sections[j] instanceof MemoryHex){
															memories[j] = ((MemoryHex)sections[j]).name;
														}
													}
						                    		errMemory(memories);
						                    	}
						                    }else{
				                    			errAction();
						                    }
										}
									}
								}else{
									err.println("Target ID is invalid");
								}
							}else{
								err.println("Target initialization error...");
							}
						} catch (IOException e) {
							err.println(deviceException(e));
						}finally{
							if(program!=null)
								program.close();
						}
					}else if(programmer!=null){
						errProgrammer(programmers);
					}else{
						errPort(super.getDeviceList());
					}
				}else if(programmers != null && programmers.length > 0){
					errProgrammer(programmers);
				}else{
					errTarget(target);
				}
			}
		}
		out.println("----------------------------------------");
		return ret;
	}
	
	private int action(byte action, MemoryHex memory, byte codec, String filename) throws IOException{
		int ret = -1;
		memory.setStatusListener(new StatusBar(out));
		DataArray tabdata = new DataArray();
		tabdata.sections.clear();
		tabdata.sections.add(memory);
		tabdata.sectionsLock = true;
		tabdata.sectionsCalculation();
		if(action == 'r'){
			out.println("reading "+memory.name);
			if(memory.read(program, tabdata.rawdata, 0) >= 0){
				out.println("");
				if(codec != 'm' && filename != null && filename.length() > 0){
					out.println("writing output file "+filename);
					tabdata.saveSection(filename, codec, memory);
				}else{
					out.println("writing output : ");
					tabdata.saveSection(out, codec, memory);
				}
			}else{
				err.println("An error occured at address 0x"+Integer.toHexString(memory.progAddress).toUpperCase());
			}
		}else if(action == 'w'){
			//TODO
			out.println("reading input file "+filename);
			out.println("writing "+memory.name);
			//memory.write(program, tabdata.rawdata, 0);
			out.println("");
		}else if(action == 'v'){
			//TODO
			out.println("reading input file "+filename);
			out.println("verifying "+memory.name);
			//memory.verify(program, tabdata.rawdata, 0);
			out.println("");
		}
		return ret;
	}
	
	private void errAction(){
		err.println("---------------- action ---------------");
		err.println("----------------------------------------");
		err.println("-U <memtype>:r|w|v:<filename>[:format]");
		err.println("* memory, action or file not found");
	}
	private void errMemory(String memories[]){
		err.println("---------------- memtype --------------");
		err.println("----------------------------------------");
		err.println("-U <memtype>:r|w|v:<filename>[:format]");
		if(memories != null && memories.length > 0){
			for(int i = 0; i < memories.length; i++){
				if(memories[i] != null)
					err.println("* "+memories[i]);
			}
		}
	}
	private void errTarget(String name){
		err.println("----------------- target ---------------");
		err.println("----------------------------------------");
		err.println("-p <partno>		Target name");
		err.println("* target "+name+" not found");
	}
	private void errProgrammer(String programmers[]){
		err.println("--------------- programmer -------------");
		err.println("----------------------------------------");
		err.println("-c <programmer>		Programmer name");
		if(programmers != null && programmers.length > 0){
			for(int i = 0; i < programmers.length; i++){
				err.println("* "+programmers[i]);
			}
		}
	}
	private void errPort(String ports[]){
		err.println("------------------ port ----------------");
		err.println("----------------------------------------");
		err.println("-P <port>		Connection port");
		if(ports != null && ports.length > 0){
			for(int i = 0; i < ports.length; i++){
				err.println("* "+ports[i]);
			}
		}
		err.println("* sp://password@[ipaddr]/");
		err.println("* sp://	(QR code scan)");
	}
	private void help(){
		help(out);
	}

	public static void help(PrintStream out){
		out.println("------------------ help ----------------");
		out.println("----------------------------------------");
		out.println("Usage : mcuprogrammer [options]");
		out.println("Options :");
		out.println("-C <config-file>	Configuration file xml");
		out.println("-P <port>		Connection port");
		out.println("-c <programmer>		Programmer name");
		out.println("-p <partno>		Target name");
		out.println("-U <memtype>:r|w|v:<filename>[:format]");
		out.println("-e			Erase all memories");
		out.println("-h			Print this help");
		out.println("----------------- example --------------");
		out.println("Read an arduino 2560 eeprom in file eeprom.hex");
		out.println("");
		out.println("McuProgrammer -P COM7 -p \"Mega 2560\" -c STK500v2 -U eeprom:r:eeprom.hex:h");
	}
	
	private class StatusBar extends ProgressBar implements StatusListener{
		public StatusBar(PrintStream stream) {
			super(stream);
		}
	}

}
