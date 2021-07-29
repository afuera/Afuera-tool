package afuera.exp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import afuera.core.Signaler;
import afuera.core.StackFrame;
import afuera.core.ThrowClause;
import afuera.flow.config.FileConfig;
import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.UnitPatchingChain;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;

public class InApp {
	public static void main(String args[]) throws IOException {
		//for all apps
		//how many apps have invoked such apis, 
		//On average, how many invocations for APIs with potential unchecked exception?
		//how many apps have 
		//among such invocations, how many have been handled? how many not?
		
		/*
		 * 1. To prove the prevelance of using such APIs in the apps. 
		 * 
		 */
		List<String> all_apis, ue_apis;//= new ArrayList<String>();
		all_apis = read(FileConfig.ALL_API);
		ue_apis =  read(FileConfig.UE_API);
		int small = 0;
		for(File app : new File("script/apps/").listFiles()) {
			if(!app.getName().endsWith("apk")) {
				continue;
			}
//			if(small > 1) {
//				break;
//			}
			try {
				System.out.println("Processing "+app.getName());
				double ratio = analyze(app, all_apis, ue_apis);
				System.out.println(app.getName() + " ratio: "+ratio);
			}catch(Exception e) {
				e.printStackTrace();
			}
			small++;
		}
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
	
	public static double analyze(File app, List<String> all_apis, List<String> ue_apis) {
		double ratio = 0d;
		List<String> ue_used = new ArrayList<String>();
		List<String> all_used= new ArrayList<String>();
		G.reset();
//		List<String> processPaths = new ArrayList<String>();		
//		processPaths.add(app.getAbsolutePath());
//		Options.v().set_process_dir(processPaths);
//		Options.v().set_android_jars(FileConfig.MAC_ANDROID_PLATFORM);
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);;	
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_output_format(Options.output_format_none);
	    //Options.v().set_validate(true); // Validate Jimple bodies in each transofrmation pack
		Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);
		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

			@Override
			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
				CallGraph cg = Scene.v().getCallGraph();
				final PatchingChain<Unit> units = b.getUnits();		
				//important to use snapshotIterator here
				for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
					final Unit u = iter.next();
					u.apply(new AbstractStmtSwitch() {

						public void caseInvokeStmt(InvokeStmt stmt) {
							if(stmt.containsInvokeExpr()) {
								InvokeExpr invkExpr = stmt.getInvokeExpr();
								String methodSig = invkExpr.getMethod().getSignature();
								if(all_apis.contains(methodSig)) {
									all_used.add(methodSig);
									//System.out.println(invkExpr.getMethod().getSignature());
									//TODO: Is this Body b invoked by a Component callback, or itself is one?
									//analyzeStackTrace(new ArrayList<StackFrame>(), cg);
									//TODO: Is this Body b handled before invoked by a Component callback, or it handled this api by itself?
								}
								if(ue_apis.contains(methodSig)) {
									ue_used.add(methodSig);
								}
							}
						}

					});
				}
			}
		}));
		System.out.println(app.getAbsolutePath());
		String args[] = {"-android-jars", FileConfig.SERVER_ANDROID_PLATFORM,
						"-process-dir", app.getAbsolutePath()};
		soot.Main.main(args);
//		PackManager.v().runPacks();
		ratio = (double) ue_used.size() / all_used.size() ;
		return ratio;
	}
	
	//TODO: not yet down
	public static void analyzeStackTrace(List<StackFrame> path, CallGraph cg) { 
		StackFrame caller = path.get(path.size()-1);
		if(isComponentCallback(caller)) {
			//TODO
		}
		if(path.size()>5) {
			return;
		}
		Iterator<MethodOrMethodContext> sources = new Sources(cg.edgesInto((caller.stackFrameMethod))); 
		while (sources.hasNext()) {
			SootMethod src = (SootMethod) sources.next();
			StackFrame stackFrame = new StackFrame(src, caller);
			//TODO: Here not modified yet.
			if(stackFrame.handledPassedException()) {
				continue;
			}
			List<StackFrame> babyPath = new ArrayList<StackFrame>(path);
			babyPath.add(stackFrame);
			analyzeStackTrace(babyPath, cg);
		}
	}
	
	public static boolean isComponentCallback(StackFrame caller) {
		//TODO
		return false;
	}
	
	public String componentType(StackFrame caller) {
		//TODO: pick one from the below
		return "Activity, BroadcastReceiver, Service, ContentProvider";
	}
}
