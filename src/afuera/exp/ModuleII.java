package afuera.exp;

import java.io.IOException;

import afuera.core.TaintProcessStackTrace;
import afuera.flow.analysis.FlowAnalysis;
import afuera.flow.config.FileConfig;
import afuera.instrumentation.JarInstrumenter;

public class ModuleII {

	public static void main(String args[]) throws IOException {
		CountStackTraces cst = CountStackTraces.v();
		TaintProcessStackTrace taintProcessStackTrace = new TaintProcessStackTrace();
		cst.run(args, taintProcessStackTrace);
		int size = taintProcessStackTrace.list.size();
		System.out.println("Total (API,Signaler,Throw) tuple, where each API has at least one parameter, and there is condition guarding throw: "+size);
		//Found 15072 such tuple. Given Confidence Level 0.95, Given Confidence Interval Size 10%, 
		//we need to examine 96 cases.
		int startSmall = 0;
		for(JarInstrumenter jarInstrumenter: taintProcessStackTrace.list) {
			/**
			 * TODO: For all such lists, we want only the 100 (UE-API,UE) pairs sampled
			 * in 			FileConfig.SAMPLED_API_EXCEPTION, first develop a matching , then dump all information.
			 * We also want to using multithreading to accelerate the process. The bottleneck is not really the CPU,
			 * it is the memory that limits us. For each thread, a considerable 32 GB ram is required at least.
			 */

			startSmall+=1;
			if(startSmall>96)
				break;
			new FlowAnalysis().run(jarInstrumenter,FileConfig.FRAMEWORK_JAR);
		}
	}
}
