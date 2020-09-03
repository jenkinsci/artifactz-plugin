package io.iktech.jenkins.plugins.artifactor;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.iktech.jenkins.plugins.artifactor.model.Stage;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ArtifactRetrieverStep extends Step {
    private static Logger logger = LoggerFactory.getLogger(ArtifactRetrieverStep.class);
    private String stage;
    private List<String> names;
    private String variable;

    @DataBoundConstructor
    public ArtifactRetrieverStep(String stage, List<String> names, String variable) {
        this.stage = stage;
        this.names = names;
        this.variable = variable;
    }

    public String getStage() {
        return stage;
    }

    @DataBoundSetter
    public void setStage(String stage) {
        this.stage = stage;
    }

    public List<String> getNames() {
        return names;
    }

    @DataBoundSetter
    public void setNames(List<String> names) {
        this.names = names;
    }

    public String getVariable() {
        return variable;
    }

    @DataBoundSetter
    public void setVariable(String variable) {
        this.variable = variable;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(stage, names, variable, context);
    }

    private static final class Execution extends SynchronousNonBlockingStepExecution<Stage> {
        private static final long serialVersionUID = 6190377462479580850L;
        private static final ObjectMapper objectMapper = new ObjectMapper();

        private String stage;
        private List<String> names;
        private String variable;

        Execution(String stage, List<String> names, String variable, StepContext context) {
            super(context);
            this.stage = stage;
            this.names = names;
            this.variable = variable;
        }

        @Override protected Stage run() throws Exception {
            Run<?, ?> run = getContext().get(Run.class);
            TaskListener taskListener = getContext().get(TaskListener.class);

            PrintStream l = taskListener.getLogger();
            l.println("Retrieving versions of the following artifacts at the stage '" + this.stage + "'");
            this.stage = URLEncoder.encode(this.stage, "UTF-8").replace("+", "%20");
            String param = String.join("&", this.names.stream().map(n -> {
                try {
                    n = URLEncoder.encode(n, "UTF-8");
                    l.println("  - " + n);
                    return "artifact=" + n;
                } catch (UnsupportedEncodingException e) {
                    l.println("Cannot encode '" + n + "'");
                    return "";
                }
            }).collect(Collectors.toList()));
            StringCredentials token = CredentialsProvider.findCredentialById(Configuration.get().getCredentialsId(), StringCredentials.class, run);
            Stage result = null;
            try {
                String content = RequestHelper.retrieveVersion(token.getSecret().getPlainText(), this.stage, param);
                logger.info(content);
                result = objectMapper.readValue(content, Stage.class);
                l.println("Successfully retrieved artifact versions");

            } catch (ExchangeException e) {
                logger.error("Error while retrieving artifact versions", e);
                taskListener.fatalError("Error while retrieving artifact versions: " + e.getMessage());
                run.getExecutor().interrupt(Result.FAILURE);
            }

            return result;
        }
    }

    @Extension
    public static final class DescriptorImpl extends StepDescriptor {
        @Override
        public String getFunctionName() {
            return "retrieveArtifacts";
        }

        @Override
        public String getDisplayName() {
            return "Retrieve Artifact Versions";
        }

        @Override
        public boolean isMetaStep() {
            return true;
        }

        @Override public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
        }
    }
}
