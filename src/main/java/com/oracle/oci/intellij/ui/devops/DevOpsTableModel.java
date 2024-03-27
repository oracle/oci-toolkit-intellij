package com.oracle.oci.intellij.ui.devops;

import javax.swing.table.DefaultTableModel;

public class DevOpsTableModel extends DefaultTableModel {
  public static final String[] DEVOPS_COLUMN_NAMES =
    new String[] { "Display Name", "Description", "State", "Created" };
  /*
     * 
     */
    private static final long serialVersionUID = 1L;

  public DevOpsTableModel(int rowCount) {
    super(DEVOPS_COLUMN_NAMES, rowCount);
  }

  @Override
    public boolean isCellEditable(int row, int column){
      return false;
    }

  @Override
  public String getColumnName(int index){
    return DEVOPS_COLUMN_NAMES[index];
  }

  @Override
  public Class<?> getColumnClass(int column){
    return String.class; //(column == 2) ? JLabel.class : String.class;
  }
  
}
