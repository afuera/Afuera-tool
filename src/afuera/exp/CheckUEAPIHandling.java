package afuera.exp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import afuera.core.ThrowClause;
import afuera.flow.config.FileConfig;
import afuera.preprocess.apiparsing.SelfAPIList;
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
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.util.Chain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import soot.Body;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ThrowStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph.ExceptionDest;
import soot.jimple.toolkits.callgraph.Sources;

public class CheckUEAPIHandling {
	public static String path_android_platform = FileConfig.SERVER_ANDROID_PLATFORM;
	public static void main(String args[]) throws IOException {
		// List<String> all_apis, ue_apis;//= new ArrayList<String>();
		// all_apis = read(FileConfig.ALL_API);
		// ue_apis =  read(FileConfig.UE_API);
		Map<String,List<String>> ue_apis_exception = readToMap(FileConfig.DOC_API_EXCEPTION);
		new CheckUEAPIHandling().analyze(new File(args[0]), ue_apis_exception);
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
	public static Map<String,List<String>> readToMap(String filePath) throws IOException{
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
		String line = null;
		while((line = br.readLine())!=null) {
			String[] sp = line.split("-");
			List<String> list = map.getOrDefault(sp[0],new ArrayList<String>());
			list.add(sp[1]);
			map.put(sp[0],list);
		}
		br.close();
		return map;
	}
	public CallGraph cg = null;
	public double analyze(File app, Map<String,List<String>> ue_apis_exception) {
		double ratio = 0d;
		List<String> ue_used = new ArrayList<String>();
		//List<String> all_used= new ArrayList<String>();
        List<String> ue_unhandled = new ArrayList<String>();
		G.reset();
		Options.v().set_whole_program(true);Options.v().setPhaseOption("cg.spark","enabled:true");
		Options.v().set_allow_phantom_refs(true);;
		List<String> processPaths = new ArrayList<String>();		
		Options.v().set_src_prec(Options.src_prec_apk);
		//Options.v().set_android_jars(FileConfig.SERVER_ANDROID_PLATFORM);
		Options.v().set_android_jars(path_android_platform);
		Options.v().set_android_api_version(19);
		processPaths.add(app.getAbsolutePath());
		Options.v().set_process_dir(processPaths);
		Options.v().set_output_format(Options.output_format_none);
	    //Options.v().set_validate(true); // Validate Jimple bodies in each transofrmation pack
		Scene.v().loadNecessaryClasses();
//		SelfAPIList apiList = new SelfAPIList("res/GeneratedAPIList.txt");
//		List<SootMethod> entryPoints = apiList.getEntryPoints();
//		Scene.v().setEntryPoints(entryPoints);
//		List<Signaler> signalers = new ArrayList<Signaler>();
		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

			@Override
			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
				cg = Scene.v().getCallGraph();
				final PatchingChain<Unit> units = b.getUnits();		
				//important to use snapshotIterator here
				for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
					final Unit u = iter.next();
					u.apply(new AbstractStmtSwitch() {

						public void caseInvokeStmt(InvokeStmt stmt) {
							if(stmt.containsInvokeExpr()) {
								InvokeExpr invkExpr = stmt.getInvokeExpr();
								SootMethod sootMethod = invkExpr.getMethod();
								String methodSig = sootMethod.getSignature();
								// if(all_apis.contains(methodSig)) {
								// 	all_used.add(methodSig);
								// 	//System.out.println(invkExpr.getMethod().getSignature());
								// 	//TODO: Is this Body b invoked by a Component callback, or itself is one?
								// 	//analyzeStackTrace(new ArrayList<StackFrame>(), cg);
								// 	//TODO: Is this Body b handled before invoked by a Component callback, or it handled this api by itself?
								// }
								if(ue_apis_exception.containsKey(methodSig)) {
									ue_used.add(methodSig);
                                    /**
                                     * TODO: Check Handled.
                                     */
									for(String thrownException : ue_apis_exception.get(methodSig)){
										SootClass st = Scene.v().getSootClass(thrownException);
										List<StackFrameHandle> path = new ArrayList<StackFrameHandle>();
										path.add(new StackFrameHandle(b.getMethod(),stmt,st));
										if(hasBeenHandled(path)){
											ue_unhandled.add(methodSig);
											//break;
										}
									}
								}
							}
						}

					});
				}
			}
		}));
		PackManager.v().runPacks();
		G.reset();
		try {
			write(FileConfig.HANDLE_USAGES+app.getName()+".txt",ue_unhandled);
			//write(FileConfig.ALL_USAGEs+app.getName()+".txt",all_used);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ratio = (double) ue_unhandled.size() / (double) ue_used.size();
		System.out.println("Ratio of Unhandled/UE = " + ratio);// / all_used.size() ;
		return ratio;
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
	public Set<SootMethod> handlerMethods = new HashSet<SootMethod>();
	public boolean isCallBack(StackFrameHandle caller){
		//return caller.stackFrameMethod.getDeclaringClass().isLibraryClass();
		return caller.stackFrameMethod.getName().startsWith("on");
	}
	public List<List<StackFrameHandle>> validStackTraces = new ArrayList<List<StackFrameHandle>>();
	/**
	 * 
	 * @param path
	 * @return true if UE_API usage can reach callback, false otherwise.
	 */
	public boolean hasBeenHandled(List<StackFrameHandle> path) { 
	//		if(hasDuplicate(path)) {
	//			return;
	//		}
			StackFrameHandle caller = path.get(path.size()-1);
	//		if(hasDuplicate(caller)) {
			// if(hasDuplicate(path)) {
			// 	return false;
			// }/*else {
			// 	this.touchedStackFrame.add(caller);
			// }*/
			System.out.println("Analyzing: "+caller.stackFrameMethod.getDeclaringClass().getName()+"."+caller.stackFrameMethod.getName());
			if(caller.handledPassedException()) {
				//this.handledCount+=caller.handledCount;
				// this.handledCount += (caller.targetUnits.size()-caller.unHandledUnits.size());
				// handlerMethods.add(caller.stackFrameMethod);
				return true;
			}
			if(this.isCallBack(caller)) {
				this.validStackTraces.add(path);
				return false;
			}
			/**
			 * If (caller,exception) is documented, no need to continue;
			 */
			if(path.size()>4) {
				return false;
			}
			Iterator<MethodOrMethodContext> sources = new Sources(cg.edgesInto((caller.stackFrameMethod))); 
	//		boolean debug = false;
	//		if(caller.stackFrameMethod.getSignature().equals("<android.app.Instrumentation: android.app.Instrumentation$ActivityResult execStartActivity(android.content.Context,android.os.IBinder,android.os.IBinder,android.app.Activity,android.content.Intent,int,android.os.Bundle)>"))
	//			debug = true;
			while (sources.hasNext()) {
				SootMethod src = (SootMethod) sources.next();
	//			if(debug)
	//				System.out.println(src.getSignature());
				StackFrameHandle stackFrame = new StackFrameHandle(src, caller);
	//			if(stackFrame.handledPassedException()) {
	//				continue;
	//			}
				List<StackFrameHandle> babyPath = new ArrayList<StackFrameHandle>(path);
				babyPath.add(stackFrame);
				if(this.hasBeenHandled(babyPath) == true){
					return true;
				}
			}
			return false;
		}
	private boolean hasDuplicate(List<StackFrameHandle> path) {
		Set<SootMethod> dup = new HashSet<SootMethod>();
		for(StackFrameHandle sf : path) {
			if(path.size()>10)
				System.out.println(sf.stackFrameMethod.getSignature());
			dup.add(sf.stackFrameMethod);
		}
		if(path.size()>6)
			System.out.println();
		return dup.size() < path.size();
	}
	class StackFrameHandle{
		public SootMethod stackFrameMethod = null;
		public List<Unit> targetUnits = new ArrayList<Unit>();
		public List<Unit> unHandledUnits = new ArrayList<Unit>();
		public boolean isSignaler = false;
		public SootClass thrownException = null;
		/**
		 * The very initial throw statement from the signaler.
		 */
		public int handledCount = 0;
		public StackFrameHandle(SootMethod stackFrameMethod, StackFrameHandle tar) {
			this.stackFrameMethod = stackFrameMethod;
			this.computeTargetUnits(tar);
			this.thrownException = tar.thrownException;

			this.computeUnHandledUnits();
		}
		public StackFrameHandle(SootMethod signalerMethod, InvokeStmt invkStmt, SootClass thrownException) {
			this.stackFrameMethod = signalerMethod;
			this.targetUnits.add(invkStmt);
			this.thrownException = thrownException;
			this.isSignaler = true;
			this.handledCount = this.computeUnHandledUnits();
		}
		
		public IfStmt getIfStmtPrecedingUnit(Unit u) {
			//TODO
			Body body = this.stackFrameMethod.retrieveActiveBody();
			ExceptionalUnitGraph ug = new ExceptionalUnitGraph(body);
			List<Unit> preds = ug.getUnexceptionalPredsOf(u);
			for(Unit pre: preds) {
				if(pre instanceof IfStmt) {
					return (IfStmt) pre;
				}
			}
			return null;
		}
		public void computeTargetUnits(StackFrameHandle tar) {
			Body body = stackFrameMethod.retrieveActiveBody();
			ExceptionalUnitGraph ug = new ExceptionalUnitGraph(body);
			Iterator<Unit> iter = ug.iterator();
//			iter = 			body.getUnits().iterator();
//			boolean debug = false;
//			if(tar.stackFrameMethod.getSignature().equals("<android.app.Instrumentation: android.app.Instrumentation$ActivityResult execStartActivity(android.content.Context,android.os.IBinder,android.os.IBinder,android.app.Activity,android.content.Intent,int,android.os.Bundle)>"))
//				debug = true;
//			if(debug)
//				System.out.println(body);
			while(iter.hasNext()) {
				Unit u = (Unit) iter.next();
				if(u instanceof InvokeStmt) {
					InvokeStmt invokeStmt = (InvokeStmt) u;
					if(invokeStmt.getInvokeExpr().getMethod().equals(tar.stackFrameMethod)) {
						this.targetUnits.add(invokeStmt);
					}
				}else if(u instanceof AssignStmt) {
					AssignStmt assignStmt = (AssignStmt) u;
					if(assignStmt.containsInvokeExpr()) {
						if(assignStmt.getInvokeExpr().getMethod().equals(tar.stackFrameMethod))
							this.targetUnits.add(u);
					}

				}
			}
		}
		
		/**
		 * 
		 * @return how many times handled turned into true;
		 */
		public int computeUnHandledUnits() {
			int count = 0;
			Body body = stackFrameMethod.retrieveActiveBody();
			ExceptionalUnitGraph ug = new ExceptionalUnitGraph(body);
			for(Unit unit : targetUnits) {
//				for(Unit succ : ug.getExceptionalSuccsOf(unit)){
//					
//				}
				boolean handled = false;
				for(ExceptionDest dest : ug.getExceptionDests(unit)) {
					Trap trap = dest.getTrap();
					if(trap!=null) {
//						System.out.println(body);
//						System.out.println(trap.getBeginUnit().toString());
//						System.out.println(trap.getEndUnit().toString());
//						System.out.println(trap.getHandlerUnit().toString());
						SootClass trapException = trap.getException();
						// System.out.println("The trapping exception:" + trapException.getName());
						// System.out.println("The thrown exception: "+ this.thrownException);
						if(ThrowClause.isSubclass(thrownException, trapException)) {
							//DEBUG
//							System.out.println("Trapping Exception: "+trapException.getName());
//							System.out.println("Throwing Exception: "+this.thrownException.getName());
//							System.out.println();
							//DEBUG
							handled = true;
							count++;
							break;//if handled, no need to check more.
						}
					}
				}
				if(!handled) {
					this.unHandledUnits.add(unit);
				}
			}
			return count;
		}
		public boolean handledPassedException() {
			return this.unHandledUnits.size()==0?true:false;
		}
	}
}
