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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;

/**
 * {@link UncaughtExceptionHandler} decorator that allows a thread to exit silently.
 *
 * <p>
 *     允许程序以平静的状态结束这个线程
 * </p>
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
class SilentExitExceptionHandler implements UncaughtExceptionHandler {

	private final UncaughtExceptionHandler delegate;

	SilentExitExceptionHandler(UncaughtExceptionHandler delegate) {
		this.delegate = delegate;
	}

	/**
	 *
	 * 当线程抛出异常时的处理方案
	 * @param thread
	 * @param exception
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable exception) {
		if (exception instanceof SilentExitException) {
			if (isJvmExiting(thread)) {
				preventNonZeroExitCode();
			}
			return;
		}
		if (this.delegate != null) {
			//由委托者进行处理
			this.delegate.uncaughtException(thread, exception);
		}
	}

	/**
	 * 是否是jvm结束了
	 * 只要遍历所用的线程，存在活跃的非结束的非守护线程就ok
	 * @param exceptionThread
	 * @return
	 */
	private boolean isJvmExiting(Thread exceptionThread) {
		for (Thread thread : getAllThreads()) {
			if (thread != exceptionThread && thread.isAlive() && !thread.isDaemon()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获得当前的所有的线程
	 * @return
	 */
	protected Thread[] getAllThreads() {
		ThreadGroup rootThreadGroup = getRootThreadGroup();
		Thread[] threads = new Thread[32];
		int count = rootThreadGroup.enumerate(threads);
		while (count == threads.length) {
			threads = new Thread[threads.length * 2];
			count = rootThreadGroup.enumerate(threads);
		}
		return Arrays.copyOf(threads, count);
	}

	/**
	 * 向上找到所有的线程组
	 * @return
	 */
	private ThreadGroup getRootThreadGroup() {
		ThreadGroup candidate = Thread.currentThread().getThreadGroup();
		while (candidate.getParent() != null) {
			candidate = candidate.getParent();
		}
		return candidate;
	}

	/**
	 * 正式的结束
	 */
	protected void preventNonZeroExitCode() {
		System.exit(0);
	}

	/**
	 * 为线程设置UncaughException
	 * @param thread
	 */
	public static void setup(Thread thread) {
		UncaughtExceptionHandler handler = thread.getUncaughtExceptionHandler();
		if (!(handler instanceof SilentExitExceptionHandler)) {
			handler = new SilentExitExceptionHandler(handler);
			thread.setUncaughtExceptionHandler(handler);
		}
	}

	/**
	 * 结束当前的线程
	 */
	public static void exitCurrentThread() {
		throw new SilentExitException();
	}

	/**
	 * 一个特殊的异常，用于平静的退出整个线程。
	 */
	private static class SilentExitException extends RuntimeException {

	}

}
