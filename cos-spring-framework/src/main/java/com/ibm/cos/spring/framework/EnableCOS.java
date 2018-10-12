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

package com.ibm.cos.spring.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * Add this annotation to an {@code @Configuration} class to expose an AmazonS3ClientBuilder and
 * AmazonS3 bean connected to the COS instance specified as Spring properties.
 *
 * <h1>Usage Examples</h1>
 *
 * <code>
 * &#064;Configuration
 * &#064;EnableCOS
 * public class COSConfig{}
 * </code>
 *
 * @author Robert Veitch
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({COSConfiguration.class, COSConfigurationProperties.class})
public @interface EnableCOS {}
