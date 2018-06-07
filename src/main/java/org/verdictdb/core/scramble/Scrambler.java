package org.verdictdb.core.scramble;

import org.verdictdb.core.rewriter.ScrambleMetaForTable;

public class Scrambler {
  
  String originalSchemaName;
  
  String originalTableName;
  
  String scrambledSchemaName;
  
  String scrambledTableName;
  
  int aggregationBlockCount;
  
  // Configuration parameters
  static String aggregationBlockColumn = "verdictAggBlock";
  
//  static String inclusionProbabilityColumn = "verdictIncProb";
//  
//  static String inclusionProbabilityBlockDifferenceColumn = "verdictIncProbBlockDiff";
  
  static String subsampleColumn = "verdictSubsampleId";
  
  static String tierColumn = "verdicttier";
  
  public Scrambler(
      String originalSchemaName, String originalTableName,
      String scrambledSchemaName, String scrambledTableName,
      int aggregationBlockCount) {
    this.originalSchemaName = originalSchemaName;
    this.originalTableName = originalTableName;
    this.scrambledSchemaName = scrambledSchemaName;
    this.scrambledTableName = scrambledTableName;
    this.aggregationBlockCount = aggregationBlockCount;
  }
  
  ScrambleMetaForTable generateMeta() {
    ScrambleMetaForTable meta = new ScrambleMetaForTable();
    meta.setSchemaName(scrambledSchemaName);
    meta.setTableName(scrambledTableName);
    meta.setAggregationBlockCount(aggregationBlockCount);
    meta.setAggregationBlockColumn(aggregationBlockColumn);
//    meta.setInclusionProbabilityColumn(inclusionProbabilityColumn);
//    meta.setInclusionProbabilityBlockDifferenceColumn(inclusionProbabilityBlockDifferenceColumn);
    meta.setSubsampleColumn(subsampleColumn);
    meta.setTierColumn(tierColumn);
    return meta;
  }

  public static String getAggregationBlockColumn() {
    return aggregationBlockColumn;
  }

//  public static String getInclusionProbabilityColumn() {
//    return inclusionProbabilityColumn;
//  }

//  public static String getInclusionProbabilityBlockDifferenceColumn() {
//    return inclusionProbabilityBlockDifferenceColumn;
//  }

  public static String getSubsampleColumn() {
    return subsampleColumn;
  }
  
  public static String getTierColumn() {
    return tierColumn;
  }

}