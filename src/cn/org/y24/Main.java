package cn.org.y24;

import cn.org.y24.interfaces.ICompiler;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println(String.format("Expect only one argument,but actually: %d", args.length));
            System.exit(1);
        }
        ICompiler compiler = new SimpleCompiler(args[0]);
        compiler.compile();
    }
}
