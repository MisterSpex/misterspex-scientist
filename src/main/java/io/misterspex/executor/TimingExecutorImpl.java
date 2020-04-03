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
import static java.time.Duration.ofNanos;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import static java.lang.System.nanoTime;

/**
 *
 * @author sascha.kohlmann
 */
final class TimingExecutorImpl extends ChainExecutorSupport implements TimingExecutor {

    private static final ThreadLocal<AtomicLong> DURATION_HOLDER = new ThreadLocal<AtomicLong>(){
        @Override
        protected AtomicLong initialValue() {
            return new AtomicLong();
        }
    };

    protected TimingExecutorImpl() {
        super(null);
    }
    
    protected TimingExecutorImpl(final Executor chain) {
        super(chain);
        assertExecutor(chain);
    }

    @Override
    public <V> V execute(final Callable<V> executable) throws ExecutionException {
        assertExecutableNotNull(executable);
        final long start = nanoTime();
        try {
            return doExecute(executable);
        } catch (final Exception e) {
            if (e instanceof ExecutionException) {
                throw (ExecutionException) e;
            }
            throw new ExecutionException(e);
        } finally {
            final long finish = nanoTime();
            final AtomicLong durationHolder = DURATION_HOLDER.get();
            durationHolder.set(finish - start);
        }
    }

    @Override
    public Duration duration() {
        return ofNanos(DURATION_HOLDER.get().get());
    }
}
