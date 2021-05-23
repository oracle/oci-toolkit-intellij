package com.oracle.oci.intellij.account;

import com.intellij.notification.NotificationType;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.oci.intellij.ui.common.UIUtil;
import com.oracle.oci.intellij.util.LogHandler;

import static com.oracle.bmc.ClientRuntime.setClientUserAgent;
import static com.oracle.oci.intellij.account.SystemPreferences.getUserAgent;

/**
 * The Oracle Cloud account configurator and accessor.
 */
public class OracleCloudAccount {
  private static final OracleCloudAccount ORACLE_CLOUD_ACCOUNT_INSTANCE = new OracleCloudAccount();

  private AuthenticationDetailsProvider authenticationDetailsProvider = null;
  private final IdentityClientProxy identityClientProxy = new IdentityClientProxy();
  private final DatabaseClientProxy databaseClientProxy = new DatabaseClientProxy();

  private OracleCloudAccount() {
  }

  public static OracleCloudAccount getInstance() {
    return ORACLE_CLOUD_ACCOUNT_INSTANCE;
  }

  public void configure(String configFile, String givenProfile) {
    reset();

    final String fallbackProfileName = SystemPreferences.DEFAULT_PROFILE_NAME;
    try {
      final ConfigFileHandler.ProfileSet profileSet = ConfigFileHandler.parse(configFile);

      ConfigFileHandler.Profile profile;
      if (profileSet.containsKey(givenProfile)) {
        profile = profileSet.get(givenProfile);
        authenticationDetailsProvider = new ConfigFileAuthenticationDetailsProvider(configFile, givenProfile);
      } else {
        // Last used profile is not found. Use the default profile.
        if (profileSet.containsKey(fallbackProfileName)) {
          LogHandler.warn(String.format("The profile %s isn't found in config file %s. Switched to profile %s.",
                  givenProfile,
                  configFile,
                  fallbackProfileName));
          profile = profileSet.get(fallbackProfileName);
          authenticationDetailsProvider = new ConfigFileAuthenticationDetailsProvider(configFile, fallbackProfileName);
        } else {
          // Even the default profile is not found in the given config.
          throw new IllegalStateException(
                  String.format("The profile %s isn't found in the config file %s.", givenProfile, configFile));
        }
      }
      setClientUserAgent(getUserAgent());

      identityClientProxy.init(authenticationDetailsProvider, profile.get("region"));
      SystemPreferences.addPropertyChangeListener(identityClientProxy);

      databaseClientProxy.init(authenticationDetailsProvider, profile.get("region"));
      SystemPreferences.addPropertyChangeListener(databaseClientProxy);

      SystemPreferences.setConfigInfo(configFile, profile.getName(), profile.get("region"));

    } catch (Exception ioException) {
      final String message = "Oracle Cloud account configuration failed: " + ioException.getMessage();
      UIUtil.fireNotification(NotificationType.ERROR,message);
      LogHandler.error(message);
    }
  }

  private void validate() {
    if (authenticationDetailsProvider == null) {
      final String message = "Configure Oracle Cloud account first.";
      UIUtil.fireNotification(NotificationType.ERROR,message);
      LogHandler.error(message);
      throw new RuntimeException(message);
    }
  }

  public IdentityClientProxy getIdentityClient() {
    validate();
    return identityClientProxy;
  }

  public DatabaseClientProxy getDatabaseClient() {
    validate();
    return databaseClientProxy;
  }

  private void reset() {
    authenticationDetailsProvider = null;
  }

}
