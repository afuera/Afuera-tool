package afuera.preprocess.apiparsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import soot.AmbiguousMethodException;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

public class SelfAPIList extends Parse{
	public List<SootMethod> entryPoints = new ArrayList<SootMethod>();
	
	public SelfAPIList(String apiListFilePath) throws IOException {
		super(apiListFilePath);
		// TODO Auto-generated constructor stub
	}
	
	public List<SootMethod> getEntryPoints(){
		int startWithSmall = 0;
		for(Parse.API api : this.getAPIList()) {
			if(startWithSmall>5000000)
				break;
			try {
				SootClass b = Scene.v().forceResolve(api.classFullName, SootClass.BODIES);
				if(!b.isPublic()) {
					System.out.println(b.getName());
					continue;
				}
				if(b.isInnerClass()) {
					SootClass outerClass = b.getOuterClass();
					if(!outerClass.isPublic()) {
						System.out.println(b.getName());
						continue;
					}
					if(outerClass.isInnerClass()) {
						SootClass outer2Class = outerClass.getOuterClass();
						if(!outer2Class.isPublic()) {
							System.out.println(b.getName());
							continue;
						}
					}
				}
				for(SootMethod sm: b.getMethods()) {
					if(sm.getName().equals(api.methodName) && !entryPoints.contains(sm) ) {
						if(!sm.isAbstract() && (!b.isFinal() && sm.isProtected()) || sm.isPublic()) {
							entryPoints.add(sm);
							startWithSmall+=1;
						}else {
						}
					}
				}
			}catch(AmbiguousMethodException e) {
				e.printStackTrace();
				System.out.println(api.fullName);
				try {
					if(api.signature.equals("<android.app.Activity: void startActivityForResult(android.content.Intent, int)>")) {
						System.out.println("target found.");
					}
					SootMethod sm0 = Scene.v().getMethod(api.signature);
					entryPoints.add(sm0);
				}catch(RuntimeException f) {
					f.printStackTrace();
					System.out.println(api.signature);
				}
				System.out.println();
			}catch(RuntimeException r) {
				r.printStackTrace();
			}
		}
		System.out.println("Total API: "+this.entryPoints.size());
		System.out.println("Total API: "+startWithSmall);
		return this.entryPoints;
	}
	
	@Override
	public void generateList() throws IOException{
		//TODO
		//API may be ending with ");"?
		//name is the string before "(" and after " "?
		//First of all, find blocks surrounded in "{" and "}".
		//Also look for package name.
		BufferedReader br = new BufferedReader(new FileReader(this.apiListFile));
		String line = null;
		String className = null;
		String methodName =  null;
		br.readLine();//
		while((line = br.readLine()) != null) {
			if(className != null) {
				if(line.endsWith(");")){
					//method line
					//need also to check if the method is deprecated.
					if(line.contains("@Deprecated"))
						continue;
					if(line.contains(" abstract "))
						continue;//abstract method does not have body, soot will crash!
					String[] lineSplitLeftBrace = line.split("\\(");
					String splitLeftBrace = lineSplitLeftBrace[lineSplitLeftBrace.length-2];
					String[] lineSplitSpace = splitLeftBrace.split(" ");
					String nextMethodName = lineSplitSpace[lineSplitSpace.length-1];
					methodName = nextMethodName;
					if(methodName.equals(className))
						methodName = "<init>";//means it is constructor.
					API api = new API(className, methodName);
					api.signature = this.fromMethodDeclaration2SootSignature(className, line);
					this.methodNamelist.add(api);
				}else if(line.contains("}") && !line.contains("{")) {
					className = null;
				}else {
					continue;
				}
			}else if(line.contains("class ")) {
				String[] lineSplitSpace = line.split(" ");
				int index2Name = -1;
				for(String word: lineSplitSpace) {
					index2Name += 1;
					if(word.equals("class")) {
						break;
					}
				}
				className = lineSplitSpace[index2Name+1];
			}else {
				continue;
			}
		}
		br.close();
	}
	
	public String fromMethodDeclaration2SootSignature(String classFullName, String md) {
		//    public boolean onTransact(int, android.os.Parcel, android.os.Parcel, int) throws android.os.RemoteException;
		//"<android.accounts.AccountManager: boolean addAccountExplicly(android.accounts.Account,java.lang.String,android.os.Bundle)>"
		//TODO: find the last index of public static final protected.
		md = md.replace(";", "");
		
		int lastIndex = 0;
		int curIndex = 0;
		curIndex = md.lastIndexOf("public ");
		lastIndex = lastIndex<curIndex?curIndex+6:lastIndex;
		curIndex = md.lastIndexOf("static ");
		lastIndex = lastIndex<curIndex?curIndex+6:lastIndex;
		curIndex = md.lastIndexOf("final ");
		lastIndex = lastIndex<curIndex?curIndex+5:lastIndex;
		curIndex = md.lastIndexOf("protected ");
		lastIndex = lastIndex<curIndex?curIndex+9:lastIndex;
		lastIndex = lastIndex==0?1:lastIndex;
		//System.out.println(md);
		String sootSignature = "<"+classFullName+":"+md.substring(lastIndex, md.length())+">";
		String[] splitBrace = sootSignature.split("\\(");
		String[] splitSpace = splitBrace[0].split(" ");
		String methodName = splitSpace[splitSpace.length-1];
		if(classFullName.equals(methodName)) {
			sootSignature = splitBrace[0].substring(0,splitBrace[0].length()-1-methodName.length()) + " void <init>(" + splitBrace[1];
		}
		//System.out.println(sootSignature);
		return sootSignature;
	}

}
