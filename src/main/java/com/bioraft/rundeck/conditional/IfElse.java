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
import com.dtolabs.rundeck.plugins.step.PluginStepContext;

import java.util.Map;

/**
 * Workflow Node Step Plug-in to choose one of several values to uplift into a
 * step variable.
 *
 * @author Karl DeBisschop <kdebisschop@gmail.com>
 * @since 2019-12-11
 */
public class IfElse {

	public static final String STRING_NE = "ne";
	public static final String STRING_LT = "lt";
	public static final String STRING_LE = "le";
	public static final String STRING_EQ = "eq";
	public static final String STRING_GE = "ge";
	public static final String STRING_GT = "gt";
	public static final String STRING_BEG = "begins with";
	public static final String STRING_END = "ends with";
	public static final String NUMBER_NE = "!=";
	public static final String NUMBER_LT = "<";
	public static final String NUMBER_LE = "<=";
	public static final String NUMBER_EQ = "=";
	public static final String NUMBER_GE = ">=";
	public static final String NUMBER_GT = ">";

	private PluginStepContext ctx;

	private Map<String, Object> cfg;

	/** If specified, also create a variable in global export context. */
	private boolean elevate = false;

	/**
	 * Constructor sets PluginStepContext.
	 *
	 * @param ctx Plugin step context.
	 */
	public IfElse(PluginStepContext ctx) {
		this.ctx = ctx;
	}

	public IfElse setElevate(boolean elevate) {
		this.elevate = elevate;
		return this;
	}

	public IfElse setCfg(Map<String, Object> cfg) {
		this.cfg = cfg;
		return this;
	}

	/**
	 * Add ifTrue value to SharedOutputContext if condition passes, otherwise add
	 * ifFalse if not null.
	 * 
	 * @param group           The group to place the variable in.
	 * @param name            The name of the variable.
	 * @param testValue       The value to test.
	 * @param operator        The comparison operator.
	 * @param comparisonValue The value to test against.
	 * @param ifTrue          The value to return if comparison is true.
	 * @param ifFalse         The value to return if comparison is false.
	 */
	public void ifElse(String group, String name, String testValue, String operator, String comparisonValue,
			String ifTrue, String ifFalse) {

		group = cfg.getOrDefault("group", group).toString();
		name = cfg.getOrDefault("name", name).toString();
		testValue = cfg.getOrDefault("testValue", testValue).toString();
		operator = cfg.getOrDefault("operator", operator).toString();
		comparisonValue = cfg.getOrDefault("comparisonValue", comparisonValue).toString();
		ifTrue = cfg.getOrDefault("ifTrue", ifTrue).toString();
		ifFalse = cfg.getOrDefault("ifFalse", ifFalse).toString();

		String value;
		String matched;

		if (operator.equals(STRING_EQ) && testValue.equals(comparisonValue)) {
			matched =  STRING_EQ;
		} else if (operator.equals(STRING_NE) && !testValue.equals(comparisonValue)) {
			matched =  STRING_NE;
		} else {
			matched = compareString(operator, testValue, comparisonValue);
		}

		if (matched.equals("")) {
			matched = compareNumeric(operator, testValue, comparisonValue);
		}

		if (matched.equals("")) {
			if (ifFalse.length() == 0) {
				ctx.getLogger().log(Constants.DEBUG_LEVEL, "No match, default is empty.");
				return;
			}
			ctx.getLogger().log(Constants.DEBUG_LEVEL, "No match, using default.");
			value = ifFalse;
		} else {
			value = ifTrue;
			ctx.getLogger().log(Constants.DEBUG_LEVEL, "Matched " + matched + ", returning ifTrue value.");
		}

		ctx.getOutputContext().addOutput(group, name, value);
		if (elevate) {
			String groupName = group + "." + name;
			ctx.getOutputContext().addOutput(ContextView.global(), "export", groupName, value);
			ctx.getLogger().log(Constants.DEBUG_LEVEL, "Elevating to globsal ${export." + groupName + "}.");
		}
	}

	private String compareString(String operator, String testValue, String comparisonValue) {
		if (operator.equals(STRING_BEG) && testValue.startsWith(comparisonValue)) {
			return STRING_BEG;
		} else if (operator.equals(STRING_END) && testValue.endsWith(comparisonValue)) {
			return STRING_END;
		}

		int compare = testValue.compareTo(comparisonValue);
		if (operator.equals(STRING_LT) && compare < 0) {
			return STRING_LT;
		} else if (operator.equals(STRING_LE) && compare <= 0) {
			return STRING_LE;
		} else if (operator.equals(STRING_GE) && compare >= 0) {
			return STRING_GE;
		} else if (operator.equals(STRING_GT) && compare > 0) {
			return STRING_GT;
		}
		return "";
	}

	private String compareNumeric(String operator, String testValue, String comparisonValue) {
		try {
			double testDouble = Double.parseDouble(testValue);
			double comparisonDouble = Double.parseDouble(comparisonValue);
			if (operator.equals(NUMBER_LT) && testDouble < comparisonDouble) {
				return NUMBER_LT;
			} else if (operator.equals(NUMBER_LE) && testDouble <= comparisonDouble) {
				return NUMBER_LE;
			} else if (operator.equals(NUMBER_GE) && testDouble >= comparisonDouble) {
				return NUMBER_GE;
			} else if (operator.equals(NUMBER_GT) && testDouble > comparisonDouble) {
				return NUMBER_GT;
			} else if (operator.equals(NUMBER_EQ) && testDouble == comparisonDouble) {
				return NUMBER_EQ;
			} else if (operator.equals(NUMBER_NE) && testDouble != comparisonDouble) {
				return NUMBER_NE;
			}
		} catch (Exception e) {
			return "";
		}
		return "";
	}
}
