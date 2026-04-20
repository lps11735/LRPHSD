package test1.LRP_ALNS_Basline;

import java.io.IOException;

public class F3Reader {

    public static LRPInstance read(String filePath) throws IOException {
        return InstanceReader.readOriginalFormat(filePath);
    }
}