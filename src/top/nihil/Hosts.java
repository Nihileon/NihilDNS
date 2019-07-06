package top.nihil;

import lombok.Data;
import lombok.extern.java.Log;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Data
@Log
public class Hosts {
    private String filePath;
    private Map<String, String> hostMap = new HashMap<>();

    public Hosts(String filePath) {
        this.filePath = filePath;
        readHosts();
    }

    private void readHosts() {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(filePath));

            BufferedReader br = new BufferedReader(inputStreamReader);
            String line;
            while ((line = br.readLine()) != null) {
                String[] ipAndHost = line.split(" ");
                if (ipAndHost.length == 2) {
                    hostMap.put(ipAndHost[1], ipAndHost[0]);
                }
            }

            br.close();
            log.info("Read hosts successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
