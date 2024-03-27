package com.oracle.oci.intellij.git;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;

import com.oracle.oci.intellij.ui.git.config.GitConfig;
import com.oracle.oci.intellij.ui.git.config.GitParser;
import com.oracle.oci.intellij.ui.git.config.GitParser.GitParserException;

public class GitConfigParserTest {

  public static void main(String[] args) throws IOException, GitParserException {
    try (InputStream resourceAsStream =
      GitConfigParserTest.class.getClassLoader().getResourceAsStream("config");
         BufferedInputStream bis = new BufferedInputStream(resourceAsStream);
         Reader reader = new InputStreamReader(bis)) {

      GitParser parser = new GitParser();
      GitConfig config = parser.parse(reader);
      Optional.ofNullable(config).ifPresent(c -> System.out.println(c.toString()));
    }
  }
}
