package com.oracle.oci.intellij.ui.common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class WizardContext {

  protected final PropertyChangeSupport pcs;
  
  public WizardContext() {
    pcs = new PropertyChangeSupport(this);
  }
  
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }
  public void addPropertyChangeListener(String propName, PropertyChangeListener propListener) {
    pcs.addPropertyChangeListener(propName, propListener);
  }
  
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(propertyName, listener);
  }

}
