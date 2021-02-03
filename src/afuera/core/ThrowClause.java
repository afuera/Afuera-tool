package afuera.core;
import soot.RefType;
import soot.SootClass;
import soot.Unit;
import soot.jimple.ThrowStmt;

public class ThrowClause {
	public Unit unit = null;
	
	public ThrowClause(Unit unit) {
		this.unit = unit;
	}
	
	public boolean hasThrownInstanceOfRuntimeException() {
		if(unit instanceof ThrowStmt) {
			ThrowStmt ts = (ThrowStmt) unit;
			RefType rf = (RefType) ts.getOp().getType();
			SootClass scThrow = rf.getSootClass();
			if(isSubclassOfRuntimeException(scThrow)) {
				return true;
			}
		}else {
			return false;
		}
		return false;
	}
	
	public static boolean isSubclassOfRuntimeException(SootClass sootclass){
		 do {
			if(sootclass.getName().equals("java.lang.RuntimeException") || sootclass.getName().equals("java.lang.Error")) {
				return true;
			}else {
				if(sootclass.hasSuperclass()) {
					sootclass = sootclass.getSuperclass();
				}else {
					break;
				}
			}
		 }while(true);
		 return false;
	}
	
	
	public static boolean isSubclass(SootClass baby, SootClass parent) {
		 do {
			if(baby.getName().equals(parent.getName())) {
				return true;
			}else {
				if(baby.hasSuperclass()) {
					baby = baby.getSuperclass();
				}else {
					break;
				}
			}
		 }while(true);
		 return false;
	}
	
	public SootClass getThrownType() {
		if(unit instanceof ThrowStmt) {
			ThrowStmt ts = (ThrowStmt) unit;
			RefType rf = (RefType) ts.getOp().getType();
			SootClass scThrow = rf.getSootClass();
			if(isSubclassOfRuntimeException(scThrow)) {
				return scThrow;
			}
		}else {
			return null;
		}
		return null;
	}
}
