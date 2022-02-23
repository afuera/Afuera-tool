package afuera.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import afuera.instrumentation.JarInstrumenter;
import soot.ValueBox;

public class TaintProcessStackTrace extends ProcessStackTrace {
	public List<JarInstrumenter> list = new ArrayList<JarInstrumenter>();
	
	@Override
	public void process(List<List<StackFrame>> validStackTraces) {
		super.process(validStackTraces);
//		int startSmall = 0;
		//for(List<StackFrame> st : this.purge(validStackTraces)) {
			/**
			 * Since major revision, we don't purge zero-parameterd APIs anymore, per request from reviewer.
			 */
		for(List<StackFrame> st : validStackTraces) {
//			startSmall++;
//			if(startSmall > 200)
//				break;
			String apiSignature = st.get(st.size()-1).stackFrameMethod.getSignature();
			List<ValueBox> valueBoxes;
			String signalerSignature;
			int signalerPatchingChainPosition;
			/*
			 * The reason below is that we witnessed many example of below Signalers:
			 *  public int getConnectionState(BluetoothDevice device) {
        	 *		throw new UnsupportedOperationException("Use BluetoothManager#getConnectionState instead.");
    		 *	}
    		 * in <android.bluetooth.BluetoothGattServer: int getConnectionState(android.bluetooth.BluetoothDevice)>
			 */
			int i = 0;
			do {
				valueBoxes = new Signaler(st.get(i).stackFrameMethod).getValueBoxesPrecedingThrowClause(st.get(i).unHandledUnits.get(0));
				i++;
			}while(valueBoxes == null && i < st.size() );
			
			if(valueBoxes != null) {
				signalerSignature = st.get(i-1).stackFrameMethod.getSignature();
				signalerPatchingChainPosition = st.get(i-1).getUnitGraphPositionByThrowStmt();
			}else {
				continue;
			}
			
//			//DEBUG:
//			if(!(apiSignature.equals("<android.net.http.AndroidHttpClient: void enableCurlLogging(java.lang.String,int)>")
//					&& signalerSignature.equals("<android.net.http.AndroidHttpClient: void enableCurlLogging(java.lang.String,int)>")))
//				continue;
//			//DEBUG:
			
//			//DEBUG:
//			if(/*!(apiSignature.equals("<android.app.Activity: boolean startActivityIfNeeded(android.content.Intent,int,android.os.Bundle)>")
//				&& */!signalerSignature.equals("<android.app.Instrumentation: void checkStartActivityResult(int,java.lang.Object)>")) {
//				continue;
//			}else {
//				System.out.println(apiSignature);
//			}
//			//DEBUG:

			//TODO: same api and same signaler and same throwstmt would mean one FlowAnalysis, to eliminate duplicates.
			JarInstrumenter jarInstrumenter = new JarInstrumenter(apiSignature, signalerSignature, signalerPatchingChainPosition,st.get(0).thrownException.getName());
			/**
			 * We are adding stack trace to jarInstrumenter, so that we can document them in final json file.
			 */
			jarInstrumenter.stackTrace = st;
			jarInstrumenter.updateJimpleForICC(true);
			this.list.add(jarInstrumenter);
		}
	}
	
	/**
	 * 
	 * @param validStackTraces
	 * @return purged valid stack traces where API has zero parameters, and shuffled
	 */
	public List<List<StackFrame>> purge(List<List<StackFrame>> validStackTraces) {
		List<List<StackFrame>> purgedValidStackTraces = new ArrayList<List<StackFrame>>();
		for(List<StackFrame> st : validStackTraces) {
			if(st.get(st.size()-1).stackFrameMethod.getParameterCount() == 0)
				continue;
//			boolean duplicate = false;
//			for(List<StackFrame> stCur : purgedValidStackTraces) {
//				if(st.get(0).stackFrameMethod.getSignature().equals(stCur.get(0).stackFrameMethod.getSignature())
//						&&
//					st.get(st.size()-1).stackFrameMethod.getSignature().equals(stCur.get(stCur.size()-1).stackFrameMethod.getSignature())) {
//					duplicate = true;
//					break;
//				}
//			}
//			if(!duplicate)
				purgedValidStackTraces.add(st);
		}
		Collections.shuffle(purgedValidStackTraces);
		return purgedValidStackTraces;
	}
}
