/*
 * Copyright 2012-2014 the original author or authors.
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

/**
 * Strategy interface used to check if a property name matches specific criteria.
 *
 * <p>
 *     用于检查属性名称是否与特定条件匹配的策略接口。
 * </p>
 * @author Phillip Webb
 * @since 1.2.0
 */
interface PropertyNamePatternsMatcher {

	/**
	 * 全部可以进行相应的匹配
	 */
	PropertyNamePatternsMatcher ALL = new PropertyNamePatternsMatcher() {

		@Override
		public boolean matches(String propertyName) {
			return true;
		}

	};

	/**
	 * 全部无法进行相关的匹配
	 */
	PropertyNamePatternsMatcher NONE = new PropertyNamePatternsMatcher() {

		@Override
		public boolean matches(String propertyName) {
			return false;
		}

	};

	/**
	 * Return {@code true} of the property name matches.
	 * @param propertyName the property name
	 * @return {@code true} if the property name matches
	 */
	boolean matches(String propertyName);

}
