package io.iktech.jenkins.plugins.artifactor;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;

public class InjectVariable implements EnvironmentContributingAction {
    private String key;
    private String value;

    public InjectVariable( String key, String value )
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }

    @Override
    public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
        if( env != null && key != null && value != null) {
            env.put(this.key, this.value);
        }
    }
}