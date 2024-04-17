package com.oracle.oci.intellij.ui.devops.wizard.mirrorgh;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.oracle.oci.intellij.ui.common.SpringUtilities;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.ui.common.UIUtil.ModelHolder;
import com.oracle.oci.intellij.ui.devops.actions.MirrorGitHubRepoAction;
import com.oracle.oci.intellij.ui.git.config.GitConfigManager;

public class SelectRemoteGithubWizardStep
  extends WizardStep<MirrorGitHubWizardModel> {
  private JPanel mainPanel;
  private MyWizardContext context;

  public SelectRemoteGithubWizardStep(final MyWizardContext context) {
    super("Select the public Github repository to mirror",
          "You will be asked for a Developer token to access the repository.  It must be accessible via the public internet.");
    this.context = context;
  }

  @SuppressWarnings("unchecked")
  @Override
  public JComponent prepare(WizardNavigationState state) {
    SpringLayout layout = new SpringLayout();
    mainPanel = new JPanel(layout);

    {
      JLabel devProjectLbl = new JLabel("DevOps Project: ", JLabel.TRAILING);
      mainPanel.add(devProjectLbl);
      JTextField projectName = new JTextField();
      projectName.setEditable(false);
      projectName.setText(context.getProject().getName());
      projectName.setMaximumSize(new Dimension(Short.MAX_VALUE, projectName.getPreferredSize().height));
      mainPanel.add(projectName);
      devProjectLbl.setLabelFor(devProjectLbl);
    }

    JComboBox<ModelHolder<String>> comboBox =
      new JComboBox<UIUtil.ModelHolder<String>>();
    comboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, comboBox.getPreferredSize().height));
    {
      JLabel githubRemote = new JLabel("GitHub Remote: ", JLabel.TRAILING);
      mainPanel.add(githubRemote);
      GitConfigManager.getInstance().getConfig(context.getProject(), (gc) -> {
        Set<String> gitHubUrls = MirrorGitHubRepoAction.findGitHubUrls(gc);
        gitHubUrls.forEach(g -> comboBox.addItem(UIUtil.holdModel(g)
                                                       .setTextProvider(s -> s)));
        return gc;
      });
    }

    comboBox.addItemListener(new ItemListener() {

      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          ModelHolder<String> item = (ModelHolder<String>) e.getItem();
          if (item != null) {
            context.setGithub(item.get());
          }
        }
      }

    });

    if (comboBox.getSelectedItem() != null) {
      ModelHolder<String> github =
        (ModelHolder<String>) comboBox.getSelectedItem();
      if (github != null) {
        context.setGithub(github.get());

      }
    }
    mainPanel.add(comboBox);

    // Lay out the panel.
    SpringUtilities.makeCompactGrid(mainPanel, // parent
                                    2, 2, 3, 3,  // initX, initY
                                    3, 3); // xPad, yPad
    state.NEXT.setEnabled(comboBox.getModel().getSize() > 0
                          && comboBox.getSelectedItem() != null);
    return mainPanel;
  }

  
}