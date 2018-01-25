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

package org.springframework.boot.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * An {@link ApplicationListener} that halts application startup if the system file
 * encoding does not match an expected value set in the environment. By default has no
 * effect, but if you set {@code spring.mandatory_file_encoding} (or some camelCase or
 * UPPERCASE variant of that) to the name of a character encoding (e.g. "UTF-8") then this
 * initializer throws an exception when the {@code file.encoding} System property does not
 * equal it.
 *
 * <p>
 * The System property {@code file.encoding} is normally set by the JVM in response to the
 * {@code LANG} or {@code LC_ALL} environment variables. It is used (along with other
 * platform-dependent variables keyed off those environment variables) to encode JVM
 * arguments as well as file names and paths. In most cases you can override the file
 * encoding System property on the command line (with standard JVM features), but also
 * consider setting the {@code LANG} environment variable to an explicit
 * character-encoding value (e.g. "en_GB.UTF-8").
 *
 * <p>
 *     如果系统文件编码与环境中设置的期望值不匹配，ApplicationListener将暂停应用程序启动。
 *     默认情况下是没有效果的，
 *     但是如果你设置了spring.mandatory_file_encoding（或者一些camelCase或者UPPERCASE的变体）到一个字符编码的名字（比如“UTF-8”），
 *     那么这个初始化器会在file.encoding系统 财产不等于它。
 * <p> 系统属性file.encoding通常由JVM设置，以响应LANG或LC_ALL环境变量。
 * 它被用来（和其他依赖于平台的变量一起被键入这些环境变量）编码JVM参数以及文件名和路径。
 * 在大多数情况下，您可以在命令行（使用标准JVM功能）上覆盖文件编码System属性，但也可以考虑将LANG环境变量设置为明确的字符编码值（例如“en_GB.UTF-8”）。
 * </p>
 *
 * @author Dave Syer
 */
public class FileEncodingApplicationListener
		implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

	private static final Log logger = LogFactory
			.getLog(FileEncodingApplicationListener.class);

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(
				event.getEnvironment(), "spring.");
		if (resolver.containsProperty("mandatoryFileEncoding")) {
			String encoding = System.getProperty("file.encoding");
			String desired = resolver.getProperty("mandatoryFileEncoding");
			if (encoding != null && !desired.equalsIgnoreCase(encoding)) {
				logger.error("System property 'file.encoding' is currently '" + encoding
						+ "'. It should be '" + desired
						+ "' (as defined in 'spring.mandatoryFileEncoding').");
				logger.error("Environment variable LANG is '" + System.getenv("LANG")
						+ "'. You could use a locale setting that matches encoding='"
						+ desired + "'.");
				logger.error("Environment variable LC_ALL is '" + System.getenv("LC_ALL")
						+ "'. You could use a locale setting that matches encoding='"
						+ desired + "'.");
				throw new IllegalStateException(
						"The Java Virtual Machine has not been configured to use the "
								+ "desired default character encoding (" + desired
								+ ").");
			}
		}
	}

}
