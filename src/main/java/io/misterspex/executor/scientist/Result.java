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

import java.util.Map;
import java.util.Optional;
import static java.util.Objects.requireNonNull;

/**
 * The result of {@link Experiment#execute(java.util.concurrent.Callable, java.util.concurrent.Callable)}.
 * @see Experiment#publish(io.misterspex.executor.scientist.Result) 
 */
public final class Result<V> {
    
    private final Experiment<V> scientist;
    private final Observation<V> control;
    private final Optional<Observation<V>> candidate;
    private final Map<String, Object> context;
    
    Result(final Experiment<V> scientist, final Observation<V> control, final Observation<V> candidate, final Map<String, Object> context) {
        this.context = requireNonNull(context, "Context must be provided");
        this.scientist = requireNonNull(scientist, "Scientist must be provided");
        this.control = requireNonNull(control, "Control Observation must be provided");
        this.candidate = Optional.ofNullable(candidate);
    }
    
    /** The executing instance.
     * @return the executing instance
     */
    public Experiment<V> experiment() {
        return this.scientist;
    }

    /** The observation of the <em>control</em> execution.
     * @return the <em>control</em> execution observation
     */
    public Observation<V> controlObservation() {
        return this.control;
    }

    /** The observation of the <em>candidate</em> execution.
     * @return the <em>candidate</em> execution observation
     */
    public Optional<Observation<V>> candidateObservation() {
        return this.candidate;
    }

    public Map<String, Object> context() {
        return this.context;
    }

    @Override
    public String toString() {
        return "Result{" + "scientist=" + scientist + ", control=" + control + ", candidate=" + candidate + ", context=" + context + '}';
    }
}
