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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
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

/**
 * Tests for IfTestStepPlugin.
 *
 * @author Karl DeBisschop <kdebisschop@gmail.com>
 * @since 2019-12-11
 */
@RunWith(MockitoJUnitRunner.class)
public class IfElseStepPluginTest {

	IfElseStepPlugin plugin;

	@Mock
	PluginStepContext context;

	@Mock
	PluginLogger logger;
	
	@Mock
	SharedOutputContext sharedOutputContext;

	@Before
	public void setUp() {
		this.plugin = new IfElseStepPlugin();
	}

	@Test
	public void runTrueTests() throws StepException {
		int i = 0;
		this.runTestTrue("apple", "eq", "apple", ++i);
		this.runTestTrue("apple", "ne", "pear", ++i);
		this.runTestTrue("bannana", "lt", "strawberry", ++i);
		this.runTestTrue("pear", "gt", "apple", ++i);
		this.runTestTrue("apple", "le", "apple", ++i);
		this.runTestTrue("applepie", "ge", "apple", ++i);
		this.runTestTrue("apple pie", "begins with", "apple", ++i);
		this.runTestTrue("layer cake", "ends with", " cake", ++i);
		this.runTestTrue("1.00", "=", "1.000", ++i);
		this.runTestTrue("3", "<", "200", ++i);
		this.runTestTrue("1.23", ">", "1.199", ++i);
		this.runTestTrue("1", "<=", "1", ++i);
		this.runTestTrue("1", ">=", "0", ++i);
		this.runTestTrue("1", "!=", "2", ++i);
	}

	@Test
	public void runFalseTests() throws StepException {
		int i = 0;
		this.runTestFalse("apple", "eq", "apples", ++i);
		this.runTestFalse("apple", "ne", "apple", ++i);
		this.runTestFalse("bannana", "gt", "strawberry", ++i);
		this.runTestFalse("pear", "lt", "apple", ++i);
		this.runTestFalse("apples", "le", "apple", ++i);
		this.runTestFalse("apple", "ge", "banana", ++i);
		this.runTestFalse("apple pie", "begins with", "pie", ++i);
		this.runTestFalse("layer cake", "ends with", "layer", ++i);
		this.runTestFalse("1", "=", "2", ++i);
		this.runTestFalse("300", "<", "200", ++i);
		this.runTestFalse("1.13", ">", "1.199", ++i);
		this.runTestFalse("1", "<=", "0", ++i);
		this.runTestFalse("1", ">=", "2", ++i);
		this.runTestFalse("2.0", "!=", "2.00", ++i);
	}

	private void runTestTrue(String testValue, String operator, String comparison, int calls) throws StepException {
		String group = "raft";
		String name = "test";
		String ifTrue = "1";
		String ifFalse = "0";

		Map<String, Object> configuration = new HashMap<>();
		configuration.put("group", group);
		configuration.put("name", name);
		configuration.put("testValue", testValue);
		configuration.put("operator", operator);
		configuration.put("comparisonValue", comparison);
		configuration.put("ifTrue", ifTrue);
		configuration.put("ifFalse", ifFalse);

		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		when(context.getLogger()).thenReturn(logger);

		this.plugin.executeStep(context, configuration);
		verify(context, atLeast(calls)).getOutputContext();
		verify(sharedOutputContext, atLeast(calls)).addOutput(eq(group), eq(name), eq(ifTrue));
	}

	private void runTestFalse(String testValue, String operator, String comparison, int calls)
			throws StepException {
		String group = "boat";
		String name = "real";
		String ifTrue = "yes";
		String ifFalse = "no";

		Map<String, Object> configuration = new HashMap<>();
		configuration.put("group", group);
		configuration.put("name", name);
		configuration.put("testValue", testValue);
		configuration.put("operator", operator);
		configuration.put("comparisonValue", comparison);
		configuration.put("ifTrue", ifTrue);
		configuration.put("ifFalse", ifFalse);

		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		when(context.getLogger()).thenReturn(logger);

		this.plugin.executeStep(context, configuration);
		verify(context, atLeast(calls)).getOutputContext();
		verify(sharedOutputContext, atLeast(calls)).addOutput(eq(group), eq(name), eq(ifFalse));
	}
}
