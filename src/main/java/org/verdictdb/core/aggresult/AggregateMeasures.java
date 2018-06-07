package org.verdictdb.core.aggresult;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.verdictdb.exception.ValueException;

public class AggregateMeasures {

  List<String> attributeNames;

  List<Object> attributeValues;   // will be numeric types
  
  public AggregateMeasures() {
    this.attributeNames = new ArrayList<>();
    this.attributeValues = new ArrayList<>();
  }

  public AggregateMeasures(List<String> attributeNames, List<Object> attributeValues) {
    this.attributeNames = attributeNames;
    this.attributeValues = attributeValues;
  }
  
  public void addMeasure(String attributeName, Object attributeValue) {
    attributeNames.add(attributeName);
    attributeValues.add(attributeValue);
  }
  
  public int getIndexOfAttributeName(String attributeName) throws ValueException {
    int index = attributeNames.indexOf(attributeName);
    if (index == -1) {
      throw new ValueException(attributeName + " does not appear in " + attributeNames);
    }
    return index;
  }
  
  public Object getAttributeValueAt(int index) {
    return attributeValues.get(index);
  }
  
  public Object getAttributeValue(String attributeName) throws ValueException {
    return attributeValues.get(getIndexOfAttributeName(attributeName));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((attributeNames == null) ? 0 : attributeNames.hashCode());
    result = prime * result + ((attributeValues == null) ? 0 : attributeValues.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AggregateMeasures other = (AggregateMeasures) obj;
    if (attributeNames == null) {
      if (other.attributeNames != null)
        return false;
    } else if (!attributeNames.equals(other.attributeNames))
      return false;
    if (attributeValues == null) {
      if (other.attributeValues != null)
        return false;
    } else if (!attributeValues.equals(other.attributeValues))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}