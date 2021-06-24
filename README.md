# Oracle Cloud Infrastructure Toolkit for IntelliJ

The Oracle Cloud Infrastructure Toolkit for IntelliJ is an open source plug-in maintained by Oracle for the IntelliJ IDEA Integrated Development Environment (IDE).

#### The toolkit for IntelliJ IDEA features support for creating the Autonomous Database with the following workload types:

Data Warehouse 

Transaction Processing 

JSON

APEX

#### The toolkit also features support for the following actions on database instances:

Administrator Password Change

Clone Autonomous Database Instance

Download Client Credentials

Restore Instance

Scale Up / Down

Start / Stop Database Instance

Terminate Database Instance

Update License Type

Display Autonomous Database Instance Information

## Configuring the toolkit

The toolkit requires basic configuration, like user credentials and tenancy OCID. This information can be provided by using a configuration file.

The default path and recommended file name of configuration file for toolkit is <home_dir>/config. 

You can add multiple profiles with different values for these entries in the configuration file, then you can specify which profile to load in the 'Configure' dialog.

Like some Oracle Cloud Infrastructure SDKs, the toolkit requires a DEFAULT profile. Add profiles to the configuration file as given in the following example.

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

For more details, see [SDK and CLI Configuration File](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdkconfig.htm).

## Download and Installation

The latest version of toolkit can be downloaded from the [releases section on GitHub](https://github.com/oracle/oci-toolkit-intellij/releases).

To install, go to plugins tab in IDE settings / preferences and choose 'Install Plugin from Disk'. Browse the downloaded file and select to install.

## Uninstalling the Toolkit

To uninstall, go to plugins section in IDE settings / preferences, choose OCI Toolkit plugin under Installed list and uninstall.

## Changes

See [CHANGELOG](./CHANGELOG.md).

## Contributing

To contribute, see [CONTRIBUTING](./CONTRIBUTING.md) for details.

## Building the Toolkit

After you clone the repository on GitHub, import it as a Gradle project in IntelliJ IDE.

## License

Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.

Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl

See [LICENSE](./LICENSE.txt) for more details.
