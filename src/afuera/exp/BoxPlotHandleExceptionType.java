package afuera.exp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import afuera.flow.config.FileConfig;

/**
 * I know, putting the order of the exception types in code is ugly. We should
 * really read the order from file "res/api[versionNO.]/apiPerexception.csv", 
 * instead of copy-pasting. What preventing me is we need to trim on the exception
 * name to fit the paper page size, which requires handling of different name.
 */
public class BoxPlotHandleExceptionType {
	public static void main(String args[]) throws IOException {
		//String ues = "res/RQ2/ue/";
		//String alls= "res/RQ2/all/";
		BoxPlotHandleExceptionType v = v();
		Map<String,List<Integer>> handle = v.count(FileConfig.HANDLE_USAGES);
		Map<String,List<Integer>> ue = v.count(FileConfig.UE_USAGEs);
		Map<String,List<Double>> map = new LinkedHashMap<>();
		int totalUE = total(ue);
		int totalHandle = total(handle);
		System.out.println("Percentage of handled UE-API over all UE-API usages: "+(double) totalHandle / (double) totalUE);
		// map.put("java.lang.IllegalArgumentException", new ArrayList<>());
		// map.put("java.lang.RuntimeException", new ArrayList<>());
		// map.put("java.lang.IllegalStateException", new ArrayList<>());
		// map.put("java.lang.NullPointerException", new ArrayList<>());
		// map.put("java.lang.UnsupportedOperationException", new ArrayList<>());
		// map.put("java.util.ConcurrentModificationException",new ArrayList<>());
		// //map.put("android.content.res.Resources$NotFoundException",0);
		// map.put("java.lang.ArrayIndexOutOfBoundsException", new ArrayList<>());
		// map.put("java.lang.IndexOutOfBoundsException", new ArrayList<>());
		// map.put("java.lang.AssertionError", new ArrayList<>());
		// map.put("java.lang.SecurityException", new ArrayList<>());
		// Iterator<String> iter = map.keySet().iterator();
		// for(; iter.hasNext(); ){
		// 	String key = iter.next();
		// 	List<Double> scoreList = map.get(key);
		// 	for(int i = 0; i < handle.get(key).size(); i++){
		// 		scoreList.add((double) handle.get(key).get(i) / (double) ue.get(key).get(i));
		// 	}
		// }
		// write(FileConfig.STAT_HANDLE_EXCEPTION_BOXPLOT,v.buildStringList(map));
	}
	public static BoxPlotHandleExceptionType v(){
		return new BoxPlotHandleExceptionType();
	}
	public static int total(Map<String,List<Integer>> map){
		int count = 0;
		for(Iterator<List<Integer>> iter = map.values().iterator(); iter.hasNext(); ){
			for(int c : iter.next()){
				count += c;
			}
		}
		return count;
	}
	Set<String> set = setBothFolderHave();
	public Map<String,List<Integer>> count(String folderPath) throws IOException{
		Map<String, List<Integer>> map = new LinkedHashMap<String, List<Integer>>();
		map.put("java.lang.IllegalArgumentException", new ArrayList<>());
		map.put("java.lang.RuntimeException", new ArrayList<>());
		map.put("java.lang.IllegalStateException", new ArrayList<>());
		map.put("java.lang.NullPointerException", new ArrayList<>());
		map.put("java.lang.UnsupportedOperationException", new ArrayList<>());
		map.put("java.util.ConcurrentModificationException",new ArrayList<>());
		//map.put("android.content.res.Resources$NotFoundException",0);
		map.put("java.lang.ArrayIndexOutOfBoundsException", new ArrayList<>());
		map.put("java.lang.IndexOutOfBoundsException", new ArrayList<>());
		map.put("java.lang.AssertionError", new ArrayList<>());
		map.put("java.lang.SecurityException", new ArrayList<>());
		//map.put("android.renderscript.RSInvalidStateException", 0);
		int startSmall = 0;
		for(File ue : new File(folderPath).listFiles()) {
			if(!this.set.contains(ue.getName())){
				continue;
			}
			if(ue.getName().endsWith("-2020.apk.txt")) {
				for(Iterator<String> iter = map.keySet().iterator(); iter.hasNext(); ){
					map.get(iter.next()).add(0);
				}
				startSmall++;
				if(startSmall > 200)
					break;
				List<String> ueList = read(ue.getAbsolutePath());
				List<String> ue_exceptionList = read(FileConfig.DOC_API_EXCEPTION);
				for(String ueMethod : ueList) {
					for(String ue_exception : ue_exceptionList) {
						if(ue_exception.length() < 2){
							continue;//last line is empty
						}
						String[] arr = ue_exception.split("-");
						if(ueMethod.equals(arr[0])) {
							if(map.containsKey(arr[1])){
								List<Integer> list = map.get(arr[1]);
								int cur = list.get(list.size()-1);
								list.set(list.size()-1, cur+1);
							}
						}
					}
				}
			}
		}
		return map;
	}
	public Set<String> setBothFolderHave(){
		Set<String> set = new HashSet<String>();
		for(File ue : new File(FileConfig.UE_USAGEs).listFiles()){
			String name = ue.getName();
			for(File handle : new File(FileConfig.HANDLE_USAGES).listFiles()){
				String handle_name = handle.getName();
				if(name.equals(handle_name)){
					set.add(name);
				}
			}
		}
		return set;
	}
	public List<String> buildStringList(Map<String,List<Double>> map) throws IOException{
		List<String> records = new ArrayList<>();
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
		int size = map.values().iterator().next().size();
		for(int i = 0; i < size ;i++){
			StringBuilder sb = new StringBuilder();
			for(Iterator<String> iter = map.keySet().iterator();iter.hasNext(); ){
				List<Double> list = map.get(iter.next());
				sb.append(",").append(list.get(i));
			}
			records.add(sb.toString().substring(1));
		}
		return records;
	}
	public static List<String> read(String filePath) throws IOException{
		List<String> apis = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
		String line = null;
		while((line = br.readLine())!=null) {
			apis.add(line);//.split(",")[0]);
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
