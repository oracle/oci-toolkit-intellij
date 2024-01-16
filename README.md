# Oracle Cloud Infrastructure Toolkit for IntelliJ

The Oracle Cloud Infrastructure Toolkit for IntelliJ is an open source plugin maintained by Oracle for the IntelliJ IDEA Integrated Development Environment (IDE).

#### The toolkit for IntelliJ IDEA features support for creating the Autonomous Database with the following workload types:

* Data Warehouse 
* Transaction Processing 
* JSON
* APEX

#### The toolkit also features support for the following actions on the database instances:

* Administrator Password Change
* Clone Autonomous Database Instance
* Download Client Credentials (Wallet)
* Restore Instance
* Scale Up / Down
* Start / Stop Database Instance
* Terminate Database Instance
* Update License Type
* Display Autonomous Database Instance Information

## Configuring the toolkit

The toolkit requires basic configuration, like user credentials and tenancy OCID. This information can be provided by using a configuration file.

The default path and recommended file name of configuration file for toolkit is ``$HOME/config``.

You can add multiple profiles with different values for these entries in the configuration file, then you can specify which profile to load in the ``Configure`` dialog.

Like some Oracle Cloud Infrastructure SDKs, the toolkit requires a ``DEFAULT`` profile. Add profiles to the configuration file as given in the following example.

```
[DEFAULT]
user=ocid1.user.oc1..<unique_ID>
fingerprint=<your_fingerprint>
key_file=~/.oci/oci_api_key.pem
tenancy=ocid1.tenancy.oc1..<unique_ID>
region=us-ashburn-1

[ADMIN_USER]
user=ocid1.user.oc1..<unique_ID>
fingerprint=<your_fingerprint>
key_file=keys/admin_key.pem
pass_phrase=<your_passphrase>
```

For more details, see [SDK and CLI Configuration File](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdkconfig.htm).

## Installation

The latest version of toolkit can be downloaded from the [releases section on GitHub](https://github.com/oracle/oci-toolkit-intellij/releases).

To install, go to ``Plugins tab`` in ``IDE settings / preferences`` and choose ``Install Plugin from Disk``. Browse the downloaded file and select to install.

## Uninstalling the Toolkit

To uninstall, go to ``Plugins`` section in IDE settings / preferences, choose ``OCI Toolkit plugin`` under Installed list and uninstall.

## Changes

See [CHANGELOG](./CHANGELOG.md).

## Contributing

To contribute, see [CONTRIBUTING](./CONTRIBUTING.md) for details.

## Building the Toolkit

  1. Clone the project ``oci-toolkit-intellij`` using 'Get from VCS' in IntelliJ or by running the command,
     git clone https://username@github.com/oracle/oci-toolkit-intellij.git
  2. Download ``oci-java-sdk-2.1.0.zip`` from [oci-java-sdk  2.1.0](https://github.com/oracle/oci-java-sdk/releases/tag/v2.1.0) and extract.
  3. Copy ``oci-java-sdk-2/lib/oci-java-sdk-full-2.1.0.jar`` to ``oci-toolkit-intellij/lib/sdk`` folder and copy ``oci-java-sdk-2/third-party/lib/resilience4j-circuitbreaker-1.2.0.jar`` to ``oci-toolkit-intellij/lib/thirdparty`` folder.
  4. Build the plugin using the Gradle ``build`` target.
  5. After the successful build, install ``oci-toolkit-intellij/build/distributions/oci-intellij-plugin-x.y.z.zip`` through IntelliJ's plugin wizard.

## Security

Please consult the [security guide](./SECURITY.md) for our responsible security vulnerability disclosure process

## License

Copyright (c) 2021, 2023 Oracle and/or its affiliates.

Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.oracle.com/licenses/upl

See [LICENSE](./LICENSE.txt) for more details.
