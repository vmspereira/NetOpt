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
package pt.uminho.algoritmi.netopt.ospf.simulation.simulators;

import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;

/**
 * 
 * @author Vitor
 *
 * TODO: generalize for n traffic demand matrices 
 */

public class MultiDemandsSimul extends Simul implements IMultiDemandSimulator,ISimulator{

	public MultiDemandsSimul(NetworkTopology topology) {
		super(topology);
	}
	
	
	protected double evalWeights(int[] weights, double alfa, boolean timeDebug,
			Demands demands, Demands demands2) throws DimensionErrorException 
	{
		double[] res = evalWeightsMO(weights, alfa > 0, alfa < 1,false, demands, demands2);
		if (fullDebug) {
			System.out.println(""+(alfa * res[0] + (1.0 - alfa) * res[1])+"->obj1="+res[0]+" obj2="+res[1]);
		}
		return (alfa * res[0] + (1.0 - alfa) * res[1]);
	}
	
	public double[] evalWeightsMO(int[] weights, boolean computeDemands1,boolean computeDemands2, boolean timeDebug, Demands demands1,
			Demands demands2) {
			
		NetworkLoads loads;
		
		double res1 = 0.0;
		double res2 = 0.0;
		
		
		if (computeDemands1) {
			topology.applyWeights(weights);
			loads = new NetworkLoads(totalLoads(demands1),topology);
			res1 = congestionMeasure(loads,demands1);
		}
		
		if (computeDemands2) {
			topology.applyWeights(weights);
			loads = new NetworkLoads(totalLoads(demands2),topology);
			res2 = congestionMeasure(loads,demands2);
		}

		double[] result = new double[2];
		result[0] = res1;
		result[1] = res2;
		return result;
	}

	public double evalWeights(int[] weights, double alfa, Demands demands,
			Demands demands2) {
		double[] res = evalWeightsMO(weights,alfa > 0, alfa < 1,false, demands, demands2);
		return (alfa * res[0] + (1.0 - alfa) * res[1]);
	}
	


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	@Override
	public double[] evalWeightsMOMLU(int[] weights, Demands demands) {
	NetworkLoads loads;
		
		double res1 = 0.0;
		double res2 = 0.0;
		
		
		topology.applyWeights(weights);
		loads = new NetworkLoads(totalLoads(demands),topology);
		res1 = congestionMeasure(loads,demands);
		res2=loads.getMLU();
		double[] result = new double[2];
		result[0] = res1;
		result[1] = res2;
		return result;
	}


	@Override
	public double evalWeightsMLU(int[] weights, double alpha, Demands demands) {
		double[] res= evalWeightsMOMLU(weights,demands);
		//needs a normalization
		return (alpha * res[0] + (1.0 - alpha) * res[1]);
	}

	
	@Override
	public double[] evalWeightsMOALU(int[] weights, Demands demands) {
	NetworkLoads loads;
		
		double res1 = 0.0;
		double res2 = 0.0;
		
		
		topology.applyWeights(weights);
		loads = new NetworkLoads(totalLoads(demands),topology);
		res1 = congestionMeasure(loads,demands);
		res2=loads.getALU();
		double[] result = new double[2];
		result[0] = res1;
		result[1] = res2;
		return result;
	}


	@Override
	public double evalWeightsALU(int[] weights, double alpha, Demands demands) {
		double[] res= evalWeightsMOALU(weights,demands);
		//needs a normalization
		return (alpha * res[0] + (1.0 - alpha) * res[1]);
	}

}
