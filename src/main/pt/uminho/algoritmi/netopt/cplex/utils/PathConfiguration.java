/*******************************************************************************
 * Copyright 2012-2019,
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

package pt.uminho.algoritmi.netopt.cplex.utils;

import ilog.concert.IloNumVar;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;

public class PathConfiguration {

	
	private LabelPath path;
	private IloNumVar fraction;
	
	public PathConfiguration(IloNumVar fraction,LabelPath path){
		this.setFractionVar(fraction);
		this.setPath(path);
	}

	public LabelPath getPath() {
		return path;
	}

	public void setPath(LabelPath path) {
		this.path = path;
	}

	public IloNumVar getFractionVar() {
		return fraction;
	}

	public void setFractionVar(IloNumVar fraction) {
		this.fraction = fraction;
	}
	
}
