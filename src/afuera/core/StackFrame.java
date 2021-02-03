package afuera.core;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import soot.Body;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ThrowStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph.ExceptionDest;

public class StackFrame{
		public SootMethod stackFrameMethod = null;
		public List<Unit> targetUnits = new ArrayList<Unit>();
		public List<Unit> unHandledUnits = new ArrayList<Unit>();
		public boolean isSignaler = false;
		public SootClass thrownException = null;
		/**
		 * The very initial throw statement from the signaler.
		 */
		public ThrowStmt throwStmt = null;
		public int handledCount = 0;
		public StackFrame(SootMethod stackFrameMethod, StackFrame tar) {
			this.stackFrameMethod = stackFrameMethod;
			this.computeTargetUnits(tar);
			this.thrownException = tar.thrownException;
			this.throwStmt = tar.throwStmt;
			this.computeUnHandledUnits();
		}
		public StackFrame(SootMethod signalerMethod, ThrowStmt throwStmt) {
			this.stackFrameMethod = signalerMethod;
			this.targetUnits.add(throwStmt);
			RefType rf = (RefType) throwStmt.getOp().getType();
			this.thrownException = rf.getSootClass();
			this.isSignaler = true;
			this.handledCount = this.computeUnHandledUnits();
			this.throwStmt = throwStmt;
		}
		
		public IfStmt getIfStmtPrecedingUnit(Unit u) {
			//TODO
			Body body = this.stackFrameMethod.retrieveActiveBody();
			ExceptionalUnitGraph ug = new ExceptionalUnitGraph(body);
			List<Unit> preds = ug.getUnexceptionalPredsOf(u);
			for(Unit pre: preds) {
				if(pre instanceof IfStmt) {
					return (IfStmt) pre;
				}
			}
			return null;
		}
		public void computeTargetUnits(StackFrame tar) {
			Body body = stackFrameMethod.retrieveActiveBody();
			ExceptionalUnitGraph ug = new ExceptionalUnitGraph(body);
			Iterator<Unit> iter = ug.iterator();
//			iter = 			body.getUnits().iterator();
//			boolean debug = false;
//			if(tar.stackFrameMethod.getSignature().equals("<android.app.Instrumentation: android.app.Instrumentation$ActivityResult execStartActivity(android.content.Context,android.os.IBinder,android.os.IBinder,android.app.Activity,android.content.Intent,int,android.os.Bundle)>"))
//				debug = true;
//			if(debug)
//				System.out.println(body);
			while(iter.hasNext()) {
				Unit u = (Unit) iter.next();
				if(u instanceof InvokeStmt) {
					InvokeStmt invokeStmt = (InvokeStmt) u;
					if(invokeStmt.getInvokeExpr().getMethod().equals(tar.stackFrameMethod)) {
						this.targetUnits.add(invokeStmt);
					}
				}else if(u instanceof AssignStmt) {
					AssignStmt assignStmt = (AssignStmt) u;
					if(assignStmt.containsInvokeExpr()) {
						if(assignStmt.getInvokeExpr().getMethod().equals(tar.stackFrameMethod))
							this.targetUnits.add(u);
					}

				}
			}
		}
		
		public int getUnitGraphPositionByThrowStmt() {
			if(this.isSignaler) {
				return new Signaler(this.stackFrameMethod).getUnitGraphPositionByThrowStmt(this.throwStmt);
			}else {
				return new Signaler(this.stackFrameMethod).getUnitGraphPositionByUnit(this.unHandledUnits.get(0));// new IllegalArgumentException("The current stack frame is not a signaler: ");
			}
		}
		
		/**
		 * 
		 * @return how many times handled turned into true;
		 */
		public int computeUnHandledUnits() {
			int count = 0;
			Body body = stackFrameMethod.retrieveActiveBody();
			ExceptionalUnitGraph ug = new ExceptionalUnitGraph(body);
			for(Unit unit : targetUnits) {
//				for(Unit succ : ug.getExceptionalSuccsOf(unit)){
//					
//				}
				boolean handled = false;
				for(ExceptionDest dest : ug.getExceptionDests(unit)) {
					Trap trap = dest.getTrap();
					if(trap!=null) {
//						System.out.println(body);
//						System.out.println(trap.getBeginUnit().toString());
//						System.out.println(trap.getEndUnit().toString());
//						System.out.println(trap.getHandlerUnit().toString());
						SootClass trapException = trap.getException();
						if(ThrowClause.isSubclass(thrownException, trapException)) {
							//DEBUG
//							System.out.println("Trapping Exception: "+trapException.getName());
//							System.out.println("Throwing Exception: "+this.thrownException.getName());
//							System.out.println();
							//DEBUG
							handled = true;
							count++;
							break;//if handled, no need to check more.
						}
					}
				}
				if(!handled) {
					this.unHandledUnits.add(unit);
				}
			}
			return count;
		}
		public boolean handledPassedException() {
			return this.unHandledUnits.size()==0?true:false;
		}
	}