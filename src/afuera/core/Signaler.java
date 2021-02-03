package afuera.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.IfStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.ThrowStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;

//TODO: By definition, Signaler should subclass StackFrame
public class Signaler {
	public SootMethod sm = null;
	public List<ThrowStmt> throwStmts = new ArrayList<ThrowStmt>();
	public Signaler(SootMethod sm) {
		this.sm = sm;
		this.findThrowStmts();
	}
	
	public boolean isSignaler() {
		Body body = sm.retrieveActiveBody();
		ExceptionalUnitGraph ug = new ExceptionalUnitGraph(body);
		Iterator<Unit> iter = ug.iterator();
		while(iter.hasNext()) {
			Unit u = (Unit) iter.next();
			if(new ThrowClause(u).hasThrownInstanceOfRuntimeException()) {
				return true;
			}
		}
		return false;
	}
	
	public void findThrowStmts() {
		Body body = sm.retrieveActiveBody();
		ExceptionalUnitGraph ug = new ExceptionalUnitGraph(body);
		Iterator<Unit> iter = ug.iterator();
		while(iter.hasNext()) {
			Unit u = (Unit) iter.next();
			if(new ThrowClause(u).hasThrownInstanceOfRuntimeException()) {
				this.throwStmts.add((ThrowStmt) u);
			}
		}
	}
	
	public List<ThrowStmt> getThrowStmts(){
		return this.throwStmts;
	}
	
	public int getUnitGraphPositionByThrowStmt(ThrowStmt throwStmt) {
		Body body = sm.retrieveActiveBody();
		ExceptionalUnitGraph ug = new ExceptionalUnitGraph(body);
		Iterator<Unit> iter = ug.iterator();
		int cur = 0;
		while(iter.hasNext()) {
			Unit u = (Unit) iter.next();
			cur++;
			if(u.equals(throwStmt)) {
				return cur;
			}
		}
		throw new IllegalStateException("Did not find given throw clause: "+throwStmt.toString()+" in "+sm.getSignature());
	}
	
	public int getUnitGraphPositionByUnit(Unit unit) {
		Body body = sm.retrieveActiveBody();
		ExceptionalUnitGraph ug = new ExceptionalUnitGraph(body);
		Iterator<Unit> iter = ug.iterator();
		int cur = 0;
		while(iter.hasNext()) {
			Unit u = (Unit) iter.next();
			cur++;
			if(u.equals(unit)) {
				return cur;
			}
		}
		throw new IllegalStateException("Did not find given unit: "+unit.toString()+" in "+sm.getSignature());
	}
	
	public Unit getThrowStmtByUnitGraphPosition(int pos) {
		Body body = sm.getActiveBody();
		ExceptionalUnitGraph ug = new ExceptionalUnitGraph(body);
		Iterator<Unit> iter = ug.iterator();
		int cur = 0;
		while(iter.hasNext()) {
			Unit u = (Unit) iter.next();
			cur++;
			if(cur == pos) {
//				if(new ThrowClause(u).hasThrownInstanceOfRuntimeException()) {
//					return (ThrowStmt) u;
//				}else {
//					throw new IllegalStateException("This is not a throw clause for RuntimeException!");
//				}
				return u;
			}
		}
		throw new IllegalStateException("Did not find throw clause with given position: "+pos+" in "+sm.getSignature());
	}
	/**
	 * Should be obsolete
	 * @param u
	 * @return
	 */
	public IfStmt getIfStmtPrecedingThrowClause(Unit u) {
		//TODO
		Body body = sm.getActiveBody();
		ExceptionalUnitGraph ug = new ExceptionalUnitGraph(body);
		List<Unit> preds = ug.getUnexceptionalPredsOf(u);
		for(Unit pre: preds) {
			if(pre instanceof IfStmt) {
				return (IfStmt) pre;
			}else if(pre instanceof LookupSwitchStmt){
				//TODO
			}else {
				IfStmt sub = getIfStmtPrecedingThrowClause(pre);
				if(sub !=null) {
					return sub;
				}else {
					continue;
				}
			}
		}
		return null;
	}
	
	public List<ValueBox> getValueBoxesPrecedingThrowClause(Unit u) {
		//TODO
		Body body = sm.getActiveBody();
		ExceptionalUnitGraph ug = new ExceptionalUnitGraph(body);
		List<Unit> preds = ug.getUnexceptionalPredsOf(u);
		for(Unit pre: preds) {
			if(pre instanceof IfStmt) {
				IfStmt ifStmt = (IfStmt) pre;
				return ifStmt.getCondition().getUseBoxes();
			}else if(pre instanceof LookupSwitchStmt){
				LookupSwitchStmt lss = (LookupSwitchStmt) pre;
				List<ValueBox> box = new ArrayList<ValueBox>();
				box.add(lss.getKeyBox());
				return box;
			}else {
				List<ValueBox> sub = getValueBoxesPrecedingThrowClause(pre);
				if(sub !=null) {
					return sub;
				}else {
					continue;
				}
			}
		}
		return null;
	}
	
	public String packageName() {
		String name = this.sm.getDeclaringClass().getName();
		return name.substring(0, name.lastIndexOf("."));
	}
}
