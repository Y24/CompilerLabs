package cn.org.y24.table;

import cn.org.y24.entity.Variable;
import cn.org.y24.interfaces.BaseTable;

import java.util.LinkedList;
import java.util.List;

public class VariableTable extends BaseTable<Variable> {
    private final List<Variable> pool = new LinkedList<>();

    @Override
    public List<Variable> getPool() {
        return pool;
    }

    @Override
    public Variable get(String target) {
        for (Variable var : pool) {
            if (var.getName().equals(target))
                return var;
        }
        return null;
    }

    @Override
    public void init() {
        pool.clear();
    }
}
