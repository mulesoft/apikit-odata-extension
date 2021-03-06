#%RAML ${ramlVersion}
title: ${title}
version: ${version}
traits:
  - orderby:
        queryParameters:
          orderby:
            description: Expression for determining what values are used to order the collection of Entries
            type: string
            required: false
  - top:
        queryParameters:
          top:
            description: Identifies a subset formed by selecting only the first N items of the set, where N is a positive integer specified by this query option
            type: number
            required: false
  - skip:
        queryParameters:
          skip:
            description: Identifies a subset defined by seeking N Entries into the Collection and selecting only the remaining Entries (starting with Entry N+1)
            type: number
            required: false
  - filter:
        queryParameters:
          filter:
            description: Identifies a subset determined by selecting only the Entries that satisfy the predicate expression specified by the query option
            type: string
            required: false
  - expand:
        queryParameters:
          expand:
            description: A URI with a expand System Query Option indicates that Entries associated with the Entry or Collection of Entries identified by the Resource Path section of the URI must be represented inline
            type: string
            required: false
  - format:
        queryParameters:
          format:
            description: If the format query option is present in a request URI it takes precedence over the value(s) specified in the Accept request header. Valid values for the $format query string option are listed in the following table.
            type: string
            required: false
  - select:
        queryParameters:
          select:
            description: Specifies that a response from an OData service should return a subset of the Properties which would have been returned had the URI not included a select query option.
            type: string
            required: false
  - inlinecount:
        queryParameters:
          inlinecount:
            description: Specifies that the response to the request includes a count of the number of Entries in the Collection
            type: string
            required: false
schemas:
<#list schemas as schema>
  - ${schema.name}: |
       ${schema.json}
</#list>
<#function nonKeys properties>
    <#local result = []>
    <#list properties as prop>
        <#if prop.isKey == "false">
            <#local result = result + [prop]>
        </#if>
    </#list>
    <#return result>
</#function>
<#list resources as resource>

/${resource.name}:
  displayName: ${resource.displayName}
  is: [orderby, top, skip, filter, expand, format, select, inlinecount]
  get:
    description: Read
    responses:
      200:
        body:
          application/json:
            schema: |
              {
                "$schema": "http://json-schema.org/draft-04/schema#",
                "type": "object",
                "properties": {
                  "entries": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        <#list resource.properties as property>
                        "${property.name}" : {
                          "type": "${property.type}"
                        }<#sep>,

                        </#list>

                      },
                      "additionalProperties": false
                    }
                  }
                },
                "required": [
                  "entries"
                ],
                "additionalProperties": false
              }
  post:
    description: Create
    body:
      application/json:
        schema: |
          {
            "$schema": "http://json-schema.org/draft-04/schema#",
            "type": "object",
            "properties": {
              <#list resource.properties as property>
              "${property.name}" : {
                "type": "${property.type}"
              }<#sep>,

              </#list>

            },
            "required": [
              <#list nonKeys(resource.properties) as property>
                <#if property.isNullable == 'false'>
                  "${property.name}"<#sep>,
                </#if>
              </#list>

            ],
            "additionalProperties": false
          }
    responses:
      201:
        body:
          application/json:
            schema: |
              {
                "$schema": "http://json-schema.org/draft-04/schema#",
                "type": "object",
                "properties": {
                  "entries": {
                    "type": "array",
                    "items": {
                      "type": "object",
                      "properties": {
                        <#list resource.properties as property>
                        "${property.name}" : {
                          "type": "${property.type}"
                        }<#sep>,

                        </#list>

                      },
                      "additionalProperties": false
                    }
                  }
                },
                "required": [
                  "entries"
                ],
                "additionalProperties": false
              }

  /${resource.key}:
    displayName: ${resource.displayName} id
    is: [filter, expand, format, select]
    get:
      description: Read
      responses:
        200:
          body:
            application/json:
              schema: |
                {
                  "$schema": "http://json-schema.org/draft-04/schema#",
                  "type": "object",
                  "properties": {
                    "entries": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "properties": {
                          <#list resource.properties as property>
                          "${property.name}" : {
                            "type": "${property.type}"
                          }<#sep>,

                          </#list>

                        },
                        "additionalProperties": false
                      }
                    }
                  },
                  "required": [
                    "entries"
                  ],
                  "additionalProperties": false
                }
    delete:
      description: Delete
      responses:
        200:
          body:
            application/json: ~
    put:
      description: Update
      body:
        application/json:
          schema: |
            {
              "$schema": "http://json-schema.org/draft-04/schema#",
              "type": "object",
              "properties": {
                <#list resource.properties as property>
                "${property.name}" : {
                  "type": "${property.type}"
                }<#sep>,

                </#list>

              },
              "required": [
                <#list nonKeys(resource.properties) as property>
                  <#if property.isNullable == 'false'>
                  "${property.name}"<#sep>,
                  </#if>
                </#list>

              ],
              "additionalProperties": false
            }
      responses:
        204:
          body:
            application/json: ~
</#list>