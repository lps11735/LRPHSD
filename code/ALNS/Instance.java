package test1.LRP_ALNS_Basline;

import java.util.List;

public class Instance {
    private final int customerCount;
    private final int facilityCount;
    private final double vehicleCapacity;
    private final double vehicleFixedCost;
    private final double demandDistanceCost;
    private final double lowerBound;
    private final double upperBound;
    private final int distanceType;
    private final List<Customer> customers;
    private final List<Facility> facilities;

    public Instance(int customerCount,
                    int facilityCount,
                    double vehicleCapacity,
                    double vehicleFixedCost,
                    double demandDistanceCost,
                    double lowerBound,
                    double upperBound,
                    int distanceType,
                    List<Customer> customers,
                    List<Facility> facilities) {
        this.customerCount = customerCount;
        this.facilityCount = facilityCount;
        this.vehicleCapacity = vehicleCapacity;
        this.vehicleFixedCost = vehicleFixedCost;
        this.demandDistanceCost = demandDistanceCost;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.distanceType = distanceType;
        this.customers = customers;
        this.facilities = facilities;
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

    public double getDemandDistanceCost() {
        return demandDistanceCost;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public int getDistanceType() {
        return distanceType;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Facility> getFacilities() {
        return facilities;
    }
}