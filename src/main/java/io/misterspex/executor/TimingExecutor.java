/* Mister Spex Executor
 * Copyright 2020 Mister Spex GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * or
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.misterspex.executor;

import java.time.Duration;

/**
 * Measures the duration of an execution and returns the value <em>after</em> the last execution.
 */
public interface TimingExecutor extends Executor {

    /**
     * The duration after the last call of {@link #execute(java.util.concurrent.Callable)} or {@link #execute(java.lang.Runnable)}.
     * Before an execution call, the behavior is not defined and may raise a {@link IllegalStateException RuntimeException}.
     * @return the execution duration
     */
    Duration duration();

    /** Returns a thread safe instance measuring the execution time.
     * @return a TimingExecutor
     */
    static TimingExecutor of() {
        return new TimingExecutorImpl();
    }

    /** Returns a thread safe instance measuring the execution time. {@link #execute(java.lang.Runnable)} and 
     * {@link #execute(java.util.concurrent.Callable)} calls there corresponding method of the given {@code Executor}.
     * <p>The implementation starts the measure before calling the execution method of the given {@link Executor} and stops after
     * return of the called {@code Executor}.</p>
     * @param executor an executor to chain the call to
     * @return a TimingExecutor
     */
    static TimingExecutor of(final Executor executor) {
        return new TimingExecutorImpl(executor);
    }
}
