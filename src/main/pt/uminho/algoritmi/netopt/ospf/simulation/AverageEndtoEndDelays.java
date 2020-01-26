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

import java.io.Serializable;

@SuppressWarnings("serial")
public class AverageEndtoEndDelays implements Serializable{

	private double[][] endToEndDelays;
	private double sumDelays;
	private double delayPenalties;
	
	public AverageEndtoEndDelays(int dimension)
	{
		this.endToEndDelays = new double[dimension][dimension];
	}
	
	public void setEndToEndDelays(double[][] endToEndDelays) {
		this.endToEndDelays = endToEndDelays;
	}
	
	public void setDelayPenalties(double delayPenalties)
	{
		this.delayPenalties=delayPenalties;
	}
	
	public double getDelayPenalties()
	{
		return this.delayPenalties;
	}
	
	public double[][] getEndToEndDelays() {
		return endToEndDelays;
	}
	public void setSumDelays(double sumDelays) {
		this.sumDelays = sumDelays;
	}
	public double getSumDelays() {
		return sumDelays;
	}

}
