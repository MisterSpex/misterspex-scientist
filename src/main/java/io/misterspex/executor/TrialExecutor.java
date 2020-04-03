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

/**
 * Implementation executes the given {@code Runnable} or {@code Callable} for a given amount of trials to successful execute them.
 * If the amount of executions violates the trial value an {@link ExecutionException} will be thrown.
 * <p>Executables should be idempotent to minimize side effects.</p>
 */
public interface TrialExecutor extends Executor {
    
    /**
     * The number of executions
     * @return the execution number
     */
    int trials();

    /** Returns a thread safe instance with a maximum of 5 execution to try.
     * @return a TrialExecutor */
    static TrialExecutor of() {
        return of(5);
    }

    /** Returns a thread safe instance with a maximum of 5 execution to try.
     * {@link #execute(java.lang.Runnable)} and {@link #execute(java.util.concurrent.Callable)} calls there corresponding
     * method of the given {@code Executor}.
     * @param executor an executor to chain the call to
     * @return a TrialExecutor */
    static TrialExecutor of(final Executor executor) {
        return of(5, executor);
    }
    
    /** Returns a thread safe instance with a maximum of {@code toTry} execution to try.
     * @param toTry the number of tries before finally fail
     * @return a TrialExecutor */
    static TrialExecutor of(final int toTry) {
        return new TrialExecutorImpl(toTry);
    }

    /** Returns a thread safe instance with a maximum of {@code toTry} execution to try.
     * {@link #execute(java.lang.Runnable)} and {@link #execute(java.util.concurrent.Callable)} calls there corresponding
     * method of the given {@code Executor}.
     * @param toTry the number of tries before finally fail
     * @param executor an executor to chain the call to
     * @return a TrialExecutor */
    static TrialExecutor of(final int toTry, final Executor executor) {
        return new TrialExecutorImpl(toTry, executor);
    }
}
