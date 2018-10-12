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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.BasicAWSCredentials;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import com.ibm.cos.spring.boot.COSAutoConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.SpringVersion;

@SuppressWarnings("deprecation")
public class AutoConfigurationTest {

  private AnnotationConfigApplicationContext context;

  private static String SPRING_BOOT_TEST_VERSION = System.getProperty("spring.boot.test.version");
  private static String SPRING_TEST_VERSION = System.getProperty("spring.test.version");

  @Before
  public void setUp() {
    this.context = new AnnotationConfigApplicationContext();
  }

  @After
  public void cleanup() {
    if (this.context != null) {
      this.context.close();
    }
  }

  @Test
  public void bootVersionCheck() {
    // system property will only be set when invoking tests via surefire, not an ide
    assumeTrue(SPRING_BOOT_TEST_VERSION != null);
    assertEquals(SPRING_BOOT_TEST_VERSION, SpringBootVersion.getVersion());
  }

  @Test
  public void frameworkVersionCheck() {
    // system property will only be set when invoking tests via surefire, not an ide
    assumeTrue(SPRING_TEST_VERSION != null);
    assertEquals(SPRING_TEST_VERSION, SpringVersion.getVersion());
  }

  @Test(expected = BeanCreationException.class)
  public void clientBuilderMissingEndpoint() {
    this.context.register(COSAutoConfiguration.class);
    EnvironmentTestUtils.addEnvironment(
        this.context, "cos.location=myLocation", "cos.api-key=myApiKey");
    this.context.refresh();
  }

  @Test(expected = BeanCreationException.class)
  public void clientBuilderInvalidEndpoint() {
    this.context.register(COSAutoConfiguration.class);
    EnvironmentTestUtils.addEnvironment(
        this.context, "cos.endpoint=notAUrl", "cos.location=us-south", "cos.api-key=myApiKey");
    this.context.refresh();
  }

  @Test(expected = BeanCreationException.class)
  public void clientBuilderMissingCredentials() {
    this.context.register(COSAutoConfiguration.class);
    EnvironmentTestUtils.addEnvironment(
        this.context, "cos.endpoint=http://ibm.com", "cos.location=us-south");
    this.context.refresh();
  }

  @Test(expected = BeanCreationException.class)
  public void clientBuilderMissingAccessKey() {
    this.context.register(COSAutoConfiguration.class);
    EnvironmentTestUtils.addEnvironment(
        this.context,
        "cos.endpoint=http://ibm.com",
        "cos.location=us-south",
        "cos.secret-key=mySecretKey");
    this.context.refresh();
  }

  @Test(expected = BeanCreationException.class)
  public void clientBuilderMissingSecretKey() {
    this.context.register(COSAutoConfiguration.class);
    EnvironmentTestUtils.addEnvironment(
        this.context,
        "cos.endpoint=http://ibm.com",
        "cos.location=us-south",
        "cos.accessKey=myAccessKey");
    this.context.refresh();
  }

  @Test
  public void clientBuilderBeanCreationWithEndpoint() {
    this.context.register(COSAutoConfiguration.class);
    EnvironmentTestUtils.addEnvironment(
        this.context, "cos.endpoint=http://ibm.com", "cos.api-key=myApiKey");
    this.context.refresh();

    AmazonS3ClientBuilder clientBuilder = this.context.getBean(AmazonS3ClientBuilder.class);
    assertThat(clientBuilder, is(not(nullValue())));

    EndpointConfiguration endpoint = clientBuilder.getEndpoint();
    assertThat(endpoint.getServiceEndpoint(), is("http://ibm.com"));
    assertThat(endpoint.getSigningRegion(), is(nullValue()));
  }

  @Test
  public void clientBuilderBeanCreationWithEndpointAndLocation() {
    this.context.register(COSAutoConfiguration.class);
    EnvironmentTestUtils.addEnvironment(
        this.context,
        "cos.endpoint=http://ibm.com",
        "cos.location=us-south",
        "cos.api-key=myApiKey");
    this.context.refresh();

    AmazonS3ClientBuilder clientBuilder = this.context.getBean(AmazonS3ClientBuilder.class);
    assertThat(clientBuilder, is(not(nullValue())));

    EndpointConfiguration endpoint = clientBuilder.getEndpoint();
    assertThat(endpoint.getServiceEndpoint(), is("http://ibm.com"));
    assertThat(endpoint.getSigningRegion(), is("us-south"));
  }

  @Test
  public void clientBuilderBeanCreationWithOAuthCredentials() {
    this.context.register(COSAutoConfiguration.class);
    EnvironmentTestUtils.addEnvironment(
        this.context, "cos.endpoint=http://ibm.com", "cos.api-key=myApiKey");
    this.context.refresh();

    AmazonS3ClientBuilder clientBuilder = this.context.getBean(AmazonS3ClientBuilder.class);
    assertThat(clientBuilder, is(not(nullValue())));

    AWSCredentials credentials = clientBuilder.getCredentials().getCredentials();
    assertThat(credentials, instanceOf(BasicIBMOAuthCredentials.class));

    BasicIBMOAuthCredentials oauth = (BasicIBMOAuthCredentials) credentials;
    assertThat(oauth.getApiKey(), is("myApiKey"));
    assertThat(oauth.getServiceInstanceId(), is(nullValue()));
  }

  @Test
  public void clientBuilderBeanCreationWithOAuthCredentialsAndServiceInstanceId() {
    this.context.register(COSAutoConfiguration.class);
    EnvironmentTestUtils.addEnvironment(
        this.context,
        "cos.endpoint=http://ibm.com",
        "cos.api-key=myApiKey",
        "cos.service-instance-id=myServiceInstanceId");
    this.context.refresh();

    AmazonS3ClientBuilder clientBuilder = this.context.getBean(AmazonS3ClientBuilder.class);
    assertThat(clientBuilder, is(not(nullValue())));

    AWSCredentials credentials = clientBuilder.getCredentials().getCredentials();
    assertThat(credentials, instanceOf(BasicIBMOAuthCredentials.class));

    BasicIBMOAuthCredentials oauth = (BasicIBMOAuthCredentials) credentials;
    assertThat(oauth.getApiKey(), is("myApiKey"));
    assertThat(oauth.getServiceInstanceId(), is("myServiceInstanceId"));
  }

  @Test
  public void clientBuilderBeanCreationWithHmacCredentials() {
    this.context.register(COSAutoConfiguration.class);
    EnvironmentTestUtils.addEnvironment(
        this.context,
        "cos.endpoint=http://ibm.com",
        "cos.accessKey=myAccessKey",
        "cos.secret-key=mySecretKey");
    this.context.refresh();

    AmazonS3ClientBuilder clientBuilder = this.context.getBean(AmazonS3ClientBuilder.class);
    assertThat(clientBuilder, is(not(nullValue())));

    AWSCredentials credentials = clientBuilder.getCredentials().getCredentials();
    assertThat(credentials, instanceOf(BasicAWSCredentials.class));
    assertThat(credentials.getAWSAccessKeyId(), is("myAccessKey"));
    assertThat(credentials.getAWSSecretKey(), is("mySecretKey"));
  }

  @Test
  public void clientBuilderBeanCreationWithOAuthAndHmacCredentials() {
    this.context.register(COSAutoConfiguration.class);
    EnvironmentTestUtils.addEnvironment(
        this.context,
        "cos.endpoint=http://ibm.com",
        "cos.api-key=myApiKey",
        "cos.accessKey=myAccessKey",
        "cos.secret-key=mySecretKey");
    this.context.refresh();

    AmazonS3ClientBuilder clientBuilder = this.context.getBean(AmazonS3ClientBuilder.class);
    assertThat(clientBuilder, is(not(nullValue())));

    AWSCredentials credentials = clientBuilder.getCredentials().getCredentials();
    // OAuth takes precedence over Hmac credentials
    assertThat(credentials, instanceOf(BasicIBMOAuthCredentials.class));

    BasicIBMOAuthCredentials oauth = (BasicIBMOAuthCredentials) credentials;
    assertThat(oauth.getApiKey(), is("myApiKey"));
    assertThat(oauth.getServiceInstanceId(), is(nullValue()));
  }

  @Test
  public void clientBeanCreation() {
    this.context.register(COSAutoConfiguration.class);
    EnvironmentTestUtils.addEnvironment(
        this.context, "cos.endpoint=http://ibm.com", "cos.api-key=myApiKey");
    this.context.refresh();

    AmazonS3 client = this.context.getBean(AmazonS3.class);
    assertThat(client, is(not(nullValue())));
  }
}
