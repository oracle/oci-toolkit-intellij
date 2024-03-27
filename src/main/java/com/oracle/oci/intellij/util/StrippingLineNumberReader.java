package com.oracle.oci.intellij.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 * A line-numbering reader where by default, each line is stripped of leading
 * and trailing whitespace before it is returned.
 * 
 * @author cbateman
 *
 */
public class StrippingLineNumberReader extends LineNumberReader {

  public StrippingLineNumberReader(Reader in) {
    super(in);
  }

  public String readUnstrippedLine() throws IOException {
    return super.readLine();
  }

  @Override
  public String readLine() throws IOException {
    String nextLine = super.readLine();
    if (nextLine == null) {
      return null;
    }
    return nextLine.strip();
  }

}