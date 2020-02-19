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

import com.dtolabs.rundeck.core.Constants;
import com.dtolabs.rundeck.core.dispatcher.ContextView;
import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.Map;

/**
 * Workflow Node Step Plug-in to choose one of several values to uplift into a
 * step variable.
 *
 * @author Karl DeBisschop <kdebisschop@gmail.com>
 * @since 2019-12-11
 */
public class Switch {

	public static final String CFG_DEFAULT_VALUE = "defaultValue";

	private PluginStepContext ctx;

	/**
	 * Constructor sets PluginStepContext.
	 *
	 * @param ctx Plugin step context
	 */
	public Switch(PluginStepContext ctx) {
		this.ctx = ctx;
	}

	/**
	 * Assign a value to SharedOutputContext if a test case is matched, otherwise
	 * assign default if non-null..
	 * 
	 * @param group        The group to place the variable in.
	 * @param name         The name of the variable.
	 * @param cases        The switch cases as test1:value1;test2:value2
	 * @param test         The string to test the cases against.
	 * @param defaultValue The value to return if no cases match
	 * @param elevate      If specified, also create a variable in global export
	 *                     context.
	 */
	public void switchCase(String group, String name, String cases, String test, String defaultValue, boolean elevate) throws JsonProcessingException {
		// If no case was matched, assign defaultValue if it is not null.
		try {
			if (!switchCase(group, name, cases, test, elevate)) {
				if (defaultValue != null && defaultValue.length() > 0) {
					addOutput(elevate, group, name, defaultValue);
					ctx.getLogger().log(Constants.DEBUG_LEVEL, "No match, using default.");
				} else {
					ctx.getLogger().log(Constants.DEBUG_LEVEL, "No match, default is empty.");
				}
			}
		} catch (JsonProcessingException e) {
			ctx.getLogger().log(Constants.ERR_LEVEL, "Failed to parse cases.");
			ctx.getLogger().log(Constants.ERR_LEVEL, e.getMessage());
			throw e;
		}
	}

	/**
	 * Performs the actual comparison, returning true if a match is found.
	 * 
	 * @param group   The group to place the variable in.
	 * @param name    The name of the variable.
	 * @param cases   The switch cases as test1:value1;test2:value2
	 * @param test    The string to test the cases against.
	 * @param elevate If specified, also create a variable in global export context.
	 * 
	 * @return True if matched, false otherwise.
	 */
	public boolean switchCase(String group, String name, String cases, String test, boolean elevate) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode map = objectMapper.readTree(ensureStringIsJsonObject(cases));
		Iterator<Map.Entry<String, JsonNode>> iterator = map.fields();
		while (iterator.hasNext()) {
			Map.Entry<String, JsonNode> entry = iterator.next();
			String key = entry.getKey();
			String value = entry.getValue().asText();
			if (test.equals(key)) {
				addOutput(elevate, group, name, value);
				ctx.getLogger().log(Constants.DEBUG_LEVEL, "Matched " + key + ".");
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds output to shared context, also elevating to global if requested.
	 * 
	 * @param elevate Elevate to global context?
	 * @param group Variable group.
	 * @param name Variable name.
	 * @param value Variable value.
	 */
	private void addOutput(boolean elevate, String group, String name, String value) {
		ctx.getOutputContext().addOutput(group, name, value);
		if (elevate) {
			String groupName = group + "." + name;
			ctx.getOutputContext().addOutput(ContextView.global(), "export", groupName, value);
			ctx.getLogger().log(Constants.DEBUG_LEVEL, "Elevating to globsal ${export." + groupName + "}.");
		}
	}

	public static String ensureStringIsJsonObject(String string) {
		if (string == null) {
			return "";
		}
		String trimmed = string.trim().replaceFirst(",$", "");
		return (trimmed.startsWith("{") ? "" : "{") + trimmed + (trimmed.endsWith("}") ? "" : "}");
	}

	enum Causes implements FailureReason {
		INVALID_JSON
	}
}
