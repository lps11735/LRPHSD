package test1.LRP_ALNS_Basline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class F1Reader {

    public static LRPInstance read(String filePath) throws IOException {
        try (Scanner sc = new Scanner(new File(filePath))) {

            int customerCount = sc.nextInt();
            int facilityCount = sc.nextInt();

            double[] depotX = new double[facilityCount];
            double[] depotY = new double[facilityCount];
            for (int i = 0; i < facilityCount; i++) {
                depotX[i] = sc.nextDouble();
                depotY[i] = sc.nextDouble();
            }

            double[] customerX = new double[customerCount];
            double[] customerY = new double[customerCount];
            for (int i = 0; i < customerCount; i++) {
                customerX[i] = sc.nextDouble();
                customerY[i] = sc.nextDouble();
            }

            double vehicleCapacity = sc.nextDouble();

            double[] depotCapacities = new double[facilityCount];
            for (int i = 0; i < facilityCount; i++) {
                depotCapacities[i] = sc.nextDouble();
            }

            double[] customerDemands = new double[customerCount];
            for (int i = 0; i < customerCount; i++) {
                customerDemands[i] = sc.nextDouble();
            }

            double[] depotOpeningCosts = new double[facilityCount];
            for (int i = 0; i < facilityCount; i++) {
                depotOpeningCosts[i] = sc.nextDouble();
            }

            double vehicleFixedCost = sc.nextDouble();

            int distanceCode = sc.nextInt();

            List<Customer> customers = new ArrayList<>();
            List<Facility> facilities = new ArrayList<>();

            // Customer ids: 1 ... customerCount
            for (int i = 0; i < customerCount; i++) {
                int customerId = i + 1;
                customers.add(new Customer(
                        customerId,
                        customerX[i],
                        customerY[i],
                        customerDemands[i]
                ));
            }

            // Facility ids: customerCount + 1 ... customerCount + facilityCount
            for (int i = 0; i < facilityCount; i++) {
                int facilityId = customerCount + i + 1;

                // This format does not provide maxVehicles explicitly.
                // Use 0 to indicate "no explicit limit" in the current framework.
                int maxVehicles = 0;

                facilities.add(new Facility(
                        facilityId,
                        depotX[i],
                        depotY[i],
                        depotOpeningCosts[i],
                        depotCapacities[i],
                        maxVehicles
                ));
            }

            DistanceMode distanceMode;
            double distanceCostFactor;

            if (distanceCode == 1) {
                // Real costs: standard Euclidean distance
                distanceMode = DistanceMode.EUCLIDEAN;
                distanceCostFactor = 1.0;
            } else if (distanceCode == 0) {
                // Integer costs: trunc(100 * Euclidean distance)
                // Recommended: use a dedicated distance mode
                distanceMode = DistanceMode.TRUNCATED_EUCLIDEAN_X100;
                distanceCostFactor = 1.0;
            } else {
                throw new IllegalArgumentException("Unknown distance code in F1: " + distanceCode);
            }

            double lowerBound = 0.0;
            double upperBound = 0.0;

            return new LRPInstance(
                    customerCount,
                    facilityCount,
                    vehicleCapacity,
                    vehicleFixedCost,
                    distanceCostFactor,
                    lowerBound,
                    upperBound,
                    distanceMode,
                    customers,
                    facilities
            );
        }
    }
}