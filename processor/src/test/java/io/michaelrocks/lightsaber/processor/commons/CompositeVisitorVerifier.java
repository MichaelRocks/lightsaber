/*
 * Copyright 2015 Michael Rozumyanskiy
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
 */

package io.michaelrocks.lightsaber.processor.commons;

import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class CompositeVisitorVerifier {
    private static final int MAX_VISITOR_COUNT = 3;
    private static final boolean[][] EMPTIES_PERMUTATIONS = new boolean[][] {
            {},
            { true },
            { false },
            { true, true },
            { false, true },
            { true, false },
            { false, false },
            { true, true, true },
            { true, true, false },
            { true, false, true },
            { false, true, true },
            { true, false, false },
            { false, false, false }
    };

    public static <T> void verifyMethodInvocations(final Class<? extends CompositeVisitor<T>> compositeVisitorClass,
            final Action<T, ?> action) throws Exception {
        for (int i = 0; i <= MAX_VISITOR_COUNT; ++i) {
            // noinspection unchecked
            verifyMethodInvocation(compositeVisitorClass.newInstance(), action, i);
        }
    }

    private static <T> void verifyMethodInvocation(final CompositeVisitor<T> compositeVisitor,
            final Action<T, ?> action, final int visitorCount) {
        final List<T> visitors = new ArrayList<>(visitorCount);
        for (int i = 0; i < visitorCount; ++i) {
            // noinspection unchecked
            final T visitor = (T) mock(compositeVisitor.getClass().getSuperclass());
            visitors.add(visitor);
            compositeVisitor.addVisitor(visitor);
        }

        // noinspection unchecked
        action.invoke((T) compositeVisitor);

        for (final T visitor : visitors) {
            action.invoke(verify(visitor, only()));
        }
    }

    public static <T, R> void verifyCompositeMethodInvocations(
            final Class<? extends CompositeVisitor<T>> compositeVisitorClass,
            final Action<T, R> action, final Action<R, ?> innerAction) throws Exception {
        for (final boolean[] empties : EMPTIES_PERMUTATIONS) {
            verifyCompositeMethodInvocation(compositeVisitorClass.newInstance(), action, innerAction, empties);
        }
    }

    private static <T, R> void verifyCompositeMethodInvocation(final CompositeVisitor<T> compositeVisitor,
            final Action<T, R> action, final Action<R, ?> innerAction, final boolean[] empties) {
        final List<T> visitors = new ArrayList<>(empties.length);
        for (final boolean empty : empties) {
            final Answer answer = empty ? RETURNS_DEFAULTS : RETURNS_DEEP_STUBS;
            // noinspection unchecked
            final T visitor = (T) mock(compositeVisitor.getClass().getSuperclass(), answer);
            visitors.add(visitor);
            compositeVisitor.addVisitor(visitor);
        }

        // noinspection unchecked
        final R result = action.invoke((T) compositeVisitor);

        for (final T visitor : visitors) {
            action.invoke(verify(visitor, only()));
        }

        if (result != null) {
            innerAction.invoke(result);
        }

        boolean hasNonEmpty = false;
        for (int i = 0; i < empties.length; ++i) {
            final boolean empty = empties[i];
            if (!empty) {
                hasNonEmpty = true;
                final T visitor = visitors.get(i);
                assertNotNull(result);
                innerAction.invoke(verify(action.invoke(visitor), only()));
            }
        }

        if (!hasNonEmpty) {
            assertNull(result);
        }
    }

    public interface Action<T, R> {
        R invoke(T instance);
    }
}
