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
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author sascha.kohlmann
 */
final class TrialExecutorImpl extends ChainExecutorSupport implements TrialExecutor {

    private static final ThreadLocal<AtomicInteger> TRIAL_HOLDER = new ThreadLocal<AtomicInteger>(){
        @Override
        protected AtomicInteger initialValue() {
            return new AtomicInteger();
        }
    };
    
    private final int trials;
    
    protected TrialExecutorImpl(final int trials) {
        super(null);
        this.trials = assertTrials(trials);
    }

    protected TrialExecutorImpl(final int trials, final Executor chain) {
        super(chain);
        assertExecutor(chain);
        this.trials = assertTrials(trials);
    }

    @Override
    public <V> V execute(final Callable<V> executable) throws ExecutionException {
        assertExecutableNotNull(executable);
        final AtomicInteger trialHolder = TRIAL_HOLDER.get();
        trialHolder.set(0);
        int localTrialCounter = 0;
        do {
            try {
                trialHolder.incrementAndGet();
                return doExecute(executable);
            } catch (final Exception e) {
                localTrialCounter = trialsExhausted(localTrialCounter, e);
            }
        } while(true);
    } 

    int trialsExhausted(final int tries, final Exception toThrow) throws ExecutionException {
        if (tries == toTry()) {
            throwExecutionException(toThrow);
        }
        return tries + 1;
    }

    int assertTrials(final int trials) throws IllegalArgumentException {
        if (trials <= 0) {
            throw new IllegalArgumentException("Trials must be > 0. Is: " + trials);
        }
        return trials;
    }

    int toTry() {
        return this.trials;
    }

    @Override
    public int trials() {
        return TRIAL_HOLDER.get().intValue();
    }
}
