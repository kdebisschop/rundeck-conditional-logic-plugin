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

import com.dtolabs.rundeck.core.dispatcher.ContextView;
import com.dtolabs.rundeck.core.execution.workflow.SharedOutputContext;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for IfTestStepPlugin.
 *
 * @author Karl DeBisschop <kdebisschop@gmail.com>
 * @since 2019-12-11
 */
@RunWith(MockitoJUnitRunner.class)
public class IfElseTest {

	IfElse plugin;

	@Mock
	PluginStepContext context;

	@Mock
	PluginLogger logger;
	
	@Mock
	SharedOutputContext sharedOutputContext;

	Map<String, Object> configuration;

	@Before
	public void setUp() {
		this.plugin = new IfElse(context);
		configuration = new HashMap<>();
	}

	@Test
	public void runTrueTests() {
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
	public void runFalseTests() {
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

	@Test
	public void runNoDefaultTests() {
		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		when(context.getLogger()).thenReturn(logger);

		this.plugin.setElevate(false).setCfg(configuration).
				ifElse("raft", "test", "apple", "EQ", "apples", "1", "");
		verify(context, never()).getOutputContext();
		verify(sharedOutputContext, never()).addOutput(anyString(), anyString(), anyString());
	}

	@Test
	public void testElevation() {
		String group = "raft";
		String name = "test";
		String ifTrue = "1";

		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		when(context.getLogger()).thenReturn(logger);

		this.plugin.setElevate(true).setCfg(configuration).ifElse(group, name, name, "EQ", name, ifTrue, ifTrue);
		verify(context, times(2)).getOutputContext();
		verify(sharedOutputContext, times(1)).addOutput(eq(group), eq(name), eq(ifTrue));
		verify(sharedOutputContext, times(1)).addOutput(any(ContextView.class), eq("export"), anyString(), eq(ifTrue));
	}

	private void runTestTrue(String testValue, String operator, String comparison, int calls) {
		String group = "raft";
		String name = "test";
		String ifTrue = "1";
		String ifFalse = "0";

		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		when(context.getLogger()).thenReturn(logger);

		this.plugin.setElevate(false).setCfg(configuration).ifElse(group, name, testValue, operator, comparison, ifTrue, ifFalse);
		verify(context, atLeast(calls)).getOutputContext();
		verify(sharedOutputContext, atLeast(calls)).addOutput(eq(group), eq(name), eq(ifTrue));
	}

	private void runTestFalse(String testValue, String operator, String comparison, int calls) {
		String group = "boat";
		String name = "real";
		String ifTrue = "yes";
		String ifFalse = "no";

		when(context.getOutputContext()).thenReturn(sharedOutputContext);
		when(context.getLogger()).thenReturn(logger);

		this.plugin.setElevate(false).setCfg(configuration).ifElse(group, name, testValue, operator, comparison, ifTrue, ifFalse);
		verify(context, atLeast(calls)).getOutputContext();
		verify(sharedOutputContext, atLeast(calls)).addOutput(eq(group), eq(name), eq(ifFalse));
	}
}
