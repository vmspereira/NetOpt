package pt.uminho.algoritmi.netopt.ospf.simulation.loadballancer;

public class Exponential implements IHFunction{

	/**
	 * Extends the DEFT splitting function 
	 * by allowing more or less traffic to be forwarded along
	 * non shortest path links
	 */
	
	
	@Override
	public  double f(double h, double p) {
		if(h==0)
			return 1;
		
		if(p>= 0)
			return  Math.exp(-h * p); 
		else
			return  1 - p/h;
	}
}
