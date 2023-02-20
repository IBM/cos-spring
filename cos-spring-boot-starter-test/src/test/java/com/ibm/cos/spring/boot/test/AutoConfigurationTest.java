/*
 * Copyright © 2018 IBM Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.ibm.cos.spring.boot.test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.BasicAWSCredentials;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import com.ibm.cos.spring.boot.COSAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.SpringVersion;

@SuppressWarnings("deprecation")
class AutoConfigurationTest {

  private AnnotationConfigApplicationContext context;

  private static String SPRING_BOOT_TEST_VERSION = System.getProperty("spring.boot.test.version");
  private static String SPRING_TEST_VERSION = System.getProperty("spring.test.version");

  @BeforeEach
  void setUp() {
    this.context = new AnnotationConfigApplicationContext();
  }

  @AfterEach
  void cleanup() {
    if (this.context != null) {
      this.context.close();
    }
  }

  @Test
  void bootVersionCheck() {
    // system property will only be set when invoking tests via surefire, not an ide
    assumeTrue(SPRING_BOOT_TEST_VERSION != null);
    assertEquals(SPRING_BOOT_TEST_VERSION, SpringBootVersion.getVersion());
  }

  @Test
  void frameworkVersionCheck() {
    // system property will only be set when invoking tests via surefire, not an ide
    assumeTrue(SPRING_TEST_VERSION != null);
    assertEquals(SPRING_TEST_VERSION, SpringVersion.getVersion());
  }

  @Test
  void clientBuilderMissingEndpoint() {
    assertThrows(
        BeanCreationException.class,
        () -> {
          this.context.register(COSAutoConfiguration.class);
          TestPropertyValues.of("cos.location=myLocation")
              .and("cos.api-key=myApiKey")
              .applyTo(this.context);
          this.context.refresh();
        });
  }

  @Test
  void clientBuilderInvalidEndpoint() {
    assertThrows(
        BeanCreationException.class,
        () -> {
          this.context.register(COSAutoConfiguration.class);
          TestPropertyValues.of("cos.endpoint=notAUrl")
              .and("cos.location=us-south")
              .and("cos.api-key=myApiKey")
              .applyTo(this.context);
          this.context.refresh();
        });
  }

  @Test
  void clientBuilderMissingCredentials() {
    assertThrows(
        BeanCreationException.class,
        () -> {
          this.context.register(COSAutoConfiguration.class);
          TestPropertyValues.of("cos.endpoint=http://ibm.com")
              .and("cos.location=us-south")
              .applyTo(this.context);
          this.context.refresh();
        });
  }

  @Test
  void clientBuilderMissingAccessKey() {
    assertThrows(
        BeanCreationException.class,
        () -> {
          this.context.register(COSAutoConfiguration.class);
          TestPropertyValues.of("cos.endpoint=http://ibm.com")
              .and("cos.location=us-south")
              .and("cos.secret-key=mySecretKey")
              .applyTo(this.context);
          this.context.refresh();
        });
  }

  @Test
  void clientBuilderMissingSecretKey() {
    assertThrows(
        BeanCreationException.class,
        () -> {
          this.context.register(COSAutoConfiguration.class);
          TestPropertyValues.of("cos.endpoint=http://ibm.com")
              .and("cos.location=us-south")
              .and("cos.accessKey=myAccessKey")
              .applyTo(this.context);
          this.context.refresh();
        });
  }

  @Test
  void clientBuilderBeanCreationWithEndpoint() {
    this.context.register(COSAutoConfiguration.class);
    TestPropertyValues.of("cos.endpoint=http://ibm.com")
        .and("cos.api-key=myApiKey")
        .applyTo(this.context);
    this.context.refresh();

    final AmazonS3ClientBuilder clientBuilder = this.context.getBean(AmazonS3ClientBuilder.class);
    assertThat(clientBuilder, is(not(nullValue())));

    final EndpointConfiguration endpoint = clientBuilder.getEndpoint();
    assertThat(endpoint.getServiceEndpoint(), is("http://ibm.com"));
    assertThat(endpoint.getSigningRegion(), is(nullValue()));
  }

  @Test
  void clientBuilderBeanCreationWithEndpointAndLocation() {
    this.context.register(COSAutoConfiguration.class);
    TestPropertyValues.of("cos.endpoint=http://ibm.com")
        .and("cos.location=us-south")
        .and("cos.api-key=myApiKey")
        .applyTo(this.context);
    this.context.refresh();

    final AmazonS3ClientBuilder clientBuilder = this.context.getBean(AmazonS3ClientBuilder.class);
    assertThat(clientBuilder, is(not(nullValue())));

    final EndpointConfiguration endpoint = clientBuilder.getEndpoint();
    assertThat(endpoint.getServiceEndpoint(), is("http://ibm.com"));
    assertThat(endpoint.getSigningRegion(), is("us-south"));
  }

  @Test
  void clientBuilderBeanCreationWithOAuthCredentials() {
    this.context.register(COSAutoConfiguration.class);
    TestPropertyValues.of("cos.endpoint=http://ibm.com")
        .and("cos.api-key=myApiKey")
        .applyTo(this.context);
    this.context.refresh();

    final AmazonS3ClientBuilder clientBuilder = this.context.getBean(AmazonS3ClientBuilder.class);
    assertThat(clientBuilder, is(not(nullValue())));

    final AWSCredentials credentials = clientBuilder.getCredentials().getCredentials();
    assertThat(credentials, instanceOf(BasicIBMOAuthCredentials.class));

    final BasicIBMOAuthCredentials oauth = (BasicIBMOAuthCredentials) credentials;
    assertThat(oauth.getApiKey(), is("myApiKey"));
    assertThat(oauth.getServiceInstanceId(), is(nullValue()));
  }

  @Test
  void clientBuilderBeanCreationWithOAuthCredentialsAndServiceInstanceId() {
    this.context.register(COSAutoConfiguration.class);
    TestPropertyValues.of("cos.endpoint=http://ibm.com")
        .and("cos.api-key=myApiKey")
        .and("cos.service-instance-id=myServiceInstanceId")
        .applyTo(this.context);
    this.context.refresh();

    final AmazonS3ClientBuilder clientBuilder = this.context.getBean(AmazonS3ClientBuilder.class);
    assertThat(clientBuilder, is(not(nullValue())));

    final AWSCredentials credentials = clientBuilder.getCredentials().getCredentials();
    assertThat(credentials, instanceOf(BasicIBMOAuthCredentials.class));

    final BasicIBMOAuthCredentials oauth = (BasicIBMOAuthCredentials) credentials;
    assertThat(oauth.getApiKey(), is("myApiKey"));
    assertThat(oauth.getServiceInstanceId(), is("myServiceInstanceId"));
  }

  @Test
  void clientBuilderBeanCreationWithHmacCredentials() {
    this.context.register(COSAutoConfiguration.class);
    TestPropertyValues.of("cos.endpoint=http://ibm.com")
        .and("cos.accessKey=myAccessKey")
        .and("cos.secret-key=mySecretKey")
        .applyTo(this.context);
    this.context.refresh();

    final AmazonS3ClientBuilder clientBuilder = this.context.getBean(AmazonS3ClientBuilder.class);
    assertThat(clientBuilder, is(not(nullValue())));

    final AWSCredentials credentials = clientBuilder.getCredentials().getCredentials();
    assertThat(credentials, instanceOf(BasicAWSCredentials.class));
    assertThat(credentials.getAWSAccessKeyId(), is("myAccessKey"));
    assertThat(credentials.getAWSSecretKey(), is("mySecretKey"));
  }

  @Test
  void clientBuilderBeanCreationWithOAuthAndHmacCredentials() {
    this.context.register(COSAutoConfiguration.class);
    TestPropertyValues.of("cos.endpoint=http://ibm.com")
        .and("cos.api-key=myApiKey")
        .and("cos.accessKey=myAccessKey")
        .and("cos.secret-key=mySecretKey")
        .applyTo(this.context);
    this.context.refresh();

    final AmazonS3ClientBuilder clientBuilder = this.context.getBean(AmazonS3ClientBuilder.class);
    assertThat(clientBuilder, is(not(nullValue())));

    final AWSCredentials credentials = clientBuilder.getCredentials().getCredentials();
    // OAuth takes precedence over Hmac credentials
    assertThat(credentials, instanceOf(BasicIBMOAuthCredentials.class));

    final BasicIBMOAuthCredentials oauth = (BasicIBMOAuthCredentials) credentials;
    assertThat(oauth.getApiKey(), is("myApiKey"));
    assertThat(oauth.getServiceInstanceId(), is(nullValue()));
  }

  @Test
  void clientBeanCreation() {
    this.context.register(COSAutoConfiguration.class);
    TestPropertyValues.of("cos.endpoint=http://ibm.com")
        .and("cos.api-key=myApiKey")
        .applyTo(this.context);
    this.context.refresh();

    final AmazonS3 client = this.context.getBean(AmazonS3.class);
    assertThat(client, is(not(nullValue())));
  }
}
