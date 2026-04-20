package test1.LRP_ALNS_Basline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InstanceReader {

    public static LRPInstance read(String filePath) throws IOException {
        return readOriginalFormat(filePath);
    }

    public static LRPInstance readOriginalFormat(String filePath) throws IOException {
        try (Scanner sc = new Scanner(new File(filePath))) {

            int customerCount = sc.nextInt();
            int facilityCount = sc.nextInt();
            double vehicleCapacity = sc.nextDouble();
            double vehicleFixedCost = sc.nextDouble();
            double distanceCostFactor = sc.nextDouble();

            double lowerBound = sc.nextDouble();
            double upperBound = sc.nextDouble();
            int distanceModeCode = sc.nextInt();
            DistanceMode distanceMode = DistanceMode.fromCode(distanceModeCode);

            List<Customer> customers = new ArrayList<>();
            List<Facility> facilities = new ArrayList<>();

            for (int i = 0; i < customerCount; i++) {
                int id = sc.nextInt();
                double x = sc.nextDouble();
                double y = sc.nextDouble();
                double demand = sc.nextDouble();
                customers.add(new Customer(id, x, y, demand));
            }

            for (int i = 0; i < facilityCount; i++) {
                int id = sc.nextInt();
                double x = sc.nextDouble();
                double y = sc.nextDouble();
                double openCost = sc.nextDouble();
                double capacity = sc.nextDouble();
                int maxVehicles = sc.nextInt();

                facilities.add(new Facility(id, x, y, openCost, capacity, maxVehicles));
            }

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