package afuera.exp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import afuera.core.FileConfig;
import afuera.core.Main;
import afuera.core.Signaler;
import afuera.core.ThrowClause;
import afuera.preprocess.apiparsing.Parse;
import afuera.preprocess.apiparsing.SelfAPIList;
import soot.AmbiguousMethodException;
import soot.G;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.ThrowStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.util.Chain;

public class CountSignalers {
	public static void main(String[] args) throws IOException {
		CountSignalers.v().run(args);
	}
	
	public static CountSignalers v() {
		return new CountSignalers();
	}
	
	public void run(String args[]) throws IOException {
		G.reset();
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);;
		List<String> processPaths = new ArrayList<String>();		
		Options.v().set_src_prec(Options.src_prec_class);
		processPaths.add(FileConfig.FRAMEWORK_JAR);
		Options.v().set_process_dir(processPaths);
		Options.v().set_output_format(Options.output_format_none);
	    Options.v().set_validate(true); // Validate Jimple bodies in each transofrmation pack
		Scene.v().loadNecessaryClasses();
		SelfAPIList apiList = new SelfAPIList("res/GeneratedAPIList.txt");
		List<SootMethod> entryPoints = apiList.getEntryPoints();
		Scene.v().setEntryPoints(entryPoints);
		List<Signaler> signalers = new ArrayList<Signaler>();
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {
			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				CountSignalers.getSignalers(signalers);
			}	
		}));
		PackManager.v().runPacks();
		/*Stats*/
		//Total number of Signalers
		System.out.println("Total Number of Signalers: "+ signalers.size());
		//Total number of ThrowClauses
		System.out.println("Total Number of ThrowClauses: "+ CountSignalers.totalThrowClauses(signalers));
		//Ranking RuntimeException throw clauses.
		printSignalerPerExceptionCounts(signalers);
		printSignalerPerPackageCounts(signalers);
	}
	
	public static List<Signaler> getSignalers(List<Signaler> signalers){
		Chain<SootClass> classes = Scene.v().getApplicationClasses();
		System.out.println("Total Framework Classes: "+classes.size());
		Iterator<SootClass> iter = classes.snapshotIterator();
		int totalMethods = 0;
		while(iter.hasNext()) {
			SootClass sc = iter.next();
			Iterator<SootMethod> iter0 = sc.methodIterator();
			while(iter0.hasNext()) {
				totalMethods += 1;
				SootMethod sm = iter0.next();
				if(sm.hasActiveBody()) {
					Signaler signaler = new Signaler(sm);
					if(signaler.isSignaler()) {
						signalers.add(signaler);
					}
				}
			}
		}
		System.out.println("Total Framework Methods: "+ totalMethods);
		return signalers;
	}
	
	public static int totalThrowClauses(List<Signaler> signalers) {
		int total = 0;
		for(Signaler sig : signalers) {
			total += sig.getThrowStmts().size();
		}
		return total;
	}
	public static void printSignalerPerExceptionCounts(List<Signaler> signalers) throws IOException {
		List<ThrowCount> list = new ArrayList<ThrowCount>();
		for(Signaler sig : signalers) {
			Set<SootClass> thrownTypes = new HashSet<SootClass>();
			for(ThrowStmt stmt : sig.getThrowStmts()) {
				SootClass sc = new ThrowClause(stmt).getThrownType();
				if(thrownTypes.contains(sc)) {
					continue;
				}else {
					thrownTypes.add(sc);
				}
				boolean hasType = false;
				for(ThrowCount throwCount: list) {
					if(throwCount.thrownType.getName().equals(sc.getName())) {
						throwCount.addCount();
						hasType = true;
						break;
					}
				}
				if(!hasType) {
					list.add(new ThrowCount(sc));
				}
			}
		}
		Collections.sort(list, new Comparator<ThrowCount>() {
			@Override
			public int compare(ThrowCount o1, ThrowCount o2) {
				return Integer.compare(o2.count, o1.count);
			}
			
		});
		int others = 0;
		double total = 0d;
		BufferedWriter bw = new BufferedWriter(new FileWriter("paper/signalerPerexception.csv"));
		bw.write("type,count,per");
		bw.newLine();
		for(ThrowCount throwCount : list) {
			total += throwCount.count;
		}
		DecimalFormat df = new DecimalFormat("#%");
		for(ThrowCount throwCount : list) {
			total += throwCount.count;
			if(throwCount.count > 0) {
				System.out.println(throwCount.thrownType.getName()+" : "+throwCount.count);
				String[] packageNames = throwCount.thrownType.getName().split("\\.");
				String className = packageNames[packageNames.length-1];
				double per = throwCount.count / 2218d;
				if(className.contains("Error")) {
					bw.write(className+","+throwCount.count+","+df.format(per));
				}else {
					bw.write(className.substring(0,className.length()-9)+","+throwCount.count+","+df.format(per));
				}
				bw.newLine();
			}else {
				others += throwCount.count;
			}
		}
		System.out.println("Others : "+others);
		bw.write("others,"+others);
		bw.close();
		System.out.println("Total types of runtime exception : "+list.size());
	}
	
	public static void printSignalerPerPackageCounts(List<Signaler> signalers) throws IOException {
		List<ThrowCount> list = new ArrayList<ThrowCount>();
		for(Signaler sig : signalers) {
			String packageName = sig.packageName();
			boolean has = false;
			for(ThrowCount throwCount: list) {
				if(throwCount.packageName.equals(packageName)) {
					throwCount.addCount();
					has = true;
					break;
				}
			}
			if(!has) {
				list.add(new ThrowCount(packageName));
			}
		}
		Collections.sort(list, new Comparator<ThrowCount>() {
			@Override
			public int compare(ThrowCount o1, ThrowCount o2) {
				return Integer.compare(o2.count, o1.count);
			}
			
		});
		BufferedWriter bw = new BufferedWriter(new FileWriter("paper/signalerPerpackage.csv"));
		bw.write("type,count,per");
		bw.newLine();
		int rank = 0;
		double total = 0d;
		for(ThrowCount throwCount : list) {
			total += throwCount.count;
		}
		DecimalFormat df = new DecimalFormat("#%");
		for(ThrowCount throwCount : list) {
			rank++;
			if(rank<10) {
				double per = throwCount.count / total;
				bw.write(removeAndroidDot(throwCount.packageName)+","+throwCount.count+","+df.format(per));
				bw.newLine();
			}else {
				break;
			}
		}
		bw.close();
		System.out.println("Total Packages with Signalers : "+list.size());
	}
	
	public static String removeAndroidDot(String packageName) {
		return packageName.substring(8);
	}
	
	public static class ThrowCount{
		public SootClass thrownType;
		public int count = 0;
		public String packageName;
		public ThrowCount(SootClass thrownType) {
			this.thrownType = thrownType;
			this.addCount();
		}
		public ThrowCount(String packageName) {
			this.packageName = packageName;
			this.addCount();
		}
		public void addCount() {
			this.count++;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
