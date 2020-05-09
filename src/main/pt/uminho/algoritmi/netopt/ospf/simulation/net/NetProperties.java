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
package pt.uminho.algoritmi.netopt.ospf.simulation.net;

import java.util.Properties;

@SuppressWarnings("serial")
public class NetProperties extends Properties {
	
	public int getPropertyInt(String name, int def) {
		int r = def;
		try {
			r = Integer.parseInt(this.getProperty(name));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}
	
	
	public double getPropertyDouble(String name, double def) {
		double r = def;
		try {
			r = Double.parseDouble(this.getProperty(name));
		} catch (Exception e) {
		}
		return r;
	}

	

}
