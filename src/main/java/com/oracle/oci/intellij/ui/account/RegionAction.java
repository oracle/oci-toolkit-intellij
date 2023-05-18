package com.oracle.oci.intellij.ui.account;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.oracle.bmc.Region;
import com.oracle.bmc.identity.model.RegionSubscription;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.account.SystemPreferences;
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
public class RegionAction extends AnAction {

  private static final HashMap<String, String> iconMap;
  private static final ImageIcon regionIcon;

  static{
    iconMap = new HashMap<>() {
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
        put("ap-sydney-1", Icons.REGION_AUSTRALIA.getPath());
      }
    };

    regionIcon = new ImageIcon(
            RegionAction.class
                    .getResource(iconMap.get(SystemPreferences.getRegionName())));
  }

  public RegionAction(){
    super("Region", "Select region", regionIcon);
  }

  /**
   * Event handler.
   *
   * @param event event.
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    if (event.getInputEvent() instanceof MouseEvent) {
      final MouseEvent mouseEvent = ((MouseEvent) event.getInputEvent());

      final List<RegionSubscription> regionList =
              OracleCloudAccount.getInstance().getIdentityClient().getRegionsList();

      final JPopupMenu popupMenu = new JPopupMenu();
      final ButtonGroup regionsButtonGroup = new ButtonGroup();
      final String currentRegion = SystemPreferences.getRegionName();

      for (RegionSubscription regionSubscription : regionList) {
        final JMenuItem regionMenu = new JRadioButtonMenuItem();
        final Region selectedRegion = Region.fromRegionCode(regionSubscription.getRegionKey());

        if (Pattern.matches("\\w{2}-\\w+-\\d+", regionSubscription.getRegionName())) {
          regionMenu.setText(getFormattedRegion(regionSubscription.getRegionName()));
        } else {
          regionMenu.setText(regionSubscription.getRegionName());
        }

        if (iconMap.get(regionSubscription.getRegionName()) != null) {
          URL url = getClass().getResource(iconMap.get(regionSubscription.getRegionName()));
          if (url != null) {
            regionMenu.setIcon(new ImageIcon(url));
          }
        }

        regionMenu.addActionListener(
                actionEvent -> SystemPreferences.setRegionName(selectedRegion.getRegionId())
        );

        if (currentRegion.equals(selectedRegion.getRegionId())) {
          regionMenu.setSelected(true);
        }
        regionsButtonGroup.add(regionMenu);
        popupMenu.add(regionMenu);
      }

      popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(),
              mouseEvent.getY());
    }
  }

  @Override
  public void update(@NotNull AnActionEvent event) {
    event.getPresentation().setIcon(new ImageIcon(RegionAction.class
            .getResource(iconMap.get(SystemPreferences.getRegionName()))));
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
