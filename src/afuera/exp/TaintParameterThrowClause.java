package afuera.exp;

import java.io.IOException;

import afuera.core.TaintProcessStackTrace;
import afuera.flow.analysis.FlowAnalysis;
import afuera.flow.config.FileConfig;
import afuera.instrumentation.JarInstrumenter;

public class TaintParameterThrowClause {

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
			startSmall+=1;
			if(startSmall>96)
				break;
			new FlowAnalysis().run(jarInstrumenter,FileConfig.FRAMEWORK_JAR);
		}
	}
}
