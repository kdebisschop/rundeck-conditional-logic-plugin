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
import com.dtolabs.rundeck.plugins.step.NodeStepPlugin;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;

/**
 * Workflow Node Step Plug-in to choose one of several values to uplift into a
 * step variable.
 *
 * @author Karl DeBisschop <kdebisschop@gmail.com>
 * @since 2019-12-11
 */
@Plugin(name = SwitchCaseNodeStepPlugin.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowNodeStep)
@PluginDescription(title = "Switch/Case Conditional Node Step", description = "Chooses between options for a variable value based on switch/case structure.")
public class SwitchCaseNodeStepPlugin implements NodeStepPlugin {

	public static final String SERVICE_PROVIDER_NAME = "com.bioraft.rundeck.conditional.SwitchCaseNodeStepPlugin";

	@PluginProperty(title = "Group", description = "Variable group (i.e., ${group.x}", required = true)
	private String group;

	@PluginProperty(title = "Name", description = "Variable name (i.e., ${group.name}", required = true)
	private String name;

	@PluginProperty(title = "Cases", description = "Cases and results as colon-spliced pairs, seprated by semicolons", required = true)
	private String cases;

	@PluginProperty(title = "Test Value", description = "Test value", required = true)
	private String testValue;

	@PluginProperty(title = "Default", description = "Default value", required = false)
	private String defaultValue;

	@PluginProperty(title = "Make global?", description = "Elevate this variable to global scope (default: false)", required = false)
	private boolean elevateToGlobal;

	@Override
	public void executeNodeStep(PluginStepContext ctx, Map<String, Object> cfg, INodeEntry node)
			throws NodeStepException {

		String group = cfg.getOrDefault("group", this.group).toString();
		String name = cfg.getOrDefault("name", this.name).toString();
		String cases = cfg.getOrDefault("cases", this.cases).toString();
		String testValue = cfg.getOrDefault("testValue", this.testValue).toString();
		boolean elevateToGlobal = cfg.getOrDefault("elevateToGlobal", this.elevateToGlobal).equals("true");

		boolean globalHasDefault = defaultValue != null && defaultValue.length() > 0;
		boolean cfgHasDefault = cfg.containsKey("defaultValue") && cfg.get("defaultValue") != null;
		if (cfgHasDefault) {
			this.defaultValue = cfg.get("defaultValue").toString();
		}

		ctx.getLogger().log(Constants.DEBUG_LEVEL,
				"Setting " + group + "." + name + " based on " + testValue + " " + cases);

		if (cfgHasDefault || globalHasDefault) {
			(new Switch(ctx)).switchCase(group, name, cases, testValue, defaultValue, elevateToGlobal);
		} else {
			(new Switch(ctx)).switchCase(group, name, cases, testValue, elevateToGlobal);
		}
	}

}
