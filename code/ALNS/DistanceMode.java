package test1.LRP_ALNS_Basline;

public enum DistanceMode {
    // distance calculated methods (Follow the code in format.txt)
    EUCLIDEAN,
    CEIL_EUCLIDEAN,
    ROUND_EUCLIDEAN,
    TRUNCATED_EUCLIDEAN_X100;

    public static DistanceMode fromCode(int code) {
        switch (code) {
            case 0:
                return EUCLIDEAN;
            case 1:
                return CEIL_EUCLIDEAN;
            case 2:
                return ROUND_EUCLIDEAN;
            default:
                throw new IllegalArgumentException("Unknown distance mode code: " + code);
        }
    }
}