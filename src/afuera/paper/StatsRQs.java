package afuera.paper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import afuera.flow.config.FileConfig;

public class StatsRQs{
    public static void main(String args[]) throws JSONException, IOException{
        int rq1_5 = 0;
        int tp = 0;
        int tn = 0;
        int fp = 0;
        int fn = 0;
        int paras = 0;
        for(File folder : new File(FileConfig.GT).listFiles()){
            if(folder.isDirectory()){
                int rq0 = 0;
                for(File f : folder.listFiles()){
                    // System.out.print(folder.getName());
                    JSONObject json;
                    try{
                            json = new JSONObject(read(f));
                        if(!json.has("rq0")){
                            json.put("rq0", "true");
                        }
                        if(!json.has("rq1.5")){
                            json.put("rq1.5", "false");
                        }
                        JSONArray rq3 = json.getJSONArray("rq3");
                        JSONArray afuera_assert = json.getJSONArray("Afuera_asserted_parameters");
                        StringBuilder sb = new StringBuilder();
                        sb.append(json.getString("rq0")).append("\t")
                        .append(json.getString("rq1.5")).append("\t")
                        .append(rq3.toString()).append("\t\t")
                        .append(afuera_assert.toString());
                        // System.out.println(sb.toString());
                        if(json.getString("rq0").equals("false")){
                            rq0 += 1;
                        }
                        if(json.getString("rq1.5").equals("true")){
                            rq1_5 += 1;
                        }
                        if(rq3.isEmpty()){
                            if(afuera_assert.isEmpty()){
                                tn += 1;
                            }else{
                                fp += 1;
                            }
                        }else{
                            if(afuera_assert.isEmpty()){
                                fn += 1;
                            }else{
                                tp += 1;
                                paras += afuera_assert.length();
                            }
                        }
                    }catch(JSONException e){
                        System.out.println(f.getAbsolutePath());
                        throw e;
                    }
                }
                if(rq0 != 0){
                    System.out.println(folder.getName()+" : "+rq0);
                }
            }
        }
        System.out.println("RQ1.5: \t"+rq1_5);
        System.out.println("True Positive:\t"+tp+"\tFalse Positive\t"+fp);
        System.out.println("False Negative:\t"+fn+"\tTrue Negative\t"+tn);
        System.out.println("Asserted Relevant Parameters:\t"+paras);
    }
    public static String read(File f) throws IOException{
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = null;
		while((line = br.readLine())!=null) {
			sb.append(line);
		}
		br.close();
		return sb.toString();
	}
}