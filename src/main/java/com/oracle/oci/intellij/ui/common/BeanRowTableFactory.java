package com.oracle.oci.intellij.ui.common;

import java.util.Arrays;
import java.util.List;

import javax.swing.JTable;

import com.intellij.codeInsight.hint.ParameterInfoControllerBase.Model;

public class BeanRowTableFactory
{
  public static class Builder<BEANTYPE> {
    private BeanRowTableModel<BEANTYPE> model;
    private List<String> columns;
    private Class<BEANTYPE> beanClass;
    
    public Builder model(BeanRowTableModel<BEANTYPE> model) {
      this.model = model;
      return this;
    }
    
    public Builder columns(String...columns) {
      this.columns = Arrays.asList(columns);
      return this;
    }
    
    public Builder beanClass(Class<BEANTYPE> beanClass) {
      this.beanClass = beanClass;
      return this;
    }
    
    public BeanRowTable<BEANTYPE> build() {
      if (this.model == null) {
        this.model = new BeanRowTableModel<>(beanClass, columns);
      }
      BeanRowTable table = new BeanRowTable(model);
      
      return table;
    }
  }
  
  public static class BeanRowTable<BEANTYPE> extends JTable {

    private BeanRowTableModel<BEANTYPE> model;

    public BeanRowTable(BeanRowTableModel<BEANTYPE> model) {
      super(model);
      this.model = model;
    }
    
    public void setRows(List<BEANTYPE> rows) {
      model.setBeans(rows);
      this.validate();
    }
  }
  public static <BEANTYPE> Builder<BEANTYPE>  create() {
    return new Builder<BEANTYPE>();
  }
}
