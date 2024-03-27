package com.oracle.oci.intellij.common;

import java.io.OutputStream;

public class IOStreamRenderer extends Renderer {

  private OutputStream stream;

  public IOStreamRenderer(OutputStream stream) {
    this.stream = stream;
  }
  @Override
  public void init() {
    // TODO Auto-generated method stub
    super.init();
  }

//  @Override
//  public void render(UIModel model) {
//    
//  }

  public static class UIModel {
    
  }
}
