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
    <version>${project.version}</version>
</dependency>
```

#### Java 8

The default library is Java11, to use the Java8 library, use the classifier.
```
<dependency>
    <groupId>io.adobe.cloudmanager</groupId>
    <artifactId>aio-lib-cloudmanager</artifactId>
    <version>${project.version}</version>
    <classifier>java8</classifier>
</dependency>
```

### Usage

To make API calls into Cloud Manager, an Access Token is required. 

#### OAuth Client Credential Access Token

For OAuth Access Tokens, the API will manage creating and updating as needed. However, you need to provide a valid Workspace, found in [AIO Java client library](https://opensource.adobe.com/aio-lib-java).

```java
OAuthContext authContext = OAuthContext.builder()
    .clientSecret("<CLIENT_SECRET>")
    .addScope("<SCOPE>") // Add all necessary scopes.
    .build();
Workspace workspace = Workspace.builder()
    .authContext(authContext
    .orgId("<IMS ORG ID>")
    .apiKey("<API KEY>")
    .build();

CloudManagerApi api = CloudManagerApi.builder().workspace(workspace).build();
```


#### JWT Access Token (Deprecated)

JWT Access Tokens have been deprecated by AIO. They will continue to work until Jan 1 2025. If you need to, you can create one using the now deprecated `IdentityManagementApi`. For example:

```java
PrivateKey privateKey;

// Load the private key...

// Values are from the API Integration configuration.
AdobeClientCredentials org = new AdobeClientCredentials("Org Id", "Technical Account Id", "API Key", "Client Secret", privateKey);
IdentityManagementApi imApi = IdentityManagementApi.create();
String token = imApi.authenticate(org);

CloudManagerApi api = CloudManagerApi.create("Org Id", "API Key", token);
List<EmbeddedProgram> programs = api.listPrograms();
```

See the [JavaDocs](https://opensource.adobe.com/aio-lib-java-cloudmanager/apidocs/) for the API.
