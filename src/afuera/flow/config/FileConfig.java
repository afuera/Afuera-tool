package afuera.flow.config;

import java.io.File;

public class FileConfig {
	public static final String API_LEVEL = "res/"+"api29/"; //can change to api20, api29 etc
	public static final String DOC_API_EXCEPTION = API_LEVEL + "doc_api_exception.txt";
	public static final String SAMPLED_API_EXCEPTION = API_LEVEL + "sampled_api_exception.txt"; //sampled every 58 lines;
	public static final String DOC_API_PACKAGE = API_LEVEL + "doc_api_package.txt";
	public static final String FRAMEWORK_JAR = API_LEVEL + "framework-generated.jar";
	public static final String PARAMETER_THROW_OUTCOMES = API_LEVEL + "flow/";
	public static final String MODULE_II_SAMPLED_ANALYSIS_OUTCOME = API_LEVEL + "moduleII_Sampled/";
	public static final String PARAMETER_THROW_OUTCOMES_TEMP = API_LEVEL + "flowTemp/";
	public static final String API_LIST_FRAMEWORK = API_LEVEL + "GeneratedAPIList.txt";
	public static final String UE_API = API_LEVEL + "ue_api.txt";
	public static final String ALL_API = API_LEVEL + "all_api.txt";
	public static final String IMPLICATION_api = API_LEVEL + "implication_api.txt";
	public static final String MAC_ANDROID_PLATFORM = "../../playground/jars/android-platforms/";//path dependent on my develop environment
	public static final String SERVER_ANDROID_PLATFORM = "res/android-platforms/"; //path dependent on my server test environment

	public static final String UE_USAGEs = "res/RQ2/ue/";
	public static final String ALL_USAGEs = "res/RQ2/all/";
	public static final String HANDLE_USAGES = "res/RQ2/handle/";

	//.csv files
	//for RQ1
	public static final String STAT_SIGNALER_PER_PACKAGE = API_LEVEL + "signalerPerpackage.csv";
	public static final String STAT_SIGNALER_PER_EXCEPTION = API_LEVEL + "signalerPerexception.csv";
	public static final String STAT_API_PER_EXCEPTION = API_LEVEL + "apiPerexception.csv";
	public static final String STAT_API_PER_PACKAGE = API_LEVEL + "apiPerpackage.csv";
	//for RQ2
	public static final String STAT_EXCEPTION_BOXPLOT = API_LEVEL + "exceptionboxplot.csv";
	public static final String STAT_PACKAGE_BOXPLOT = API_LEVEL + "packageboxplot.csv";
	public static final String STAT_YEAR_BOXPLOT = API_LEVEL + "yearboxplot.csv";
	
}
