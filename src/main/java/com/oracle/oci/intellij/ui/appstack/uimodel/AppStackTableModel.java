package com.oracle.oci.intellij.ui.appstack.uimodel;

import javax.swing.table.DefaultTableModel;

public class AppStackTableModel extends DefaultTableModel {
  public static final String[] APPSTACK_COLUMN_NAMES =
    new String[] { "Display Name", "Description", "Terraform Version", "State", "Created" };
  /**
     * 
     */
    private static final long serialVersionUID = 1L;

  public AppStackTableModel(int rowCount) {
    super(APPSTACK_COLUMN_NAMES, rowCount);
  }

  @Override
    public boolean isCellEditable(int row, int column){
      return false;
    }

  @Override
  public String getColumnName(int index){
    return APPSTACK_COLUMN_NAMES[index];
  }

  @Override
  public Class<?> getColumnClass(int column){
    return String.class; //(column == 2) ? JLabel.class : String.class;
  }
}