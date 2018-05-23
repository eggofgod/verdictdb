package org.verdictdb.core.rewriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.verdictdb.core.logical_query.AbstractColumn;
import org.verdictdb.core.logical_query.AbstractRelation;
import org.verdictdb.core.logical_query.BaseColumn;
import org.verdictdb.core.logical_query.BaseTable;
import org.verdictdb.core.logical_query.ColumnOp;
import org.verdictdb.core.logical_query.ConstantColumn;
import org.verdictdb.core.logical_query.SelectQueryOp;
import org.verdictdb.exception.UnexpectedTypeException;

/**
 * AQP rewriter for partitioned tables. A sampling probability column must exist.
 * 
 * @author Yongjoo Park
 *
 */
public class ScrambleRewriter {
    
    ScrambleMeta scrambleMeta;
    
    public ScrambleRewriter(ScrambleMeta scrambleMeta) {
        this.scrambleMeta = scrambleMeta;
    }
    
    /**
     * Current Limitations:
     * 1. Only handles the query with a single aggregate (sub)query
     * 2. Only handles the query that the first select list is the aggregate query.
     * 
     * @param relation
     * @return
     * @throws UnexpectedTypeException 
     */
    public List<AbstractRelation> rewrite(AbstractRelation relation) throws UnexpectedTypeException {
//        int partitionCount = derivePartitionCount(relation);
        List<AbstractRelation> rewritten = new ArrayList<AbstractRelation>();
        
        AbstractRelation rewrittenWithoutPartition = rewriteNotIncludingMaterialization(relation);
        List<AbstractColumn> partitionPredicates = generatePartitionPredicates(relation);
        
        for (int k = 0; k < partitionPredicates.size(); k++) {
            AbstractColumn partitionPredicate = partitionPredicates.get(k);
            SelectQueryOp rewritten_k = (SelectQueryOp) rewrittenWithoutPartition;
            rewritten_k.addFilterByAnd(partitionPredicate);
            rewritten.add(rewritten_k);
        }
        
//        for (int k = 0; k < partitionCount; k++) {
//            rewritten.add(rewriteNotIncludingMaterialization(relation, k));
//        }
        
        return rewritten;
    }
    
    /**
     * Rewrite a given query into AQP-enabled form. The rewritten queries do not include any "create table ..."
     * parts.
     * @param relation
     * @param partitionNumber
     * @return
     * @throws UnexpectedTypeException
     */
    AbstractRelation rewriteNotIncludingMaterialization(AbstractRelation relation) 
            throws UnexpectedTypeException {
        // must be some select query.
        if (!(relation instanceof SelectQueryOp)) {
            throw new UnexpectedTypeException("Not implemented yet.");
        }
        
        SelectQueryOp rewritten = new SelectQueryOp();
        SelectQueryOp sel = (SelectQueryOp) relation;
        List<AbstractColumn> selectList = sel.getSelectList();
        List<AbstractColumn> modifiedSelectList = new ArrayList<>();
        for (AbstractColumn c : selectList) {
            if (c instanceof BaseColumn) {
                modifiedSelectList.add(c);
            }
            else if (c instanceof ColumnOp) {
                ColumnOp col = (ColumnOp) c;
                if (col.getOpType().equals("sum")) {
                    AbstractColumn op = col.getOperand();
                    AbstractColumn probCol = deriveInclusionProbabilityColumn(relation);
                    ColumnOp newCol = new ColumnOp("sum",
                                        new ColumnOp("divide", Arrays.asList(op, probCol)));
                    modifiedSelectList.add(newCol);
                }
                else if (col.getOpType().equals("count")) {
                    AbstractColumn probCol = deriveInclusionProbabilityColumn(relation);
                    ColumnOp newCol = new ColumnOp("sum",
                                        new ColumnOp("divide", Arrays.asList(ConstantColumn.valueOf(1), probCol)));
                    modifiedSelectList.add(newCol);
                }
                else if (col.getOpType().equals("avg")) {
                    AbstractColumn op = col.getOperand();
                    AbstractColumn probCol = deriveInclusionProbabilityColumn(relation);
                    // sum of attribute values
                    ColumnOp newCol1 = new ColumnOp("sum",
                                         new ColumnOp("divide", Arrays.asList(op, probCol)));
                    // number of attribute values
                    ColumnOp oneIfNotNull = ColumnOp.casewhenelse(
                                                ConstantColumn.valueOf(1),
                                                ColumnOp.notnull(op),
                                                ConstantColumn.valueOf(0));
                    ColumnOp newCol2 = new ColumnOp("sum",
                                         new ColumnOp("divide", Arrays.asList(oneIfNotNull, probCol)));
                    modifiedSelectList.add(newCol1);
                    modifiedSelectList.add(newCol2);
                }
                else {
                    throw new UnexpectedTypeException("Not implemented yet.");
                }
            }
            else {
                throw new UnexpectedTypeException("Unexpected column type: " + c.getClass().toString());
            }
        }
        
        for (AbstractColumn c : modifiedSelectList) {
            rewritten.addSelectItem(c);
        }
        for (AbstractRelation r : sel.getFromList()) {
            rewritten.addTableSource(r);
        }
        
        if (sel.getFilter().isPresent()) {
            rewritten.addFilterByAnd(sel.getFilter().get());
        }
        
        return rewritten;
    }
    
    List<AbstractColumn> generatePartitionPredicates(AbstractRelation relation) throws UnexpectedTypeException {
        if (!(relation instanceof SelectQueryOp)) {
            throw new UnexpectedTypeException("Unexpected relation type: " + relation.getClass().toString());
        }
        
        SelectQueryOp sel = (SelectQueryOp) relation;
        List<AbstractRelation> fromList = sel.getFromList();
        for (AbstractRelation r : fromList) {
            Optional<AbstractColumn> c = partitionColumnOfSource(r);
            if (!c.isPresent()) {
                continue;
            }
            
            List<String> partitionAttributeValues = partitionAttributeValuesOfSource(r);
        }
    }
    
//    ColumnOp derivePartitionFilter(AbstractRelation relation, int partitionNumber) throws UnexpectedTypeException {
//        AbstractColumn partCol = derivePartitionColumn(relation);
//        String partitionValue = derivePartitionValue(relation, partitionNumber);
//        return ColumnOp.equal(partCol, ConstantColumn.valueOf(partitionValue));
//    }
    
    Optional<AbstractColumn> partitionColumnOfSource(AbstractRelation source) throws UnexpectedTypeException {
        if (source instanceof BaseTable) {
            BaseTable base = (BaseTable) source;
            String colName = scrambleMeta.getPartitionColumn(base.getSchemaName(), base.getTableName());
            String aliasName = base.getTableSourceAlias();
            BaseColumn col = new BaseColumn(aliasName, colName);
            return Optional.<AbstractColumn>of(col);
        }
        else {
            throw new UnexpectedTypeException("Not implemented yet.");
        }
    }
    
//    int derivePartitionCount(AbstractRelation relation) throws UnexpectedTypeException {
//        if (!(relation instanceof SelectQueryOp)) {
//            throw new UnexpectedTypeException("Unexpected relation type: " + relation.getClass().toString());
//        }
//        // TODO: partition count should be modified to handle the joins of multiple tables.
//        SelectQueryOp sel = (SelectQueryOp) relation;
//        List<AbstractRelation> fromList = sel.getFromList();
//        int partCount = 0;
//        for (AbstractRelation r : fromList) {
//            int c = partitionCountOfSource(r);
//            if (partCount == 0) {
//                partCount = c;
//            }
//            else {
//                partCount = partCount * c;
//            }
//        }
//        return partCount;
//    }
    
//    int partitionCountOfSource(AbstractRelation source) throws UnexpectedTypeException {
//        if (source instanceof BaseTable) {
//            BaseTable tab = (BaseTable) source;
//            return scrambleMeta.getPartitionCount(tab.getSchemaName(), tab.getTableName());
//        }
//        else {
//            throw new UnexpectedTypeException("Not implemented yet.");
//        }
//    }
    
//    /**
//     * Obtains the inclusion probability expression needed for computing the aggregates within the given
//     * relation.
//     * 
//     * @param relation
//     * @return
//     * @throws UnexpectedTypeException
//     */
//    AbstractColumn deriveInclusionProbabilityColumn(AbstractRelation relation) throws UnexpectedTypeException {
//        if (!(relation instanceof SelectQueryOp)) {
//            throw new UnexpectedTypeException("Unexpected relation type: " + relation.getClass().toString());
//        }
//        
//        SelectQueryOp sel = (SelectQueryOp) relation;
//        List<AbstractRelation> fromList = sel.getFromList();
//        AbstractColumn incProbCol = null;
//        for (AbstractRelation r : fromList) {
//            Optional<AbstractColumn> c = inclusionProbabilityColumnOfSource(r);
//            if (!c.isPresent()) {
//                continue;
//            }
//            if (incProbCol == null) {
//                incProbCol = c.get();
//            }
//            else {
//                incProbCol = new ColumnOp("multiply", Arrays.asList(incProbCol, c.get()));
//            }
//        }
//        return incProbCol;
//    }
    
    AbstractColumn inclusionProbabilityColumnOfSource(AbstractRelation source) throws UnexpectedTypeException {
        if (source instanceof BaseTable) {
            BaseTable base = (BaseTable) source;
            String colName = scrambleMeta.getInclusionProbabilityColumn(base.getSchemaName(), base.getTableName());
            String aliasName = base.getTableSourceAlias();
            BaseColumn col = new BaseColumn(aliasName, colName);
            return col;
        }
        else {
            throw new UnexpectedTypeException("Derived tables cannot be used."); 
        }
    }

}