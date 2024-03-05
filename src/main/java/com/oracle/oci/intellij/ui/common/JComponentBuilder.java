package com.oracle.oci.intellij.ui.common;

import java.util.Optional;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;



public class JComponentBuilder {
  
  
  public static class JLabelBuilder extends JComponentBuilderBase<JLabel> {
    public static JLabelBuilder create() {
      return new JLabelBuilder();
    }
    private Optional<Integer> alignHorizontal;
    private Optional<Integer> alignVertical;
    private Optional<String> text;
    
    public JLabelBuilder alignLeft() {
      this.alignHorizontal = Optional.of(SwingConstants.LEFT);
      return this;
    }
    public JLabelBuilder alignRight() {
      this.alignHorizontal = Optional.of(SwingConstants.RIGHT);
      return this;
    }
    public JLabelBuilder alignTrailing() {
      this.alignHorizontal = Optional.of(SwingConstants.TRAILING);
      return this;
    }

    public JLabelBuilder alignTop() {
      this.alignVertical = Optional.of(SwingConstants.TOP);
      return this;
    }
    public JLabelBuilder alignBottom() {
      this.alignVertical = Optional.of(SwingConstants.BOTTOM);
      return this;
    }
    public JLabelBuilder text(String text) {
      this.text = Optional.of(text);
      return this;
    }
    
    public JLabel build(String text) {
      JLabel label = new JLabel();
      this.alignHorizontal.ifPresent(alignment -> label.setHorizontalAlignment(alignment));
      this.alignVertical.ifPresent(alignment -> label.setVerticalAlignment(alignment));
      if (text != null) {
        label.setText(text);
      }
      return label;
      
    }
    @Override
    public JLabel build() {
      return build(this.text.orElse(null));
    }
    
  }
  
  public abstract static class JComponentBuilderBase<T extends JComponent> {
    public abstract T build();
  }
}
