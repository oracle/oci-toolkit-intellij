package com.oracle.oci.intellij.ui.common;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;

import org.jetbrains.annotations.NotNull;

import com.intellij.ui.DocumentAdapter;


/**
 * Top-level controller for UI-Model bindings for beans.
 * 
 * @author cbateman
 *
 */
public class BindingContext {

  public <T> void bind(JTextField textField, T bean, String propertyName) {
    // textField.add
  }

  public static class BeanPropertyObservable<BEANTYPE> extends Observable<BEANTYPE, BeanPropertyChangeEvent>
  {
    private BEANTYPE bean;
    private String propertyName;

    public BeanPropertyObservable(BEANTYPE bean, String propertyName) {
      this.bean = bean;
      this.propertyName = propertyName;
    }
    
    @Override
    public void observe() {
      Class<BEANTYPE> clazz = (Class<BEANTYPE>) this.bean.getClass();
      try {
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        List<PropertyDescriptor> pds = Stream.of(propertyDescriptors)
          .filter(p -> p.getName().equals("addPropertyChangeListener")).collect(Collectors.toList());
        // TODO;
      } catch (IntrospectionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    @Override
    public void unobserve() {
      // TODO Auto-generated method stub
      
    }
    
  }
  
  public static class BeanPropertyChangeEvent extends ObserverEvent {
    
  }
  public static class JTextFieldDocumentChangeEvent extends ObserverEvent {

    private @NotNull DocumentEvent e;

    public JTextFieldDocumentChangeEvent(@NotNull DocumentEvent e) {
      this.e = e;

    }

    public String getValue() {
      try {
        return e.getDocument().getText(0, e.getLength());
      } catch (BadLocationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return null;
      }
    }
  }

  public static class JTextFieldObservable
    extends Observable<JTextField, JTextFieldDocumentChangeEvent> {
    private JTextField textField;

    public JTextFieldObservable(JTextField textField) {
      this.textField = textField;
    }

    @Override
    public void observe() {
      this.textField.getDocument().addDocumentListener(new DocumentAdapter() {
        @Override
        protected void textChanged(@NotNull DocumentEvent e) {
          JTextFieldDocumentChangeEvent event =
            new JTextFieldDocumentChangeEvent(e);
          observers.forEach(o -> o.receiveEvent(event));
        }
      });
    }

    @Override
    public void unobserve() {
      // TODO Auto-generated method stub

    }

  }

  public static abstract class Observable<ObservedType, E extends ObserverEvent> {
    protected final CopyOnWriteArrayList<Observer<E>> observers =
      new CopyOnWriteArrayList<>();

    public abstract void observe();

    public abstract void unobserve();

    public void addObserver(Observer<E> observer) {
      observers.addIfAbsent(observer);
    }

    public void removeObserver(Observer<E> observer) {
      observers.remove(observer);
    }

    public void eventOccurred(E event) {
      observers.forEach(o -> o.receiveEvent(event));
    }
  }

  public static class TextChangeObserverEvent extends ObserverEvent {

  }

  public static abstract class ObserverEvent {

  }

  public static abstract class Observer<EVENTTYPE extends ObserverEvent> {
    public abstract void receiveEvent(EVENTTYPE event);
  }
}
