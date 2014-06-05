package edu.sdsc.scigraph.internal.reachability;

/**
 * Helper class for parallel canReach() test.. 
 * @author chenjing
 *
 */
public class ReachabilityTestContext {
	
//	ExecutorService executorService;
	
	private boolean allSatisfied;
	
	public ReachabilityTestContext ( /*ExecutorService executorService*/) {
	//	this.executorService = executorService;
		allSatisfied = true;
	}
	
    boolean isAllSatisfied () {return allSatisfied;}
	
    void setAllSatisfied (boolean flag) {allSatisfied = flag;} 
	
}
