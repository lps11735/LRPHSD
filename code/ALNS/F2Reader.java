package test1.LRP_ALNS_Basline;

import java.io.IOException;

public class F2Reader {

    public static LRPInstance read(String filePath) throws IOException {
        return F1Reader.read(filePath);
    }
}