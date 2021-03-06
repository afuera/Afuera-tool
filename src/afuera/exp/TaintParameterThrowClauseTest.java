package afuera.exp;

import afuera.core.FileConfig;
import afuera.flow.analysis.FlowAnalysis;
import afuera.instrumentation.JarInstrumenter;

public class TaintParameterThrowClauseTest {
	public static void main(String args[]) {
//		
		JarInstrumenter ji = new JarInstrumenter("<android.view.View: void sendAccessibilityEventUnchecked(android.view.accessibility.AccessibilityEvent)>", "<android.view.accessibility.AccessibilityRecord: void enforceNotSealed()>", 1, "java.lang.IllegalStateException");
		ji.updateJimpleForICC(true);
		//ji.dummyIfConditionMethod.add("<dynamitedemo.Signaler: void dummy_IfCondition(int)>");
		//ji.dummyParaMethods.add("<dynamitedemo.CrashedAPI: java.lang.String dummy_Para_0(java.lang.String)>");
		new FlowAnalysis().run(ji, FileConfig.FRAMEWORK_JAR);
	}
}
