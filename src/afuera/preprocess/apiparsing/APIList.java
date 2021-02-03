package afuera.preprocess.apiparsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class APIList extends Parse{

	public APIList(String apiListFilePath) throws IOException {
		super(apiListFilePath);
		// TODO Auto-generated constructor stub
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
		String packageName = null;
		String className = null;
		String methodName =  null;
		br.readLine();//
		while((line = br.readLine()) != null) {
			if(packageName != null) {
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
						if(methodName!=null && nextMethodName.equals(methodName))
							//Currently, we only handle one overloaded method, to improve later. 
								continue;
						methodName = nextMethodName;
						if(methodName.equals(className))
							methodName = "<init>";//means it is constructor.
						this.methodNamelist.add(new API(packageName, className, methodName));
						System.out.println(packageName+"."+className+"."+methodName);
					}else if(line.contains("}")) {
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
					//System.out.println(line);
					className = lineSplitSpace[index2Name+1];
					//System.out.println(className);
				}else if(line.contains("}")) {
					packageName = null;
				}else {
					continue;
				}
			}else if(line.contains("package ")) {
				String[] lineSplitSpace = line.split(" ");
				int index2Name = -1;
				for(String word: lineSplitSpace) {
					index2Name += 1;
					if(word.equals("package")) {
						break;
					}
				}
				//System.out.println(line);
				packageName = lineSplitSpace[index2Name+1];
				//System.out.println(packageName);
			}else {
				continue;
			}
		}
		br.close();
	}


	
	
}
