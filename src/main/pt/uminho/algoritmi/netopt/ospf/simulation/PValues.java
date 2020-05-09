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
package pt.uminho.algoritmi.netopt.ospf.simulation;

import java.util.concurrent.ThreadLocalRandom;

import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;

public class PValues {
	
	double[] values;
	
	public PValues(int size){
		values=new double[size];  	
	}
	
	
	public PValues(double[] p){
		values=p;  	
	}
	
	
	public double[] getPValues(){
		return values;
	}


	public int getDimension() {
		return this.values.length;
	}


	public void setRandomWeights(double min, double max) {
		for(int i=0;i<values.length;i++){
			if(min>=max)
				values[i]=min;
			else
				values[i]=ThreadLocalRandom.current().nextDouble(min, max);
		}
	}


	public void setValue(int index, Double aValue) {
		values[index]=aValue;
	}
	
	/*
	 *  Sets all node-p values to a specified value
	 */
	public void setValue(Double aValue) {
		for(int i=0;i<values.length;i++)
			values[i]=aValue;
	}


	public void setValues(double[] pv) throws DimensionErrorException {
		if(pv.length!=values.length)
			throw new DimensionErrorException();
		for(int i=0;i<values.length;i++)
			values[i]=pv[i];
	}

}
