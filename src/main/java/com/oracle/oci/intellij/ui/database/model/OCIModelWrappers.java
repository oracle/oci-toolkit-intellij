package com.oracle.oci.intellij.ui.database.model;

import java.util.function.Function;

import com.oracle.bmc.core.model.Vcn;

public class OCIModelWrappers {

    public static abstract class OCIModelComboWrapper<OCIType> {

        private OCIType ociObject;
        private Function<OCIType, String> strFunction;

        public OCIModelComboWrapper(OCIType ociObject, Function<OCIType, String> fn) {
            this.ociObject = ociObject;
            this.strFunction = fn;
        }
        @Override
        public final int hashCode() {
            return this.ociObject == null ? 0 : this.ociObject.hashCode();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public final boolean equals(Object obj) {
            if (obj instanceof OCIModelComboWrapper)
            {
                return this.ociObject != null && 
                        this.ociObject.equals(((OCIModelComboWrapper)obj).ociObject);
            }
            return false;
        }

        public final String toString() {
            return this.strFunction.apply(this.ociObject);
        }
        
        public OCIType getValue() {
            return this.ociObject;
        }
    }
    
    public static class VcnComboWrapper extends OCIModelComboWrapper<Vcn> {

        public VcnComboWrapper(Vcn ociObject) {
            super(ociObject, VcnComboWrapper::createStrFunction);
            
        }
        
        private static String createStrFunction(Vcn vcn) {
            return vcn.getDisplayName();
        }

        @Override
        public Vcn getValue() {
            return super.getValue();
        }
    }
}
