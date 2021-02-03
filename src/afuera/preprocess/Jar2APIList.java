package afuera.preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import afuera.core.Main;

public class Jar2APIList {
	public static void main(String args[]) throws IOException {
		List<String> allPubClasses = new ArrayList<String>();
		Process p = Runtime.getRuntime().exec("jar -tf res/framework.jar");
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = null;
		while((line=br.readLine())!=null) {
			line = line.replace("/", "."); 
			allPubClasses.add(line);
			//System.out.println(line);
		}
		br.close();
		File toDelete = null;
		if((toDelete = new File("res/GeneratedAPIList.txt")).exists())
			toDelete.delete();
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("res/GeneratedAPIList.txt"),true));
		for(String cName : allPubClasses) {
			BufferedReader br1 = null;
			cName = cName.substring(0, cName.length()-6);//remove .class 
			//System.out.println(cName);
			Process q = Runtime.getRuntime().exec("javap -classpath res/framework.jar "+cName);
			br1 = new BufferedReader(new InputStreamReader(q.getInputStream()));
			String methods = null;
			br1.readLine();//to pass "Compiled from "....java"" line
			while((methods=br1.readLine())!=null) {
				System.out.println(methods);
				bw.write(methods);
				bw.newLine();
			}
			bw.newLine();
			br1.close();
		}
		bw.close();
	}
}
