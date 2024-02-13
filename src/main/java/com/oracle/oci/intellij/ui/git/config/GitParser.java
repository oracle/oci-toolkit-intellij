package com.oracle.oci.intellij.ui.git.config;

import java.io.IOException;
import java.io.Reader;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.oracle.oci.intellij.util.StrippingLineNumberReader;

class GitParser {
  private static final Pattern strippedLinePattern =
    Pattern.compile("\\[(.*)\\]");

  public GitConfig parse(Reader reader) {
    StrippingLineNumberReader bufReader =
      new StrippingLineNumberReader(reader);
    GitConfig config = new GitConfig();

    try {
      String curLine = "";
      // String nextLine = "";
      while ((curLine = bufReader.readLine()) != null) {
        curLine = curLine.strip();
        Matcher m = strippedLinePattern.matcher(curLine);
        if (m.matches()) {
          String headerContent = m.group(1);
          StringTokenizer tokenizer = new StringTokenizer(headerContent, " ");
          while (tokenizer.hasMoreElements()) {
            String sectionType = tokenizer.nextToken();
            switch (sectionType) {
            case "core":
              GitConfigCore core = handleCore(tokenizer);
              core.consume(bufReader);
              config.setCore(core);
              break;
            case "remote":
              GitConfigRemote remote = handleRemote(tokenizer);
              remote.consume(bufReader);
              config.addRemote(remote);
              break;
            case "branch":
              GitConfigBranch branch = handleBranch(tokenizer);
              branch.consume(bufReader);
              config.addBranch(branch);
              break;
            default:
            }
          }
        }
      }
      config.updateMappings();
      return config;
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return null;
    }
  }

  private GitConfigBranch handleBranch(StringTokenizer tokenizer) {
    if (!tokenizer.hasMoreElements()) {
      System.err.println("No branch name");
      return null;
    }
    String branch = tokenizer.nextToken();
    if (branch.startsWith(branch)) {
      branch = branch.substring(1);
    }
    if (branch.endsWith(branch)) {
      branch = branch.substring(0, branch.length() - 1);
    }
    return new GitConfigBranch(branch);
  }

  private GitConfigRemote handleRemote(StringTokenizer tokenizer) {
    if (!tokenizer.hasMoreElements()) {
      System.err.println("No branch name");
    }
    String remoteName = tokenizer.nextToken();
    if (remoteName.startsWith(remoteName)) {
      remoteName = remoteName.substring(1);
    }
    if (remoteName.endsWith(remoteName)) {
      remoteName = remoteName.substring(0, remoteName.length() - 1);
    }
    return new GitConfigRemote(remoteName);
  }

  private GitConfigCore handleCore(StringTokenizer tokenizer) {
    return new GitConfigCore();
  }

  private void handleOther(StringTokenizer tokenizer) {

  }
}