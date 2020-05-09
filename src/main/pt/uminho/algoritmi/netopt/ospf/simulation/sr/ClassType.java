package pt.uminho.algoritmi.netopt.ospf.simulation.sr;

public enum ClassType {
	EF(46),
	BE(0),
	AF11(10),
	AF12(12),
	AF13(14),
	AF21(18),
	AF22(20),
	AF23(22),
	AF31(26),
	AF32(28),
	AF33(30),
	AF41(34),
	AF42(36),
	AF43(38);
	
	private final int value;
	
	private ClassType(int value) {
	        this.value = value;
	}
	
	public int getValue() {
        return value;
    }
}
