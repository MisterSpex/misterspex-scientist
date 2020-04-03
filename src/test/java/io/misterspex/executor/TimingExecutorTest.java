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

import static com.google.common.truth.Truth.assertThat;
import static io.misterspex.executor.TimingExecutor.of;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author sascha.kohlmann
 */
public class TimingExecutorTest {

    @Test
    public void simple_timing() {
        // Given
        final TimingExecutor executor = of();
        
        // When
        executor.execute(() -> {;});

        // Then
        assertThat(executor.duration().getNano()).isGreaterThan(0);
    }

    @Test
    public void simple_chained_timing() {
        // Given
        final TimingExecutor base = of();
        final TimingExecutor executor = of(base);
        
        // When
        executor.execute(() -> {;});

        // Then
        assertThat(base.duration().getNano()).isGreaterThan(0);
        assertThat(executor.duration().getNano()).isGreaterThan(0);
    }

    @Test
    public void catch_not_ExecutionException_throwing() {
        // Given
        final TimingExecutor executor = of();
        
        // When
        final RuntimeException ex = assertThrows(RuntimeException.class, () -> executor.execute(() -> {throw new RuntimeException("test");}));

        // Then
        assertThat(ex).isInstanceOf(ExecutionException.class);
        assertThat(ex.getCause().getMessage()).isEqualTo("test");
        assertThat(executor.duration().getNano()).isGreaterThan(0);
    }

    @Test
    public void catch_ExecutionException_throwing() {
        // Given
        final TimingExecutor executor = of();
        
        // When
        final RuntimeException ex = assertThrows(RuntimeException.class, () -> executor.execute(() -> {throw new ExecutionException("execution", new Throwable("throwable"));}));

        // Then
        assertThat(ex).isInstanceOf(ExecutionException.class);
        assertThat(ex.getMessage()).isEqualTo("throwable");
        assertThat(executor.duration().getNano()).isGreaterThan(0);
    }
}
