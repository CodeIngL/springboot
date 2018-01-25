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

package org.springframework.boot.diagnostics.analyzer;

import org.springframework.boot.context.embedded.PortInUseException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * A {@code FailureAnalyzer} that performs analysis of failures caused by a
 * {@code PortInUseException}.
 *
 * <p>
 *     FailureAnalyzer对由PortInUseException引起的故障进行分析。
 * </p>
 *
 * @author Andy Wilkinson
 */
class PortInUseFailureAnalyzer extends AbstractFailureAnalyzer<PortInUseException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, PortInUseException cause) {
		return new FailureAnalysis(
				"Embedded servlet container failed to start. Port " + cause.getPort()
						+ " was already in use.",
				"Identify and stop the process that's listening on port "
						+ cause.getPort() + " or configure this "
						+ "application to listen on another port.",
				cause);
	}

}
