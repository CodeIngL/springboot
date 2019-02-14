/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.context.properties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Import selector that sets up binding of external properties to configuration classes
 * (see {@link ConfigurationProperties}). It either registers a
 * {@link ConfigurationProperties} bean or not, depending on whether the enclosing
 * {@link EnableConfigurationProperties} explicitly declares one. If none is declared then
 * a bean post processor will still kick in for any beans annotated as external
 * configuration. If one is declared then it a bean definition is registered with id equal
 * to the class name (thus an application context usually only contains one
 * {@link ConfigurationProperties} bean of each unique type).
 *
 *
 * <p>
 *     导入选择器，将外部属性绑定到配置类（请参阅ConfigurationProperties）。
 *     它要么注册一个ConfigurationProperties bean，要么不注册,取决于封闭的EnableConfigurationProperties是否显式声明一个。
 *     如果没有声明，那么一个bean后期处理器仍然会踢入注释为外部配置的任何bean。
 *     如果声明了一个bean，那么它的一个bean定义就会被注册，id等于类名（因此应用程序上下文通常只包含一个唯一类型的ConfigurationProperties bean）。
 * </p>
 *
 * @author Dave Syer
 * @author Christian Dupuis
 * @author Stephane Nicoll
 */
class EnableConfigurationPropertiesImportSelector implements ImportSelector {

	/**
	 * 根据导入的@Configuration类的AnnotationMetadata选择并返回应导入哪个类的名称。
	 * @param metadata 元数据
	 * @return
	 */
	@Override
	public String[] selectImports(AnnotationMetadata metadata) {
		MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(
				EnableConfigurationProperties.class.getName(), false);
		Object[] type = attributes == null ? null
				: (Object[]) attributes.getFirst("value");
		if (type == null || type.length == 0) {
			return new String[] {
					ConfigurationPropertiesBindingPostProcessorRegistrar.class.getName() };
		}
		return new String[] { ConfigurationPropertiesBeanRegistrar.class.getName(),
				ConfigurationPropertiesBindingPostProcessorRegistrar.class.getName() };
	}

	/**
	 * {@link ImportBeanDefinitionRegistrar} for configuration properties support.
	 * <p>
	 *     ImportBeanDefinitionRegistrar用于configuration properties支持。
	 * </p>
	 */
	public static class ConfigurationPropertiesBeanRegistrar
			implements ImportBeanDefinitionRegistrar {

		@Override
		public void registerBeanDefinitions(AnnotationMetadata metadata,
				BeanDefinitionRegistry registry) {
			MultiValueMap<String, Object> attributes = metadata
					.getAllAnnotationAttributes(
							EnableConfigurationProperties.class.getName(), false);
			List<Class<?>> types = collectClasses(attributes.get("value"));
			for (Class<?> type : types) {
				//获得前缀
				String prefix = extractPrefix(type);
				//名字为前缀-类型名字或者名字
				String name = (StringUtils.hasText(prefix) ? prefix + "-" + type.getName()
						: type.getName());
				//没有则注册
				if (!registry.containsBeanDefinition(name)) {
					registerBeanDefinition(registry, type, name);
				}
			}
		}

		private String extractPrefix(Class<?> type) {
			ConfigurationProperties annotation = AnnotationUtils.findAnnotation(type,
					ConfigurationProperties.class);
			if (annotation != null) {
				return annotation.prefix();
			}
			return "";
		}

		private List<Class<?>> collectClasses(List<Object> list) {
			ArrayList<Class<?>> result = new ArrayList<Class<?>>();
			for (Object object : list) {
				for (Object value : (Object[]) object) {
					if (value instanceof Class && value != void.class) {
						result.add((Class<?>) value);
					}
				}
			}
			return result;
		}

		private void registerBeanDefinition(BeanDefinitionRegistry registry,
				Class<?> type, String name) {
			BeanDefinitionBuilder builder = BeanDefinitionBuilder
					.genericBeanDefinition(type);
			AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
			registry.registerBeanDefinition(name, beanDefinition);

			ConfigurationProperties properties = AnnotationUtils.findAnnotation(type,
					ConfigurationProperties.class);
			Assert.notNull(properties,
					"No " + ConfigurationProperties.class.getSimpleName()
							+ " annotation found on  '" + type.getName() + "'.");
		}

	}

}
