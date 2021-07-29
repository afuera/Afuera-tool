package afuera.preprocess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import afuera.flow.config.FileConfig;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.Transform;
import soot.options.Options;
/**
 * Important: https://stackoverflow.com/questions/30656933/android-system-framework-jar-files
 *
 */
public class Dex2Jar {
	public static void main(String args[]) throws IOException {
		G.reset();
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);;
		Options.v().set_force_android_jar(FileConfig.MAC_ANDROID_PLATFORM+"android-25/android.jar");
		List<String> processPaths = new ArrayList<String>();
		for(File dex : new File(FileConfig.API_LEVEL).listFiles()) {
			System.out.println(dex.getAbsolutePath());
			if(dex.toString().endsWith("dex"))
				processPaths.add(dex.getAbsolutePath());
		}
//		processPaths.add("/Users/pingfan.kong/Uni.lu/code/Dynamite/res/api26/x86/boot-framework.oat");
		Options.v().process_multiple_dex();
		Options.v().set_process_dir(processPaths);
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_output_jar(true);
		Options.v().set_output_dir(FileConfig.FRAMEWORK_JAR);
	    Options.v().set_validate(true); // Validate Jimple bodies in each transofrmation pack
	    Scene.v().loadNecessaryClasses();
	    
	    PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {

			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				
			}
	    }));
	    PackManager.v().runPacks();
	    PackManager.v().writeOutput();
	}
}
