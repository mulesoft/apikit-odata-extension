/*
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.module.apikit.odata.context;

import java.util.List;

public class OdataContextVariables {

    public String remoteEntityName;
    public String keyNames;
    public List<String> fields;

    public OdataContextVariables(String remoteEntityName, String keyNames, List<String> fields){
        this.remoteEntityName = remoteEntityName;
        this.keyNames = keyNames;
        this.fields = fields;
    }
}
