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

import java.util.Map;

import com.dtolabs.rundeck.core.Constants;
import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.descriptions.SelectValues;
import com.dtolabs.rundeck.plugins.step.NodeStepPlugin;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;

/**
 * Workflow Node Step Plug-in to choose one of two values to uplift into a step
 * variable.
 *
 * @author Karl DeBisschop <kdebisschop@gmail.com>
 * @since 2019-12-11
 */
@Plugin(name = IfElseNodeStepPlugin.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowNodeStep)
@PluginDescription(title = "If-Else Conditional Node Step", description = "Chooses between two options for a variable value based on if-else test.")
public class IfElseNodeStepPlugin implements NodeStepPlugin {

	public static final String SERVICE_PROVIDER_NAME = "com.bioraft.rundeck.conditional.IfTestWorkflowNodeStepPlugin";

	@PluginProperty(title = "Group", description = "Variable group (i.e., ${group.x}", required = true)
	private String group;

	@PluginProperty(title = "Name", description = "Variable name (i.e., ${group.name}", required = true)
	private String name;

	@PluginProperty(title = "Test Value", description = "First test value", required = true)
	private String testValue;

	@PluginProperty(title = "Operator", description = "Comparison operator", required = true)
	@SelectValues(values = { IfElse.STRING_EQ, IfElse.STRING_NE, IfElse.STRING_LT, IfElse.STRING_LE, IfElse.STRING_GE,
			IfElse.STRING_GT, IfElse.STRING_BEG, IfElse.STRING_END, IfElse.NUMBER_EQ, IfElse.NUMBER_NE,
			IfElse.NUMBER_LT, IfElse.NUMBER_LE, IfElse.NUMBER_GE, IfElse.NUMBER_GT }, freeSelect = false)
	private String operator;

	@PluginProperty(title = "Comparison Value", description = "Second test value", required = true)
	private String comparisonValue;

	@PluginProperty(title = "If True", description = "Value to assign if comparison is true", required = true)
	private String ifTrue;

	@PluginProperty(title = "If False", description = "Value to assign if comparison is false")
	private String ifFalse;

	@PluginProperty(title = "Make global?", description = "Elevate this variable to global scope (default: false)")
	private boolean elevateToGlobal;

	@Override
	public void executeNodeStep(PluginStepContext ctx, Map<String, Object> cfg, INodeEntry node)
			throws NodeStepException {

		group = cfg.getOrDefault("group", this.group).toString();
		name = cfg.getOrDefault("name", this.name).toString();
		testValue = cfg.getOrDefault("testValue", this.testValue).toString();
		operator = cfg.getOrDefault("operator", this.operator).toString();
		comparisonValue = cfg.getOrDefault("comparisonValue", this.comparisonValue).toString();
		ifTrue = cfg.getOrDefault("ifTrue", this.ifTrue).toString();
		boolean elevateToGlobal = (boolean) cfg.getOrDefault("elevateToGlobal", this.elevateToGlobal);
		ifFalse = cfg.getOrDefault("ifFalse", this.ifFalse).toString();

		String message = "Setting " + group + "." + name + " based on " + testValue + " " + operator + " " + comparisonValue;
		ctx.getLogger().log(Constants.DEBUG_LEVEL, message);

		(new IfElse(ctx)).setElevate(elevateToGlobal)
				.ifElse(group, name, testValue, operator, comparisonValue, ifTrue, ifFalse);
	}

}
