package io.iktech.jenkins.plugins.artifactor;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Serializable;

public class Name extends AbstractDescribableImpl<Name> implements Serializable {
    private static final long serialVersionUID = -4829202992427812947L;
    private String name;

    @DataBoundConstructor
    public Name() {
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Name> {
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value == null || value.length() == 0) {
                return FormValidation.error(Messages.RetrieveArtifactsBuildStep_DescriptorImpl_errors_missingNames());
            }
            return FormValidation.ok();
        }
        @Override
        public String getDisplayName() {
            return Messages.ArtifactName_DescriptorImpl_DisplayName();
        }
    }
}
