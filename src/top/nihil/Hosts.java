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
    }

    private void readHosts() {
        try {
            File file = new File(filePath);
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader br = new BufferedReader(inputStreamReader);
            String line = null;
            while((line = br.readLine())!=null){
                String[] ipAndHost = line.split(" ");
                if(ipAndHost.length == 2) {
                    hostMap.put(ipAndHost[1], ipAndHost[0]);
                }
            }
            br.close();
            log.info("read hosts");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
