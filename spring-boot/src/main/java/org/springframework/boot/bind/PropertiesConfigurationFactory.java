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

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.PropertySources;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

/**
 * Validate some {@link Properties} (or optionally {@link PropertySources}) by binding
 * them to an object of a specified type and then optionally running a {@link Validator}
 * over it.
 *
 * <p>通过将一些{@link Properties}
 * （或可选的{@link PropertySources}）绑定到指定类型的对象，然后可选地在其上运行{@link Validator}来验证它们。</p>
 *
 * @param <T> the target type
 * @author Dave Syer
 */
public class PropertiesConfigurationFactory<T>
		implements FactoryBean<T>, MessageSourceAware, InitializingBean {

	private static final char[] EXACT_DELIMITERS = { '_', '.', '[' };

	private static final char[] TARGET_NAME_DELIMITERS = { '_', '.' };

	private static final Log logger = LogFactory
			.getLog(PropertiesConfigurationFactory.class);

	private boolean ignoreUnknownFields = true;

	private boolean ignoreInvalidFields;

	private boolean exceptionIfInvalid = true;

	private PropertySources propertySources;

	private final T target;

	private Validator validator;

	private MessageSource messageSource;

	private boolean hasBeenBound = false;

	private boolean ignoreNestedProperties = false;

	private String targetName;

	private ConversionService conversionService;

	private boolean resolvePlaceholders = true;

	/**
	 * Create a new {@link PropertiesConfigurationFactory} instance.
	 * @param target the target object to bind too
	 * @see #PropertiesConfigurationFactory(Class)
	 */
	public PropertiesConfigurationFactory(T target) {
		Assert.notNull(target, "target must not be null");
		this.target = target;
	}

	/**
	 * Create a new {@link PropertiesConfigurationFactory} instance.
	 * @param type the target type
	 * @see #PropertiesConfigurationFactory(Class)
	 */
	@SuppressWarnings("unchecked")
	public PropertiesConfigurationFactory(Class<?> type) {
		Assert.notNull(type, "type must not be null");
		this.target = (T) BeanUtils.instantiate(type);
	}

	/**
	 * Flag to disable binding of nested properties (i.e. those with period separators in
	 * their paths). Can be useful to disable this if the name prefix is empty and you
	 * don't want to ignore unknown fields.
	 * @param ignoreNestedProperties the flag to set (default false)
	 */
	public void setIgnoreNestedProperties(boolean ignoreNestedProperties) {
		this.ignoreNestedProperties = ignoreNestedProperties;
	}

	/**
	 * Set whether to ignore unknown fields, that is, whether to ignore bind parameters
	 * that do not have corresponding fields in the target object.
	 * <p>
	 * Default is "true". Turn this off to enforce that all bind parameters must have a
	 * matching field in the target object.
	 * @param ignoreUnknownFields if unknown fields should be ignored
	 */
	public void setIgnoreUnknownFields(boolean ignoreUnknownFields) {
		this.ignoreUnknownFields = ignoreUnknownFields;
	}

	/**
	 * Set whether to ignore invalid fields, that is, whether to ignore bind parameters
	 * that have corresponding fields in the target object which are not accessible (for
	 * example because of null values in the nested path).
	 * <p>
	 * Default is "false". Turn this on to ignore bind parameters for nested objects in
	 * non-existing parts of the target object graph.
	 * @param ignoreInvalidFields if invalid fields should be ignored
	 */
	public void setIgnoreInvalidFields(boolean ignoreInvalidFields) {
		this.ignoreInvalidFields = ignoreInvalidFields;
	}

	/**
	 * Set the target name.
	 * @param targetName the target name
	 */
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	/**
	 * Set the message source.
	 * @param messageSource the message source
	 */
	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * Set the property sources.
	 * @param propertySources the property sources
	 */
	public void setPropertySources(PropertySources propertySources) {
		this.propertySources = propertySources;
	}

	/**
	 * Set the conversion service.
	 * @param conversionService the conversion service
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	/**
	 * Set the validator.
	 * @param validator the validator
	 */
	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	/**
	 * Set a flag to indicate that an exception should be raised if a Validator is
	 * available and validation fails.
	 * @param exceptionIfInvalid the flag to set
	 * @deprecated as of 1.5, do not specify a {@link Validator} if validation should not
	 * occur
	 */
	@Deprecated
	public void setExceptionIfInvalid(boolean exceptionIfInvalid) {
		this.exceptionIfInvalid = exceptionIfInvalid;
	}

	/**
	 * Flag to indicate that placeholders should be replaced during binding. Default is
	 * true.
	 * @param resolvePlaceholders flag value
	 */
	public void setResolvePlaceholders(boolean resolvePlaceholders) {
		this.resolvePlaceholders = resolvePlaceholders;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		bindPropertiesToTarget();
	}

	@Override
	public Class<?> getObjectType() {
		if (this.target == null) {
			return Object.class;
		}
		return this.target.getClass();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public T getObject() throws Exception {
		if (!this.hasBeenBound) {
			bindPropertiesToTarget();
		}
		return this.target;
	}

	/**
	 * 进行绑定
	 * @throws BindException
	 */
	public void bindPropertiesToTarget() throws BindException {
		Assert.state(this.propertySources != null, "PropertySources should not be null");//确定属性源不会为空
		try {
			if (logger.isTraceEnabled()) {
				logger.trace("Property Sources: " + this.propertySources);
			}
			//设计标记
			this.hasBeenBound = true;
			doBindPropertiesToTarget();
		}
		catch (BindException ex) {
			if (this.exceptionIfInvalid) {
				throw ex;
			}
			PropertiesConfigurationFactory.logger.error("Failed to load Properties validation bean. "
							+ "Your Properties may be invalid.", ex);
		}
	}

	/**
	 * 真正的绑定过程
	 * @throws BindException
	 */
	private void doBindPropertiesToTarget() throws BindException {
		//存在名字使用不同的构造函数形式
		RelaxedDataBinder dataBinder = (this.targetName != null
				? new RelaxedDataBinder(this.target, this.targetName)
				: new RelaxedDataBinder(this.target));
		//存在校验器，为数据绑定设置校验器
		if (this.validator != null
				&& this.validator.supports(dataBinder.getTarget().getClass())) {
			dataBinder.setValidator(this.validator);
		}
		//存在转换器，为数据绑定设置转换器
		if (this.conversionService != null) {
			dataBinder.setConversionService(this.conversionService);
		}
		//设置自动增长的最大值
		dataBinder.setAutoGrowCollectionLimit(Integer.MAX_VALUE);
		dataBinder.setIgnoreNestedProperties(this.ignoreNestedProperties);
		dataBinder.setIgnoreInvalidFields(this.ignoreInvalidFields);
		dataBinder.setIgnoreUnknownFields(this.ignoreUnknownFields);
		//自定义绑定
		customizeBinder(dataBinder);
		//存在名字，装换为灵活的名字。
		Iterable<String> relaxedTargetNames = getRelaxedTargetNames();
		//获得名字增加相关的前缀
		Set<String> names = getNames(relaxedTargetNames);
		//获得属性源
		PropertyValues propertyValues = getPropertySourcesPropertyValues(names,
				relaxedTargetNames);
		//进行绑定的校验
		dataBinder.bind(propertyValues);
		if (this.validator != null) {
			dataBinder.validate();
		}
		checkForBindingErrors(dataBinder);
	}

	/**
	 * 对targetName进行转换，使得更加的灵活
	 * @return
	 */
	private Iterable<String> getRelaxedTargetNames() {
		return (this.target != null && StringUtils.hasLength(this.targetName)
				? new RelaxedNames(this.targetName) : null);
	}

	/**
	 * 为目标类的字段名字添加前缀，
	 * @param prefixes
	 * @return
	 */
	private Set<String> getNames(Iterable<String> prefixes) {
		Set<String> names = new LinkedHashSet<String>();
		if (this.target != null) {
			//获得类属性描述符
			PropertyDescriptor[] descriptors = BeanUtils
					.getPropertyDescriptors(this.target.getClass());
			//遍历属性描述
			for (PropertyDescriptor descriptor : descriptors) {
				String name = descriptor.getName();
				if (!name.equals("class")) {//不是class 说明是正常的
					//大写转换为-加小写
					RelaxedNames relaxedNames = RelaxedNames.forCamelCase(name);
					if (prefixes == null) {//前缀为空加入集合中
						for (String relaxedName : relaxedNames) {
							names.add(relaxedName);
						}
					}
					else {
						for (String prefix : prefixes) {//遍历前缀，添加相应的前缀
							for (String relaxedName : relaxedNames) {
								names.add(prefix + "." + relaxedName);
								names.add(prefix + "_" + relaxedName);
							}
						}
					}
				}
			}
		}
		return names;
	}

	/**
	 *
	 * 根据名字集合，获得属性源
	 * @param names 实际的名字
	 * @param relaxedTargetNames 前缀
	 * @return
	 */
	private PropertyValues getPropertySourcesPropertyValues(Set<String> names,
			Iterable<String> relaxedTargetNames) {
		//获得模式匹配器
		PropertyNamePatternsMatcher includes = getPropertyNamePatternsMatcher(names,
				relaxedTargetNames);
		//返回一个构建属性源属性值
		return new PropertySourcesPropertyValues(this.propertySources, names, includes,
				this.resolvePlaceholders);
	}

	private PropertyNamePatternsMatcher getPropertyNamePatternsMatcher(Set<String> names,
			Iterable<String> relaxedTargetNames) {
		if (this.ignoreUnknownFields && !isMapTarget()) {
			// Since unknown fields are ignored we can filter them out early to save
			// unnecessary calls to the PropertySource.
			// 由于忽略了未知字段，我们可以提前过滤它们以节省对PropertySource的不必要调用。
			return new DefaultPropertyNamePatternsMatcher(EXACT_DELIMITERS, true, names);
		}
		if (relaxedTargetNames != null) {
			// We can filter properties to those starting with the target name, but
			// we can't do a complete filter since we need to trigger the
			// unknown fields check
			// 我们可以将属性过滤到以目标名称开头的属性，但我们无法进行完整的过滤，因为我们需要触发未知字段检查
			Set<String> relaxedNames = new HashSet<String>();
			for (String relaxedTargetName : relaxedTargetNames) {
				relaxedNames.add(relaxedTargetName);
			}
			return new DefaultPropertyNamePatternsMatcher(TARGET_NAME_DELIMITERS, true,
					relaxedNames);
		}
		// Not ideal, we basically can't filter anything
		// 不理想，我们基本上无法过滤任何东西
		return PropertyNamePatternsMatcher.ALL;
	}

	private boolean isMapTarget() {
		return this.target != null && Map.class.isAssignableFrom(this.target.getClass());
	}

	private void checkForBindingErrors(RelaxedDataBinder dataBinder)
			throws BindException {
		BindingResult errors = dataBinder.getBindingResult();
		if (errors.hasErrors()) {
			logger.error("Properties configuration failed validation");
			for (ObjectError error : errors.getAllErrors()) {
				logger.error(
						this.messageSource != null
								? this.messageSource.getMessage(error,
										Locale.getDefault()) + " (" + error + ")"
								: error);
			}
			if (this.exceptionIfInvalid) {
				throw new BindException(errors);
			}
		}
	}

	/**
	 * Customize the data binder.
	 * @param dataBinder the data binder that will be used to bind and validate
	 */
	protected void customizeBinder(DataBinder dataBinder) {
	}

}
