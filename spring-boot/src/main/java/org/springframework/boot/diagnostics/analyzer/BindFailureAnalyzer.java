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

package org.springframework.boot.diagnostics.analyzer;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * An {@link AbstractFailureAnalyzer} that performs analysis of failures caused by a
 * {@link BindException}.
 *
 * <p>
 *     AbstractFailureAnalyzer对BindException引起的故障进行分析。
 * </p>
 *
 * @author Andy Wilkinson
 * @author Madhura Bhave
 */
class BindFailureAnalyzer extends AbstractFailureAnalyzer<BindException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, BindException cause) {
		if (CollectionUtils.isEmpty(cause.getAllErrors())) {
			return null;
		}
		StringBuilder description = new StringBuilder(
				String.format("Binding to target %s failed:%n", cause.getTarget()));
		for (ObjectError error : cause.getAllErrors()) {
			if (error instanceof FieldError) {
				FieldError fieldError = (FieldError) error;
				description.append(String.format("%n    Property: %s",
						cause.getObjectName() + "." + fieldError.getField()));
				description.append(
						String.format("%n    Value: %s", fieldError.getRejectedValue()));
			}
			description.append(
					String.format("%n    Reason: %s%n", error.getDefaultMessage()));
		}
		return new FailureAnalysis(description.toString(),
				"Update your application's configuration", cause);
	}

}
