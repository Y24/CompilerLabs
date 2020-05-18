package cn.org.y24.interfaces;

import java.util.List;
import java.util.function.Consumer;

public abstract class BaseTable<T> {
    protected abstract List<T> getPool();

    public void forEach(Consumer<? super T> action) {
        getPool().forEach(action);
    }

    public abstract T get(String target);

    public int size() {
        return getPool().size();
    }

    public abstract void init();

    public boolean add(T item) {
        if (contains(item))
            return false;
        getPool().add(item);
        return true;
    }

    public void update(T item) {
        getPool().remove(item);
        getPool().add(item);
    }

    public boolean remove(T item) {
        return getPool().remove(item);
    }

    public boolean contains(T item) {
        return getPool().contains(item);
    }

}
