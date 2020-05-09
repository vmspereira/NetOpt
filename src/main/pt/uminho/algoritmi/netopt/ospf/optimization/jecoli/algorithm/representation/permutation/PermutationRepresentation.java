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
/**
* Copyright 2009,
* CCTC - Computer Science and Technology Center
* IBB-CEB - Institute for Biotechnology and  Bioengineering - Centre of Biological Engineering
* University of Minho
*
* This is free software: you can redistribute it and/or modify
* it under the terms of the GNU Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This code is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Public License for more details.
*
* You should have received a copy of the GNU Public License
* along with this code.  If not, see <http://www.gnu.org/licenses/>.
* 
* Created inside the SysBio Research Group <http://sysbio.di.uminho.pt/>
* University of Minho
*/
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.representation.permutation;

import java.io.Serializable;
import java.util.List;

import jecoli.algorithm.components.representation.IComparableRepresentation;
import jecoli.algorithm.components.representation.IRepresentation;


// TODO: Auto-generated Javadoc
/**
 * The Class PermutationRepresentation.
 */
public class PermutationRepresentation implements IRepresentation,IComparableRepresentation<PermutationRepresentation>,Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5634830566101901226L;
	/** The genome. */
	List<Integer> genome;
	
    /**
     * Instantiates a new permutation representation.
     * 
     * @param genome the genome
     */
    public PermutationRepresentation(List<Integer> genome) {
        this.genome = genome;
    }

    /**
     * Gets the element.
     * 
     * @param index the index
     * 
     * @return the element
     */
    public Integer getElement(int index) {
        return genome.get(index);
    }

    /**
     * Gets the number of elements.
     * 
     * @return the number of elements
     */
    public int getNumberOfElements() {
        return genome.size();
    }

    /**
     * Sets the element.
     * 
     * @param index the index
     * @param element the element
     */
    public void setElement(int index, Integer element) {
        genome.set(index, element);
    }

    /**
     * Gets the genome as array.
     * 
     * @return the genome as array
     */
    public int[] getGenomeAsArray()
    {
    	int[] res = new int[getNumberOfElements()];
    	for(int i=0; i<getNumberOfElements(); i++)
    		res[i] = genome.get(i);
    	return res;
    }
    
    /**
     * Check if permutation.
     * 
     * @return true, if successful
     */
 /*   public boolean checkIfPermutation()
    {
    	int max = getNumberOfElements()-1;
    	boolean [] isThere = new boolean[max+1];
    	for(int i=0; i <= max; i++)	isThere[i] = false;
    	for(int i=0; i <= max; i++)
    	{
    		int element = getElement(i);
    		if (element <0 || element > max) return false; // invalid values
    		if (isThere[element]) return false; // duplicates
    		else isThere[element] = true;
    	}
    	for(int i=0; i <= max; i++) 
    		if (!isThere[i]) return false;
    	return true;
    }
*/
	/* (non-Javadoc)
	 * @see core.representation.IRepresentation#stringRepresentation()
	 */
	@Override
	public String stringRepresentation() {
		String genomeStringRepresentation = "";
		int nunberOfElements = genome.size();
		
		for(int i = 0; i < nunberOfElements;i++){
			int geneValue = genome.get(i);
			genomeStringRepresentation += " " + geneValue;
		}
			
		return genomeStringRepresentation;
	}
	
	public List<Integer> getGenome() {
		return genome;
	}

	@Override
	public boolean equals(PermutationRepresentation rep) {		
		return genome.equals(rep.getGenome());		
	}
}
