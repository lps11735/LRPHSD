package test1.LRP_ALNS_Basline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LRPInstance {
    private final int customerCount;
    private final int facilityCount;
    private final double vehicleCapacity;
    private final double vehicleFixedCost;
    private final double distanceCostFactor;
    private final double lowerBound;
    private final double upperBound;
    private final DistanceMode distanceMode;
    private final List<Customer> customers;
    private final List<Facility> facilities;

    public LRPInstance(
            int customerCount,
            int facilityCount,
            double vehicleCapacity,
            double vehicleFixedCost,
            double distanceCostFactor,
            double lowerBound,
            double upperBound,
            DistanceMode distanceMode,
            List<Customer> customers,
            List<Facility> facilities) {
        this.customerCount = customerCount;
        this.facilityCount = facilityCount;
        this.vehicleCapacity = vehicleCapacity;
        this.vehicleFixedCost = vehicleFixedCost;
        this.distanceCostFactor = distanceCostFactor;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.distanceMode = distanceMode;
        this.customers = Collections.unmodifiableList(new ArrayList<>(customers));
        this.facilities = Collections.unmodifiableList(new ArrayList<>(facilities));
    }

    public int getCustomerCount() {
        return customerCount;
    }

    public int getFacilityCount() {
        return facilityCount;
    }

    public double getVehicleCapacity() {
        return vehicleCapacity;
    }

    public double getVehicleFixedCost() {
        return vehicleFixedCost;
    }

    public double getDistanceCostFactor() {
        return distanceCostFactor;
    }

    public double getEffectiveDistanceCostFactor() {
        return distanceCostFactor > 0.0 ? distanceCostFactor : 1.0;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public DistanceMode getDistanceMode() {
        return distanceMode;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Facility> getFacilities() {
        return facilities;
    }

    public Customer getCustomerById(int id) {
        if (id < 1 || id > customers.size()) {
            throw new IllegalArgumentException("Invalid customer id: " + id);
        }
        return customers.get(id - 1);
    }

    public Facility getFacilityById(int id) {
        int index = id - customerCount - 1;
        if (index < 0 || index >= facilities.size()) {
            throw new IllegalArgumentException("Invalid facility id: " + id);
        }
        return facilities.get(index);
    }
}
