package org.forgerock.openam.vericlouds;

import java.util.List;

public class Callback {

    private String type;
    private List<NameValuePair> input;
    private List<NameValuePair> output;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<NameValuePair> getInput() {
        return input;
    }

    public void setInput(List<NameValuePair> input) {
        this.input = input;
    }

    public List<NameValuePair> getOutput() {
        return output;
    }

    public void setOutput(List<NameValuePair> output) {
        this.output = output;
    }

    public void setInputValue(final String value) {
        input.get(0).setValue(value);
    }

    @Override
    public String toString() {
        return "Callback{" +
                "type='" + type + '\'' +
                ", input=" + input +
                ", output=" + output +
                '}';
    }
}