package org.verdictdb.core.logical_query;

import java.util.Arrays;
import java.util.List;


public class ColumnOp implements AbstractColumn {
    
    /**
     * opType must be one of the following:
     * <ol>
     * <li>*</li>
     * <li>sum</li>
     * <li>count</li>
     * <li>avg</li>
     * <li>add</li>
     * <li>multiply</li>
     * <li>subtract</li>
     * <li>divide</li>
     * <li>and</li>
     * <li>or</li>
     * <li>=</li>
     * <li><></li>
     * <li>casewhenelse</li>
     * <li>not null</li>
     * </ol>
     */
    String opType;
    
    List<AbstractColumn> operands;

    public ColumnOp(String opType) {
        this.opType = opType;
    }
    
    public ColumnOp(String opType, AbstractColumn operand) {
        this.operands = Arrays.asList(operand);
        this.opType = opType;
    }
    
    public ColumnOp(String opType, List<AbstractColumn> operands) {
        this.operands = operands;
        this.opType = opType;
    }

    public AbstractColumn getOperand() {
        return getOperand(0);
    }
    
    public AbstractColumn getOperand(int i) {
        return operands.get(i);
    }

    public void setOperand(List<AbstractColumn> operands) {
        this.operands = operands;
    }

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }
    
    public static ColumnOp and(AbstractColumn predicate1, AbstractColumn predicate2) {
        return new ColumnOp("and", Arrays.asList(predicate1, predicate2));
    }
    
    public static ColumnOp equal(AbstractColumn column1, AbstractColumn column2) {
        return new ColumnOp("=", Arrays.asList(column1, column2));
    }
    
    public static ColumnOp notequal(AbstractColumn column1, AbstractColumn column2) {
        return new ColumnOp("<>", Arrays.asList(column1, column2));
    }
    
    public static ColumnOp multiply(AbstractColumn column1, AbstractColumn column2) {
        return new ColumnOp("=", Arrays.asList(column1, column2));
    }
    
    public static ColumnOp casewhenelse(AbstractColumn columnIf, AbstractColumn condition, AbstractColumn columnElse) {
        return new ColumnOp("casewhenelse", Arrays.asList(columnIf, condition, columnElse));
    }
    
    public static ColumnOp notnull(AbstractColumn column1) {
        return new ColumnOp("notnull", Arrays.asList(column1));
    }

}