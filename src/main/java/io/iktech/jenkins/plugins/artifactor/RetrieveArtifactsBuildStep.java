package io.iktech.jenkins.plugins.artifactor;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.exception.ClientException;
import io.artifactz.client.model.Stage;
import io.artifactz.client.model.Version;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RetrieveArtifactsBuildStep extends Builder implements SimpleBuildStep {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Logger logger = LoggerFactory.getLogger(RetrieveArtifactsBuildStep.class);

    private List<Name> names;
    private String stage;
    private String variableName;

    @DataBoundConstructor
    public RetrieveArtifactsBuildStep(List<Name> names,
                                      String stage,
                                      String variableName) {
        logger.info("Creating builder. Stage: " + stage + ", names: " + names.size());
        this.names = names;
        this.stage  = stage;
        this.variableName = variableName;
    }

    public List<Name> getNames() {
        return this.names == null ? new ArrayList<>() : this.names;
    }

    @DataBoundSetter
    public void setNames(List<Name> names) {
        logger.info("Setting names: " + names.size());
        this.names  = names;
    }

    public String getStage() {
        return stage;
    }

    @DataBoundSetter
    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getVariableName() {
        return variableName;
    }

    @DataBoundSetter
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        PrintStream l = taskListener.getLogger();
        l.println("Retrieving versions of the following artifacts at the stage '" + this.stage + "'");
        StringCredentials token = CredentialsProvider.findCredentialById(Objects.requireNonNull(Configuration.get().getCredentialsId()), StringCredentials.class, run);
        try {
            assert token != null;
            ServiceClient client = ServiceHelper.getClient(taskListener, token.getSecret().getPlainText());
            List<String> artifacts = new ArrayList<>();
            for (Name name : this.getNames()) {
                artifacts.add(name.getName());
            }

            Stage stage = client.retrieveVersions(this.getStage(), artifacts.toArray(new String[0]));
            logger.info("Content has been converted to the object");
            EnvVars envVars = run.getEnvironment(taskListener);
            String content;
            if (stage.getArtifacts() != null) {
                logger.info("There are artifacts in the response, converting the result to the hashmap");
                content = objectMapper.writeValueAsString(stage.getArtifacts().stream().collect(Collectors.toMap(Version::getArtifactName, Version::getVersion)));
            } else {
                String errorMessage = "No artifacts data in the response";
                taskListener.fatalError(errorMessage);
                logger.info("Service returned empty result set");
                Objects.requireNonNull(run.getExecutor()).interrupt(Result.FAILURE);
                throw new AbortException(errorMessage);
            }
            envVars.put("_response", content);

            String variableName = !StringUtils.isEmpty(this.getVariableName()) ? this.getVariableName() : "ARTIFACTOR_VERSION_DATA";
            run.addAction(new InjectVariable(variableName, content));
            l.println("Successfully retrieved artifact versions");
        } catch (ClientException e) {
            logger.error("Error while retrieving artifact versions", e);
            String errorMessage = "Error while retrieving artifact versions: " + e.getMessage();
            taskListener.fatalError(errorMessage);
            Objects.requireNonNull(run.getExecutor()).interrupt(Result.FAILURE);
            throw new AbortException(errorMessage);
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public FormValidation doCheckNames(@QueryParameter List<Name> value)
                throws IOException, ServletException {
            if (value == null || value.size() == 0) {
                return FormValidation.error(Messages.RetrieveArtifactsBuildStep_DescriptorImpl_errors_missingNames());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckStage(@QueryParameter String value)
                throws IOException, ServletException {
            if (value == null || value.length() == 0) {
                return FormValidation.error(Messages.Artifact_DescriptorImpl_errors_missingStage());
            }
            return FormValidation.ok();
        }
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.RetrieveArtifactsBuildStep_DescriptorImpl_DisplayName();
        }
    }
}
