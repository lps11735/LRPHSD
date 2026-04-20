package test1.LRP_ALNS_Basline;

import java.util.List;

public class RemovalResult {
    private final Solution partialSolution;
    private final List<Integer> removedCustomers;

    public RemovalResult(Solution partialSolution, List<Integer> removedCustomers) {
        this.partialSolution = partialSolution;
        this.removedCustomers = removedCustomers;
    }

    public Solution getPartialSolution() {
        return partialSolution;
    }

    public List<Integer> getRemovedCustomers() {
        return removedCustomers;
    }
}
