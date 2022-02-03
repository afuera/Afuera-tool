package afuera.flow.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import afuera.flow.config.FileConfig;
import afuera.flow.config.FlowConfig;
import afuera.instrumentation.JarInstrumenter;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.jimple.infoflow.IInfoflow;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.internal.JInvokeStmt;
import soot.util.MultiMap;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;

public class FlowAnalysis {
	public JarInstrumenter jarInstrumenter = null;
	public int sampledID = -1;
	public void run(JarInstrumenter jarInstrumenter, String appPath) {
		this.jarInstrumenter = jarInstrumenter;
		IInfoflow infoflow = null;
		List<String> epoints = new ArrayList<String>();
		String libPath = Scene.v().getSootClassPath();
		infoflow = initInfoflow(appPath);
		infoflow.setIPCManager(jarInstrumenter);
		epoints.add(jarInstrumenter.apiSignature);
		infoflow.computeInfoflow(appPath, libPath, epoints, jarInstrumenter.dummyParaMethods,jarInstrumenter.dummyIfConditionMethod);
		checkInfoflow(infoflow);
	}
	
	protected void checkInfoflow(IInfoflow infoflow){
		if(infoflow.isResultAvailable()) {
			InfoflowResults results = infoflow.getResults();
			//System.out.println("Taint analysis results");
			//results.printResults();
			MultiMap<ResultSinkInfo, ResultSourceInfo> res = results.getResults();
			try {
				String fileName = "";
				if(this.sampledID == -1){
					fileName = FileConfig.PARAMETER_THROW_OUTCOMES + this.jarInstrumenter.smAPI.getDeclaringClass()+"."+this.jarInstrumenter.smAPI.getName();
				}else{
					fileName = FileConfig.MODULE_II_SAMPLED_ANALYSIS_OUTCOME + this.sampledID;
				}
				if(new File(fileName).exists()){
					return;
				}
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)));				
				bw.write(this.jarInstrumenter.apiSignature);
				bw.newLine();
				bw.write(this.jarInstrumenter.signalerSignature);
				bw.newLine();
				bw.write(this.jarInstrumenter.thrownExceptionName);
				bw.newLine();
				if(res!=null) {
					for(ResultSinkInfo sink: res.keySet()) {
						JInvokeStmt sinkStmt = (JInvokeStmt) sink.getStmt();
						bw.write("Sink: "+sinkStmt.getInvokeExpr().getMethod().getName());
						bw.newLine();
						for(ResultSourceInfo source : res.get(sink)){
							AssignStmt sourceStmt = (AssignStmt) source.getStmt();
							bw.write("Source: "+sourceStmt.getInvokeExpr().getMethod().getName());
							bw.newLine();
						}
					}
				}
				bw.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
			
			
		}
	}
	
	public IInfoflow initInfoflow(String appPath) {
		Infoflow result = new Infoflow("", false, null);
		FlowConfig fc = new FlowConfig();
		result.setSootConfig(fc);
		return result;
	}
}
