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

package org.springframework.boot.devtools.restart;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.util.ClassUtils;

/**
 * Utility to determine if an Java agent based reloader (e.g. JRebel) is being used.
 *
 * <p>
 *     用于确定是否正在使用基于Java代理的重新加载器（例如JRebel）的实用程序。
 * </p>
 *
 * @author Phillip Webb
 * @since 1.3.0
 */
public abstract class AgentReloader {

	private static final Set<String> AGENT_CLASSES;

	static {
		Set<String> agentClasses = new LinkedHashSet<String>();
		agentClasses.add("org.zeroturnaround.javarebel.Integration");
		agentClasses.add("org.zeroturnaround.javarebel.ReloaderFactory");
		agentClasses.add("org.hotswap.agent.HotswapAgent");
		AGENT_CLASSES = Collections.unmodifiableSet(agentClasses);
	}

	private AgentReloader() {
	}

	/**
	 * Determine if any agent reloader is active.
	 * 检测是否存在代理被激活
	 * @return true if agent reloading is active
	 */
	public static boolean isActive() {
		//bootstrap||加载AgentReloader的classLoader||系统应用classLoader
		return isActive(null) || isActive(AgentReloader.class.getClassLoader())
				|| isActive(ClassLoader.getSystemClassLoader());
	}

	/**
	 * 在classloader中检测是否已经存在相关的类，即已经被加载到jvm中去了
	 * @param classLoader
	 * @return
	 */
	private static boolean isActive(ClassLoader classLoader) {
		for (String agentClass : AGENT_CLASSES) {
			if (ClassUtils.isPresent(agentClass, classLoader)) {
				return true;
			}
		}
		return false;
	}

}
