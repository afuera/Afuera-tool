package afuera.paper;


import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import lux.uni.serval.verdiff.runtimelog.AppRuntimeLog;
/**
 * This is to revisit Fan et al.'s ICSE2018 paper
 */
public class RQ2_3 {
    public static void main(String args[]) throws IOException{
        String recordFolder = "res/fan_et_al_data/";
        Map<String,Set<String>> map = new HashMap<>();
        for(File f : new File(recordFolder).listFiles()){
            if(f.isDirectory() || f.getName().equals(".DS_Store")) {
				continue;
			}
            AppRuntimeLog arl = new AppRuntimeLog(f.getName().split("-")[0], f);
            Set<String> set = map.getOrDefault(arl.getException(), new HashSet<String>());
            // arl.getStackTrace();
            if(arl.getException()==null){
                System.out.println("Null: "+f.getName());
                continue;
            }
            // System.out.println(arl.getException());System.out.println(arl.getFrameworkStackTrace().firstFrameworkCalledByDeveloper().methodName);
            try{
                set.add(arl.getFrameworkStackTrace().firstFrameworkCalledByDeveloper().methodName);
                map.put(arl.getException(), set);
            }catch(Exception e){
                System.out.println(f.getName());
                continue;
            }
        }
        List<Map.Entry<String,Set<String>>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, 
            (entry1, entry2) 
                -> 
            Integer.compare(entry2.getValue().size(),entry1.getValue().size()));
        int total = 0;
        for(Map.Entry<String,Set<String>> entry : list){
            System.out.println(entry.getKey()+","+entry.getValue().size());
            total += entry.getValue().size();
        }
        System.out.println(total);
    }
}
