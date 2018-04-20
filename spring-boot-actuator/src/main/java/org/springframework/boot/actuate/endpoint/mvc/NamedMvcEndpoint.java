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

/**
 * An {@link MvcEndpoint} that also includes a logical name. Unlike {@link #getPath()
 * endpoints paths}, it should not be possible for a user to change the endpoint name.
 * Names provide a consistent way to reference an endpoint, for example they may be used
 * as the {@literal 'rel'} attribute in a HAL response.
 *
 * <p>
 *     还包含一个逻辑名称的MvcEndpoint。 与端点路径不同，用户不可能更改端点名称。 名称提供了引用端点的一致方式，例如，它们可以用作HAL响应中的“rel”属性。
 * </p>
 *
 * @author Madhura Bhave
 * @since 1.5.0
 */
public interface NamedMvcEndpoint extends MvcEndpoint {

	/**
	 * Return the logical name of the endpoint. Names should be non-null, non-empty,
	 * alpha-numeric values.
	 * @return the logical name of the endpoint
	 */
	String getName();

}
