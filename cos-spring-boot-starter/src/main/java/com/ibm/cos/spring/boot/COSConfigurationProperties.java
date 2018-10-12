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

import java.net.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * COS Configuration properties. For authentication, either apiKey (IAM) or accessKey and secretKey
 * (HMAC) are required
 */
@ConfigurationProperties(prefix = "cos")
public class COSConfigurationProperties {

  /** COS service endpoint */
  private URL endpoint;

  /** location of COS Bucket */
  private String location;

  /** IAM API Key */
  private String apiKey;

  /** (optional) COS service instance id for Bucket listing and Bucket creation */
  private String serviceInstanceId;

  /** HMAC Access Key */
  private String accessKey;

  /** HMAC Secret Key */
  private String secretKey;

  public URL getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(URL endpoint) {
    this.endpoint = endpoint;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getServiceInstanceId() {
    return serviceInstanceId;
  }

  public void setServiceInstanceId(String serviceInstanceId) {
    this.serviceInstanceId = serviceInstanceId;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }
}
