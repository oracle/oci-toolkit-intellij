package com.oracle.oci.intellij.git;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitUrlConverterTest {

  public static void main(String args[]) {
    String url = "git@github.com:CBatemanOracle/oci-react-samples.git";
    
    Pattern pattern = Pattern.compile("git@github.com:(.*)/(.*).git");
    Matcher matcher = pattern.matcher(url);
    if (matcher.matches()) {
      System.out.printf("https://github.com/%s/%s.git\n", matcher.group(1), matcher.group(2));
    }
  }
}
