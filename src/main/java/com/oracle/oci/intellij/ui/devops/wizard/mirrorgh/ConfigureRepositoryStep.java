package com.oracle.oci.intellij.ui.devops.wizard.mirrorgh;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;

import org.jetbrains.annotations.NotNull;

import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.oracle.oci.intellij.ui.common.JComponentBuilder.JLabelBuilder;
import com.oracle.oci.intellij.ui.common.SpringUtilities;

public class ConfigureRepositoryStep extends WizardStep<MirrorGitHubWizardModel> {
  private JPanel mainPanel;
  private MyWizardContext context;

  
  public ConfigureRepositoryStep(MyWizardContext context) {
    super("Name Your Mirror Repo", 
     "Configure a name and optional description for you mirror repo.  This is what will be displayed when you list your DevOps repos");
    this.context = context;
  }

  @Override
  public JComponent prepare(WizardNavigationState state) {
    this.mainPanel = new JPanel(new SpringLayout());

    JLabelBuilder labelBuilder = JLabelBuilder.create().alignLeft().alignTop();

    JLabel repoName = labelBuilder.build("Repository Name:");
    repoName.setHorizontalAlignment(SwingConstants.LEFT); ;
    repoName.setVerticalAlignment(SwingConstants.TOP);   
    
    JTextField repoNameText = new JTextField(16);
    repoNameText.setMaximumSize(new Dimension(Short.MAX_VALUE, 
                                          repoNameText.getPreferredSize().height));

    JLabel repoDescription = labelBuilder.build("Repository Description:");
    JTextField repoDesc = new JTextField(16);
    repoDesc.setMaximumSize(new Dimension(Short.MAX_VALUE, 
                                          repoDesc.getPreferredSize().height));

    this.mainPanel.add(repoName);
    this.mainPanel.add(repoNameText);
    this.mainPanel.add(repoDescription);
    this.mainPanel.add(repoDesc);

    repoNameText.getDocument().addDocumentListener(new DocumentAdapter() {

      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        context.setRepoName(repoNameText.getText());
      }
      
    });
    
    repoDesc.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        context.setRepoDescription(repoDesc.getText());
      }
    });
    
    // Lay out the panel.
    SpringUtilities.makeCompactGrid(mainPanel, // parent
                                    2, 2, 3, 3,  // initX, initY
                                    3, 3); // xPad, yPad
    return this.mainPanel;
  }
  
  
}