package com.oracle.oci.intellij.ui.common;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTable;

import com.oracle.oci.intellij.ui.common.JComponentBuilder.JTableBuilder;


public class BeanRowTableFactory
{
  public static class Builder<BEANTYPE> {
    private BeanRowTableModel<BEANTYPE> model;
    private List<String> columns;
    private Class<BEANTYPE> beanClass;
    private JTableBuilder tableFactory;
    
    public Builder<BEANTYPE> tableFactory(JTableBuilder tableFactory) {
      this.tableFactory = tableFactory;
      return this;
    }
    public Builder<BEANTYPE> model(BeanRowTableModel<BEANTYPE> model) {
      this.model = model;
      return this;
    }
    
    public Builder<BEANTYPE> columns(String...columns) {
      this.columns = Arrays.asList(columns);
      return this;
    }
    
    public Builder<BEANTYPE> beanClass(Class<BEANTYPE> beanClass) {
      this.beanClass = beanClass;
      return this;
    }
    
    public BeanRowTable<BEANTYPE> build() {
      if (this.model == null) {
        this.model = new BeanRowTableModel<>(beanClass, columns);
      }
      BeanRowTable<BEANTYPE> table = null;
      if (this.tableFactory != null) {
        table = this.tableFactory.build(() -> new BeanRowTable<>(model));
      }
      else {
        table = new BeanRowTable<>(model);
      }
      return table;
    }
  }
  
  public static class BeanRowTable<BEANTYPE> extends JTable {

    private static final long serialVersionUID = -884168569139764278L;
    private BeanRowTableModel<BEANTYPE> model;

    public BeanRowTable(BeanRowTableModel<BEANTYPE> model) {
      super(model);
      this.model = model;
    }

    public void setRows(List<BEANTYPE> rows) {
      int oldSize = model.beans.size();
      int newSize = rows.size();
      model.fireTableRowsDeleted(0, oldSize);
      model.setBeans(rows);
      model.fireTableRowsInserted(0, newSize);
      this.validate();
    }
  }
  public static <BEANTYPE> Builder<BEANTYPE>  create() {
    return new Builder<BEANTYPE>();
  }
}
