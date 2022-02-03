package afuera.exp;

import java.io.IOException;
import java.util.Map;

import afuera.flow.analysis.FlowAnalysis;
import afuera.flow.config.FileConfig;
import afuera.instrumentation.JarInstrumenter;

public class ModuleIITest {
	public static void main(String args[]) {
//		
		// JarInstrumenter ji = new JarInstrumenter("<android.view.View: void sendAccessibilityEventUnchecked(android.view.accessibility.AccessibilityEvent)>", "<android.view.accessibility.AccessibilityRecord: void enforceNotSealed()>", 1, "java.lang.IllegalStateException");
		// ji.updateJimpleForICC(true);
		// //ji.dummyIfConditionMethod.add("<dynamitedemo.Signaler: void dummy_IfCondition(int)>");
		// //ji.dummyParaMethods.add("<dynamitedemo.CrashedAPI: java.lang.String dummy_Para_0(java.lang.String)>");
		// new FlowAnalysis().run(ji, FileConfig.FRAMEWORK_JAR);

		try {
			Map<String,Integer> map = ModuleII.read(FileConfig.SAMPLED_API_EXCEPTION);
			System.out.println(map);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
