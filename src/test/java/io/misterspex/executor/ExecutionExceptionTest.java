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
import org.junit.jupiter.api.Test;

/**
 *
 * @author sascha.kohlmann
 */
public class ExecutionExceptionTest {

    @Test
    public void getMessage_with_cause() {
        
        // Given
        final Throwable t = new Throwable("test");
        
        // When
        final ExecutionException ex = new ExecutionException("out", t);
        
        // Then
        assertThat(ex.getMessage()).isEqualTo("test");
    }

    @Test
    public void with_cause() {
        
        // Given
        final Throwable t = new Throwable("test");
        
        // When
        final ExecutionException ex = new ExecutionException(t);
        
        // Then
        assertThat(ex.getMessage()).isEqualTo("test");
    }

    @Test
    public void getMessage_with_null_cause() {
        
        // When
        final ExecutionException ex = new ExecutionException("out", null);
        
        // Then
        assertThat(ex.getMessage()).isEqualTo("out");
    }

    @Test
    public void getLocalizedMessage_with_cause() {
        
        // Given
        final Throwable t = new Throwable("test");
        
        // When
        final ExecutionException ex = new ExecutionException("out", t);
        
        // Then
        assertThat(ex.getLocalizedMessage()).isEqualTo("test");
    }

    @Test
    public void getLocalizedMessage_with_null_cause() {
        
        // When
        final ExecutionException ex = new ExecutionException("out", null);
        
        // Then
        assertThat(ex.getLocalizedMessage()).isEqualTo("out");
    }
}
