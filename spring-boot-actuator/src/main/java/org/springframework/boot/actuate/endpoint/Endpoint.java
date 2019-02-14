/*
 * Copyright 2012-2015 the original author or authors.
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

package org.springframework.boot.actuate.endpoint;

/**
 * An endpoint that can be used to expose useful information to operations. Usually
 * exposed via Spring MVC but could also be exposed using some other technique. Consider
 * extending {@link AbstractEndpoint} if you are developing your own endpoint.
 *
 * <p>
 *     可用于向操作公开有用信息的端点。
 * </p>
 * <p>
 *     通常通过Spring MVC暴露，但也可以使用其他技术暴露。
 * </p>
 * <p>
 *     如果您正在开发自己的endpoint，请考虑继承{@link AbstractEndpoint}。
 * </p>
 *
 * @param <T> the endpoint data type
 * @author Phillip Webb
 * @author Dave Syer
 * @author Christian Dupuis
 * @see AbstractEndpoint
 */
public interface Endpoint<T> {

	/**
	 * The logical ID of the endpoint. Must only contain simple letters, numbers and '_'
	 * characters (i.e. a {@literal "\w"} regex).
	 *
	 * <p>
	 *     endpoint的逻辑ID。 只能包含简单的字母，数字和“_”字符（即“\ w”正则表达式）。
	 * </p>
	 * @return the endpoint ID
	 */
	String getId();

	/**
	 * Return if the endpoint is enabled.
	 * <p>
	 *     返回endpoint是否启用。
	 * </p>
	 * @return if the endpoint is enabled
	 */
	boolean isEnabled();

	/**
	 * Return if the endpoint is sensitive, i.e. may return data that the average user
	 * should not see. Mappings can use this as a security hint.
	 * <p>
	 *     返回endpoint是否是敏感的，即可以返回普通用户不应该看到的数据。 映射可以使用这个作为安全提示。
	 * </p>
	 * @return if the endpoint is sensitive
	 */
	boolean isSensitive();

	/**
	 * Called to invoke the endpoint.
	 * <p>
	 *     调用来调用endpoint。
	 * </p>
	 * @return the results of the invocation
	 */
	T invoke();

}
