/*******************************************************************************
 * Copyright 2012-2017,
 *  Centro Algoritmi - University of Minho
 * 
 *  This is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This code is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Public License for more details.
 * 
 *  You should have received a copy of the GNU Public License
 *  along with this code.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  @author Vítor Pereira
 ******************************************************************************/
package pt.uminho.algoritmi.netopt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class SystemConf {

	private SystemConf() {
	}

	public static SystemConf getInstance() {
		return _instance;
	}

	public static Properties getProperties(String s) {
		Properties properties = new Properties();
		try {
			FileInputStream fileinputstream = new FileInputStream(s);
			properties.load(fileinputstream);
			fileinputstream.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return properties;
	}

	public static Properties getProperties() {
		System.out.println(CONFFILE);
		return SystemConf.getProperties(CONFFILE);
	}

	public static void setProperties(Properties properties, String s) {
		try {
			File file = new File(s);
			FileOutputStream fileoutputstream = new FileOutputStream(file);
			properties.store(fileoutputstream, "");
			fileoutputstream.close();
		} catch (Exception exception) {
		}
	}
	
	public static void setProperties(Properties properties) {
		try {
			File file = new File(CONFFILE);
			FileOutputStream fileoutputstream = new FileOutputStream(file);
			properties.store(fileoutputstream, "");
			fileoutputstream.close();
		} catch (Exception exception) {
		}
	}

	public static String getProperty(String s) {
		Properties properties = new Properties();
		try {
			FileInputStream fileinputstream = new FileInputStream(
					SystemConf.CONFFILE);
			
			properties.load(fileinputstream);
			fileinputstream.close();
		} catch (Exception exception) {
			//exception.printStackTrace();
		}
		return properties.getProperty(s);
	}

	private static SystemConf _instance = new SystemConf();
	private static final String CONFFILE;

	static {
		CONFFILE = System.getProperty("user.dir") + File.separator + "conf"
				+ File.separator + "netopt.conf";
	}

	public static int getPropertyInt(String name, int def) {
		int r = def;
		try {
			r = Integer.parseInt(SystemConf.getProperty(name));
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return r;
	}
	
	
	public static double getPropertyDouble(String name, double def) {
		double r = def;
		try {
			r = Double.parseDouble(SystemConf.getProperty(name));
		} catch (Exception e) {
		}
		return r;
	}
	
	
	public static String getPropertyString(String name, String def) {
		String r = def;
		try {
			String s = SystemConf.getProperty(name);
			if(s!=null)
				r=s;
		} catch (Exception e) {
		}
		return r;
	}

	public static boolean getPropertyBoolean(String name, boolean def) {
		boolean r = def;
		try {
			String s = SystemConf.getProperty(name);
			if(s!=null)
				r=Boolean.valueOf(s);
		} catch (Exception e) {
		}
		return r;
	}

}
