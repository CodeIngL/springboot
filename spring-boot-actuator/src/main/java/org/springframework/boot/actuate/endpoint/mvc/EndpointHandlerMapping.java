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

package org.springframework.boot.actuate.endpoint.mvc;

import java.util.Collection;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.HandlerMapping;

/**
 * {@link HandlerMapping} to map {@link Endpoint}s to URLs via {@link Endpoint#getId()}.
 * The semantics of {@code @RequestMapping} should be identical to a normal
 * {@code @Controller}, but the endpoints should not be annotated as {@code @Controller}
 * (otherwise they will be mapped by the normal MVC mechanisms).
 * <p>
 * One of the aims of the mapping is to support endpoints that work as HTTP endpoints but
 * can still provide useful service interfaces when there is no HTTP server (and no Spring
 * MVC on the classpath). Note that any endpoints having method signatures will break in a
 * non-servlet environment.
 *
 * <p>
 *     {@link HandlerMapping} 通过{@link Endpoint#getId()}将{@link Endpoint}映射到URL。
 *     {@code @RequestMapping}的语义应该与一个普通的@Controller相同，
 *     但是不应该把终结点注解为@Controller（否则它们将被正常的MVC机制映射）。
 * </p>
 * <p>
 *      映射的目标之一是支持作为HTTP端点的端点，
 *      但是当没有HTTP服务器（并且在类路径上没有Spring MVC）时仍然可以提供有用的服务接口。
 *      请注意，任何具有方法签名的端点将在非servlet环境中中断。
 * </p>
 *
 * @author Phillip Webb
 * @author Christian Dupuis
 * @author Dave Syer
 */
public class EndpointHandlerMapping extends AbstractEndpointHandlerMapping<MvcEndpoint> {

	/**
	 * Create a new {@link EndpointHandlerMapping} instance. All {@link Endpoint}s will be
	 * detected from the {@link ApplicationContext}. The endpoints will not accept CORS
	 * requests.
	 * @param endpoints the endpoints
	 */
	public EndpointHandlerMapping(Collection<? extends MvcEndpoint> endpoints) {
		super(endpoints);
	}

	/**
	 * Create a new {@link EndpointHandlerMapping} instance. All {@link Endpoint}s will be
	 * detected from the {@link ApplicationContext}. The endpoints will accepts CORS
	 * requests based on the given {@code corsConfiguration}.
	 * @param endpoints the endpoints
	 * @param corsConfiguration the CORS configuration for the endpoints
	 * @since 1.3.0
	 */
	public EndpointHandlerMapping(Collection<? extends MvcEndpoint> endpoints,
			CorsConfiguration corsConfiguration) {
		super(endpoints, corsConfiguration);
	}

}
