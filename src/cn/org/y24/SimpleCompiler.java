package cn.org.y24;

import cn.org.y24.core.SimpleLexicalAnalyzer;
import cn.org.y24.core.TopDownSyntaxAnalyzer;
import cn.org.y24.interfaces.IAnalyzer;
import cn.org.y24.interfaces.ICompiler;
import cn.org.y24.table.SymbolTable;

import java.io.IOException;


public class SimpleCompiler implements ICompiler {
    final String filename;
    final IAnalyzer lexicalAnalyzer;
    final IAnalyzer syntaxAnalyzer;
    final SymbolTable symbolTable;

    public SimpleCompiler(String filename) {
        this.filename = filename;
        lexicalAnalyzer = new SimpleLexicalAnalyzer(filename);
        syntaxAnalyzer = new TopDownSyntaxAnalyzer(filename);
        symbolTable = new SymbolTable();
    }

    @Override
    public void compile() {
        try {
            lexicalAnalyzer.analyze();
            syntaxAnalyzer.analyze();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
