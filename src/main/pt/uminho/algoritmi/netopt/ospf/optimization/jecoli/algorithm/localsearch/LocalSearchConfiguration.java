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
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.localsearch;

import jecoli.algorithm.components.configuration.AbstractConfiguration;
import jecoli.algorithm.components.configuration.IConfiguration;
import jecoli.algorithm.components.configuration.InvalidConfigurationException;
import jecoli.algorithm.components.representation.IRepresentation;
import jecoli.algorithm.components.solution.ISolutionFactory;
import jecoli.algorithm.components.terminationcriteria.InvalidNumberOfIterationsException;

@SuppressWarnings("serial")
public class LocalSearchConfiguration<T extends IRepresentation, S extends ISolutionFactory<T>> extends AbstractConfiguration<T>{

	@Override
	public void verifyConfiguration() throws InvalidConfigurationException {
		// TODO Auto-generated method stub
	}

	@Override
	public IConfiguration<T> deepCopy() throws InvalidNumberOfIterationsException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
