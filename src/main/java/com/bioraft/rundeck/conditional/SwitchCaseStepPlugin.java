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
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.descriptions.RenderingOption;
import com.dtolabs.rundeck.plugins.descriptions.RenderingOptions;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.fasterxml.jackson.core.JsonProcessingException;

import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.CODE_SYNTAX_MODE;
import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.DISPLAY_TYPE_KEY;
import static com.bioraft.rundeck.conditional.Switch.CFG_DEFAULT_VALUE;

/**
 * Workflow Node Step Plug-in to choose one of several values to uplift into a
 * step variable.
 *
 * @author Karl DeBisschop <kdebisschop@gmail.com>
 * @since 2019-12-11
 */
@Plugin(name = SwitchCaseStepPlugin.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
@PluginDescription(title = "Switch/Case Conditional", description = "Chooses between options for a variable value based on switch/case structure.")
public class SwitchCaseStepPlugin implements StepPlugin {
	public static final String SERVICE_PROVIDER_NAME = "com.bioraft.rundeck.conditional.SwitchCaseStepPlugin";

	@PluginProperty(title = "Group", description = "Variable group (i.e., ${group.x}", required = true)
	private String group;

	@PluginProperty(title = "Name", description = "Variable name (i.e., ${group.name}", required = true)
	private String name;

	@PluginProperty(title = "Cases", description = "Cases and results as colon-spliced pairs of quoted strings, separated by commas (i.e., members of a JSON object)", required = true)
	@RenderingOptions({
			@RenderingOption(key = DISPLAY_TYPE_KEY, value = "CODE"),
			@RenderingOption(key = CODE_SYNTAX_MODE, value = "json"),
	})
	private String cases;

	@PluginProperty(title = "Test Value", description = "Test value", required = true)
	private String testValue;

	@PluginProperty(title = "Default", description = "Default value")
	private String defaultValue;

	@PluginProperty(title = "Make global?", description = "Elevate this variable to global scope (default: false)")
	private boolean elevateToGlobal;

	@Override
	public void executeStep(final PluginStepContext ctx, final Map<String, Object> cfg) throws StepException {

		elevateToGlobal = cfg.getOrDefault("elevateToGlobal", String.valueOf(elevateToGlobal)).equals("true");

		try {
			(new Switch(ctx, cfg, defaultValue)).switchCase(group, name, cases, testValue, elevateToGlobal);
		} catch (JsonProcessingException e) {
			throw new StepException(e.getMessage(), Switch.Causes.INVALID_JSON);
		}
	}

}
