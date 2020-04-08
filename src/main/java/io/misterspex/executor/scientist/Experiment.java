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

import io.misterspex.executor.TimingExecutor;
import static io.misterspex.executor.scientist.Experiment.ExecutionOrder.CANDIDATE_FIRST;
import static io.misterspex.executor.scientist.Experiment.ExecutionOrder.CONTROL_FIRST;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Pure Java SE implementation of <a href='https://github.com/github/scientist/'>Github Scientist</a>.
 *
 * <p>{@code Experiment} executes a <em>control</em> function and a <em>candidate</em> function. Both excutions
 * should return an equal <em>result</em> or throw an equal exception.</p>
 *
 * <p>The implementation delegates all publishing and validation of results to external services.
 * Override {@link #publish(io.misterspex.executor.scientist.Result) publish(Result)} to get <em>results</em>
 * published for your requirements.</p>
 * 
 * <p>{@code Experiment} returns the <em>result</em> of the <em>control</em> function to the caller
 * or throws the exception of the <em>control</em> execution. In future version this might be configurable.
 * The execution result of <em>control</em> and <em>candidate</em> together with additional metrics 
 * (execution time, eventual thrown exceptions…) are collected and published.
 * {@link #publish(io.misterspex.executor.scientist.Result) publish} is always called before returning
 * {@link #execute(java.util.concurrent.Callable, java.util.concurrent.Callable) execute(…)}.</p>
 * 
 * <p>With {@link #enabled()} and {@link #executionOrder()} it is possible to control the execution
 * behavior of the implementation.</p>
 * 
 * <p>The implementation supports synchronous and asynchronous execution of the <em>control</em> and <em>candidate</em>.
 * To enable asynchronous execution deliver a {@link ExecutorService} while create an {@code Experiment} instance.</p>
 * 
 * @param <V> the type of the execution result
 */
public class Experiment<V> {

    private final String name;
    private final Map<String, Object> context;
    private final ExecutorService executorService;
    
    /**
     * Creates a default {@code Scientist} instance. The instance executes synchronous and the {@link #name() name} is 
     * "{@code Scientist}". The context is an empty {@link Map}.
     */
    public Experiment() {
        this("Scientist");
    }

    /**
     * Creates a default {@code Experiment} instance with the given {@link #name() name}. The instance executes synchronous.
     * The context is an empty {@link Map}.
     * @param name the name of the instance. Must not be {@code null}
     * @throws NullPointerException if and only if <em>name</em> is {@code null}
     */
    public Experiment(final String name) {
        this(name, Collections.emptyMap());
    }

    /**
     * Creates a default {@code Experiment} instance with the given {@link #name() name}. The instance executes synchronous.
     * @param name the name of the instance. Must not be {@code null}
     * @param context the context of the instance. Must not be {@code null}
     * @throws NullPointerException if and only if <em>name</em> or <em>context</em> is {@code null}
     * @see Result
     */
    public Experiment(final String name, final Map<String, Object> context) {
        this.name = requireNonNull(name, "Name must be provided");
        this.context = requireNonNull(context, "Context must be provided");
        this.executorService = null;
    }

    /**
     * Creates an asynchronous executing instance. The context is an empty {@link Map}.
     * @param name the name of the instance. Must not be {@code null}
     * @param executorService the executor service to execute <em>control</em> and <em>candidate</em> ansychronous. Must not be {@code null}.
     * @throws NullPointerException if and only if <em>name</em> or <em>executorService</em> is {@code null}
     */
    public Experiment(final String name, final ExecutorService executorService) {
        this(name, Collections.emptyMap(), executorService);
    }

    /**
     * Creates an asynchronous executing instance with a context.
     * @param name the name of the instance. Must not be {@code null}
     * @param context the context of the instance. Must not be {@code null}
     * @param executorService the executor service to execute <em>control</em> and <em>candidate</em> ansychronous. Must not be {@code null}.
     * @throws NullPointerException if and only if <em>name</em> or <em>context</em> or <em>executorService</em> is {@code null}
     */
    public Experiment(final String name, final Map<String, Object> context, final ExecutorService executorService) {
        this.name = requireNonNull(name, "Name must be provided");
        this.context = requireNonNull(context, "Context must be provided");
        this.executorService = requireNonNull(executorService, "ExecutorService must be provided");
    }

    /**
     * Executes <em>control</em> and may be <em>candidate</em> and returns the result of <em>control</em>. Execution is performed
     * asynchronous if and only if an {@link ExecutorService} is available.
     * <p>Control the execution behavior with {@link #enabled()} and {@link #executionOrder()}.</p>
     * @param control the control to execute
     * @param candidate the candidate to exceute
     * @return the result of <em>ccontrol</em> execution
     * @throws Exception an exception if thrown during execution
     */
    public final V execute(final Callable<V> control, final Callable<V> candidate) throws Exception {
        if (isAsync()) {
            return executeAsync(control, candidate);
        }
        return executeSync(control, candidate);
    }
    
    private V executeSync(final Callable<V> control, final Callable<V> candidate) throws Exception {
        final Observation<V> controlObservation;
        final Observation<V> candidateObservation;

        if (enabled()) {
            if (executionOrder().isCandidateFirst()) {
                candidateObservation = executeResult(candidate);
                controlObservation = executeResult(control);
            } else {
                controlObservation = executeResult(control);
                candidateObservation = executeResult(candidate);
            }
        } else {
            controlObservation = executeResult(control);
            candidateObservation = null;
        }

        final Result<V> result = new Result<>(this, controlObservation, candidateObservation, this.context);
        doPublish(result);
        if (controlObservation.exception().isPresent()) {
            throw controlObservation.exception().get();
        }
        return controlObservation.value();
    }

    private V executeAsync(final Callable<V> control, final Callable<V> candidate) throws Exception {
        final FutureTask<Observation<V>> controlObservationFuture = new FutureTask<>(() -> executeResult(control));
        final FutureTask<Observation<V>> candidateObservationFuture;

        if (enabled()) {
            candidateObservationFuture = new FutureTask<>(() -> executeResult(candidate));
            if (executionOrder().isCandidateFirst()) {
                this.executorService.submit(candidateObservationFuture);
                this.executorService.submit(controlObservationFuture);
            } else {
                this.executorService.submit(controlObservationFuture);
                this.executorService.submit(candidateObservationFuture);
            }
        } else {
            this.executorService.submit(controlObservationFuture);
            candidateObservationFuture = null;
        }
        
        final Observation<V> controlObservation;
        try {
            controlObservation = controlObservationFuture.get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        
        final Future<Void> publishedResult = this.executorService.submit(() -> publishAsync(controlObservation, candidateObservationFuture));

        if (controlObservation.exception().isPresent()) {
            throw controlObservation.exception().get();
        }
        
        return controlObservation.value();
    }
    
    private Void publishAsync(final Observation<V> controlObservation, final Future<Observation<V>> candidateObservationFuture) {
        final Observation<V> candidateObservation;
        if (candidateObservationFuture != null) {
            try {
                candidateObservation = candidateObservationFuture.get();
            } catch (final InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            candidateObservation = null;
        }

        final Result<V> result = new Result<>(this, controlObservation, candidateObservation, this.context);
        doPublish(result);
        return null;
    }

    /**
     * Defines the order of execution if and only if <em>control</em> and <em>candidate</em> must be executed.
     * <p>The default implementation gurantees a 50/50 execution order distribution. Override this method for
     * your own distribution.</p>
     * @return the execution order. Never {@code null}
     */
    protected ExecutionOrder executionOrder() {
        final double order = Math.random();
        if (order < 0.5) {
            return CONTROL_FIRST;
        }
        return CANDIDATE_FIRST;
    }
    
    private Observation<V> executeResult(final Callable<V> executable) throws Exception {
        Exception exception = null;
        V result = null;
        TimingExecutor executor = TimingExecutor.of();

        try {
            result = executor.execute(executable);
        } catch (final io.misterspex.executor.ExecutionException e) {
            exception = causeExceptionOf(e);
        }

        return new Observation(exception, result, executor.duration());
    }

    /**
     * Decider to execute <em>control</em> and <em>candidate</em>.
     * <p>Default is {@code true}.</p>
     * @return {@code true} to execute <em>control</em> and <em>candidate</em>. {@code false} for execute <em>control</em> only.
     */
    protected boolean enabled() {
        return true;
    }

    private boolean isAsync() {
        return this.executorService != null;
    }
    
    /**
     * Return the name of the instance.
     * @return the name
     */
    public final String name() {
        return name;
    }

    /**
     * Publish the result of the execution.
     * <p>The default implementation returns immediately. Override this method for your own behavior.</p>
     * <p><strong>Note:</strong> {@code publish} is protected against exceptions thrown by overriding methods.
     * Caught exceptions from overwriting methods are ignored.</p>
     * @param result the result to publish. Never {@code null}
     */
    protected void publish(final Result result) {}

    private void doPublish(final Result result) {
        try {
            publish(result);
        } catch (final Exception e) {
            // Ignore like described in #publish(Result);
        }
    }
    
    /**
     * Indicates the execution order of <em>control</em> and <em>candidate</em>.
     */
    public static enum ExecutionOrder {
        /** <em>Control</em> should be executed first. */
        CONTROL_FIRST {@Override public boolean isControlFirst() {return true;}},
        /** <em>Candidate</em> should be executed first. */
        CANDIDATE_FIRST {@Override public boolean isCandidateFirst() {return true;}};
        
        /**
         * @return {@code true} if and only if instance is of {@link #CONTROL_FIRST}
         */
        public boolean isControlFirst() {return false;}
        
        /**
         * @return {@code true} if and only if instance is of {@link #CANDIDATE_FIRST}
         */
        public boolean isCandidateFirst() {return false;}
    }

    private Exception causeExceptionOf(final io.misterspex.executor.ExecutionException ex) {
        final Throwable t = ex.getCause();
        if (t instanceof Exception) {
            return (Exception) t;
        }
        return ex;
    }
}
