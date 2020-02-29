package com.bioraft.rundeck.conditional;

import java.util.HashMap;
import java.util.Map;

public class SwitchTestBase {

    protected final String group = "raft";
    protected final String name = "test";
    protected final String testValue = "any";
    protected final String defaultValue = "any";

    protected Map<String, Object> getConfiguration(String testValue, StringBuffer caseString, String defaultValue) {
        Map<String, Object> configuration = new HashMap<>();
        configuration.put("group", group);
        configuration.put("name", name);
        configuration.put("cases", caseString);
        configuration.put("testValue", testValue);
        configuration.put("defaultValue", defaultValue);
        return configuration;
    }

}
