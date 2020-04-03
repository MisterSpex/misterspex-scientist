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

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * An {@code Executor} instance will execute a given {@code Runnable} or {@code Callable} for different behaviors.
 */
public interface Executor {

    /**
     * Executes the given {@code Runnable} and throws an exception in case of failure.
     * @param executable the {@code Runbnable} to execute
     * @throws ExecutionException if an exception raise during execution
     */
    public default void execute(final Runnable executable) throws ExecutionException {
        ChainExecutorSupport.assertExecutableNotNull(executable);
        execute(Executors.callable(executable));
    }

    /**
     * Executes the given {@code Runnable} and returns the result or throws an exception in case of failure.
     * @param executable the {@code Callable} to execute
     * @param <V> the return type
     * @return the result of the execution
     * @throws ExecutionException if an exception raise during execution
     */
    <V> V execute(final Callable<V> executable) throws ExecutionException;
}
