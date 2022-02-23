package afuera.instrumentation;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import afuera.core.Signaler;
import afuera.core.StackFrame;
import heros.InterproceduralCFG;
import soot.Local;
import soot.Modifier;
import soot.PatchingChain;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.infoflow.ipc.IIPCManager;

public class JarInstrumenter implements IIPCManager{

	public String apiSignature, signalerSignature;
	public int signalerPatchingChainPosition;
	public SootMethod smAPI;
	public SootMethod smSignaler;
	public Unit unitThrownUncheckException;
	public Set<String> dummyParaMethods = new HashSet<String>();
	public Set<String> dummyIfConditionMethod = new HashSet<String>();
	private boolean isPreprocessing = false;
	public String thrownExceptionName = null;
	public List<StackFrame> stackTrace;
	
	@Override
	public boolean isIPC(Stmt arg0, InterproceduralCFG<Unit, SootMethod> arg1) {
		return false;
	}
	protected JarInstrumenter() {
		
	}

	@Override
	public void updateJimpleForICC() {
		this.find_API_Signaler_ThrowClause();
		this.addDummyParaMethods();
		//DEBUG:
		System.out.println(this.smAPI.retrieveActiveBody());
		//DEBUG:
		this.addDummyIfConditionMethod();
		//DEBUG:
		System.out.println(this.smSignaler.retrieveActiveBody());
		//DEBUG:
		this.dumpLog();
	}
	
	public void updateJimpleForICC(boolean isPreprocessing) {
		this.isPreprocessing = isPreprocessing;
		this.find_API_Signaler_ThrowClause();
		this.addDummyParaMethods();
		this.addDummyIfConditionMethod();
		this.isPreprocessing = false;
	}
	
	public void dumpLog() {
		System.out.println("API signature: " +this.apiSignature);
		System.out.println(this.smAPI.retrieveActiveBody());
		System.out.println("Signaler signature: "+this.signalerSignature);
		System.out.println(this.smSignaler.getActiveBody());
		System.out.println("Throw int: "+this.signalerPatchingChainPosition);
		System.out.println("ThrowStmt: "+this.unitThrownUncheckException.toString());
		for(String source : this.dummyParaMethods) {
			System.out.println("Source: "+source);
		}
		for(String sink : this.dummyIfConditionMethod) {
			System.out.println("Sink: "+sink);
		}
	}
	
	public void addDummyParaMethods() {
		List<SootMethod> dummySourceMethodList = new ArrayList<SootMethod>();
		int paraId = -1;
		SootMethod sm = this.smAPI;
		for(Type para: sm.getParameterTypes()) {
			paraId+=1;
			List<Type> paraListDummySource = new ArrayList<Type>(); 
			paraListDummySource.add(para);
			//TODO: dummyParaMethods should have same name.
			SootMethod dummySourceMethod = new SootMethod(
					"dummy_Para_"+paraId,
					paraListDummySource, para,
					Modifier.STATIC | Modifier.PUBLIC);
			JimpleBody body = Jimple.v().newBody(dummySourceMethod);
			dummySourceMethod.setActiveBody(body);
			Local local = Jimple.v().newLocal("r0", para);
			body.getLocals().add(local);
			PatchingChain<Unit> units = body.getUnits();
			units.add(Jimple.v().newIdentityStmt(local, Jimple.v().newParameterRef(para, 0)));
			units.add(Jimple.v().newReturnStmt(local));
			dummySourceMethodList.add(dummySourceMethod);
			if(this.isPreprocessing) {
				this.dummyParaMethods.add("<"+sm.getDeclaringClass().getName()+": "+dummySourceMethod.getSubSignature()+">");
			}else {
				sm.getDeclaringClass().addMethod(dummySourceMethod);
				this.insertDummyParaInvocation(dummySourceMethod, paraId);
			}
		}				
	}
	
	public void addDummyIfConditionMethod() {
		SootMethod sm = this.smSignaler;
		Unit ts = this.unitThrownUncheckException;
		List<ValueBox> valueBoxes;
		//DEBUG
//		if(sm.getSignature().equals("<android.view.ViewGroup: void setDescendantFocusability(int)>")) {
//			System.out.println(sm.retrieveActiveBody());
//			System.out.println();
//		}
		//DEBUG
		valueBoxes = new Signaler(sm).getValueBoxesPrecedingThrowClause(ts);
		if(valueBoxes == null) {
			System.out.println("Found no IfStmt preceding ThrowStmt: "+this.unitThrownUncheckException.toString()+" in "+sm.getSignature());
			return;
		}
		List<Type> paraListDummySink = new ArrayList<Type>(); 
		List<Value> valueListDummySink = new ArrayList<Value>();
		boolean onlyTakeFirstValue = true;
		for(ValueBox vb: valueBoxes) {
			if(!((vb.getValue().getType() instanceof RefType) || (vb.getValue().getType() instanceof PrimType)))
				continue;
			paraListDummySink.add(vb.getValue().getType());
			valueListDummySink.add(vb.getValue());
			if(onlyTakeFirstValue)
				break;
		}
		String dummySinkMethodName = ("dummy_IfCondition");
		SootMethod dummySinkMethod = new SootMethod(
				dummySinkMethodName,
				paraListDummySink, VoidType.v(),
				Modifier.STATIC | Modifier.PUBLIC);
		JimpleBody sinkBody = Jimple.v().newBody(dummySinkMethod);
		dummySinkMethod.setActiveBody(sinkBody);
		int paraId = 0;
		for(Type t : paraListDummySink) {
			Local local = Jimple.v().newLocal("r"+paraId, t);
			sinkBody.getLocals().add(local);
			PatchingChain<Unit> units = sinkBody.getUnits();
			units.add(Jimple.v().newIdentityStmt(local, Jimple.v().newParameterRef(t, paraId)));
			units.add(Jimple.v().newReturnVoidStmt());
			paraId++;
		}
		if(this.isPreprocessing) {
			this.dummyIfConditionMethod.add("<"+sm.getDeclaringClass().getName()+": "+dummySinkMethod.getSubSignature()+">");
		}else {
			sm.getDeclaringClass().addMethod(dummySinkMethod);
			this.insertDummyIfConditionInvocation(dummySinkMethod, valueListDummySink);
		}
	}
	
	public void insertDummyParaInvocation(SootMethod dummySourceMethod, int paraId) {
		SootMethod sm = this.smAPI;
		PatchingChain<Unit> smUnits = sm.retrieveActiveBody().getUnits();
		Unit insertBeforeUnit = null;
		for(Unit smUnit: smUnits) {
			if(!(smUnit instanceof IdentityStmt)) {
				insertBeforeUnit = smUnit;
				break;
			}
		}
		//insert before smUnit
		Local paraLocal = sm.getActiveBody().getParameterLocal(paraId);
		sm.getActiveBody().getUnits().insertBefore(
				Jimple.v().newAssignStmt(
						paraLocal
						,Jimple.v().newStaticInvokeExpr(
								dummySourceMethod.makeRef()
								,paraLocal
								)
						)
				, insertBeforeUnit
				);
	}
	
	public void insertDummyIfConditionInvocation(SootMethod dummySinkMethod, List<Value> valueListDummySink) {
		SootMethod sm = this.smSignaler;
		sm.getActiveBody().getUnits().insertBefore(
				Jimple.v().newInvokeStmt(
						Jimple.v().newStaticInvokeExpr(
								dummySinkMethod.makeRef()
								,valueListDummySink
								)
						)
				, this.unitThrownUncheckException);
	}
	public void find_API_Signaler_ThrowClause() {
		for(SootClass sc: Scene.v().getApplicationClasses()) {
			for(SootMethod sm : sc.getMethods()) {
				if(sm.getSignature().equals(this.apiSignature)) {
					this.smAPI = sm;
					//System.out.println(sm.retrieveActiveBody());
				}
				if(sm.getSignature().equals(this.signalerSignature)) {
					this.smSignaler = sm;
					//System.out.println(sm.retrieveActiveBody());
					this.unitThrownUncheckException = new Signaler(sm).getThrowStmtByUnitGraphPosition(this.signalerPatchingChainPosition);
				}
			}
		}
	}

	public JarInstrumenter(String apiSignature, String signalerSignature, int signalerPatchingChainPosition, String thrownExceptionName) {
		this.apiSignature = apiSignature;
		this.signalerSignature = signalerSignature;
		this.signalerPatchingChainPosition = signalerPatchingChainPosition;
		this.thrownExceptionName = thrownExceptionName;
	}
	
	
	
	
	
}
