## Adobe I/O Java Cloud Manager Library

This is a Java Library wrapping the [Adobe Cloud Manager API](https://www.adobe.io/apis/experiencecloud/cloud-manager/docs.html).


### Prerequisites

To use this library in a project, the Adobe IO organization must be set up to support [API Integrations](https://www.adobe.io/apis/experiencecloud/cloud-manager/docs.html#!AdobeDocs/cloudmanager-api-docs/master/create-api-integration.md). The API integration details are used to configure the clients.

### Maven Dependency

Include via Maven:

Example:
```
<dependency>
    <groupId>io.adobe.cloudmanager</groupId>
    <artifactId>aio-lib-cloudmanager</artifactId>
    <version>1.0.2-SNAPSHOT</version>
</dependency>
```

#### Java 8

The default library is Java11, to use the Java8 library, use the classifier.
```
<dependency>
    <groupId>io.adobe.cloudmanager</groupId>
    <artifactId>aio-lib-cloudmanager</artifactId>
    <version>1.0.2-SNAPSHOT</version>
    <classifier>java8</classifier>
</dependency>
```

### Usage

To make API calls into Cloud Manager, a JWT Token is required. This is created using the `IdentityManagementApi`. For example:

```java

PrivateKey privateKey;

// Load the private key...

// Values are from the API Integration configuration.
AdobeClientCredentials org = new AdobeClientCredentials("Org Id", "Technical Account Id", "API Key", "Client Secret", privateKey);
IdentityManagementApi imApi = IdentityManagementApi.create();
String token = imApi.authenticate(org);

``` 

Use the token returned from the IdentityManagementApi to instantiate the Cloud Manager API. Then make the desired requests:

```java

CloudManagerApi api = CloudManagerApi.create("Org Id", "API Key", token);
List<EmbeddedProgram> programs = api.listPrograms();

```

See the [JavaDocs](https://opensource.adobe.com/aio-lib-java-cloudmanager/apidocs/) for the API.
