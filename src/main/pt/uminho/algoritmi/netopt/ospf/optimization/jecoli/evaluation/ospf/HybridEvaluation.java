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
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.ospf;

import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode.NodeType;

public class HybridEvaluation extends OSPFIntegerEvaluation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public HybridEvaluation(NetworkTopology topology, Demands[] demands) {
		super(topology, demands);
	}
	
	
	protected int[] decodeWeights (ILinearRepresentation<Integer> solution)
	{
		int size= this.topology.getNumberEdges();
		int[] res = new int[size];
		for(int i=0; i < size; i++)
		{
			res[i] = solution.getElementAt(i);
		}
		return res;
	}

	
	protected int[] decodeNodes (ILinearRepresentation<Integer> solution)
	{
		int wsize= this.topology.getNumberEdges();
		int nsize=solution.getNumberOfElements()-wsize;
		int[] res = new int[nsize];
		for(int i=0; i < nsize; i++)
		{
			res[i] = solution.getElementAt(i+wsize);
		}
		return res;
	}
	
	
	@Override
	public double evaluate(ILinearRepresentation<Integer> solution) throws DimensionErrorException {	
		int[] weights = decodeWeights(solution);
		int[] nodes = decodeNodes(solution);
		for(int i=0;i<nodes.length;i++){
			NodeType type =NodeType.LEGACY;
			if(nodes[i]>0)
				type =NodeType.SDN_SR;
			topology.getNetGraph().getNodeAt(i).setNodeType(type);
		}
		double fitness = evalWeights(weights);
		return fitness;
	}
	
	@Override
	public HybridEvaluation deepCopy(){
		Demands[] d = new Demands[demands.length];
		for(int i=0; i<demands.length;i++)
			d[i]=demands[i].copy();
		return new HybridEvaluation(this.topology.copy(),d);
	}
	
	
	
		
	
}
