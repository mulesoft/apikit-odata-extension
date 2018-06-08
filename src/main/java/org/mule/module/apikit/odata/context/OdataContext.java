/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.context;

import org.mule.module.apikit.odata.metadata.OdataMetadataManager;

import java.util.Arrays;

/**
 * This class contains the metadata and method used in odata requests
 * Created by arielsegura on 10/1/15.
 */
public class OdataContext {
    public static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE"};
    private OdataMetadataManager odataMetadataManager;
    private String method;

    public OdataContext(OdataMetadataManager odataMetadataManager, String method) {
        setOdataMetadataManager(odataMetadataManager);
        setMethod(method);
    }

    public OdataContext(OdataMetadataManager odataMetadataManager) {
        setOdataMetadataManager(odataMetadataManager);
    }
    
    public OdataMetadataManager getOdataMetadataManager() {
        return odataMetadataManager;
    }

    public void setOdataMetadataManager(OdataMetadataManager odataMetadataManager) {
        this.odataMetadataManager = odataMetadataManager;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        if(Arrays.asList(ALLOWED_METHODS).contains(method.toUpperCase())) {
            this.method = method;
        } else{
            throw new UnsupportedOperationException("Method not allowed.");
        }
    }
}
