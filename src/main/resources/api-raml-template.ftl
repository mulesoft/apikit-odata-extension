#%RAML 1.0
title: ${title}
version: ${version}
mediaType:  application/json

uses:
  model: !include odata.raml
  annotations: !include libraries/odataAnnotations.raml

resourceTypes:
  collection: !include resources/collection.raml
  member: !include resources/member.raml

<#list resources as resource>
/${resource.collectionName}:
  displayName: ${resource.collectionName}
  type: { collection: { model: model.${resource.name} } }
  /${resource.id}:
    displayName: ${resource.elementName}
    type: { member: { model: model.${resource.name} } }
</#list>