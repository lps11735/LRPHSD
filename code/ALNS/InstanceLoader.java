package test1.LRP_ALNS_Basline;

public class InstanceLoader {

    public static LRPInstance load(String instanceType, String filePath) throws Exception {
        return switch (instanceType.toUpperCase()) {
            case "F1" -> F1Reader.read(filePath);
            case "F2" -> F2Reader.read(filePath);
            case "F3" -> F3Reader.read(filePath);
            case "F4" -> F4Reader.read(filePath);
            case "F5" -> F5Reader.read(filePath);
            default -> throw new IllegalArgumentException("Unknown instance type: " + instanceType);
        };
    }
}