/*
  Copyright (c) 2021, Oracle and/or its affiliates.
  Licensed under the Universal Permissive License v 1.0 as shown at http://oss.oracle.com/licenses/upl.
 */
package com.oracle.oci.intellij.ui.common;

/**
 * Path of icons used by plugin.
 */
public enum Icons {
    BUCKET("icons/bucket.png"),
    TOOLBAR_LOGIN("icons/toolbar-login.png"),
    COMPUTE("icons/compute.png"),
    COMPUTE_INSTANCE("icons/compute-instance.png"),
    OBJECT_STORAGE("icons/object-storage.png"),
    BLOCK_STORAGE("icons/block-storage.png"),
    REGION_US("/icons/regions/us-orb.png"),
    REGION_GERMANY("/icons/regions/germany-orb.png"),
    REGION_UK("/icons/regions/uk-orb.png"),
    REGION_CANADA("/icons/regions/canada-flag.png"),
    REGION_INDIA("/icons/regions/india-flag.png"),
    REGION_JAPAN("/icons/regions/japan-flag.png"),
    REGION_SOUTH_KOREA("/icons/regions/south-korea-flag.png"),
    REGION_SWITZERLAND("/icons/regions/switzerland-flag.png"),
    REGION_AUSTRALIA("/icons/regions/australia-flag.png"),
    REGION_BRAZIL("/icons/regions/brazil-flag.png"),
    REGION_CHILE("/icons/regions/chile.png"),
    REGION_OMAN("/icons/regions/oman.png"),
    REGION_FRANCE("/icons/regions/france.png"),
    REGION_ISRAEL("/icons/regions/israel.png"),
    REGION_MEXICO("/icons/regions/mexico.png"),
    REGION_NETHERLANDS("/icons/regions/netherlands.png"),
    REGION_SAUDI_ARABIA("/icons/regions/saudi_arabia.png"),
    REGION_SERBIA("/icons/region/wales.png"),
    REGION_SINGAPORE("/icons/regions/singapore.png"),
    REGION_SOUTH_AFRICA("/icons/regions/south_africa.png"),
    REGION_SPAIN("/icons/regions/spain.png"),
    REGION_ITALY("/icons/regions/italy.png"),
    REGION_SWEDEN("/icons/regions/sweden.png"),
    REGION_UNITED_ARABE_EMIRATES("/icons/regions/united_arab_emirates.png"),
    REGION_WALES("/icons/regions/wales.png"),
    REGION_IRELAND("/icons/regions/ireland.png"),

    DEFAULT_REGION("/icons/regions/default-flag.png"),
    DATABASE("icons/database.png"),
    DATABASE_AVAILABLE_STATE("icons/db-available-state.png"),
    DATABASE_IN_PROGRESS_STATE("icons/db-inprogress-state.png"),
    DATABASE_UNAVAILABLE_STATE("icons/db-unavailable-state.png"),
    BACKUP_ACTIVE_STATE("icons/backup-active-state.png"),
    CONTAINER("icons/compute.png");

    private final String path;

    /**
     * Construct the icon path.
     * @param path the icon file path.
     */
    Icons(String path) {
        this.path = path;
    }

    /**
     * Get icon path.
     * @return icon path.
     */
    public String getPath() {
        return path;
    }
}
