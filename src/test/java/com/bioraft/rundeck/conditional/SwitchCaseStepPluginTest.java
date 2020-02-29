/*
 * Copyright 2019 BioRAFT, Inc. (http://bioraft.com)
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
package com.bioraft.rundeck.conditional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.dtolabs.rundeck.core.execution.workflow.SharedOutputContext;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.google.common.collect.ImmutableMap;

/**
 * Tests for IfTestStepPlugin.
 *
 * @author Karl DeBisschop <kdebisschop@gmail.com>
 * @since 2019-12-11
 */
@RunWith(MockitoJUnitRunner.class)
public class SwitchCaseStepPluginTest {

	SwitchCaseStepPlugin plugin;

	@Mock
	PluginStepContext context;

	@Mock
	PluginLogger logger;
	
	@Mock
	SharedOutputContext sharedOutputContext;

	private final String group = "raft";
	private final String name = "test";
	private final String testValue = "any";
	private final String defaultValue = "any";

	@Before
	public void setUp() {
		this.plugin = new SwitchCaseStepPlugin();
	}

	@Test
	public void runTestOne() throws StepException {
		Map<String, String> cases = ImmutableMap.<String, String>builder().put("k1", "v1").put("k2", "v2").build();
		this.runTest("v1", "k1", cases, "any");
	}

	@Test
	public void runTestTwo() throws StepException {
		Map<String, String> cases = ImmutableMap.<String, String>builder().put("k1", "v1").put("k2", "v2").build();
		this.runTest("v2", "k2", cases, "thing");
	}

	@Test
	public void returnsDefaultOnNoMatch() throws StepException {
		Map<String, String> cases = ImmutableMap.<String, String>builder().put("k1", "v1").put("k2", "v2").build();
		this.runTest("any", "k3", cases, "any");
	}

	@Test
	public void runTestFour() throws StepException {
		Map<String, Object> configuration = new HashMap<>();
		configuration.put("defaultValue", null);
		this.runTestNoDefault(configuration);
	}

	@Test
	public void runTestFive() throws StepException {
		Map<String, Object> configuration = new HashMap<>();
		this.runTestNoDefault(configuration);
	}

	@Test
	public void runTestDefaultIsNull() throws StepException {
		Map<String, Object> configuration = new HashMap<>();
		configuration.put("defaultValue", null);
		this.runTestNoDefault(configuration);
	}

	@Test
	public void runTestNoDefaultValue() throws StepException {
		Map<String, Object> configuration = new HashMap<>();
		this.runTestNoDefault(configuration);
	}

	@Test
	public void testStrippingTrailingComma() throws StepException {
		StringBuffer caseString = new StringBuffer();
		Map<String, String> cases = ImmutableMap.<String, String>builder().put("k1", "v1").put("k2", "v2").build();
		cases.forEach((k, v) -> caseString.append('"').append(k).append('"').append(":").append('"').append(v).append('"').append(","));
		validInput(caseString.toString());
	}

	@Test(expected = StepException.class)
	public void testInvalidCases() throws StepException {
		StringBuffer caseString = new StringBuffer();
		Map<String, String> cases = ImmutableMap.<String, String>builder().put("k1", "v1").put("k2", "v2").build();
		cases.forEach((k, v) -> caseString.append('"').append(k).append('"').append(":").append('"').append(v).append('"').append("."));
		invalidInput(caseString.toString());
	}

	private void validInput(String caseString)
			throws StepException {

		Map<String, Object> configuration = new HashMap<>();
		configuration.put("group", group);
		configuration.put("name", name);
		configuration.put("cases", caseString);
		configuration.put("testValue", testValue);
		configuration.put("defaultValue", defaultValue);

		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		when(context.getLogger()).thenReturn(logger);

		this.plugin.executeStep(context, configuration);
		verify(context, times(1)).getOutputContext();
		verify(sharedOutputContext, times(1)).addOutput(eq(group), eq(name), eq(defaultValue));
	}

	private void invalidInput(String caseString)
			throws StepException {

		Map<String, Object> configuration = new HashMap<>();
		configuration.put("group", group);
		configuration.put("name", name);
		configuration.put("cases", caseString);
		configuration.put("testValue", testValue);
		configuration.put("defaultValue", defaultValue);

		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		when(context.getLogger()).thenReturn(logger);

		this.plugin.executeStep(context, configuration);
	}

	private void runTest(String expected, String testValue, Map<String, String> cases, String defaultValue)
			throws StepException {
		String group = "raft";
		String name = "test";
		StringBuffer caseString = new StringBuffer();
		cases.forEach((k, v) -> caseString.append('"').append(k).append('"').append(":").append('"').append(v).append('"').append(","));
		caseString.setLength(caseString.length() - 1);

		Map<String, Object> configuration = new HashMap<>();
		configuration.put("group", group);
		configuration.put("name", name);
		configuration.put("cases", caseString);
		configuration.put("testValue", testValue);
		configuration.put("defaultValue", defaultValue);

		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		when(context.getLogger()).thenReturn(logger);

		this.plugin.executeStep(context, configuration);
		verify(context, times(1)).getOutputContext();
		verify(sharedOutputContext, times(1)).addOutput(eq(group), eq(name), eq(expected));
	}

	public void runTestNoDefault(Map<String, Object> configuration) throws StepException {
		String group = "raft";
		String name = "test";

		Map<String, String> cases = ImmutableMap.<String, String>builder().put("k1", "v1").put("k2", "v2").build();
		StringBuilder caseString = new StringBuilder();
		cases.forEach((k, v) -> caseString.append('"').append(k).append('"').append(":").append('"').append(v).append('"').append(","));
		caseString.setLength(caseString.length() - 1);
		configuration.put("cases", caseString.toString());

		configuration.put("group", group);
		configuration.put("name", name);		
		configuration.put("testValue", "v3");

		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		when(context.getLogger()).thenReturn(logger);

		this.plugin.executeStep(context, configuration);
		verify(context, never()).getOutputContext();
		verify(sharedOutputContext, never()).addOutput(any(String.class), any(String.class), any(String.class));
	}
}
