/**
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */

package com.oracle.oci.intellij.ui.account;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.oracle.bmc.Region;
import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.oci.intellij.LogHandler;
import com.oracle.oci.intellij.account.IdentClient;
import com.oracle.oci.intellij.account.PreferencesWrapper;
import com.oracle.oci.intellij.ui.common.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class RegionSelectionAction extends AnAction {

  public static HashMap<String, String> iconMap = new HashMap<String, String>() {
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
          .getResource(iconMap.get(PreferencesWrapper.getRegion())));

  public RegionSelectionAction() {
    super("Region", "Select Region", regionIcon);
  }

  @Override
  // TODO: See if non-blocking UI calls required here.
  public void actionPerformed(@NotNull AnActionEvent e) {
    try {
      if (e.getInputEvent() instanceof MouseEvent) {
        final MouseEvent mouseEvent = ((MouseEvent) e.getInputEvent());
        final List<RegionSubscription> regionList = IdentClient.getInstance()
            .getRegionsList();
        final JPopupMenu popupMenu = new JPopupMenu();
        final ButtonGroup menuGroup = new ButtonGroup();
        final String currentRegion = PreferencesWrapper.getRegion();
        for (RegionSubscription r : regionList) {
          final JMenuItem regionMenu = new JRadioButtonMenuItem();
          final Region selectedRegion = Region.fromRegionCode(r.getRegionKey());
          if (Pattern.matches("\\w{2}-\\w+-\\d+", r.getRegionName())) {
            regionMenu.setText(getFormattedRegion(r.getRegionName()));
          }
          else {
            regionMenu.setText(r.getRegionName());
          }
          if (iconMap.get(r.getRegionName()) != null) {
            URL url = getClass().getResource(iconMap.get(r.getRegionName()));
            if (url != null)
              regionMenu.setIcon(new ImageIcon(url));
          }
          regionMenu.addActionListener((e1) -> {
            PreferencesWrapper.setRegion(selectedRegion.getRegionId());
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
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setIcon(new ImageIcon(RegionSelectionAction.class
        .getResource(iconMap.get(PreferencesWrapper.getRegion()))));
    super.update(e);
  }

  private String getFormattedRegion(String regionId) {
    final String[] label = regionId.split("-");
    final String[] new_label = new String[label.length - 1];
    new_label[0] = label[0].toUpperCase();
    new_label[1] = label[1];
    return String.join(" ", new_label);
  }
}
