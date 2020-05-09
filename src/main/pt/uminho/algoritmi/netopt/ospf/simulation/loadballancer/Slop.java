package pt.uminho.algoritmi.netopt.ospf.simulation.loadballancer;

public class Slop implements IHFunction {

	double slop;
	
	public Slop(double a){
		this.slop =a;
	}
	
	public Slop(){
		this(0.1);
	}
	
	@Override
	public double f(double h, double p) {
		double v = - slop/p * h +1;
		if(v>0)
		    return v;
		else 
			return 0;
	}
}
