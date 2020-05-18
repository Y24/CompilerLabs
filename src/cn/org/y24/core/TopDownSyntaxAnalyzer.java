package cn.org.y24.core;

import cn.org.y24.entity.Procedure;
import cn.org.y24.entity.Token;
import cn.org.y24.entity.Variable;
import cn.org.y24.enums.FileType;
import cn.org.y24.interfaces.IAnalyzer;
import cn.org.y24.table.SymbolTable;
import cn.org.y24.util.FileProcessor;
import cn.org.y24.util.SyntaxAnalyzerRecorder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.org.y24.util.StringUtil.*;

public class TopDownSyntaxAnalyzer implements IAnalyzer {
    private final Map<Integer, String> infoList = new HashMap<>(30) {{
        this.put(1, "Miss keyword: begin");
        this.put(2, "Miss keyword: end");
        this.put(3, "Miss keyword: integer");
        this.put(4, "Miss keyword: if");
        this.put(5, "Miss keyword: then");
        this.put(6, "Miss keyword: else");
        this.put(7, "Miss keyword: function");
        this.put(8, "Miss keyword: read");
        this.put(9, "Miss keyword: write");
        this.put(10, "Miss an identifier");
        this.put(11, "Miss a constant");
        this.put(12, "Miss boolean operator");
        this.put(18, "Miss a -");
        this.put(19, "Miss a *");
        this.put(20, "Miss a :=");
        this.put(21, "Miss a (");
        this.put(22, "Miss a )");
        this.put(23, "Miss a ;");

    }};
    final String filename;
    final List<String> errorMessage = new LinkedList<>();
    final SymbolTable symbolTable = new SymbolTable();
    final SyntaxAnalyzerRecorder recorder = new SyntaxAnalyzerRecorder();
    Token[] tokens;


    public TopDownSyntaxAnalyzer(String filename) {
        this.filename = filename;
    }

    /* (1) 缺少符号错;
          (2) 符号匹配错;
          (3) 符号无定义或重复定义。
       */
    @Override
    public void analyze() throws IOException {
        // check if error occurs in lexical analyse.
        if (hasLexicalError(filename)) {
            System.out.println("Syntax analyse cannot continue because of lexical error!");
            return;
        }
        init();
        program();
        end();
    }


    private void init() throws IOException {
        // clean work.
        errorMessage.clear();
        symbolTable.getProcedureTable().init();
        symbolTable.getVariableTable().init();
        recorder.init();
        // init tokens and index
        final BufferedReader reader = FileProcessor.getInstance().bufferedReadFile(filename + FileProcessor.getSuffix(FileType.lexicalFile));
        final BufferedWriter writer = FileProcessor.getInstance().bufferedWriteFile(filename + FileProcessor.getSuffix(FileType.syntaxFile), false);
        String line;
        List<Token> list = new ArrayList<>();
        do {
            line = reader.readLine();
            if (line == null) {
                break;
            }
            writer.write(line);
            writer.newLine();
            list.add(Token.from(line));
        } while (true);
        reader.close();
        writer.flush();
        writer.close();
        tokens = list.toArray(new Token[0]);
    }

    // program -> subProgram
    private void program() {
        subProgram();
    }

    // subProgram -> begin varStats ; execStats end
    private void subProgram() {
        tryMove(1);
        symbolTable.getProcedureTable().get("main").setFirstVarAddress(0);
        descriptionStats();
        AtomicInteger index = new AtomicInteger();
        symbolTable.getVariableTable().forEach(variable -> {
            if (variable.getProc().equals("main"))
                index.set(variable.getAddress());
        });
        symbolTable.getProcedureTable().get("main").setLastVarAddress(index.get());
        execStats();
        tryMove(2);

    }

    private void descriptionStats() {
        if (next().getKind() != 3) {
            symbolTable.getProcedureTable().get(recorder.varProc.peek()).setLastVarAddress(symbolTable.getVariableTable().size() - 1);
            return;
        }
        descriptionStat();
        tryMove(23);
        descriptionStats();
    }

    private void descriptionStat() {
        tryMove(3);
        final Token next = next();
        switch (next.getKind()) {
            case 10:
                varDescriptionStat();
                break;
            case 7:
                funcDescriptionStat();
                break;
            default:
                errorMessage.add(getErrorOutFormat("Miss identifier or function keyword", recorder.lineNumber));
        }
    }

    private void funcDescriptionStat() {
        tryMove(7);
        final Token funcName = next();
        if (funcName.getKind() == 10) {
            recorder.varProc.push(funcName.getValue());
            if (!symbolTable.getProcedureTable().add(new Procedure(funcName.getValue(), recorder.level)))
                errorMessage.add(getErrorOutFormat(String.format("Procedure: %s redefined", funcName.getValue()), recorder.lineNumber));
        }
        tryMove(10);
        tryMove(21);
        final Token argument = next();
        if (argument.getKind() == 10) {
            final int size = symbolTable.getVariableTable().size();
            symbolTable.getProcedureTable().get(recorder.varProc.peek()).setFirstVarAddress(size);
            symbolTable.getVariableTable()
                    .add(new Variable(
                            argument.getValue(),
                            recorder.varProc.peek(),
                            1,
                            recorder.level + 1, size));
        }
        tryMove(10);
        tryMove(22);
        tryMove(23);
        funcBody();
    }

    private void funcBody() {
        tryMove(1);
        descriptionStats();
        execStats();
        tryMove(2);
    }

    private void varDescriptionStat() {
        final Token var = next();
        if (var.getKind() == 10) {
            if (!symbolTable.getVariableTable()
                    .add(new Variable(
                            var.getValue(),
                            recorder.varProc.peek(),
                            0,
                            recorder.level,
                            symbolTable.getVariableTable().size())))
                errorMessage.add(getErrorOutFormat(String.format("Variable: %s redefined", var.getValue()), recorder.lineNumber));
        }
        tryMove(10);
    }


    private void execStats() {
        final int kind = next().getKind();
        if (List.of(8, 9, 10).contains(kind)) {
            execStat();
            tryMove(23);
            execStats();
        } else if (kind == 4) {
            execStat();
            execStats();
        }
    }

    private void execStat() {
        final Token next = next();
        switch (next.getKind()) {
            case 8:
                readStat();
                break;
            case 9:
                writeStat();
                break;
            case 10:
                assignStat();
                break;
            case 4:
                ifStat();
                break;
            default:
                errorMessage.add(getErrorOutFormat("Miss identifier or keywords read,write,if", recorder.lineNumber));
        }
    }

    private void ifStat() {
        tryMove(4);
        conditionExp();
        tryMove(5);
        execStats();
        tryMove(6);
        execStats();
    }

    private void conditionExp() {
        arithmeticExp();
        final int kind = moveForward().getKind();
        if (kind > 17 || kind < 12)
            errorMessage.add(getErrorOutFormat("Miss boolean operator", recorder.lineNumber));
        arithmeticExp();
    }

    private void assignStat() {
        final Token next = next();
        if (!symbolTable.getVariableTable().contains(new Variable(next.getValue(), recorder.varProc.peek(), 0, recorder.level, 0))
                && !symbolTable.getProcedureTable().contains(new Procedure(next.getValue(), recorder.level - 1)))
            errorMessage.add(getErrorOutFormat("Undefined variable: " + next.getValue(), recorder.lineNumber));
        tryMove(10);
        tryMove(20);
        arithmeticExp();
    }

    private void arithmeticExp() {
        item();
        subArithmeticExp();
    }

    private void subArithmeticExp() {
        if (next().getKind() == 18) {
            moveForward();
            item();
            subArithmeticExp();
        }
    }


    private void item() {
        factor();
        subItem();
    }

    private void subItem() {
        if (next().getKind() == 19) {
            moveForward();
            factor();
            subItem();
        }
    }

    private void factor() {
        final Token token = moveForward();
        switch (token.getKind()) {
            case 11:
                break;
            case 10:
                if (next().getKind() == 21) {
                    boolean result = false;
                    for (int i = recorder.level; i >= 0; i--)
                        if (symbolTable.getProcedureTable().contains(new Procedure(token.getValue(), i))) {
                            result = true;
                            break;
                        }
                    if (!result)
                        errorMessage.add(getErrorOutFormat("Undefined procedure: " + token.getValue(), recorder.lineNumber));
                    moveForward();
                    arithmeticExp();
                    tryMove(22);
                } else {
                    boolean result = false;
                    for (int i = recorder.level; i >= 0; i--)
                        if (symbolTable.getVariableTable().contains(new Variable(token.getValue(), recorder.varProc.get(i), 0, i, 0))) {
                            result = true;
                            break;
                        }
                    if (!result)
                        errorMessage.add(getErrorOutFormat("Undefined variable: " + token.getValue(), recorder.lineNumber));

                }
                break;
            default:
                errorMessage.add(getErrorOutFormat("Miss constant or identifier", recorder.lineNumber));
        }
    }


    private void writeStat() {
        tryMove(9);
        rwSubStat();
    }

    private void rwSubStat() {
        tryMove(21);
        final Token token = next();
        if (token.getKind() == 10 && !symbolTable.getVariableTable().contains(new Variable(token.getValue(), recorder.varProc.peek(), 0, recorder.level, 0)))
            errorMessage.add(getErrorOutFormat("Undefined variable: " + token.getValue(), recorder.lineNumber));
        tryMove(10);
        tryMove(22);
    }

    private void readStat() {
        tryMove(8);
        rwSubStat();
    }

    private void drop() {
        int kind;
        do {
            kind = tokens[recorder.index].getKind();
            recorder.index++;
            if (kind == 23 || kind == 25)
                continue;
            if (kind == 24) {
                recorder.lineNumber++;
                continue;
            }
            break;
        } while (true);

    }

    private Token next() {
        if (recorder.index == tokens.length - 1)
            unexpectedEnd();
        int temp = recorder.index + 1;
        while (tokens[temp].getKind() == 24)
            temp++;
        return tokens[temp];
    }

    private Token moveForward() {
        recorder.index++;
        while (tokens[recorder.index].getKind() == 24) {
            recorder.lineNumber++;
            recorder.index++;
        }
        return tokens[recorder.index];
    }

    private void tryMove(int kind) {
        final int next = next().getKind();
        if (next != kind)
            errorMessage.add(getErrorOutFormat(infoList.get(kind), recorder.lineNumber));
        else {
            record(kind);
            moveForward();
        }

    }

    private void record(int kind) {
        switch (kind) {
            case 25:
                unexpectedEnd();
                break;
            case 1:
                recorder.level++;
                break;
            case 2:
                recorder.level--;
                recorder.varProc.pop();
                break;
            default:
                break;
        }
    }

    private void cleanUpErrorMessage() {
        final Map<Integer, List<String>> sorted = new HashMap<>();
        errorMessage.forEach(s -> {
            final Matcher matcher =
                    Pattern.compile("\\*{3}LINE:(\\d+) {2}(.+)").matcher(s);
            if (matcher.matches()) {
                final int lineNumber = Integer.parseInt(matcher.group(1));
                if (!sorted.containsKey(lineNumber))
                    sorted.put(lineNumber, new LinkedList<>());
                sorted.get(lineNumber).add(s);
            }
        });
        errorMessage.clear();
        sorted.values().forEach(strings -> errorMessage.add(strings.get(0)));
    }

    private void end() {
        cleanUpErrorMessage();
        try {
            final BufferedWriter errorWriter = FileProcessor.getInstance().bufferedWriteFile(filename + FileProcessor.getSuffix(FileType.errorFile), true);
            final BufferedWriter varWriter = FileProcessor.getInstance().bufferedWriteFile(filename + FileProcessor.getSuffix(FileType.variableFile), false);
            final BufferedWriter procWriter = FileProcessor.getInstance().bufferedWriteFile(filename + FileProcessor.getSuffix(FileType.procedureFile), false);
            for (String s : errorMessage) {
                errorWriter.write(s);
                errorWriter.newLine();
            }
            varWriter.write(paddingRightAll(new String[]{"name", "proc", "kind", "type", "level", "address"}, Variable.counter));
            varWriter.newLine();
            symbolTable.getVariableTable().forEach(var -> {
                try {
                    varWriter.write(var.toString());
                    varWriter.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            procWriter.write(paddingRightAll(new String[]{"name", "type", "level", "firstVarAddress", "lastVarAddress"}, Procedure.counter));
            procWriter.newLine();
            symbolTable.getProcedureTable().forEach(proc -> {
                try {
                    procWriter.write(proc.toString());
                    procWriter.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            errorWriter.flush();
            errorWriter.close();
            varWriter.flush();
            varWriter.close();
            procWriter.flush();
            procWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Exception occurs when writing syntax error file.");
        }
    }

    private void unexpectedEnd() {
        end();
        System.err.println("Unexpected end.");
        System.exit(1);
    }

    private boolean hasLexicalError(String file) throws IOException {
        FileProcessor fileProcessor = FileProcessor.getInstance();
        final Reader reader = fileProcessor.bufferedReadFile(file + FileProcessor.getSuffix(FileType.errorFile));
        int errorFirstChar = reader.read();
        reader.close();
        return errorFirstChar != -1;
    }
}
