package pt.uminho.algoritmi.netopt.ospf.utils;

import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;

/**
 * 
 * Interface for Shortest Path Comparator
 *
 */
public interface SPComparator {
	
	public double compare() throws DimensionErrorException;

}
