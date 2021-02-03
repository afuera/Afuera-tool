package afuera.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.options.Options;
import soot.util.Chain;

public class WriteAllAPI {

	public static void main(String args[]) {
		G.reset();
		Options.v().set_whole_program(true);
		Options.v().setPhaseOption("cg.cha", "enabled:true");
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_android_jars(FileConfig.MAC_ANDROID_PLATFORM);
		List<String> processPaths = new ArrayList<String>();
		processPaths.add(FileConfig.CLASSES_DEX);
		Options.v().set_process_dir(processPaths);
		Options.v().set_app(true);
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_output_format(Options.output_format_none);
	    Options.v().ignore_resolution_errors();//-ire
	    Options.v().prepend_classpath();//-pp
	    Scene.v().loadBasicClasses();
	    //Scene.v().loadNecessaryClasses();
	    Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
	    Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);
	    Options.v().set_validate(true); // Validate Jimple bodies in each transofrmation pack
		List<SootMethod> entryPoints = new ArrayList<SootMethod>();
		SootClass c = Scene.v().forceResolve("android.app.Instrumentation", SootClass.BODIES);
		c.setApplicationClass();
		//Scene.v().loadNecessaryClasses();
		SootMethod method = c.getMethodByName("checkStartActivityResult");
		entryPoints.add(method);
		Scene.v().setEntryPoints(entryPoints);
		//SootMethod method = c.getMethodByName("crashedAPI");	
		List<String> includeList = new LinkedList<String>();
		includeList.add("java.lang.*");
		includeList.add("java.util.*");
		includeList.add("java.io.*");
		includeList.add("sun.*");
		includeList.add("java.net.*");
		includeList.add("javax.servlet.*");
		includeList.add("javax.crypto.*");
		includeList.add("android.*");
		includeList.add("org.apache.*");
		includeList.add("de.test.*");
		includeList.add("soot.*");
		includeList.add("com.example.*");
		includeList.add("libcore.icu.*");
		includeList.add("securibench.*");
		Options.v().set_include(includeList);
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {

			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
				for(Iterator<SootClass> iter = sootClasses.snapshotIterator();iter.hasNext();) {
					SootClass sc = iter.next();
					List<SootMethod> methods = sc.getMethods();
					for(SootMethod cur: methods) {
						System.out.println(cur.getSignature());
//						if(sc.getName().equals(method.getDeclaringClass().getName()) && cur.getName().equals(method.getName())){
//							CallGraph cg = Scene.v().getCallGraph();
//							printPossibleCallees(cg, cur);
//						}
					}
				}
			}
		}));
		PackManager.v().runPacks();
	}
}
