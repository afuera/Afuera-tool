package afuera.preprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import afuera.core.FileConfig;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.Transform;
import soot.options.Options;

public class Dex2Jar {
	public static void main(String args[]) throws IOException {
		G.reset();
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);;
		Options.v().set_force_android_jar(FileConfig.MAC_ANDROID_PLATFORM+"android-25/android.jar");
		List<String> processPaths = new ArrayList<String>();
		processPaths.add(FileConfig.CLASSES_DEX);
		Options.v().set_process_dir(processPaths);
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_output_jar(true);
		Options.v().set_output_dir("res/framework.jar");
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
