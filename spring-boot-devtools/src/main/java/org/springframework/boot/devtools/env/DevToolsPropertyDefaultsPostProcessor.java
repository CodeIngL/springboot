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

package org.springframework.boot.devtools.env;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.devtools.restart.Restarter;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * {@link EnvironmentPostProcessor} to add properties that make sense when working at
 * development time.
 *
 * <P>{@link EnvironmentPostProcessor}用于添加在开发时工作时有意义的属性。</P>
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.3.0
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class DevToolsPropertyDefaultsPostProcessor implements EnvironmentPostProcessor {

	private static final Map<String, Object> PROPERTIES;


	/**
	 * devtool的关键就是，帮助用户在开发时排查相应的问题，
	 * 也就是说，需要禁用一些可能会造成问题的现象，或者暴露一些现象，
	 * 因此这里禁用很多关于缓存相关的配置
	 */
	static {
		Map<String, Object> devToolsProperties = new HashMap<String, Object>();
		devToolsProperties.put("spring.thymeleaf.cache", "false");
		devToolsProperties.put("spring.freemarker.cache", "false");
		devToolsProperties.put("spring.groovy.template.cache", "false");
		devToolsProperties.put("spring.mustache.cache", "false");
		devToolsProperties.put("server.session.persistent", "true");
		devToolsProperties.put("spring.h2.console.enabled", "true");
		devToolsProperties.put("spring.resources.cache-period", "0");
		devToolsProperties.put("spring.resources.chain.cache", "false");
		devToolsProperties.put("spring.template.provider.cache", "false");
		devToolsProperties.put("spring.mvc.log-resolved-exception", "true");
		devToolsProperties.put("server.jsp-servlet.init-parameters.development", "true");
		devToolsProperties.put("spring.reactor.stacktrace-mode.enabled", "true");
		PROPERTIES = Collections.unmodifiableMap(devToolsProperties);
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		//开启的另一个条件吗是本地的环境，并且有着相关的支持
		if (isLocalApplication(environment) && canAddProperties(environment)) {
			//添加对应的属性源
			PropertySource<?> propertySource = new MapPropertySource("refresh",
					PROPERTIES);
			//加入到环境中去。
			environment.getPropertySources().addLast(propertySource);
		}
	}

	private boolean isLocalApplication(ConfigurableEnvironment environment) {
		return environment.getPropertySources().get("remoteUrl") == null;
	}

	private boolean canAddProperties(Environment environment) {
		return isRestarterInitialized() || isRemoteRestartEnabled(environment);
	}

	private boolean isRestarterInitialized() {
		try {
			Restarter restarter = Restarter.getInstance();
			return (restarter != null && restarter.getInitialUrls() != null);
		}
		catch (Exception ex) {
			return false;
		}
	}

	private boolean isRemoteRestartEnabled(Environment environment) {
		RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(environment,
				"spring.devtools.remote.");
		return resolver.containsProperty("secret");
	}

}
