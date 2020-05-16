package cn.org.y24.core;

import cn.org.y24.interfaces.IAnalyzer;

public class SemanticAnalyzer implements IAnalyzer {
    final String filename;

    public SemanticAnalyzer(String filename) {
        this.filename = filename;
    }

    @Override
    public void analyze() {

    }
}
