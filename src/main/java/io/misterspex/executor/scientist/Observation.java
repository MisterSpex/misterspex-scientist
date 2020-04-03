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
package io.misterspex.executor.scientist;

import java.time.Duration;
import java.util.Optional;
import static java.util.Objects.requireNonNull;

/**
 * The result of an observation wether it is a <em>control</em> or <em>candidate</em> execution.
 */
public final class Observation<V> {
 
    private final Optional<Exception> exception;
    private final V value;
    private final Duration duration;

    Observation(final Exception exception, final V value, final Duration duration) {
        this.duration = requireNonNull(duration, "Duration must be provided");
        this.exception = Optional.ofNullable(exception);
        this.value = value;
    }

    /**
     * An optional exception thrown by the execution.
     * @return an optional exception
     */
    public Optional<Exception> exception() {
        return exception;
    }

    /**
     * The result of the execution.
     * @return the result
     */
    public V value() {
        return this.value;
    }

    /**
     * The duration of the execution.
     * @return the execution duration
     */
    public Duration duration() {
        return this.duration;
    }
    
    @Override
    public String toString() {
        return "Observation{" + "exception=" + exception + ", value=" + value + ", duration=" + duration + '}';
    }
}
