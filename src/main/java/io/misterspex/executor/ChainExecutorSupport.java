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

/**
 *
 * @author sascha.kohlmann
 */
abstract class ChainExecutorSupport {
    
    private final Executor executor;

    protected ChainExecutorSupport(final Executor chain) {
        this.executor = chain;
    }
    
    protected final <V> V doExecute(final Callable<V> executable) throws Exception {
        final Executor exec = executor();
        if (exec == null) {
            return executable.call();
        }
        return exec.execute(executable);
    }

    protected final Executor executor() {
        return this.executor;
    }

    protected final Executor assertExecutor(final Executor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("Executor must be provided");
        }
        return executor;
    }
    
    protected void throwExecutionException(final Exception e) throws ExecutionException {
        if (e instanceof ExecutionException) {
            throw (ExecutionException) e;
        }
        
        throw new ExecutionException(e.getMessage(), e);
    } 

    public static void assertExecutableNotNull(final Object executable) throws IllegalArgumentException {
        if (executable == null) {
            throw new IllegalArgumentException("Executable must be provided");
        }
    }
}
