package afuera.exp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import afuera.core.ProcessStackTrace;
import afuera.core.Signaler;
import afuera.core.StackFrame;
import afuera.exp.CountSignalers.ThrowCount;
import afuera.flow.config.FileConfig;
import afuera.preprocess.apiparsing.Parse;
import afuera.preprocess.apiparsing.SelfAPIList;
import soot.AmbiguousMethodException;
import soot.Body;
import soot.G;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Trap;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph.ExceptionDest;
import soot.util.Chain;

public class CountStackTraces {
	public CallGraph cg = null;
	public List<SootMethod> entryPoints;
	public List<List<StackFrame>> validStackTraces = new ArrayList<List<StackFrame>>();
	public Set<SootMethod> handlerMethods = new HashSet<SootMethod>();
	public Set<StackFrame> touchedStackFrame = new HashSet<StackFrame>();
	public int handledCount = 0;
	public static void main(String[] args) throws IOException {
		CountStackTraces.v().run(args, new ProcessStackTrace());
	}
	
	public static CountStackTraces v() {
		return new CountStackTraces();
	}
	
	public void run(String args[], ProcessStackTrace processStackTrace) throws IOException {
		G.reset();
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true); Options.v().setPhaseOption("cg.spark","enabled:true");
		List<String> processPaths = new ArrayList<String>();		
		Options.v().set_src_prec(Options.src_prec_class);
		processPaths.add(FileConfig.FRAMEWORK_JAR);
		Options.v().set_process_dir(processPaths);
		Options.v().set_output_format(Options.output_format_none);
	    Options.v().set_validate(true); // Validate Jimple bodies in each transofrmation pack
		Scene.v().loadNecessaryClasses();
		SelfAPIList apiList = new SelfAPIList("res/GeneratedAPIList.txt");
		this.entryPoints = apiList.getEntryPoints();
		this.writedownAPIs(this.entryPoints, FileConfig.ALL_API);
		Scene.v().setEntryPoints(entryPoints);
		List<Signaler> signalers = new ArrayList<Signaler>();
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {

			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				cg = Scene.v().getCallGraph();
				CountSignalers.getSignalers(signalers);
				for(Signaler signaler : signalers) {
					for(ThrowStmt throwStmt : signaler.throwStmts) {
						StackFrame signalerStackFrame = new StackFrame(signaler.sm, throwStmt);
						List<StackFrame> path = new ArrayList<StackFrame>();
						path.add(signalerStackFrame);
						analyzeStackTrace(path);
					}
				}
				processStackTrace.process(validStackTraces);
			}	
		}));
		PackManager.v().runPacks();
		/*Stats*/
		System.out.println("Total number of unique (API, Signaler, ThrowClause) tuple : "+this.validStackTraces.size());
		System.out.println("Total number of affected APIs: "+ this.listOfAffectedAPIs().size());
		System.out.println("Total number of affected APIs with non-zero arguments: "+ this.listOfAffectedAPIsNonZeroArguments().size());
		List<PackageCount> listOfPackageCount = this.listOfPackageCount();
		System.out.println("Total number of affected Packages: "+ listOfPackageCount.size());
		System.out.println("Framework methods that at least handled one exception: "+this.handlerMethods.size());
		System.out.println("Total times an unchecked exception is trapped: "+this.handledCount);
		rankAffectedAPIsByPackage(listOfPackageCount);
		rankAffectedAPIsByException(this.listOfExceptionCount());
		documentAPIWithException();
		documentAPIWithPackage();
		this.writedownAPIs(this.listOfAffectedAPIs(),FileConfig.UE_API);
		
	}
	
	private boolean hasDuplicate(List<StackFrame> path) {
		Set<SootMethod> dup = new HashSet<SootMethod>();
		for(StackFrame sf : path) {
			if(path.size()>10)
				System.out.println(sf.stackFrameMethod.getSignature());
			dup.add(sf.stackFrameMethod);
		}
		if(path.size()>6)
			System.out.println();
		return dup.size() < path.size();
	}
	
	private boolean hasDuplicate(StackFrame sf) {
		for(StackFrame cur : this.touchedStackFrame) {
			if(cur.stackFrameMethod.equals(sf.stackFrameMethod) && cur.throwStmt.equals(sf.throwStmt)) {
				return true;
			}
		}
		return false;
	}
	public void analyzeStackTrace(List<StackFrame> path) { 
//		if(hasDuplicate(path)) {
//			return;
//		}
		StackFrame caller = path.get(path.size()-1);
//		if(hasDuplicate(caller)) {
		if(hasDuplicate(path)) {
			return;
		}/*else {
			this.touchedStackFrame.add(caller);
		}*/
		if(caller.handledPassedException()) {
			//this.handledCount+=caller.handledCount;
			this.handledCount += (caller.targetUnits.size()-caller.unHandledUnits.size());
			handlerMethods.add(caller.stackFrameMethod);
			return;
		}
		if(this.isAPI(caller)) {
			this.validStackTraces.add(path);
		}
		/**
		 * If (caller,exception) is documented, no need to continue;
		 */
		if(path.size()>4) {
			return;
		}
		Iterator<MethodOrMethodContext> sources = new Sources(cg.edgesInto((caller.stackFrameMethod))); 
//		boolean debug = false;
//		if(caller.stackFrameMethod.getSignature().equals("<android.app.Instrumentation: android.app.Instrumentation$ActivityResult execStartActivity(android.content.Context,android.os.IBinder,android.os.IBinder,android.app.Activity,android.content.Intent,int,android.os.Bundle)>"))
//			debug = true;
		while (sources.hasNext()) {
			SootMethod src = (SootMethod) sources.next();
//			if(debug)
//				System.out.println(src.getSignature());
			StackFrame stackFrame = new StackFrame(src, caller);
//			if(stackFrame.handledPassedException()) {
//				continue;
//			}
			List<StackFrame> babyPath = new ArrayList<StackFrame>(path);
			babyPath.add(stackFrame);
			this.analyzeStackTrace(babyPath);
		}
	}
	
	public boolean isAPI(StackFrame caller) {
		return entryPoints.contains(caller.stackFrameMethod);
	}
	
	public List<PackageCount> listOfPackageCount() {
		List<PackageCount> list = new ArrayList<PackageCount>();
		for(SootMethod api : this.listOfAffectedAPIs()) {
			boolean contains = false;
			PackageCount pcCur = new PackageCount(api);
			for(PackageCount pc : list) {
				if(pc.packageName.equals(pcCur.packageName)) {
					pc.addCount();
					contains = true;
					break;
				}
			}
			if(!contains)
				list.add(pcCur);
		}
		return list;
	}
	
	public void rankAffectedAPIsByPackage(List<PackageCount> list) throws IOException {
		Collections.sort(list, new Comparator<PackageCount>() {
			@Override
			public int compare(PackageCount o1, PackageCount o2) {
				return Integer.compare(o2.count, o1.count);
			}
		});
		int others = 0;
		BufferedWriter bw = new BufferedWriter(new FileWriter(FileConfig.STAT_API_PER_PACKAGE));
		bw.write("type,count,per");
		bw.newLine();
		double total = 0d;
		for(PackageCount packageCount : list) {
			total+=packageCount.count;
		}
		DecimalFormat df = new DecimalFormat("#%");
		for(PackageCount packageCount : list) {
			if(packageCount.count > 100) {
				System.out.println(packageCount.packageName+" : "+packageCount.count);
				double per = packageCount.count / total;
				bw.write(CountSignalers.removeAndroidDot(packageCount.packageName)+","+packageCount.count+","+df.format(per));
				bw.newLine();
			}else {
				others += packageCount.count;
			}
		}
		System.out.println("Others : "+others);
		bw.write("others,"+others);
		bw.close();
	}
	
	public List<SootMethod> listOfAffectedAPIs() {
		List<SootMethod> list = new ArrayList<SootMethod>();
		for(List<StackFrame> st : this.validStackTraces) {
			if(list.contains(st.get(st.size()-1).stackFrameMethod)) {
				continue;
			}else {
				list.add(st.get(st.size()-1).stackFrameMethod);
			}
		}
		return list;
	}
	
	public List<SootMethod> listOfAffectedAPIsNonZeroArguments() {
		List<SootMethod> list = new ArrayList<SootMethod>();
		for(List<StackFrame> st : this.validStackTraces) {
			if(list.contains(st.get(st.size()-1).stackFrameMethod)) {
				continue;
			}else if(st.get(st.size()-1).stackFrameMethod.getParameterCount() == 0){
				continue;
			}else {
				list.add(st.get(st.size()-1).stackFrameMethod);
			}
		}
		return list;
	}
	
	public void writedownAPIs(List<SootMethod> list, String filePath) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filePath)));
		for(SootMethod sm : list) {
			bw.write(sm.getSignature());
			bw.newLine();
		}
		bw.close();
	}
	public static class API{
		public SootClass thrownException;
		public SootMethod api;
		public API(SootClass thrownException, SootMethod api) {
			this.thrownException = thrownException;
			this.api = api;
		}
		public boolean duplicates(API comp) {
			return this.thrownException.equals(comp.thrownException) && this.api.equals(comp.api);
		}
	}
	public List<API> listOfAffectedAPIWithException(){
		List<API> list = new ArrayList<API>();
		for(List<StackFrame> st : this.validStackTraces) {
			boolean contains = false;
			API cur = new API(st.get(st.size()-1).thrownException,st.get(st.size()-1).stackFrameMethod);
			for(API api : list) {
				if(api.duplicates(cur)) {
					contains = true;
					break;
				}
			}
			if(!contains) {
				list.add(cur);
			}
		}
		return list;
	}
	/**
	 * TODO: Sort API by same api signature and number of unique exceptions.
	 * @return 
	 */
	public List<API> sortByExceptions(List<API> unSorted){
		
		return null;
	}
	
	public void documentAPIWithException() throws IOException {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(FileConfig.DOC_API_EXCEPTION)));
			try {
				for(API api : this.listOfAffectedAPIWithException()) {
					bw.write(api.api.getSignature()+"-"+api.thrownException.getName());
					bw.newLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bw.close();
		}
	 /**
	  * Contqains redundance, will be removed when use.
	 * @throws IOException 
	  */
	public void documentAPIWithPackage() throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(FileConfig.DOC_API_PACKAGE)));
			try {
				for(API api : this.listOfAffectedAPIWithException()) {
					bw.write(api.api.getSignature()+"-"+api.api.getDeclaringClass().getPackageName());
					bw.newLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		bw.close();
	}
	public List<ExceptionCount> listOfExceptionCount() {
		List<ExceptionCount> list = new ArrayList<ExceptionCount>();
		for(API api : this.listOfAffectedAPIWithException()) {
			boolean contains = false;
			for(ExceptionCount pc : list) {
				if(pc.thrownException.equals(api.thrownException)) {
					pc.addCount();
					contains = true;
					break;
				}else {
					//
				}
			}
			if(!contains)
				list.add(new ExceptionCount(api.thrownException));
		}
		return list;
	}
	
	public void rankAffectedAPIsByException(List<ExceptionCount> list) throws IOException {
		Collections.sort(list, new Comparator<ExceptionCount>() {
			@Override
			public int compare(ExceptionCount o1, ExceptionCount o2) {
				return Integer.compare(o2.count, o1.count);
			}
		});
		int others = 0;
		BufferedWriter bw = new BufferedWriter(new FileWriter(FileConfig.STAT_API_PER_EXCEPTION));
		bw.write("type,count,per");
		bw.newLine();
		double total = 0d;
		for(ExceptionCount exceptionCount : list) {
			total+=exceptionCount.count;
		}
		DecimalFormat df = new DecimalFormat("#%");
		for(ExceptionCount exceptionCount : list) {
			if(exceptionCount.count > 0) {
				String[] packageNames = exceptionCount.thrownException.getName().split("\\.");
				String className = packageNames[packageNames.length-1];
				double per = exceptionCount.count / 5524d;
				if(className.contains("Error")) {
					bw.write(className+","+exceptionCount.count+","+df.format(per));
				}else {
					bw.write(className.substring(0,className.length()-9)+","+exceptionCount.count+","+df.format(per));
				}
				bw.newLine();
			}else {
				others += exceptionCount.count;
			}
		}
		//System.out.println("Others : "+others);
		bw.write("others,"+others);
		bw.close();
	}
	
	public static class ExceptionCount{
		public SootClass thrownException;
		public int count = 0;
		public ExceptionCount(SootClass thrownException) {
			this.thrownException = thrownException;
			this.addCount();
		}
		public void addCount() {
			this.count++;
		}
	}
	
	public static class PackageCount{
		public String packageName;
		public int count = 0;
		public PackageCount(SootMethod api) {
			this.getPackageName(api.getDeclaringClass());
			this.addCount();
		}
		public void addCount() {
			this.count++;
		}
		public String getPackageName(SootClass sc) {
			String classFullName = sc.getName();
			String[] classFullNameArray = classFullName.split("\\.");
			this.packageName = classFullName.substring(0,classFullName.length() - (classFullNameArray[classFullNameArray.length-1].length() + 1));
//			System.out.println("ClassFullName: "+sc.getName());
//			System.out.println("PackageName: "+this.packageName);
			return this.packageName;
		}
	}
}
