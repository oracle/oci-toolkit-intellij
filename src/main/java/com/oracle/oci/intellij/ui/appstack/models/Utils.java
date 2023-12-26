package com.oracle.oci.intellij.ui.appstack.models;

import com.oracle.bmc.certificatesmanagement.model.CertificateSummary;
import com.oracle.bmc.core.model.Subnet;
import com.oracle.bmc.core.model.Vcn;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.devops.model.RepositorySummary;
import com.oracle.bmc.dns.model.ZoneSummary;
import com.oracle.bmc.http.client.internal.ExplicitlySetBmcModel;
import com.oracle.bmc.identity.model.AvailabilityDomain;
import com.oracle.bmc.identity.model.Compartment;
import com.oracle.bmc.keymanagement.model.KeySummary;
import com.oracle.bmc.keymanagement.model.VaultSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils{
    static LinkedHashMap<String, SuggestConsumor<PropertyDescriptor, LinkedHashMap<String, PropertyDescriptor>, List<? extends ExplicitlySetBmcModel>,VariableGroup>> suggestedValues = new LinkedHashMap<>();
    static {
        suggestedValues.put("oci:identity:compartment:id",(pd,pds,varGroup)->{
            /* there are :
             * default: ${compartment_id}
             * default: compartment_ocid
             */
            // we have to pop up the compartment selection ....
            Compartment rootCompartment = OracleCloudAccount.getInstance().getIdentityClient().getRootCompartment();
            List<Compartment> compartmentList = OracleCloudAccount.getInstance().getIdentityClient().getCompartmentList(rootCompartment);

            return compartmentList;
        });

        suggestedValues.put("oci:core:vcn:id",(pd,pds,varGroup)->{
            PropertyDescriptor compartmentPd = pds.get("vcn_compartment_id");
            String vcn_compartment_id ;
            VariableGroup compartmentVarGroup =  Controller.getInstance().getVariableGroups().get("Network");
            vcn_compartment_id =((Compartment) compartmentPd.getReadMethod().invoke(compartmentVarGroup)).getId();


            List<Vcn> vcn = OracleCloudAccount.getInstance().getVirtualNetworkClientProxy().listVcns(vcn_compartment_id);

            return vcn;
        });

        suggestedValues.put("oci:core:subnet:id",(pd,pds,varGroup)->{
            PropertyDescriptor compartmentPd = pds.get("vcn_compartment_id");
            VariableGroup networkVarGroup =  Controller.getInstance().getVariableGroups().get("Network");

            String vcn_compartment_id ;
            vcn_compartment_id =((Compartment) compartmentPd.getReadMethod().invoke(networkVarGroup)).getId();
            Vcn vcn = (Vcn) pds.get("existing_vcn_id").getReadMethod().invoke(networkVarGroup);
            if (vcn == null) return null;
            String existing_vcn_id =vcn.getId();;


            // todo
//            LinkedHashMap dependsOn = (LinkedHashMap) pd.getValue("dependsOn");
//            boolean hidePublicSubnet = (boolean) dependsOn.get("hidePublicSubnet");
            boolean hidePublicSubnet = Boolean.parseBoolean(getVaribaleValue("hidePublicSubnet",pd.getValue("dependsOn")));
            List<Subnet> subnets = OracleCloudAccount.getInstance().getVirtualNetworkClientProxy().listSubnets(vcn_compartment_id,existing_vcn_id,hidePublicSubnet);

            return subnets;

        });

        suggestedValues.put("oci:identity:availabilitydomain:name",(pd,pds,varGroup)->{
            VariableGroup general_ConfigurationVarGroup =  Controller.getInstance().getVariableGroups().get("General_Configuration");

            String compartment_id =( (Compartment)pds.get("compartment_id").getReadMethod().invoke(general_ConfigurationVarGroup)).getId();
            List<AvailabilityDomain> availabilityDomains = OracleCloudAccount.getInstance().getIdentityClient().getAvailabilityDomainsList(compartment_id);
            return availabilityDomains;
        });

        suggestedValues.put("oci:database:autonomousdatabase:id",(pd,pds,varGroup)->{

//            VariableGroup general_ConfigurationVarGroup = Controller.getInstance().getVariableGroup(pd);


            String compartment_id = ( (Compartment)pds.get("db_compartment").getReadMethod().invoke(varGroup)).getId();
            if (compartment_id== null) return null;
            List<AutonomousDatabaseSummary> autonomousDatabases = OracleCloudAccount.getInstance().getDatabaseClient().getAutonomousDatabaseList(compartment_id);
            return autonomousDatabases;

        });

        suggestedValues.put("oci:kms:vault:id",(pd,pds,varGroup)->{
            VariableGroup general_ConfigurationVarGroup = Controller.getInstance().getVariableGroups().get("Stack_authentication");

            String vault_compartment_id = ((Compartment) pds.get("vault_compartment_id").getReadMethod().invoke(general_ConfigurationVarGroup)).getId();;

            List<VaultSummary> vaultList = OracleCloudAccount.getInstance().getIdentityClient().getVaultsList(vault_compartment_id);
            return vaultList;
        });

        suggestedValues.put("oci:kms:key:id",(pd,pds,varGroup)->{
            VariableGroup general_ConfigurationVarGroup =  Controller.getInstance().getVariableGroups().get("Stack_authentication");

            String vault_compartment_id = ( (Compartment) pds.get("vault_compartment_id").getReadMethod().invoke(general_ConfigurationVarGroup)).getId();
            VaultSummary vault =(VaultSummary) pds.get("vault_id").getReadMethod().invoke(varGroup);
            if (vault == null) return null ;


            List<KeySummary> keyList = OracleCloudAccount.getInstance().getIdentityClient().getKeyList(vault_compartment_id,vault);

            return keyList;

        });
        suggestedValues.put("oci:devops:repository:id",(pd,pds,varGroup)->{
//            VariableGroup general_ConfigurationVarGroup =  Controller.getInstance().getVariableGroups().get("General_Configuration");

            String compartment_id = ( (Compartment) pds.get("devops_compartment").getReadMethod().invoke(varGroup)).getId();

            List<RepositorySummary> repositorySummaries = OracleCloudAccount.getInstance().getIdentityClient().getRepoList(compartment_id);

            return repositorySummaries;

        });
        suggestedValues.put("oci:certificatesmanagement:certificate:id",(pd,pds,varGroup)->{
//            VariableGroup general_ConfigurationVarGroup =  Controller.getInstance().getVariableGroups().get("General_Configuration");

            String compartment_id = ( (Compartment) pds.get("dns_compartment").getReadMethod().invoke(varGroup)).getId();

            List<CertificateSummary> certificateSummaries = OracleCloudAccount.getInstance().getIdentityClient().getAllCertificates(compartment_id);

            return certificateSummaries;

        });

        suggestedValues.put("oci:dns:zone:id",(pd,pds,varGroup)->{
//            VariableGroup general_ConfigurationVarGroup =  Controller.getInstance().getVariableGroups().get("General_Configuration");

            String compartment_id = ( (Compartment) pds.get("dns_compartment").getReadMethod().invoke(varGroup)).getId();

            List<ZoneSummary> repositorySummaries = OracleCloudAccount.getInstance().getIdentityClient().getAllDnsZone(compartment_id);

            return repositorySummaries;

        });



    }

    private static String getVaribaleValue(String variableName, Object dependsOn) {
        Pattern pattern = Pattern.compile(variableName+"=([^,}]*)");
        Matcher matcher = pattern.matcher(dependsOn.toString());

        if (matcher.find()){
            return matcher.group(1);
        }
        return "";
    }

    static public Map<String , List<String>> depondsOn = new LinkedHashMap<>(){{
        put("compartment_id", List.of("availability_domain"));
        put("vault_compartment_id",List.of("vault_id","key_id"));
        put("vault_id",List.of("key_id"));
        put("vcn_compartment_id",List.of("existing_vcn_id","existing_app_subnet_id","existing_db_subnet_id","existing_lb_subnet_id"));
        put("existing_vcn_id",List.of("existing_app_subnet_id","existing_db_subnet_id","existing_lb_subnet_id"));
        put("db_compartment",List.of("autonomous_database"));
        put("devops_compartment",List.of("repo_name"));
        put("dns_compartment",List.of("zone","certificate_ocid"));

    }};

    static public Map<String, List<String>> visibility = new LinkedHashMap<>() {{
        put("application_source", List.of("application_type", "repo_name", "branch", "build_command", "artifact_location", "artifact_id", "registry_id", "image_path", "exposed_port", "use_username_env", "use_password_env", "use_tns_admin_env", "tns_admin_env", "use_default_ssl_configuration", "cert_pem", "private_key_pem", "ca_pem", "vm_options", "program_arguments"));
        put("application_type", List.of("program_arguments", "use_default_ssl_configuration"));
        put("use_existing_database", List.of("autonomous_database_display_name", "autonomous_database_admin_password", "data_storage_size_in_tbs", "cpu_core_count", "ocpu_count", "autonomous_database", "autonomous_database_user", "autonomous_database_password", "use_existing_db_subnet", "db_subnet_cidr","db_compartment"));
        put("use_existing_vault", List.of("new_vault_display_name", "vault_compartment_id", "vault_id", "key_id"));
        put("use_existing_token", List.of("current_user_token"));
        put("use_connection_url_env", List.of("connection_url_env"));
        put("use_username_env", List.of("username_env"));
        put("use_password_env", List.of("password_env"));
        put("use_tns_admin_env", List.of("tns_admin_env"));
        put("use_default_ssl_configuration", List.of("port_property", "keystore_property", "key_alias_property", "keystore_password_property", "keystore_type_property"));
        put("create_fqdn", List.of("dns_compartment", "zone", "subdomain", "certificate_ocid"));
        put("create_new_vcn", List.of("vcn_compartment_id", "existing_vcn_id", "vcn_cidr", "use_existing_app_subnet", "use_existing_db_subnet", "use_existing_lb_subnet"));
        put("use_existing_app_subnet", List.of("existing_app_subnet_id", "app_subnet_cidr"));
        put("use_existing_db_subnet", List.of("existing_db_subnet_id", "db_subnet_cidr"));
        put("use_existing_lb_subnet", List.of("existing_lb_subnet_id", "lb_subnet_cidr"));
        put("use_default_lb_configuration", List.of("maximum_bandwidth_in_mbps", "minimum_bandwidth_in_mbps", "health_checker_url_path", "health_checker_return_code", "enable_session_affinity"));
        put("enable_session_affinity", List.of("session_affinity", "session_affinity_cookie_name"));
    }};

    public static SuggestConsumor<PropertyDescriptor,LinkedHashMap<String,PropertyDescriptor> ,List<? extends ExplicitlySetBmcModel>,VariableGroup> getSuggestedValuesOf(String type){
        return suggestedValues.get(type);
    }


}
@FunctionalInterface
interface SuggestConsumor<T,U,R,O> {
    R apply(T t,U u,O o) throws InvocationTargetException, IllegalAccessException;
}
