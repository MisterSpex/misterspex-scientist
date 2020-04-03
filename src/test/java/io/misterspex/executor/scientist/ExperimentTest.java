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

import static com.google.common.truth.Truth.assertThat;
import io.misterspex.executor.ExecutionException;
import io.misterspex.executor.scientist.Experiment.ExecutionOrder;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author sascha.kohlmann
 */
public class ExperimentTest {
    
    @Test
    public void synchronize_without_context() throws Exception {
        
        // Given
        final AtomicReference<Result> resultReference = new AtomicReference<>();
        final Experiment<String> scientist = new Experiment<String>() {
            @Override
            protected void publish(final Result result) {
                resultReference.set(result);
            }
        };
        
        // When
        final String result = scientist.execute(() -> "control", () -> "candidate");
        
        // Then
        assertThat(result).isEqualTo("control");
        assertThat(scientist.name()).isEqualTo("Scientist");
        assertThat(resultReference.get().experiment()).isSameInstanceAs(scientist);
        assertThat(resultReference.get().context()).isEmpty();

        assertThat(resultReference.get().controlObservation().exception().isPresent()).isFalse();
        assertThat(resultReference.get().controlObservation().duration().getNano()).isGreaterThan(0);
        assertThat(resultReference.get().controlObservation().value()).isEqualTo("control");
        
        final Observation<String> candidate = (Observation<String>) resultReference.get().candidateObservation().get();
        assertThat(candidate.exception().isPresent()).isFalse();
        assertThat(candidate.duration().getNano()).isGreaterThan(0);
        assertThat(candidate.value()).isEqualTo("candidate");
    }

    @Test
    public void asynchronize_without_context() throws Exception {
        
        // Given
        final ExecutorService executorService = Executors.newFixedThreadPool(3);
        final AtomicReference<Result> resultReference = new AtomicReference<>();
        final Experiment<String> scientist = new Experiment<String>("single", executorService) {
            @Override
            protected void publish(final Result result) {
                resultReference.set(result);
            }
        };
        
        // When
        final String result = scientist.execute(() -> "control", () -> "candidate");
        Thread.sleep(10);
        executorService.shutdown();
        
        // Then
        assertThat(result).isEqualTo("control");
        assertThat(scientist.name()).isEqualTo("single");
        
        assertThat(resultReference.get().toString()).contains("control");
        assertThat(resultReference.get().toString()).contains("candidate");
        assertThat(resultReference.get().toString()).contains("Result");
        assertThat(resultReference.get().toString()).contains("Observation");

        assertThat(resultReference.get().experiment()).isSameInstanceAs(scientist);
        assertThat(resultReference.get().context()).isEmpty();

        assertThat(resultReference.get().controlObservation().exception().isPresent()).isFalse();
        assertThat(resultReference.get().controlObservation().duration().getNano()).isGreaterThan(0);
        assertThat(resultReference.get().controlObservation().value()).isEqualTo("control");
        
        final Observation<String> candidate = (Observation<String>) resultReference.get().candidateObservation().get();
        assertThat(candidate.exception().isPresent()).isFalse();
        assertThat(candidate.duration().getNano()).isGreaterThan(0);
        assertThat(candidate.value()).isEqualTo("candidate");

        assertThat(candidate.toString()).contains("candidate");
        assertThat(candidate.toString()).contains("Observation");
    }
    
    @Test
    public void execution_order() {
        assertThat(Experiment.ExecutionOrder.CANDIDATE_FIRST.isCandidateFirst()).isTrue();
        assertThat(Experiment.ExecutionOrder.CANDIDATE_FIRST.isControlFirst()).isFalse();
        assertThat(Experiment.ExecutionOrder.CONTROL_FIRST.isCandidateFirst()).isFalse();
        assertThat(Experiment.ExecutionOrder.CONTROL_FIRST.isControlFirst()).isTrue();
    }
    
    @Test
    public void synchronize_not_enabled_with_context() throws Exception {
        // Given
        final Map<String, Object> context = new HashMap<>();
        context.put("key", "value");

        final AtomicReference<Result> resultReference = new AtomicReference<>();
        final Experiment<String> scientist = new Experiment<String>("context", context) {
            @Override
            protected void publish(final Result result) {
                resultReference.set(result);
            }
            @Override
            protected boolean enabled() {
                return false;
            }
        };
        
        // When
        final String result = scientist.execute(() -> "control", () -> "candidate");
        
        // Then
        assertThat(resultReference.get().context()).isSameInstanceAs(context);
        assertThat(resultReference.get().controlObservation().value()).isEqualTo("control");
        assertThat(resultReference.get().candidateObservation().isPresent()).isFalse();
    }

    @Test
    public void asynchronize_not_enabled_with_context() throws Exception {
        // Given
        final Map<String, Object> context = new HashMap<>();
        context.put("key", "value");

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final AtomicReference<Result> resultReference = new AtomicReference<>();
        final Experiment<String> scientist = new Experiment<String>("single", context, executorService) {
            @Override
            protected void publish(final Result result) {
                resultReference.set(result);
            }
            @Override
            protected boolean enabled() {
                return false;
            }
        };
        
        // When
        final String result = scientist.execute(() -> "control", () -> "candidate");
        Thread.sleep(10);
        executorService.shutdown();
        
        // Then
        assertThat(result).isEqualTo("control");
        assertThat(scientist.name()).isEqualTo("single");
        
        assertThat(resultReference.get().context()).isSameInstanceAs(context);
        assertThat(resultReference.get().controlObservation().value()).isEqualTo("control");
        assertThat(resultReference.get().candidateObservation().isPresent()).isFalse();
    }

    @Test
    public void asynchronize_with_candidate_first() throws Exception {
        // Given
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final AtomicReference<Result> resultReference = new AtomicReference<>();
        final Experiment<Long> scientist = new Experiment<Long>("single", executorService) {
            @Override
            protected void publish(final Result result) {
                resultReference.set(result);
            }
            @Override
            protected ExecutionOrder executionOrder() {
                return ExecutionOrder.CANDIDATE_FIRST;
            }
        };
        
        // When
        final Long result = scientist.execute(() -> {
                                                Thread.sleep(100);
                                                return currentTimeMillis();
                                              },
                                              () -> currentTimeMillis());
        Thread.sleep(110);
        executorService.shutdown();
        
        // Then
        final long controlStart = (long) resultReference.get().controlObservation().value();
        final Observation<Long> candidate = (Observation<Long>) resultReference.get().candidateObservation().get();
        final long candidateStart = (long) candidate.value();
        
        assertThat(candidateStart).isLessThan(controlStart);
    }

    @Test
    public void asynchronize_with_control_first() throws Exception {
        // Given
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final AtomicReference<Result> resultReference = new AtomicReference<>();
        final Experiment<Long> scientist = new Experiment<Long>("single", executorService) {
            @Override
            protected void publish(final Result result) {
                resultReference.set(result);
            }
            @Override
            protected ExecutionOrder executionOrder() {
                return ExecutionOrder.CONTROL_FIRST;
            }
        };
        
        // When
        final Long result = scientist.execute(() -> currentTimeMillis(), 
                                              () -> {
                                                  Thread.sleep(100);
                                                  return currentTimeMillis();
                                              });
        Thread.sleep(110);
        executorService.shutdown();

        // Then
        final long controlStart = (long) resultReference.get().controlObservation().value();
        final Observation<Long> candidate = (Observation<Long>) resultReference.get().candidateObservation().get();
        final long candidateStart = (long) candidate.value();
        
        assertThat(candidateStart).isGreaterThan(controlStart);
    }


    @Test
    public void synchronize_with_candidate_first() throws Exception {
        // Given
        final AtomicReference<Result> resultReference = new AtomicReference<>();
        final Experiment<Long> scientist = new Experiment<Long>() {
            @Override
            protected void publish(final Result result) {
                resultReference.set(result);
            }
            @Override
            protected ExecutionOrder executionOrder() {
                return ExecutionOrder.CANDIDATE_FIRST;
            }
        };
        
        // When
        final Long result = scientist.execute(() -> {
                                                Thread.sleep(100);
                                                return currentTimeMillis();
                                              },
                                              () -> currentTimeMillis());
        
        // Then
        final long controlStart = (long) resultReference.get().controlObservation().value();
        final Observation<Long> candidate = (Observation<Long>) resultReference.get().candidateObservation().get();
        final long candidateStart = (long) candidate.value();
        
        assertThat(candidateStart).isLessThan(controlStart);
    }

    @Test
    public void synchronize_with_control_first() throws Exception {
        // Given
        final AtomicReference<Result> resultReference = new AtomicReference<>();
        final Experiment<Long> scientist = new Experiment<Long>() {
            @Override
            protected void publish(final Result result) {
                resultReference.set(result);
            }
            @Override
            protected ExecutionOrder executionOrder() {
                return ExecutionOrder.CONTROL_FIRST;
            }
        };
        
        // When
        final Long result = scientist.execute(() -> currentTimeMillis(), 
                                              () -> {
                                                  Thread.sleep(100);
                                                  return currentTimeMillis();
                                              });

        // Then
        final long controlStart = (long) resultReference.get().controlObservation().value();
        final Observation<Long> candidate = (Observation<Long>) resultReference.get().candidateObservation().get();
        final long candidateStart = (long) candidate.value();
        
        assertThat(candidateStart).isGreaterThan(controlStart);
    }

    @Test
    public void synchronous_execption_throwing_execution_throwing() throws Exception {
        // Given
        final AtomicReference<Result> resultReference = new AtomicReference<>();
        final Experiment<String> scientist = new Experiment<String>() {
            @Override
            protected void publish(final Result result) {
                resultReference.set(result);
            }
        };
        
        // When
        final RuntimeException ex = assertThrows(RuntimeException.class, () -> scientist.execute(() -> {throw new RuntimeException("execution exception");}, () -> "candidate"));
        
        // Then
        assertThat(ex.getMessage()).isEqualTo("execution exception");
        assertThat(resultReference.get()).isNotNull();
    }

    @Test
    public void asynchronous_execption_throwing_control_execution_throwing() throws Exception {
        // Given
        final AtomicReference<Result> resultReference = new AtomicReference<>();
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Experiment<String> scientist = new Experiment<String>("async", executorService) {
            @Override
            protected void publish(final Result result) {
                resultReference.set(result);
            }
        };
        
        // When
        final RuntimeException ex = assertThrows(RuntimeException.class, () -> scientist.execute(() -> {throw new RuntimeException("execution exception");}, () -> "candidate"));
        Thread.sleep(110);
        executorService.shutdown();
        
        // Then
        assertThat(ex.getMessage()).isEqualTo("execution exception");
        assertThat(resultReference.get().controlObservation().exception().isPresent()).isTrue();
        assertThat(resultReference.get().candidateObservation().isPresent()).isTrue();

        final Observation<String> candidate = (Observation<String>) resultReference.get().candidateObservation().get();
        assertThat(candidate.value()).isEqualTo("candidate");
    }

    @Test
    public void asynchronous_execption_throwing_candidate_execution_throwing() throws Exception {
        // Given
        final AtomicReference<Result> resultReference = new AtomicReference<>();
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Experiment<String> scientist = new Experiment<String>("async", executorService) {
            @Override
            protected void publish(final Result result) {
                resultReference.set(result);
            }
        };
        
        // When
        final String result = scientist.execute(() -> "control", () -> {throw new RuntimeException("execution exception");});
        Thread.sleep(110);
        executorService.shutdown();
        
        // Then
        assertThat(result).isEqualTo("control");
        assertThat(resultReference.get().controlObservation().exception().isPresent()).isFalse();
        assertThat(resultReference.get().candidateObservation().isPresent()).isTrue();

        final Observation<String> candidate = (Observation<String>) resultReference.get().candidateObservation().get();
        assertThat(candidate.exception().get().getMessage()).isEqualTo("execution exception");
    }

    @Test
    public void throw_exception_in_publish() throws Exception {
        
        // Given
        final AtomicReference<Result> resultReference = new AtomicReference<>();
        final Experiment<String> scientist = new Experiment<String>() {
            @Override
            protected void publish(final Result result) {
                resultReference.set(result);
                throw new RuntimeException();
            }
        };
        
        // When
        final String result = scientist.execute(() -> "control", () -> "candidate");
        
        // Then
        assertThat(result).isEqualTo("control");
    }

    @Test
    public void throws_checked_exception() throws Exception {
        
        // Given
        final Experiment<String> scientist = new Experiment<>();
        
        // When
        final IOException ex = assertThrows(IOException.class, () -> scientist.execute(() -> {throw new IOException("io exception");}, () -> "candidate"));
        
        // Then
        assertThat(ex.getMessage()).isEqualTo("io exception");
    }

    @Test
    public void throws_ExecutionException_with_cause() throws Exception {
        
        // Given
        final Experiment<String> scientist = new Experiment<>();
        
        // Then
        assertThrows(IllegalStateException.class, () -> scientist.execute(() -> {throw new ExecutionException(new IllegalStateException());}, () -> "candidate"));
    }

    @Test
    public void throws_ExecutionException_with_null_cause() throws Exception {
        
        // Given
        final Experiment<String> scientist = new Experiment<>();
        
        // Then
        assertThrows(ExecutionException.class, () -> scientist.execute(() -> {throw new ExecutionException(null);}, () -> "candidate"));
    }
}
