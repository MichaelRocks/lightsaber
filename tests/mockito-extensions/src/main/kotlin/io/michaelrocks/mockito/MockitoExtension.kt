/*
 * Copyright 2016 Michael Rozumyanskiy
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

package io.michaelrocks.mockito

import org.mockito.InOrder
import org.mockito.MockSettings
import org.mockito.MockingDetails
import org.mockito.Mockito
import org.mockito.stubbing.Answer
import org.mockito.stubbing.DeprecatedOngoingStubbing
import org.mockito.stubbing.OngoingStubbing
import org.mockito.stubbing.Stubber
import org.mockito.verification.VerificationAfterDelay
import org.mockito.verification.VerificationMode
import org.mockito.verification.VerificationWithTimeout

val RETURNS_DEFAULTS: Answer<Any>
  get() = Mockito.RETURNS_DEFAULTS

val RETURNS_SMART_NULLS: Answer<Any>
  get() = Mockito.RETURNS_SMART_NULLS

val RETURNS_MOCKS: Answer<Any>
  get() = Mockito.RETURNS_MOCKS

val RETURNS_DEEP_STUBS: Answer<Any>
  get() = Mockito.RETURNS_DEEP_STUBS

val CALLS_REAL_METHODS: Answer<Any>
  get() = Mockito.CALLS_REAL_METHODS

inline fun <reified T : Any> mock(): T = Mockito.mock(T::class.java)
inline fun <reified T : Any> mock(name: String): T = Mockito.mock(T::class.java, name)
inline fun <reified T : Any> mock(defaultAnswer: Answer<Any>): T = Mockito.mock(T::class.java, defaultAnswer)
inline fun <reified T : Any> mock(mockSettings: MockSettings): T = Mockito.mock(T::class.java, mockSettings)

fun mockingDetails(toInspect: Any): MockingDetails = Mockito.mockingDetails(toInspect)

fun <T> spy(instance: T): T = Mockito.spy(instance)
inline fun <reified T : Any> spy(): T = Mockito.spy(T::class.java)

fun <T> stub(methodCall: T): DeprecatedOngoingStubbing<T> = Mockito.stub(methodCall)

fun <T> given(methodCall: T): OngoingStubbing<T> = Mockito.`when`(methodCall)

fun <T> verify(mock: T): T = Mockito.verify(mock)
fun <T> verify(mock: T, mode: VerificationMode = times(1)): T = Mockito.verify(mock, mode)
fun verifyNoMoreInteractions(vararg mocks: Any) = Mockito.verifyNoMoreInteractions(*mocks)
fun verifyZeroInteractions(vararg mocks: Any) = Mockito.verifyZeroInteractions(*mocks)

fun <T> reset(vararg mocks: T) = Mockito.reset(*mocks)

fun doThrow(toBeThrown: Throwable): Stubber = Mockito.doThrow(toBeThrown)
inline fun <reified T : Throwable> doThrow(): Stubber = Mockito.doThrow(T::class.java)
fun doCallRealMethod(): Stubber = Mockito.doCallRealMethod()
fun doAnswer(answer: Answer<Any>): Stubber = Mockito.doAnswer(answer)
fun doNothing(): Stubber = Mockito.doNothing()
fun doReturn(toBeReturned: Any): Stubber = Mockito.doReturn(toBeReturned)

fun inOrder(vararg mocks: Any): InOrder = Mockito.inOrder(*mocks)
fun ignoreStubs(vararg mocks: Any): Array<Any> = Mockito.ignoreStubs(*mocks)

fun times(wantedNumberOfInvocations: Int): VerificationMode = Mockito.times(wantedNumberOfInvocations)
fun never(): VerificationMode = Mockito.never()
fun atLeastOnce(): VerificationMode = Mockito.atLeastOnce()
fun atLeast(minNumberOfInvocations: Int): VerificationMode = Mockito.atLeast(minNumberOfInvocations)
fun atMost(maxNumberOfInvocations: Int): VerificationMode = Mockito.atMost(maxNumberOfInvocations)
fun calls(wantedNumberOfInvocations: Int): VerificationMode = Mockito.calls(wantedNumberOfInvocations)
fun only(): VerificationMode = Mockito.only()
fun timeout(millis: Long): VerificationWithTimeout = Mockito.timeout(millis)
fun after(millis: Long): VerificationAfterDelay = Mockito.after(millis)

fun validateMockitoUsage() = Mockito.validateMockitoUsage()
fun withSettings(): MockSettings = Mockito.withSettings()
fun description(description: String): VerificationMode = Mockito.description(description)
