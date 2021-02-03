package afuera.preprocess.apiparsing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import afuera.preprocess.apiparsing.Parse.API;

public abstract class Parse {
	
	public File apiListFile = null;
	public List<API> methodNamelist = new ArrayList<API>();
	public List<API> signatureList = new ArrayList<API>();
	
	public Parse(String apiListFilePath) throws IOException {
		this.apiListFile = new File(apiListFilePath);
		this.generateList();
	}

	public abstract void generateList() throws IOException;
	
	public class API{
		public String packageName = null;
		public String className = null;
		public String methodName = null;
		public String fullName = null;
		public String classFullName = null;
		public String signature = null;
		
		public API(String packageName, String className, String methodName) {
			this.packageName = packageName;
			this.className = className;
			this.methodName = methodName;
			this.fullName = packageName+"."+className+"."+methodName;
		}
		
		public API(String classFullName, String methodName) {
			this.classFullName = classFullName;
			this.methodName = methodName;
			this.fullName = classFullName+"."+methodName;
		}
		public API(String signature) {
			this.signature = signature;
		}
		
		public String getFullName() {
			return this.fullName;
		}
	}
	
	public List<API> getAPIList(){
		return methodNamelist;
	}
	
	public List<API> getSignatureAPIList(){
		return this.signatureList;
	}
	
	
}

