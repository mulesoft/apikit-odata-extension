#%RAML 1.0
title: ${title}
version: ${version}
mediaType:  application/json

uses:
  model: odata.raml
  odata: libraries/odataLibrary.raml

<#list resources as resource>
/${resource.collectionName}:
  displayName: ${resource.collectionName}
  type: { odata.collection: { model: model.${resource.name} } }
  /${resource.id}:
    displayName: ${resource.elementName}
    type: { odata.member: { model: model.${resource.name} } }
</#list>