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

package org.springframework.boot.autoconfigure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.Assert;

/**
 * Sort {@link EnableAutoConfiguration auto-configuration} classes into priority order by
 * reading {@link AutoConfigureOrder}, {@link AutoConfigureBefore} and
 * {@link AutoConfigureAfter} annotations (without loading classes).
 *
 * <p>
 *     通过读取AutoConfigureOrder，AutoConfigureBefore和AutoConfigureAfter注解（无需加载类），将自动配置类按优先级顺序排序。
 * </p>
 *
 * @author Phillip Webb
 */
class AutoConfigurationSorter {

	private final MetadataReaderFactory metadataReaderFactory;

	private final AutoConfigurationMetadata autoConfigurationMetadata;

	AutoConfigurationSorter(MetadataReaderFactory metadataReaderFactory,
			AutoConfigurationMetadata autoConfigurationMetadata) {
		Assert.notNull(metadataReaderFactory, "MetadataReaderFactory must not be null");
		this.metadataReaderFactory = metadataReaderFactory;
		this.autoConfigurationMetadata = autoConfigurationMetadata;
	}

	/**
	 * 对一组classNames进行相关的配置和解析
	 * @param classNames
	 * @return
	 */
	public List<String> getInPriorityOrder(Collection<String> classNames) {
		//构建对应的结构，自动配置Classes
		final AutoConfigurationClasses classes = new AutoConfigurationClasses(
				this.metadataReaderFactory, this.autoConfigurationMetadata, classNames);
		//需要排序的class
		List<String> orderedClassNames = new ArrayList<String>(classNames);
		// Initially sort alphabetically
		// 开始使用字符串顺序进行排序
		Collections.sort(orderedClassNames);
		// Then sort by order
		// 使用order排序
		Collections.sort(orderedClassNames, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int i1 = classes.get(o1).getOrder();
				int i2 = classes.get(o2).getOrder();
				return (i1 < i2) ? -1 : (i1 > i2) ? 1 : 0;
			}

		});
		// Then respect @AutoConfigureBefore @AutoConfigureAfter
		// 然后通过注解@AutoConfigureBefore和@AutoConfigureAfter进行排序
		orderedClassNames = sortByAnnotation(classes, orderedClassNames);
		return orderedClassNames;
	}

	/**
	 * 通过注解进行排序
	 * @param classes
	 * @param classNames
	 * @return
	 */
	private List<String> sortByAnnotation(AutoConfigurationClasses classes,
			List<String> classNames) {
		List<String> toSort = new ArrayList<String>(classNames);
		Set<String> sorted = new LinkedHashSet<String>();
		Set<String> processing = new LinkedHashSet<String>();
		while (!toSort.isEmpty()) {
			doSortByAfterAnnotation(classes, toSort, sorted, processing, null);
		}
		return new ArrayList<String>(sorted);
	}

	/**
	 * 进行排序
	 * @param classes
	 * @param toSort
	 * @param sorted
	 * @param processing
	 * @param current
	 */
	private void doSortByAfterAnnotation(AutoConfigurationClasses classes,
			List<String> toSort, Set<String> sorted, Set<String> processing,
			String current) {
		if (current == null) {
			current = toSort.remove(0);
		}
		processing.add(current);
		for (String after : classes.getClassesRequestedAfter(current)) {
			Assert.state(!processing.contains(after),
					"AutoConfigure cycle detected between " + current + " and " + after);
			if (!sorted.contains(after) && toSort.contains(after)) {
				doSortByAfterAnnotation(classes, toSort, sorted, processing, after);
			}
		}
		processing.remove(current);
		sorted.add(current);
	}

	/**
	 * 自动配置类，集合他将负责进行排序
	 */
	private static class AutoConfigurationClasses {

		private final Map<String, AutoConfigurationClass> classes = new HashMap<String, AutoConfigurationClass>();

		/**
		 * 为每一个字符串代表的自动配置类，进配置一个对应的元信息类。也就是
		 * AutoConfigurationClass
		 * @param metadataReaderFactory
		 * @param autoConfigurationMetadata 自动配置的相关的信息。
		 * @param classNames
		 */
		AutoConfigurationClasses(MetadataReaderFactory metadataReaderFactory,
				AutoConfigurationMetadata autoConfigurationMetadata,
				Collection<String> classNames) {
			for (String className : classNames) {
				this.classes.put(className, new AutoConfigurationClass(className,
						metadataReaderFactory, autoConfigurationMetadata));
			}
		}

		public AutoConfigurationClass get(String className) {
			return this.classes.get(className);
		}

		public Set<String> getClassesRequestedAfter(String className) {
			Set<String> rtn = new LinkedHashSet<String>();
			rtn.addAll(get(className).getAfter());
			for (Map.Entry<String, AutoConfigurationClass> entry : this.classes
					.entrySet()) {
				if (entry.getValue().getBefore().contains(className)) {
					rtn.add(entry.getKey());
				}
			}
			return rtn;
		}

	}

	/**
	 * 代表了一个自动配置类
	 */
	private static class AutoConfigurationClass {

		/**
		 * 类名
		 */
		private final String className;

		/**
		 * 元信息读取工厂，通常会使用防止加载
		 */
		private final MetadataReaderFactory metadataReaderFactory;

		/**
		 * 直接配置的元信息
		 */
		private final AutoConfigurationMetadata autoConfigurationMetadata;

		/**
		 * 注解信息
		 */
		private AnnotationMetadata annotationMetadata;

		/**
		 * 在本类前面的类
		 */
		private final Set<String> before;

		/**
		 * 在本类后面的类
		 */
		private final Set<String> after;

		AutoConfigurationClass(String className,
				MetadataReaderFactory metadataReaderFactory,
				AutoConfigurationMetadata autoConfigurationMetadata) {
			this.className = className;
			this.metadataReaderFactory = metadataReaderFactory;
			this.autoConfigurationMetadata = autoConfigurationMetadata;
			this.before = readBefore();
			this.after = readAfter();
		}

		public Set<String> getBefore() {
			return this.before;
		}

		public Set<String> getAfter() {
			return this.after;
		}

		/**
		 * 获得order值
		 * @return
		 */
		private int getOrder() {
			//已经在里面了，使用里面的配置
			if (this.autoConfigurationMetadata.wasProcessed(this.className)) {
				return this.autoConfigurationMetadata.getInteger(this.className,
						"AutoConfigureOrder", Ordered.LOWEST_PRECEDENCE);
			}
			//不在里面使用外面的配置信息
			Map<String, Object> attributes = getAnnotationMetadata()
					.getAnnotationAttributes(AutoConfigureOrder.class.getName());
			return (attributes == null ? Ordered.LOWEST_PRECEDENCE
					: (Integer) attributes.get("value"));
		}

		/**
		 * 获得before
		 * @return
		 */
		private Set<String> readBefore() {
			//已经在里面了，使用里面的配置
			if (this.autoConfigurationMetadata.wasProcessed(this.className)) {
				return this.autoConfigurationMetadata.getSet(this.className,
						"AutoConfigureBefore", Collections.<String>emptySet());
			}
			//不在里面使用外面的配置信息
			return getAnnotationValue(AutoConfigureBefore.class);
		}

		/**
		 * 获得after
		 * @return
		 */
		private Set<String> readAfter() {
			//已经在里面了，使用里面的配置
			if (this.autoConfigurationMetadata.wasProcessed(this.className)) {
				return this.autoConfigurationMetadata.getSet(this.className,
						"AutoConfigureAfter", Collections.<String>emptySet());
			}
			//不在里面使用外面的配置信息
			return getAnnotationValue(AutoConfigureAfter.class);
		}

		private Set<String> getAnnotationValue(Class<?> annotation) {
			Map<String, Object> attributes = getAnnotationMetadata()
					.getAnnotationAttributes(annotation.getName(), true);
			if (attributes == null) {
				return Collections.emptySet();
			}
			Set<String> value = new LinkedHashSet<String>();
			Collections.addAll(value, (String[]) attributes.get("value"));
			Collections.addAll(value, (String[]) attributes.get("name"));
			return value;
		}

		private AnnotationMetadata getAnnotationMetadata() {
			if (this.annotationMetadata == null) {
				try {
					MetadataReader metadataReader = this.metadataReaderFactory
							.getMetadataReader(this.className);
					this.annotationMetadata = metadataReader.getAnnotationMetadata();
				}
				catch (IOException ex) {
					throw new IllegalStateException(
							"Unable to read meta-data for class " + this.className, ex);
				}
			}
			return this.annotationMetadata;
		}

	}

}
