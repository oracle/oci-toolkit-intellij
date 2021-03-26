/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.account;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.oracle.bmc.Region;
import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.oci.intellij.util.LogHandler;
import com.oracle.oci.intellij.account.Identity;
import com.oracle.oci.intellij.account.ServicePreferences;
import com.oracle.oci.intellij.ui.common.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Action handler for selection event of UI component 'Region'.
 */
public class RegionSelectionAction extends AnAction {

  private static final HashMap<String, String> iconMap = new HashMap<String, String>() {
    {
      put("us-ashburn-1", Icons.REGION_US.getPath());
      put("us-phoenix-1", Icons.REGION_US.getPath());
      put("eu-frankfurt-1", Icons.REGION_GERMANY.getPath());
      put("uk-london-1", Icons.REGION_UK.getPath());
      put("ca-toronto-1", Icons.REGION_CANADA.getPath());
      put("ap-mumbai-1", Icons.REGION_INDIA.getPath());
      put("ap-seoul-1", Icons.REGION_SOUTH_KOREA.getPath());
      put("ap-tokyo-1", Icons.REGION_JAPAN.getPath());
      put("eu-zurich-1", Icons.REGION_SWITZERLAND.getPath());
    }
  };

  private static ImageIcon regionIcon = new ImageIcon(
      RegionSelectionAction.class
          .getResource(iconMap.get(ServicePreferences.getRegion())));

  public RegionSelectionAction() {
    super("Region", "Select region", regionIcon);
  }

  /**
   * Event handler.
   *
   * @param event event.
   */
  @Override
  // TODO: See if non-blocking UI calls required here.
  public void actionPerformed(@NotNull AnActionEvent event) {
    try {
      // TODO: Why is only MouseEvent handled?
      if (event.getInputEvent() instanceof MouseEvent) {
        final MouseEvent mouseEvent = ((MouseEvent) event.getInputEvent());

        final List<RegionSubscription> regionList = Identity.getInstance()
            .getRegionsList();

        final JPopupMenu popupMenu = new JPopupMenu();
        final ButtonGroup menuGroup = new ButtonGroup();
        
        final String currentRegion = ServicePreferences.getRegion();
        for (RegionSubscription subscription : regionList) {
          final JMenuItem regionMenu = new JRadioButtonMenuItem();
          final Region selectedRegion = Region.fromRegionCode(subscription.getRegionKey());

          if (Pattern.matches("\\w{2}-\\w+-\\d+", subscription.getRegionName())) {
            regionMenu.setText(getFormattedRegion(subscription.getRegionName()));
          } else {
            regionMenu.setText(subscription.getRegionName());
          }

          if (iconMap.get(subscription.getRegionName()) != null) {
            URL url = getClass().getResource(iconMap.get(subscription.getRegionName()));
            if (url != null)
              regionMenu.setIcon(new ImageIcon(url));
          }

          regionMenu.addActionListener((e1) -> {
            ServicePreferences.updateRegion(selectedRegion.getRegionId());
          });

          if (currentRegion.equals(selectedRegion.getRegionId()))
            regionMenu.setSelected(true);
          menuGroup.add(regionMenu);
          popupMenu.add(regionMenu);
        }

        popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(),
            mouseEvent.getY());
      }
    }
    catch(Exception ex) {
      LogHandler.error(ex.getMessage(), ex);
    }
  }

  @Override
  public void update(@NotNull AnActionEvent event) {
    event.getPresentation().setIcon(new ImageIcon(RegionSelectionAction.class
        .getResource(iconMap.get(ServicePreferences.getRegion()))));
    super.update(event);
  }

  private String getFormattedRegion(String regionId) {
    final String[] label = regionId.split("-");
    final String[] new_label = new String[label.length - 1];
    new_label[0] = label[0].toUpperCase();
    new_label[1] = label[1];
    return String.join(" ", new_label);
  }
}
