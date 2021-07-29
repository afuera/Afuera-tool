package afuera.flow.config;

import java.util.ArrayList;
import java.util.List;

import afuera.core.Signaler;
import afuera.preprocess.apiparsing.SelfAPIList;
import soot.G;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.InfoflowConfiguration.CallgraphAlgorithm;
import soot.jimple.infoflow.InfoflowConfiguration.PathBuildingAlgorithm;
import soot.jimple.infoflow.InfoflowConfiguration.PathReconstructionMode;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.options.Options;


public class FlowConfig implements IInfoflowConfig{

	@Override
	public void setSootOptions(Options options, InfoflowConfiguration config) {
//		// TODO Auto-generated method stub
//		config.setCallgraphAlgorithm(CallgraphAlgorithm.CHA);
//		Options.v().setPhaseOption("cg.cha", "enabled:true");//Q: what is enabled?
//		Options.v().set_allow_phantom_refs(true);
//		options.set_output_format(Options.output_format_none);
//		Options.v().set_whole_program(true);
//		options.set_include_all(true);//I want all classes in framework to be studied, for now
		
		
		
		G.reset();
		config.setCallgraphAlgorithm(CallgraphAlgorithm.CHA);
		Options.v().setPhaseOption("cg.cha", "enabled:true");//Q: what is enabled?
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);;
		List<String> processPaths = new ArrayList<String>();		
		Options.v().set_src_prec(Options.src_prec_class);
		processPaths.add(FileConfig.FRAMEWORK_JAR);
		Options.v().set_process_dir(processPaths);
		Options.v().set_output_format(Options.output_format_none);
	    Options.v().set_validate(true); // Validate Jimple bodies in each transofrmation pack
		options.set_include_all(true);//I want all classes in framework to be studied, for now
		Scene.v().loadNecessaryClasses();
		//in order to obtain path
		/*
		 * Contexsensitive reconstructing path
		 */
		config.getPathConfiguration().setPathBuildingAlgorithm(PathBuildingAlgorithm.ContextSensitive);
		/*
		 * Fast reconstructing the path
		 */
		config.getPathConfiguration().setPathReconstructionMode(PathReconstructionMode.Fast);
	}
}
