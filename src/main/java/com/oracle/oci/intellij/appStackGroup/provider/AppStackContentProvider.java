package com.oracle.oci.intellij.appStackGroup.provider;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.oracle.bmc.resourcemanager.model.Stack.LifecycleState;
import com.oracle.bmc.resourcemanager.model.StackSummary;

public class AppStackContentProvider {
  public static class AppStackContent {
    private StackSummary stackSummary;

    public AppStackContent(StackSummary stackSummary) {
      this.stackSummary = stackSummary;
    }

    public String getCompartmentId() {
      return stackSummary.getCompartmentId();
    }

    public String getDescription() {
      return stackSummary.getDescription();
    }

    public String getDisplayName() {
      return stackSummary.getDisplayName();
    }

    public String getId() {
      return stackSummary.getId();
    }

    public LifecycleState getLifecycleState() {
      return stackSummary.getLifecycleState();
    }

    public String getTerraformVersion() {
      return stackSummary.getTerraformVersion();
    }

    public Date getTimeCreated() {
      return stackSummary.getTimeCreated();
    }
  }
  
  public List<AppStackContent> getElements(final List<StackSummary> stackSummaries) {
    AbstractList<AppStackContent> list = new AbstractList<>() {
      final List<StackSummary> summaries = stackSummaries;
      ArrayList<AppStackContent> contents = new ArrayList<>(Collections.nCopies(stackSummaries.size(), null));
      //final List<AppStackContent> contents = new ArrayList<>(summaries.size()).;
      @Override
      public int size() {
        return stackSummaries.size();
      }

      @Override
      public boolean addAll(Collection<? extends AppStackContent> c) {
        throw new UnsupportedOperationException();
      }

      @Override
      public AppStackContent get(int index) {
        AppStackContent content = contents.get(index);
        if (content == null) {
          content = new AppStackContent(summaries.get(index));
          contents.set(index, content);
        }
        return content;
      }
      
    };
    return list;
  }
  
  public Object getColumn(AppStackContent content, String columnName) {
    switch(columnName) {
    case "id":
      return content.getId();
    case "displayname":
      return content.getDisplayName();
    case "description":
      return content.getDescription();
    case "timeCreated":
      return content.getTimeCreated();
    case "lifecyclestate":
      return content.getLifecycleState();
    case "terraformversion":
      return content.getTerraformVersion();
    default:
      return "NoColumn";
    }
    
  }
}
