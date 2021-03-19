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
         * 下面方法是将DubboConfigConfiguration内部类Single的bean定义注入到spring容器中，即把dubboConfigConfiguration.Single作为key，
         * value为对应的BeanDefinition，添加到Spring上下文DefaultListableBeanFactory的beanDefinitionMap中
         *
         * 解析dubbo启动类的时候，启动类上会有如下两个注解，
         * @EnableDubbo
         * @SpringBootApplication
         * 执行SpringApplication的run方法中，prepareContext会把启动类注入到spring的beanDefinitionMap中，然后执行refreshContext方法的时候，
         *执行到AbstractApplicationContext的refresh方法中的invokeBeanFactoryPostProcessors方法，该方法中会用ConfigurationClassPostProcessor处理器
         * 去解析配置类，会在spring的beanDefinitionMap中寻找candidates，只有启动类符合，然后就会解析启动类，而在解析启动类的过程中，连带它的注解以及子注解，
         * 以及注解通过@Import方式引入的配置或配置类都会进行解析，连带@Import的内容都会进行解析，比如会去执行@EnableDubboConfig注解
         * import的类DubboConfigConfigurationRegistrar的registerBeanDefinitions方法，即下面这个方法，这个方法会把DubboConfigConfiguration的内部类Single添加到beanDefinitionMap中，
         * 接着在ConfigurationClassPostProcessor的processConfigBeanDefinitions方法往下执行，会从Spring容器新增的beanDefinition中去接着寻找符合条件的candidates(直到没有符合条件的candidates才会跳出循环)，
         * 第二轮符合条件的candidates有DubboConfigConfiguration的两个内部类Single和Multiple，因此会把这个两个类当做配置类，接着去解析，同样会去解析它们的注解，
         * 而内部类Single上有注解@EnableConfigurationBeanBindings，因此会执行该注解import的ConfigurationBeanBindingsRegister类的registerBeanDefinitions方法，
         * 该方法中会把Single类上的注解包含的注解@EnableConfigurationBeanBinding中配置的type的值注入到spring容器的beanDefinitionMap中。后面getBean的时候会进行实例化，因此这些type对应的类
         * 都会注入成spring容器的bean。同理内部类Multiple是一样的。比如会把ApplicationConfig，RegistryConfig，ProtocolConfig等等注入成spring的bean，会执行getBean方法。
         */
        registerBeans(registry, DubboConfigConfiguration.Single.class);

        // Since 2.6.6 https://github.com/apache/dubbo/issues/3193
        if (multiple) {
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
