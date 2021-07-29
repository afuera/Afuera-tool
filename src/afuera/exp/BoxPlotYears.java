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

public class BoxPlotYears {
	public static void main(String args[]) throws IOException {
		String ues = "script/ue/";
		String alls= "script/all/";
		Map<String, Double> map = new LinkedHashMap<String, Double>();
		boolean runitonce = true;
		List<String> records = new ArrayList<String>();
		List<String> identifiers = new ArrayList<String>();
		double sumAllSize = 0d;
		double sumUESize = 0d;
		double sumRatio = 0d;
		int fileCount = 0;
		for(File ue : new File(ues).listFiles()) {
			String identifier = ue.getName().split("-")[0];
			if(identifiers.contains(identifier))
				continue;
			identifiers.add(identifier);
			/*
			 * Only since 2015
			 */
			//map.put("2015", 0d); //well,we need to add data for 2015.
			map.put("2014", 0d);
			map.put("2015", 0d);
			map.put("2016", 0d);
			map.put("2017", 0d);
			map.put("2018", 0d);
			map.put("2019", 0d);
			map.put("2020", 0d);
			if(runitonce) {
				StringBuilder sb0 = new StringBuilder();
				for(Iterator<String> iter0 = map.keySet().iterator(); iter0.hasNext();) {
					sb0.append(","+iter0.next());
				}
				records.add(sb0.toString().substring(1));
				runitonce = false;
			}
			boolean incomplete = false;
			for(Iterator<String> iter1 = map.keySet().iterator();iter1.hasNext();){
				String year = iter1.next();
				String yearFile = identifier+"-"+year+".apk.txt";
				if(!new File(ues+yearFile).exists()) {
					incomplete = true;
					break;
				}
				double all_size = (double) read(alls+yearFile).size();
				double ue_size = (double) read(ues+yearFile).size();
				double ratio = ue_size / all_size;
				if(year.equals("2020")){
					sumUESize += ue_size;
					sumAllSize += all_size;
					sumRatio += ue_size/all_size;
					fileCount ++;
				}
				map.put(year, ratio);
			}
			if(incomplete)
				continue;
			StringBuilder sb = new StringBuilder();
			Iterator<String> iter = map.keySet().iterator();
			sb.append(map.get(iter.next()));
			while(iter.hasNext()) {
				sb.append(",").append(map.get(iter.next()));
			}
			records.add(sb.toString());
		}
		System.out.println("Average UE-API used per app: "+sumUESize/fileCount);
		System.out.println("Average API used per app: "+sumAllSize / fileCount);
		System.out.println("Average ratio per app: "+sumRatio / fileCount);
		write("paper/yearboxplot.csv", records);
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
