package cn.org.y24.table;

import cn.org.y24.entity.Procedure;
import cn.org.y24.entity.Variable;
import cn.org.y24.interfaces.BaseTable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SymbolTable {
    final BaseTable<Procedure> procedureTable = new ProcedureTable();
    final BaseTable<Variable> variableBaseTable = new VariableTable();

    public BaseTable<Procedure> getProcedureTable() {
        return procedureTable;
    }

    public BaseTable<Variable> getVariableTable() {
        return variableBaseTable;
    }
}
