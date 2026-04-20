package test1.LRP_ALNS_Basline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Route {
    private int facilityId;
    private final List<Integer> customerIds;

    public Route(int facilityId) {
        this.facilityId = facilityId;
        this.customerIds = new ArrayList<>();
    }

    public Route(Route other) {
        this.facilityId = other.facilityId;
        this.customerIds = new ArrayList<>(other.customerIds);
    }

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public List<Integer> getCustomerIds() {
        return customerIds;
    }

    public List<Integer> getCustomerIdsView() {
        return Collections.unmodifiableList(customerIds);
    }

    public boolean isEmpty() {
        return customerIds.isEmpty();
    }

    public int size() {
        return customerIds.size();
    }

    public double getDemand(LRPInstance instance) {
        double sum = 0.0;
        for (int customerId : customerIds) {
            sum += instance.getCustomerById(customerId).getDemand();
        }
        return sum;
    }

    public void addCustomer(int customerId) {
        customerIds.add(customerId);
    }

    public void addCustomer(int position, int customerId) {
        customerIds.add(position, customerId);
    }

    public boolean removeCustomer(Integer customerId) {
        return customerIds.remove(customerId);
    }

    public int removeCustomerAt(int index) {
        return customerIds.remove(index);
    }
}
