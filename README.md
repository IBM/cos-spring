# IBM Cloud Object Storage Spring Library

The is a library to provide Spring developers easy configuration of the [IBM COS SDK for Java](https://github.com/ibm/ibm-cos-sdk-java/).

The library is split into two parts:

* `cos-spring-boot-starter` for [Spring Boot](https://projects.spring.io/spring-boot/) applications
* `cos-spring-framework` for [Spring Framework](https://projects.spring.io/spring-framework/) applications

## Installation and Usage

### Spring Boot Applications

Gradle:

```groovy
dependencies {
    compile group: 'com.ibm.cos', name: 'cos-spring-boot-starter', version: '1.0.0'
}
```

Maven:

```xml
<dependency>
  <groupId>com.ibm.cos</groupId>
  <artifactId>cos-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Spring Framework Applications

Gradle:

```groovy
dependencies {
    compile group: 'com.ibm.cos', name: 'cos-spring-framework', version: '1.0.0'
}
```

Maven:

```xml
<dependency>
  <groupId>com.ibm.cos</groupId>
  <artifactId>cos-spring-framework</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Getting Started

This section contains simple examples of connecting to COS using the two libraries.

### Spring Boot Applications

To enable auto-configuration you must provide the following properties to define the connection to your COS instance:

* `cos.endpoint`
* `cos.location`

Additionally, you must determine which type of credentials you wish to use; either IAM API Key or HMAC credentials. If
using an IAM API Key credential, an optional Service Instance ID may be provided to enable support for Bucket listing and
Bucket creation operations. If using HMAC credentials, Access Key and Secret Key properties are both required.

For example using IAM credentials with an optional Service Instance ID in an `application.properties` file:

```properties
cos.endpoint=https://s3-api.us-geo.objectstorage.softlayer.net
cos.location=us
cos.api-key=myApiKey
cos.service-instance-id=myServiceInstanceId
```

Another example using HMAC credentials:

```properties
cos.endpoint=https://s3-api.us-geo.objectstorage.softlayer.net
cos.location=us
cos.access-key=myAccessKey
cos.secret-key=mySecretKey
```

Spring Boot will create a `com.ibm.cloud.objectstorage.services.s3.AmazonS3` bean that can be used to interact with your COS instance:

```java
@Autowired
private AmazonS3 client;

public boolean isBucketAvailable(String bucket) {
    return client.doesBucketExist(bucket);
}
```

To provide custom client options you can override the `com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder` bean and provide your own properties:

```java
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Value("${cos.endpoint}")
    private URL endpoint;

    @Value("${cos.location}")
    private String location;

    @Value("${cos.api-key}")
    private String apiKey

    @Bean
    public AmazonS3ClientBuilder builder() {
        return AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(
            new EndpointConfiguration(endpoint.toString(), location))
        .withCredentials(new AWSStaticCredentialsProvider(new BasicIBMOAuthCredentials(apiKey, null);))
        .withPathStyleAccessEnabled(true);
    }
}
```

application.properties:

```properties
cos.endpoint=https://s3-api.us-geo.objectstorage.softlayer.net
cos.location=us
cos.api-key=myApiKey
```

### Spring Framework Applications

See Spring Boot section for required and optional properties.

To enable the creation of the `com.ibm.cloud.objectstorage.services.s3.AmazonS3` bean you must add an `com.ibm.cos.spring.framework.EnableCOS` annotation to your application configuration:

```java
@Configuration
@EnableWebMvc
@EnableCOS
@ComponentScan
public class SpringConfig {}
```

```java
@Autowired
private AmazonS3 client;

public boolean isBucketAvailable(String bucket) {
    return client.doesBucketExist(bucket);
}
```

## Related documentation

* [IBM COS SDK for Java](https://github.com/ibm/ibm-cos-sdk-java/)
* [COS documentation](https://console.bluemix.net/docs/services/cloud-object-storage/)
* [Spring Boot documentation](https://projects.spring.io/spring-boot/)
* [Spring Framework documentation](https://projects.spring.io/spring-framework/)

## License

Copyright © 2018 IBM Corp. All rights reserved.

Licensed under the apache license, version 2.0 (the "license"); you may not use this file except in compliance with the license.  you may obtain a copy of the license at

`http://www.apache.org/licenses/LICENSE-2.0.html`

Unless required by applicable law or agreed to in writing, software distributed under the license is distributed on an "as is" basis, without warranties or conditions of any kind, either express or implied. See the license for the specific language governing permissions and limitations under the license.

## Getting help

Feel free to use GitHub issues for tracking bugs and feature requests, but for help please use one of the following resources:

* Ask a question on [StackOverflow][stack-overflow] and tag it with `ibm` and `object-storage`
* Open a support ticket with [IBM Cloud Support][ibm-bluemix-support]
* If it turns out that you may have found a bug, please [open an issue][open-an-issue]

[stack-overflow]: http://stackoverflow.com/questions/tagged/object-storage+ibm
[ibm-bluemix-support]: https://cloud.ibm.com/unifiedsupport/supportcenter/
[open-an-issue]: https://github.com/IBM/cos-spring/issues/new
