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

package org.springframework.boot.devtools.restart;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * {@link ApplicationListener} to initialize the {@link Restarter}.
 *
 * <p>
 *     ApplicationListener初始化Restarter
 * </p>
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.3.0
 * @see Restarter
 */
public class RestartApplicationListener
		implements ApplicationListener<ApplicationEvent>, Ordered {

	private int order = HIGHEST_PRECEDENCE;

	private static final String ENABLED_PROPERTY = "spring.devtools.restart.enabled";

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationStartingEvent) {
			//开始事件
			onApplicationStartingEvent((ApplicationStartingEvent) event);
		}
		if (event instanceof ApplicationPreparedEvent) {
			//准备事件
			onApplicationPreparedEvent((ApplicationPreparedEvent) event);
		}
		if (event instanceof ApplicationReadyEvent
				|| event instanceof ApplicationFailedEvent) {
			//完成事件
			Restarter.getInstance().finish();
		}
		if (event instanceof ApplicationFailedEvent) {
			//失败事件
			onApplicationFailedEvent((ApplicationFailedEvent) event);
		}
	}

	/**
	 * 任务开始的事件
	 * @param event
	 */
	private void onApplicationStartingEvent(ApplicationStartingEvent event) {
		// It's too early to use the Spring environment but we should still allow
		// users to disable restart using a System property.
		// 现在使用Spring环境还为时过早，但我们仍然应该允许用户使用System属性禁用重启。
		String enabled = System.getProperty(ENABLED_PROPERTY);
		//禁用重启
		if (enabled == null || Boolean.parseBoolean(enabled)) { // 开启
			String[] args = event.getArgs();//事件参数
			DefaultRestartInitializer initializer = new DefaultRestartInitializer();
			boolean restartOnInitialize = !AgentReloader.isActive();
			Restarter.initialize(args, false, initializer, restartOnInitialize);
		}
		else {
			//被禁用
			Restarter.disable();
		}
	}

	private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
		Restarter.getInstance().prepare(event.getApplicationContext());
	}

	private void onApplicationFailedEvent(ApplicationFailedEvent event) {
		Restarter.getInstance().remove(event.getApplicationContext());
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * Set the order of the listener.
	 * @param order the order of the listener
	 */
	public void setOrder(int order) {
		this.order = order;
	}

}
