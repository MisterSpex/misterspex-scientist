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

/**
 * Thrown if an execution failed.
 */
public class ExecutionException extends RuntimeException {

    /**
     * Constructs an instance of <code>ExecutionException</code> with the specified detail message and the causal throwable.
     * @param message the detail message
     * @param cause the cause
     */
    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance of <code>ExecutionException</code> with the causal throwable.
     * @param cause the cause
     */
    public ExecutionException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Return the exception message auf the {@linkplain #getCause() cause} if available. Otherwise the message of this.
     * @return the exception message
     */
    @Override
    public String getMessage() {
        final Throwable cause = getCause();
        if (cause != null) {
            return cause.getMessage();
        }
        return super.getMessage();
    }

    /**
     * Return the localized exception message auf the {@linkplain #getCause() cause} if available.
     * Otherwise the localized message of this.
     * @return the localized exception message
     */
    @Override
    public String getLocalizedMessage() {
        final Throwable cause = getCause();
        if (cause != null) {
            return cause.getLocalizedMessage();
        }
        return super.getLocalizedMessage();
    }
}
