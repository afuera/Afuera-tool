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
import soot.SootClass;

public class BoxPlotExceptionType {
	public static void main(String args[]) throws IOException {
		String ues = "script/ue/";
		String alls= "script/all/";
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		boolean runitonce = true;
		List<String> records = new ArrayList<String>();
		int startSmall = 0;
		for(File ue : new File(ues).listFiles()) {
			/*
			 * Put top 10 exception based on RQ1;
			 */
			map.put("java.lang.IllegalArgumentException", 0);
			map.put("java.lang.RuntimeException", 0);
			map.put("java.lang.IllegalStateException", 0);
			map.put("java.lang.NullPointerException", 0);
			map.put("java.lang.UnsupportedOperationException", 0);
			map.put("android.content.res.Resources$NotFoundException",0);
			map.put("java.lang.AssertionError", 0);
			map.put("java.lang.IndexOutOfBoundsException", 0);
			map.put("android.renderscript.RSInvalidStateException", 0);
			map.put("java.lang.ArrayIndexOutOfBoundsException", 0);
			if(ue.getName().endsWith("-2020.apk.txt")) {
				startSmall++;
				if(startSmall > 200)
					break;
				double all_size = (double) read(alls+ue.getName()).size();
				double ue_size = (double) read(ues+ue.getName()).size();
				List<String> ueList = read(ue.getAbsolutePath());
				List<String> ue_exceptionList = read(FileConfig.DOC_API_EXCEPTION);
				for(String ueMethod : ueList) {
					for(String ue_exception : ue_exceptionList) {
						//System.out.println(ue_exception);
						String[] arr = ue_exception.split("-");
						if(ueMethod.equals(arr[0])) {
							if(map.containsKey(arr[1]))
								map.put(arr[1],map.get(arr[1])+1);
						}
					}
				}
				if(runitonce) {
					StringBuilder sb0 = new StringBuilder();
					for(Iterator<String> iter0 = map.keySet().iterator(); iter0.hasNext();) {
						String[] arr = iter0.next().split("\\.");
						String className = arr[arr.length-1];
						if(className.contains("Error")) {
							//
						}else {
							className = className.substring(0,className.length()-9);
						}
						sb0.append(","+className);
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
				records.add(sb.toString());
				System.out.println(records.size());
			}
		}
		write("paper/exceptionboxplot.csv", records);
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
