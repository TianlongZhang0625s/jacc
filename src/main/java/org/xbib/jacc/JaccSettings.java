package org.xbib.jacc;

import org.xbib.jacc.grammar.Grammar;
import org.xbib.jacc.grammar.LALRMachine;
import org.xbib.jacc.grammar.LR0Machine;
import org.xbib.jacc.grammar.LookaheadMachine;
import org.xbib.jacc.grammar.SLRMachine;

/**
 *
 */
class JaccSettings {

    private MachineType machineType;
    private String packageName;
    private String className;
    private String interfaceName;
    private String extendsName;
    private String implementsNames;
    private String typeName;
    private String getToken;
    private String nextToken;
    private String getSemantic;
    private StringBuilder preTextBuffer;
    private StringBuilder postTextBuffer;

    JaccSettings() {
        this.machineType = MachineType.LALR1;
        this.preTextBuffer = new StringBuilder();
        this.postTextBuffer = new StringBuilder();
    }

    void setMachineType(MachineType machineType) {
        this.machineType = machineType;
    }

    LookaheadMachine makeMachine(Grammar grammar) {
        switch (machineType) {
            case LR0:
                return new LR0Machine(grammar);
            case SLR1:
                return new SLRMachine(grammar);
            case LALR1:
                return new LALRMachine(grammar);
            default:
                return null;
        }
    }

    String getPackageName() {
        return packageName;
    }

    void setPackageName(String s) {
        packageName = s;
    }

    String getClassName() {
        return className;
    }

    void setClassName(String s) {
        className = s;
    }

    String getInterfaceName() {
        return interfaceName;
    }

    void setInterfaceName(String s) {
        interfaceName = s;
    }

    String getExtendsName() {
        return extendsName;
    }

    void setExtendsName(String s) {
        extendsName = s;
    }

    void addImplementsNames(String s) {
        if (implementsNames != null) {
            implementsNames += ", " + s;
        } else {
            implementsNames = s;
        }
    }

    String getImplementsNames() {
        return implementsNames;
    }

    public void setImplementsNames(String s) {
        implementsNames = s;
    }

    String getTypeName() {
        return typeName;
    }

    void setTypeName(String s) {
        typeName = s;
    }

    String getGetToken() {
        return getToken;
    }

    void setGetToken(String s) {
        getToken = s;
    }

    String getNextToken() {
        return nextToken;
    }

    void setNextToken(String s) {
        nextToken = s;
    }

    String getGetSemantic() {
        return getSemantic;
    }

    void setGetSemantic(String s) {
        getSemantic = s;
    }

    void addPreText(String s) {
        preTextBuffer.append(s);
    }

    String getPreText() {
        return preTextBuffer.toString();
    }

    void addPostText(String s) {
        postTextBuffer.append(s);
    }

    String getPostText() {
        return postTextBuffer.toString();
    }

    void fillBlanks(String s) {
        if (getClassName() == null) {
            setClassName(s + "Parser");
        }
        if (getInterfaceName() == null) {
            setInterfaceName(s + "Tokens");
        }
        if (getTypeName() == null) {
            setTypeName("Object");
        }
        if (getInterfaceName() != null) {
            addImplementsNames(getInterfaceName());
        }
        if (getGetSemantic() == null) {
            setGetSemantic("lexer.getSemantic()");
        }
        if (getGetToken() == null) {
            setGetToken("lexer.getToken()");
        }
        if (getNextToken() == null) {
            setNextToken("lexer.nextToken()");
        }
    }
}
