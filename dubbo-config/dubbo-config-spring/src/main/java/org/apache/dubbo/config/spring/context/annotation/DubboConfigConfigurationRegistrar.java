/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.config.spring.context.annotation;

import org.apache.dubbo.config.AbstractConfig;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import static com.alibaba.spring.util.AnnotatedBeanDefinitionRegistryUtils.registerBeans;
import static org.apache.dubbo.config.spring.util.DubboBeanUtils.registerCommonBeans;

/**
 * Dubbo {@link AbstractConfig Config} {@link ImportBeanDefinitionRegistrar register}, which order can be configured
 *
 * @see EnableDubboConfig
 * @see DubboConfigConfiguration
 * @see Ordered
 * @since 2.5.8
 */
public class DubboConfigConfigurationRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableDubboConfig.class.getName()));

        boolean multiple = attributes.getBoolean("multiple");

        // Single Config Bindings
        /**
         * 会把DubboConfigConfiguration内部类Single的bean定义注入到spring容器中，即
         * 把dubboConfigConfiguration.Single作为key，value为对应的BeanDefinition，
         * 添加到Spring上下文DefaultListableBeanFacotry的beanDefinitionMap中
         *
         * 解析dubbo启动类的时候，连带@Import的内容都会进行解析，因此会执行到下面这个方法，下面这个方法又会去注入DubboConfigConfiguration的内部类Single，
         * 将Single的时候注入到spring上下文DefaultListableBeanFactory的beanDefinitionMap过程中，会执行loadBeanDefinitionsFromRegistrars方法，
         * 该方法又会去获取它的importBeanDefinitionRegistrars属性，这个属性中包含了ConfigurationBeanBindingsRegister对象，
         * 会执行ConfigurationBeanBindingsRegister的registerBeanDefinitions方法，这个方法里面会获取注解@EnableConfigurationBeanBindings的value，是
         *注解@EnableConfigurationBeanBinding的集合，然后遍历这个集合，获取@EnableConfigurationBeanBinding注解的type值，
         * 会把type（type为org.apache.dubbo.config.ApplicationConfig）注入到spring的beanDefinitionMap中。后面就会实例化ApplicationConfig对象。
         */
        registerBeans(registry, DubboConfigConfiguration.Single.class);

        if (multiple) { // Since 2.6.6 https://github.com/apache/dubbo/issues/3193
            /**
             * 会把DubboConfigConfiguration内部类Multiple的bean定义注入到spring容器中，即
             * 把dubboConfigConfiguration.Multiple作为key，value为对应的BeanDefinition，
             * 添加到Spring上下文DefaultListableBeanFacotry的beanDefinitionMap中
             */
            registerBeans(registry, DubboConfigConfiguration.Multiple.class);
        }

        // Since 2.7.6
        registerCommonBeans(registry);
    }
}
