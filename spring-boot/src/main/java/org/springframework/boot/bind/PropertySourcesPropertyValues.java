/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.bind;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.util.Assert;
import org.springframework.validation.DataBinder;

/**
 * A {@link PropertyValues} implementation backed by a {@link PropertySources}, bridging
 * the two abstractions and allowing (for instance) a regular {@link DataBinder} to be
 * used with the latter.
 *
 * <p>由{@link PropertySources}支持的{@link PropertyValues}实现，
 * 桥接两个抽象并允许（例如）常规{@link DataBinder}与后者一起使用</p>
 *
 * @author Dave Syer
 * @author Phillip Webb
 */
public class PropertySourcesPropertyValues implements PropertyValues {

	private static final Pattern COLLECTION_PROPERTY = Pattern
			.compile("\\[(\\d+)\\](\\.\\S+)?");

	private final PropertySources propertySources;

	private final Collection<String> nonEnumerableFallbackNames;

	private final PropertyNamePatternsMatcher includes;

	private final Map<String, PropertyValue> propertyValues = new LinkedHashMap<String, PropertyValue>();

	private final ConcurrentHashMap<String, PropertySource<?>> collectionOwners = new ConcurrentHashMap<String, PropertySource<?>>();

	private final boolean resolvePlaceholders;

	/**
	 * Create a new PropertyValues from the given PropertySources.
	 * @param propertySources a PropertySources instance
	 */
	public PropertySourcesPropertyValues(PropertySources propertySources) {
		this(propertySources, true);
	}

	/**
	 * Create a new PropertyValues from the given PropertySources that will optionally
	 * resolve placeholders.
	 * @param propertySources a PropertySources instance
	 * @param resolvePlaceholders {@code true} if placeholders should be resolved.
	 * @since 1.5.2
	 */
	public PropertySourcesPropertyValues(PropertySources propertySources,
			boolean resolvePlaceholders) {
		this(propertySources, (Collection<String>) null, PropertyNamePatternsMatcher.ALL,
				resolvePlaceholders);
	}

	/**
	 * Create a new PropertyValues from the given PropertySources.
	 * @param propertySources a PropertySources instance
	 * @param includePatterns property name patterns to include from system properties and
	 * environment variables
	 * @param nonEnumerableFallbackNames the property names to try in lieu of an
	 * {@link EnumerablePropertySource}.
	 */
	public PropertySourcesPropertyValues(PropertySources propertySources,
			Collection<String> includePatterns,
			Collection<String> nonEnumerableFallbackNames) {
		this(propertySources, nonEnumerableFallbackNames,
				new PatternPropertyNamePatternsMatcher(includePatterns), true);
	}

	/**
	 * Create a new PropertyValues from the given PropertySources.
	 * @param propertySources a PropertySources instance
	 * @param nonEnumerableFallbackNames the property names to try in lieu of an
	 * {@link EnumerablePropertySource}. 要使用的属性名称尝试代替Enumerable PropertySource
	 * @param includes the property name patterns to include  属性名模式
	 * @param resolvePlaceholders flag to indicate the placeholders should be resolved 声明占位符应该被被解析
	 */
	PropertySourcesPropertyValues(PropertySources propertySources,
			Collection<String> nonEnumerableFallbackNames,
			PropertyNamePatternsMatcher includes, boolean resolvePlaceholders) {
		Assert.notNull(propertySources, "PropertySources must not be null");
		Assert.notNull(includes, "Includes must not be null");
		this.propertySources = propertySources; //属性源
		this.nonEnumerableFallbackNames = nonEnumerableFallbackNames;
		this.includes = includes;
		this.resolvePlaceholders = resolvePlaceholders;
		//构建属性源解析器
		PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(
				propertySources);
		//处理属性源
		for (PropertySource<?> source : propertySources) {
			processPropertySource(source, resolver);
		}
	}

	/**
	 * 处理属性源
	 * @param source
	 * @param resolver
	 */
	private void processPropertySource(PropertySource<?> source,
			PropertySourcesPropertyResolver resolver) {
		if (source instanceof CompositePropertySource) {//组合属性源
			processCompositePropertySource((CompositePropertySource) source, resolver); //递归进行处理
		}
		else if (source instanceof EnumerablePropertySource) {//处理可以枚举的属性源
			processEnumerablePropertySource((EnumerablePropertySource<?>) source,
					resolver, this.includes);
		}
		else {
			processNonEnumerablePropertySource(source, resolver); //处理不可以枚举的属性源，通常属性源应该是可以枚举的
		}
	}

	private void processCompositePropertySource(CompositePropertySource source,
			PropertySourcesPropertyResolver resolver) {
		for (PropertySource<?> nested : source.getPropertySources()) {
			processPropertySource(nested, resolver);
		}
	}

	/**
	 * 处理可以枚举的属性源
	 * @param source
	 * @param resolver
	 * @param includes
	 */
	private void processEnumerablePropertySource(EnumerablePropertySource<?> source,
			PropertySourcesPropertyResolver resolver,
			PropertyNamePatternsMatcher includes) {
		//属性源的属性名称大于0
		if (source.getPropertyNames().length > 0) {
			for (String propertyName : source.getPropertyNames()) { //变量相关的名字
				if (includes.matches(propertyName)) {//是否可以匹配
					Object value = getEnumerableProperty(source, resolver, propertyName); //获得相关的值
					putIfAbsent(propertyName, value, source);
				}
			}
		}
	}

	/**
	 * 获得属性源中名字为propertyName的值，并进行相应的解析。
	 * @param source
	 * @param resolver
	 * @param propertyName
	 * @return
	 */
	private Object getEnumerableProperty(EnumerablePropertySource<?> source,
			PropertySourcesPropertyResolver resolver, String propertyName) {
		try {
			if (this.resolvePlaceholders) {
				return resolver.getProperty(propertyName, Object.class);
			}
		}
		catch (RuntimeException ex) {
			// Probably could not resolve placeholders, ignore it here
		}
		return source.getProperty(propertyName);
	}

	private void processNonEnumerablePropertySource(PropertySource<?> source,
			PropertySourcesPropertyResolver resolver) {
		// We can only do exact matches for non-enumerable property names, but
		// that's better than nothing...
		// 我们只能对非可枚举的属性名称进行精确匹配，但这比没有好...
		if (this.nonEnumerableFallbackNames == null) {
			return;
		}
		//遍历相应的属性，只对我们设置的不可以枚举的属性进行设置。
		for (String propertyName : this.nonEnumerableFallbackNames) {
			if (!source.containsProperty(propertyName)) {
				continue;
			}
			//得到值后
			Object value = null;
			try {
				//就尝试去解析
				value = resolver.getProperty(propertyName, Object.class);
			}
			catch (RuntimeException ex) {
				// Probably could not convert to Object, weird, but ignorable
			}
			//尝试从源中获得值
			if (value == null) {
				value = source.getProperty(propertyName.toUpperCase());
			}
			//尝试放入，乳沟有的话
			putIfAbsent(propertyName, value, source);
		}
	}

	@Override
	public PropertyValue[] getPropertyValues() {
		Collection<PropertyValue> values = this.propertyValues.values();
		return values.toArray(new PropertyValue[values.size()]);
	}

	@Override
	public PropertyValue getPropertyValue(String propertyName) {
		PropertyValue propertyValue = this.propertyValues.get(propertyName);
		if (propertyValue != null) {
			return propertyValue;
		}
		for (PropertySource<?> source : this.propertySources) {
			Object value = source.getProperty(propertyName);
			propertyValue = putIfAbsent(propertyName, value, source);
			if (propertyValue != null) {
				return propertyValue;
			}
		}
		return null;
	}

	/**
	 * 如果不存在就放入。
	 * @param propertyName
	 * @param value
	 * @param source
	 * @return
	 */
	private PropertyValue putIfAbsent(String propertyName, Object value,
			PropertySource<?> source) {
		if (value != null && !this.propertyValues.containsKey(propertyName)) {
			PropertySource<?> collectionOwner = this.collectionOwners.putIfAbsent(
					COLLECTION_PROPERTY.matcher(propertyName).replaceAll("[]"), source);
			if (collectionOwner == null || collectionOwner == source) {
				//包装的属性值。有名字，值，原始名字，来自的属性源构成。
				PropertyValue propertyValue = new OriginCapablePropertyValue(propertyName,
						value, propertyName, source);
				this.propertyValues.put(propertyName, propertyValue);
				return propertyValue;
			}
		}
		return null;
	}

	@Override
	public PropertyValues changesSince(PropertyValues old) {
		MutablePropertyValues changes = new MutablePropertyValues();
		// for each property value in the new set
		for (PropertyValue newValue : getPropertyValues()) {
			// if there wasn't an old one, add it
			PropertyValue oldValue = old.getPropertyValue(newValue.getName());
			if (oldValue == null || !oldValue.equals(newValue)) {
				changes.addPropertyValue(newValue);
			}
		}
		return changes;
	}

	@Override
	public boolean contains(String propertyName) {
		return getPropertyValue(propertyName) != null;
	}

	@Override
	public boolean isEmpty() {
		return this.propertyValues.isEmpty();
	}

}
