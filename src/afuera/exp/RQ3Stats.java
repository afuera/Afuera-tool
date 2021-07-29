package afuera.exp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import afuera.core.Signaler;
import afuera.core.StackFrame;
import afuera.flow.config.FileConfig;
import afuera.preprocess.apiparsing.SelfAPIList;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.jimple.ThrowStmt;
import soot.options.Options;

public class RQ3Stats {

	public static void main(String args[]) throws IOException {
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
		SelfAPIList apiList = new SelfAPIList(FileConfig.API_LIST_FRAMEWORK);
		Scene.v().setEntryPoints(apiList.getEntryPoints());
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransform", new SceneTransformer() {

			@Override
			protected void internalTransform(String phaseName, Map<String, String> options) {
				int fileId = 1;
				for(File f : new File(FileConfig.PARAMETER_THROW_OUTCOMES).listFiles()){
					if(!f.getName().contains("android"))
						continue;
					try {
						BufferedReader br = new BufferedReader(new FileReader(f));
						String apiSignature = br.readLine();
						String signalerSignature = br.readLine();
						String exceptionType = br.readLine();
						String sink = null;
						List<String> sources = new ArrayList<String>();
						if((sink = br.readLine()) != null) {
							String source = null;
							while((source = br.readLine()) != null) {
								sources.add(source);
							}
						}
						br.close();
						StringBuilder sb = new StringBuilder();
						for(SootClass sc: Scene.v().getApplicationClasses()) {
							for(SootMethod sm : sc.getMethods()) {
//								if(sm.getSignature().equals(apiSignature)) {
//									sb.append(sm.getDeclaringClass().getName());
//									sb.append(".").append(sm.getName()).append("&");
//									sb.append(justName(exceptionType)+"&");
//									sb.append(sm.getParameterCount()).append("&");
//									List<String> listTypes = new ArrayList<String>();
//									for(Type type : sm.getParameterTypes()) {
//										listTypes.add(type.toString());
//										sb.append(justName(type.toString())+ " ");
//									}
//									sb.append("& ");
//									for(String source : sources) {
//										String[] arrSource = source.split("_");
//										sb.append(arrSource[arrSource.length-1]+":"+listTypes.get(Integer.valueOf(arrSource[arrSource.length-1]))+" ");
//									}
//									System.out.println(sb.append("&  \\\\").toString().replace("$", "\\$"));
//									
//								}
//								if(sm.getSignature().equals(signalerSignature)) {
//
//								}
								String gitPath = "https://github.com/afuera/Module-II/blob/main/";
								if(sm.getSignature().equals(apiSignature)) {
									if(apiSignature.equals(signalerSignature))
										System.out.println("++++++++++++++++++++++");
									String fullName = f.getName();
									sb.append("|"+fileId);
									fileId++;
									sb.append("|["+justName(sm.getDeclaringClass().getName()));
									String methodName = sm.getName();
									if(methodName.equals("<init>")) {
										methodName = "\\<init\\>";//:methodName;
									}
									sb.append(".").append(methodName).append("](").append(gitPath).append(fullName).append(")").append("|");
									sb.append(justName(exceptionType)+"|");
//									sb.append(justName(exceptionType)+"&");
//									sb.append(sm.getParameterCount()).append("&");
									List<String> listTypes = new ArrayList<String>();
									for(Type type : sm.getParameterTypes()) {
										listTypes.add(type.toString());
									}
//									sb.append("& ");
									int bad = -1;
									for(String source : sources) {
										String[] arrSource = source.split("_");
										bad = Integer.valueOf(arrSource[arrSource.length-1]);
//										sb.append(","+arrSource[arrSource.length-1]+":"+justName(listTypes.get(Integer.valueOf(arrSource[arrSource.length-1])))+" ");
									}
									int temp = -1;
									for(Type type : sm.getParameterTypes()) {
										temp++;
										if(bad==temp) {
											sb.append("***~~"+justName(type.toString())+ "~~***, ");
										}else {
											sb.append(justName(type.toString()));
											if(temp==sm.getParameterCount()-1) {
												
											}else {
												sb.append(", ");
											}
										}
									}
//									sb.append("& ");
								}
							}
						}
//						sb.append(justName(exceptionType)+"&");
						for(SootClass sc: Scene.v().getApplicationClasses()) {
							for(SootMethod sm : sc.getMethods()) {
								if(sm.getSignature().equals(signalerSignature)) {
//									sb.append(sm.getDeclaringClass().getName());
//									sb.append(".").append(sm.getName()).append("&");
								}
							}
						}
						if(sources.size()>0) {
							System.out.println(sb.append("|TP|"));//.append("&  \\\\").toString().replace("$", "\\$"));
							//Since we have manually confirmed that all positives are true.
						}else {
							System.out.println(sb.append("|   |"));//.append("&  \\\\").toString().replace("$", "\\$"));
						}
					}catch(IOException e) {
						e.printStackTrace();
					}
				}
			}	
		}));
		PackManager.v().runPacks();
	}
	
	public static String justName(String fullName) {
		String[] arr = fullName.split("\\.");
		return arr[arr.length-1];
	}
}
