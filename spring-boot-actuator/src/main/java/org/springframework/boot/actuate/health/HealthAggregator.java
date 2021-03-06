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

package org.springframework.boot.actuate.health;

import java.util.Map;

/**
 * Strategy interface used by {@link CompositeHealthIndicator} to aggregate {@link Health}
 * instances into a final one.
 * <p>
 * This is especially useful to combine subsystem states expressed through
 * {@link Health#getStatus()} into one state for the entire system. The default
 * implementation {@link OrderedHealthAggregator} sorts {@link Status} instances based on
 * a priority list.
 * <p>
 * It is possible to add more complex {@link Status} types to the system. In that case
 * either the {@link OrderedHealthAggregator} needs to be properly configured or users
 * need to register a custom {@link HealthAggregator} as bean.
 *
 * <p>
 *     CompositeHealthIndicator用于将Health实例聚合为最终实例的策略接口。
 * </p>
 * <p>
 *     这对于将通过Health.getStatus()表示的子系统状态组合为整个系统的一个状态特别有用。
 *     默认实现OrderedHealthAggregator根据优先级列表对Status实例进行排序。
 * </p>
 * <p>
 *     可以向系统添加更复杂的状态类型。
 *     在这种情况下，需要正确配置OrderedHealthAggregator，或者用户需要将自定义HealthAggregator注册为bean
 * </p>
 *
 * @author Christian Dupuis
 * @since 1.1.0
 */
public interface HealthAggregator {

	/**
	 * Aggregate several given {@link Health} instances into one.
	 * @param healths the health instances to aggregate
	 * @return the aggregated health
	 */
	Health aggregate(Map<String, Health> healths);

}
