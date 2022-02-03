package afuera.exp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import afuera.flow.config.FileConfig;

public class BoxPlotPackage {
	public static void main(String args[]) throws IOException {
		//String ues = "script/ue/";
		//String alls= "script/all/";
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		boolean runitonce = true;
		List<String> records = new ArrayList<String>();
		int startSmall = 0;
		for(File ue : new File(FileConfig.UE_USAGEs).listFiles()) {
			/*
			 * Put top 10 exception based on RQ1;
			 */
			map.put("android.widget", 0);
			map.put("android.app", 0);
			map.put("android.view", 0);
			map.put("android.content", 0);
			map.put("android.os", 0);
			map.put("android.graphics", 0);
			map.put("android.provider", 0);
			map.put("android.media",0);
			map.put("android.renderscript", 0);
			map.put("android.net", 0);
			//map.put("android.webkit", 0);
			//map.put("android.filterfw.core", 0);
			if(ue.getName().endsWith("-2020.apk.txt")) {
				startSmall++;
				if(startSmall > 200)
					break;
				double all_size = (double) read(FileConfig.ALL_USAGEs+ue.getName()).size();
				double ue_size = (double) read(FileConfig.UE_USAGEs+ue.getName()).size();
				List<String> ueList = read(ue.getAbsolutePath());
				List<String> ue_exceptionList = read(FileConfig.DOC_API_PACKAGE);
				for(String ueMethod : ueList) {
					for(String ue_exception : ue_exceptionList) {
						//System.out.println(ue_exception);
						String[] arr = ue_exception.split("-");
						if(ueMethod.equals(arr[0])) {
							if(map.containsKey(arr[1])) {
								map.put(arr[1],map.get(arr[1])+1);
								break;//only count package one time	
							}
						}
					}
				}
				if(runitonce) {
					StringBuilder sb0 = new StringBuilder();
					for(Iterator<String> iter0 = map.keySet().iterator(); iter0.hasNext();) {
						sb0.append(","+CountSignalers.removeAndroidDot(iter0.next()));
					}
					records.add(sb0.toString().substring(1));
					runitonce = false;
				}
				StringBuilder sb = new StringBuilder();
				Iterator<String> iter = map.keySet().iterator();
				sb.append(map.get(iter.next()) / ue_size);
				while(iter.hasNext()) {
					sb.append(",").append(map.get(iter.next()) / ue_size);
				}
				System.out.println(records.size());
				records.add(sb.toString());
			}
		}
		write(FileConfig.STAT_PACKAGE_BOXPLOT, records);
	}
	public static List<String> read(String filePath) throws IOException{
		List<String> apis = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
		String line = null;
		while((line = br.readLine())!=null) {
			apis.add(line.split(",")[0]);
		}
		br.close();
		return apis;
	}
	public static void write(String filePath, List<String> list) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filePath)));
		for(String line : list) {
			if(line==null)
				continue;
			bw.write(line);
			bw.newLine();
		}
		bw.close();
	}
}
