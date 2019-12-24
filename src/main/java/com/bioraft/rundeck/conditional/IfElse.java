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

	/**
	 * Constructor sets PluginStepContext.
	 *
	 * @param ctx
	 */
	public IfElse(PluginStepContext ctx) {
		this.ctx = ctx;
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
	 * @param elevateToGlobal If specified, also create a variable in global export
	 *                        context.
	 */
	public void ifElse(String group, String name, String testValue, String operator, String comparisonValue,
			String ifTrue, String ifFalse, boolean elevateToGlobal) {

		String value;
		String matched = null;

		if (operator.equals(STRING_EQ) && testValue.endsWith(comparisonValue)) {
			matched = STRING_EQ;
		} else if (operator.equals(STRING_NE) && !testValue.equals(comparisonValue)) {
			matched = STRING_NE;
		} else if (operator.equals(STRING_BEG) && testValue.startsWith(comparisonValue)) {
			matched = STRING_BEG;
		} else if (operator.equals(STRING_END) && testValue.endsWith(comparisonValue)) {
			matched = STRING_END;
		} else if (operator.equals(STRING_LT) && testValue.compareTo(comparisonValue) < 0) {
			matched = STRING_LT;
		} else if (operator.equals(STRING_LE) && testValue.compareTo(comparisonValue) <= 0) {
			matched = STRING_LE;
		} else if (operator.equals(STRING_GE) && testValue.compareTo(comparisonValue) >= 0) {
			matched = STRING_GE;
		} else if (operator.equals(STRING_GT) && testValue.compareTo(comparisonValue) > 0) {
			matched = STRING_GT;
		} else if (operator.equals(NUMBER_LT) && Double.parseDouble(testValue) < Double.parseDouble(comparisonValue)) {
			matched = NUMBER_LT;
		} else if (operator.equals(NUMBER_LE) && Double.parseDouble(testValue) <= Double.parseDouble(comparisonValue)) {
			matched = NUMBER_LE;
		} else if (operator.equals(NUMBER_GE) && Double.parseDouble(testValue) >= Double.parseDouble(comparisonValue)) {
			matched = NUMBER_GE;
		} else if (operator.equals(NUMBER_GT) && Double.parseDouble(testValue) > Double.parseDouble(comparisonValue)) {
			matched = NUMBER_GT;
		} else if (operator.equals(NUMBER_EQ) && Double.parseDouble(testValue) == Double.parseDouble(comparisonValue)) {
			matched = NUMBER_EQ;
		} else if (operator.equals(NUMBER_NE) && Double.parseDouble(testValue) != Double.parseDouble(comparisonValue)) {
			matched = NUMBER_NE;
		}

		if (matched != null) {
			value = ifTrue;
			ctx.getLogger().log(Constants.DEBUG_LEVEL, "Matched " + matched + ", returning ifTrue value.");
		} else {
			if (ifFalse == null || ifFalse.length() == 0) {
				ctx.getLogger().log(Constants.DEBUG_LEVEL, "No match, default is empty.");
				return;
			}
			ctx.getLogger().log(Constants.DEBUG_LEVEL, "No match, using default.");
			value = ifFalse;
		}

		ctx.getOutputContext().addOutput(group, name, value);
		if (elevateToGlobal) {
			String groupName = group + "." + name;
			ctx.getOutputContext().addOutput(ContextView.global(), "export", groupName, value);
			ctx.getLogger().log(Constants.DEBUG_LEVEL, "Elevating to globsal ${export." + groupName + "}.");
		}
	}

}
