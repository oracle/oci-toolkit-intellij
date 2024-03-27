package com.oracle.oci.intellij.appStackGroup.provider;

import com.oracle.bmc.resourcemanager.model.Stack.LifecycleState;

public class AppStackLabelProvider {
  public String getText(Object field, String columnName) {
    if (field == null) {
      throw new IllegalArgumentException("field can't be null");
    }
    if (columnName == null) {
      throw new IllegalArgumentException("columnName can't be null");
    }
    switch(columnName) {
    case "id":
      return field.toString();
    case "displayname":
      return field.toString();
    case "description":
      return field.toString();
    case "timeCreated":
      return field.toString();  // TODO field instanceof(Date)
    case "lifecylestate":
      LifecycleState state = (LifecycleState) field;
      return state.getValue();
    case "terraformversion":
      return field.toString();
    default:
      return "Unknown_Field";
    }
    
  }
  
  public Object getIcon() {
    return null;
  }
}
