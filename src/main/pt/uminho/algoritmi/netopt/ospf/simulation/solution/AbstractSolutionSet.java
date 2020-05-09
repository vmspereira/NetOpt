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
package pt.uminho.algoritmi.netopt.ospf.simulation.solution;

import java.util.ArrayList;
import java.util.List;

public class AbstractSolutionSet<E> implements ASolutionSet<E>{
	
	List<ASolution<E>> set;
	
	
	public AbstractSolutionSet(){
		set =new ArrayList<ASolution<E>>();
	}
	

	@Override
	public int getNumberOfSolutions() {
		return set.size();
	}

	@Override
	public int getNumberOfObjectives() {
		try{
			return set.get(0).getNumberOfObjectives();
		}catch(NullPointerException e){
			return 0;
		}
	}

	@Override
	public ASolution<E> getSolution(int i) {
		return set.get(i);
	}

	@Override
	public void setSolution(int i, ASolution<E> solution) {
		set.set(i, solution);
	}

	@Override
	public void add(ASolution<E> solution) {
		set.add(solution);
	}


	@Override
	public List<ASolution<E>> getSolutions() {
		return set;
	}
	

}
