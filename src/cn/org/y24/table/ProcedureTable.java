package cn.org.y24.table;

import cn.org.y24.entity.Procedure;
import cn.org.y24.interfaces.BaseTable;

import java.util.LinkedList;
import java.util.List;

public class ProcedureTable extends BaseTable<Procedure> {
    private final List<Procedure> pool = new LinkedList<>();


    @Override
    public List<Procedure> getPool() {
        return pool;
    }

    @Override
    public Procedure get(String target) {
        for (Procedure proc : pool) {
            if (proc.getName().equals(target))
                return proc;
        }
        return null;
    }

    @Override
    public void init() {
        pool.clear();
        pool.add(new Procedure("main", 0));
    }
}
