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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.google.common.truth.Truth.assertThat;
import static io.misterspex.executor.TrialExecutor.of;
import java.util.concurrent.Callable;

/**
 *
 * @author sascha.kohlmann
 */
public class TrialExecutorTest {

    @Test
    public void lower_1_trials_no_return() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> of(0).execute(() -> doReturn(new Holder())));
        assertThat(ex.getLocalizedMessage()).startsWith("Trials must be > 0");
    }

    @Test
    public void null_executable() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> of().execute((Runnable) null));
        assertThat(ex.getLocalizedMessage()).isEqualTo("Executable must be provided");
    }
    
    @Test
    public void equal_1_trials_with_return() {
        final Holder holder = new Holder();
        of(1).execute(() -> doReturn(holder));
        assertThat(holder.hold).isEqualTo("abc");
    }
    
    @Test
    public void equal_1_trials_no_return() {
        final Holder holder = new Holder();
        of(1).execute(() -> noReturn(holder));
        assertThat(holder.hold).isEqualTo("abc");
    }
    
    @Test
    public void max_tries_no_return() {
        final ExecutionException ex = assertThrows(ExecutionException.class, () -> of(1).execute(() -> doThrowNoReturn("no return")));
        assertThat(ex.getCause().getLocalizedMessage()).isEqualTo("no return");
    }
    
    @Test
    public void lower_1_trials_return() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> of(0).execute(() -> {return doReturn(new Holder());}));
        assertThat(ex.getLocalizedMessage()).startsWith("Trials must be > 0");
    }

    @Test
    public void null_return_executable() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> of().execute((Callable) null));
        assertThat(ex.getLocalizedMessage()).isEqualTo("Executable must be provided");
    }
    
    @Test
    public void equal_1_trials_return() {
        final Holder holder = new Holder();
        final Holder retval = of(1).execute(() -> {return doReturn(holder);});
        assertThat(retval.hold).isEqualTo("abc");
    }

    @Test
    public void chained_trial_execution() {
        final Holder holder = new Holder();
        final TrialExecutor base = of();
        final TrialExecutor chaining = of(base);
        chaining.execute(() -> {return doReturn(holder);});
        assertThat(base.trials()).isEqualTo(1);
        assertThat(chaining.trials()).isEqualTo(1);
    }

    @Test
    public void max_tries_return() {
        final ExecutionException ex = assertThrows(ExecutionException.class, () -> of().execute(() -> {return doThrow("return");}));
        assertThat(ex.getCause().getLocalizedMessage()).isEqualTo("return");
    }

    private Holder doReturn(final Holder holder) {holder.hold = "abc"; return holder;}
    private void noReturn(final Holder holder) {holder.hold = "abc";}
    private String doThrow(final String message) {throw new RuntimeException(message);}
    private void doThrowNoReturn(final String message) {throw new RuntimeException(message);}

    private static final class Holder {
        public String hold = null;
    }
}
