package afuera.exp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import afuera.core.TaintProcessStackTrace;
import afuera.flow.analysis.FlowAnalysis;
import afuera.flow.config.FileConfig;
import afuera.instrumentation.JarInstrumenter;
import java.util.HashMap;

public class ModuleII {
	public static void main(String args[]) throws IOException {
		CountStackTraces cst = CountStackTraces.v();
		TaintProcessStackTrace taintProcessStackTrace = new TaintProcessStackTrace();
		cst.run(args, taintProcessStackTrace);
		int size = taintProcessStackTrace.list.size();
		System.out.println("Total (API,Signaler,Throw) tuple, and there is condition guarding throw: "+size);
		//Found 15072 such tuple. Given Confidence Level 0.95, Given Confidence Interval Size 10%, 
		//we need to examine 96 cases.
		int startSmall = 0;
		Map<String,Integer> sampledAPIExceptionMap = read(FileConfig.SAMPLED_API_EXCEPTION);
		for(JarInstrumenter jarInstrumenter: taintProcessStackTrace.list) {
			/**
			 * TODO: For all such lists, we want only the 100 (UE-API,UE) pairs sampled
			 * in 			FileConfig.SAMPLED_API_EXCEPTION, first develop a matching , then dump all information.
			 * We also want to using multithreading to accelerate the process. The bottleneck is not really the CPU,
			 * it is the memory that limits us. For each thread, a considerable 32 GB ram is required at least.
			 */

			if(startSmall>0)
				break;
			int sampledID = checkIfSampled(jarInstrumenter, sampledAPIExceptionMap);
			if(-1 == sampledID || new File(FileConfig.MODULE_II_SAMPLED_ANALYSIS_OUTCOME+sampledID).exists()){
				continue;
			}
			startSmall+=1;
			FlowAnalysis fa = new FlowAnalysis();
			fa.sampledID = sampledID;
			fa.run(jarInstrumenter,FileConfig.FRAMEWORK_JAR);
		}
	}
	/**
	 * Check if the current jarInstrumenter is within our sampled (UE-API,UE) pair in
	 * file FileConfig.AMPLED_API_EXCEPTION
	 * @return the line number, put after one time, we put the line numebr at -1 and consider it analyzed.
	 * @throws IOException
	 */
	private static int checkIfSampled(JarInstrumenter jarInstrumenter, Map<String,Integer> sampledAPIExceptionMap) throws IOException{
		String jiSignature = jarInstrumenter.apiSignature + "-" + jarInstrumenter.thrownExceptionName;
		System.out.println(jiSignature);
		if(sampledAPIExceptionMap.containsKey(jiSignature)){
			int sampledID = sampledAPIExceptionMap.get(jiSignature);
			if(sampledID == -1){
				return -1;
			}
			sampledAPIExceptionMap.put(jiSignature ,-1);
			return sampledID;
		}else{
			return -1;
		}
	}

	public static Map<String,Integer> read(String filePath) throws IOException{
		Map<String,Integer> apis = new HashMap<String,Integer>();
		BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
		String line = null;
		int lineNum = 0;
		while((line = br.readLine())!=null) {
			lineNum += 1;
			String[] temp = line.split("\\$\\$\\$");
			String sampledSignature = temp[temp.length-1];
			apis.put(sampledSignature, lineNum);
		}
		br.close();
		return apis;
	}
}
