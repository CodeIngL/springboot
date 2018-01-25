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

package org.springframework.boot.env;

import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * Allows for customization of the application's {@link Environment} prior to the
 * application context being refreshed.
 * <p>
 * EnvironmentPostProcessor implementations have to be registered in
 * {@code META-INF/spring.factories}, using the fully qualified name of this class as the
 * key.
 * <p>
 * {@code EnvironmentPostProcessor} processors are encouraged to detect whether Spring's
 * {@link org.springframework.core.Ordered Ordered} interface has been implemented or if
 * the @{@link org.springframework.core.annotation.Order Order} annotation is present and
 * to sort instances accordingly if so prior to invocation.
 *
 * <p>
 *     允许在刷新应用程序上下文之前自定义应用程序的环境。
 * </p>
 * <p>
 *     EnvironmentPostProcessor实现必须在META-INF/spring.factories中注册，使用这个类的完全限定名作为键。
 * </p>
 * <p>
 *     鼓励EnvironmentPostProcessor处理器检测是否已经实现了Spring的Ordered接口，或者是否存在@Order注释，并在调用之前对其进行排序。
 * </p>
 *
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @since 1.3.0
 */
public interface EnvironmentPostProcessor {

	/**
	 * Post-process the given {@code environment}.
	 * @param environment the environment to post-process
	 * @param application the application to which the environment belongs
	 */
	void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application);

}
