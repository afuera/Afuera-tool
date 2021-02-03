package afuera.core;

import java.util.ArrayList;
import java.util.List;

public class ProcessStackTrace {
	public void process(List<List<StackFrame>> validStackTraces) {
		List<List<StackFrame>> uniqueStackTraces = new ArrayList<List<StackFrame>>();
		for(List<StackFrame> st : validStackTraces) {
			boolean contains = false;
			for(List<StackFrame> cur : uniqueStackTraces) {
				if(cur.get(0).stackFrameMethod.getSignature().equals(st.get(0).stackFrameMethod.getSignature())
						&&
						cur.get(cur.size()-1).stackFrameMethod.getSignature().equals(st.get(st.size()-1).stackFrameMethod.getSignature())
						&&
						cur.get(0).throwStmt.equals(st.get(0).throwStmt)) {
					contains = true;
					break;
				}
					
			}
			if(contains) {
				//
			}else {
				uniqueStackTraces.add(st);
			}
		}
		validStackTraces = uniqueStackTraces;
	}
}
