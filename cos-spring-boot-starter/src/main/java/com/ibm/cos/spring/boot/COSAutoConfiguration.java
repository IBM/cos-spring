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

package com.ibm.cos.spring.boot;

import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.ClientConfigurationFactory;
import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider;
import com.ibm.cloud.objectstorage.auth.BasicAWSCredentials;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(COSConfigurationProperties.class)
public class COSAutoConfiguration {

  @Autowired private COSConfigurationProperties config;

  @Bean
  @ConditionalOnMissingBean
  public AmazonS3ClientBuilder clientBuilder() {
    if (config.getEndpoint() == null) {
      throw new NullPointerException("endpoint must not be null");
    }

    return AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(
            new EndpointConfiguration(config.getEndpoint().toString(), config.getLocation()))
        .withCredentials(new AWSStaticCredentialsProvider(createAWSCredentials()))
        .withClientConfiguration(createClientConfig())
        .withPathStyleAccessEnabled(true);
  }

  private AWSCredentials createAWSCredentials() {
    if (config.getApiKey() != null) {
      return new BasicIBMOAuthCredentials(config.getApiKey(), config.getServiceInstanceId());
    }
    return new BasicAWSCredentials(config.getAccessKey(), config.getSecretKey());
  }

  private ClientConfiguration createClientConfig() {
    String suffix = String.format("spring_boot/%s", SpringBootVersion.getVersion());
    return new ClientConfigurationFactory().getConfig().withUserAgentSuffix(suffix);
  }

  @Bean
  @ConditionalOnMissingBean
  public AmazonS3 client(AmazonS3ClientBuilder builder) {
    return builder.build();
  }
}
