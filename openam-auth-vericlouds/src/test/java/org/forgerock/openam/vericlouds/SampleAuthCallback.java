package org.forgerock.openam.vericlouds;

import java.util.List;

public class SampleAuthCallback {

    private String authId;
    private String template;
    private String stage;
    private String header;
    private List<Callback> callbacks;

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public List<Callback> getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(List<Callback> callbacks) {
        this.callbacks = callbacks;
    }

    public void setUsernameAndPassword(final String username, final String password) {
        Callback callback = this.getCallbacks().get(0);
        if (callback.getType().equals("NameCallback")) {
            callback.setInputValue(username);
            this.getCallbacks().get(1).setInputValue(password);
        }
        else {
            callback.setInputValue(password);
            this.getCallbacks().get(1).setInputValue(username);
        }
    }

    @Override
    public String toString() {
        return "SampleAuthCallback{" +
                "authId='" + authId + '\'' +
                ", template='" + template + '\'' +
                ", stage='" + stage + '\'' +
                ", header='" + header + '\'' +
                ", callbacks=" + callbacks +
                '}';
    }
}
