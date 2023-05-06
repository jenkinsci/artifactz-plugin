package io.iktech.jenkins.plugin.artifactz;

import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.exception.ClientException;
import io.artifactz.client.model.Stage;
import io.artifactz.client.model.Version;
import io.iktech.jenkins.plugins.artifactz.Configuration;
import io.iktech.jenkins.plugins.artifactz.Name;
import io.iktech.jenkins.plugins.artifactz.RetrieveArtifactsBuildStep;
import io.iktech.jenkins.plugins.artifactz.SingletonStore;
import io.jenkins.cli.shaded.org.apache.commons.io.FileUtils;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class RetrieveArtifactsBuildStepTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws IOException {
        TestHelper.addCredential(j, "test");
    }

    @Test
    public void retrieveArtifactSuccessTest() throws Exception {
        List<Version> artifacts = new ArrayList<>();

        Version version = new Version("test-artifact", "Test Artifact", "DockerImage", null, null, "1.0.0");
        artifacts.add(version);

        Stage stage = new Stage("Development", artifacts);

        ServiceClient client = ((TestServiceClientFactory)SingletonStore.getInstance()).getServiceClient();
        when(client.retrieveVersions(eq("Development"), eq("test-artifact"))).thenReturn(stage);

        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");
        List<Name> names = new ArrayList<>();
        Name name = new Name();
        name.setName("test-artifact");
        names.add(name);

        RetrieveArtifactsBuildStep step = new RetrieveArtifactsBuildStep(null, names, "Development", null);
        step.setVariableName("ARTIFACT_VERSIONS");

        project.getBuildersList().add(step);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Successfully retrieved artifact versions"));
        EnvVars vars = build.getEnvironment();
        assertNotNull(vars);
        assertEquals("{\"test-artifact\":\"1.0.0\"}", vars.get("ARTIFACT_VERSIONS"));
    }

    @Test
    public void retrieveArtifactWithTokenSuccessTest() throws Exception {
        List<Version> artifacts = new ArrayList<>();

        Version version = new Version("test-artifact", "Test Artifact", "DockerImage", null, null, "1.0.0");
        artifacts.add(version);

        Stage stage = new Stage("Development", artifacts);

        ServiceClient client = ((TestServiceClientFactory)SingletonStore.getInstance()).getServiceClient();
        when(client.retrieveVersions(eq("Development"), eq("test-artifact"))).thenReturn(stage);

        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");
        List<Name> names = new ArrayList<>();
        Name name = new Name();
        name.setName("test-artifact");
        names.add(name);

        RetrieveArtifactsBuildStep step = new RetrieveArtifactsBuildStep(null, names, "Development", null);
        step.setToken("508c8d0d-79c4-4bec-be70-8e47de3a2be5");
        step.setVariableName("ARTIFACT_VERSIONS");

        project.getBuildersList().add(step);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Successfully retrieved artifact versions"));
        EnvVars vars = build.getEnvironment();
        assertNotNull(vars);
        assertEquals("{\"test-artifact\":\"1.0.0\"}", vars.get("ARTIFACT_VERSIONS"));
    }

    @Test
    public void retrieveArtifactSuccessDefaultVariableTest() throws Exception {
        List<Version> artifacts = new ArrayList<>();

        Version version = new Version("test-artifact", "Test Artifact", "DockerImage", null, null, "1.0.0");
        artifacts.add(version);

        Stage stage = new Stage("Development", artifacts);

        ServiceClient client = ((TestServiceClientFactory)SingletonStore.getInstance()).getServiceClient();
        when(client.retrieveVersions(eq("Development"), eq("test-artifact"))).thenReturn(stage);

        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");
        List<Name> names = new ArrayList<>();
        Name name = new Name();
        name.setName("test-artifact");
        names.add(name);


        RetrieveArtifactsBuildStep step = new RetrieveArtifactsBuildStep("test", names, "Development", null);
        project.getBuildersList().add(step);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Successfully retrieved artifact versions"));
        EnvVars vars = build.getEnvironment();
        assertNotNull(vars);
        assertEquals("{\"test-artifact\":\"1.0.0\"}", vars.get("ARTIFACTZ_VERSION_DATA"));
    }

    @Test
    public void retrieveArtifactEmptyDataSetTest() throws Exception {
        Stage stage = new Stage();
        stage.setStage("Development");

        ServiceClient client = ((TestServiceClientFactory)SingletonStore.getInstance()).getServiceClient();
        when(client.retrieveVersions(eq("Development"), eq("test-artifact"))).thenReturn(stage);

        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");
        List<Name> names = new ArrayList<>();
        Name name = new Name();
        name.setName("test-artifact");
        names.add(name);

        RetrieveArtifactsBuildStep step = new RetrieveArtifactsBuildStep(null, names, "Development", "ARTIFACT_VERSIONS");
        step.setNames(names);
        step.setStage("Development");
        step.setVariableName("ARTIFACT_VERSIONS");

        project.getBuildersList().add(step);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("No artifacts data in the response"));
    }

    @Test
    public void retrieveArtifactFailureTest() throws Exception {

        Stage stage = new Stage();
        stage.setStage("Development");

        ServiceClient client = ((TestServiceClientFactory)SingletonStore.getInstance()).getServiceClient();
        when(client.retrieveVersions(eq("Development"), eq("test-artifact"))).thenThrow(new ClientException("test exception"));

        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");
        List<Name> names = new ArrayList<>();
        Name name = new Name();
        name.setName("test-artifact");
        names.add(name);

        RetrieveArtifactsBuildStep step = new RetrieveArtifactsBuildStep(null, names, "Development", null);
        step.setNames(names);
        step.setStage("Development");

        project.getBuildersList().add(step);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Error while retrieving artifact versions: test exception"));
    }

    @Test
    public void descriptorDisplayNameTest() throws Exception {
        List<Name> names = new ArrayList<>();
        Name name = new Name();
        name.setName("test-artifact");
        names.add(name);

        RetrieveArtifactsBuildStep publisher = new RetrieveArtifactsBuildStep(null, names, "Development", null);
        assertEquals("Retrieve Artifact version for stage", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).getDisplayName());
    }

    @Test
    public void descriptorDoCheckNamesTest() throws Exception {
        List<Name> names = new ArrayList<>();
        Name name = new Name();
        name.setName("test-artifact");
        names.add(name);

        RetrieveArtifactsBuildStep publisher = new RetrieveArtifactsBuildStep(null, names, "Development", null);
        assertEquals("OK", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckNames(names).kind.name());
        assertEquals("ERROR", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckNames(new ArrayList<>()).kind.name());
        assertEquals("ERROR", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckNames(null).kind.name());
        assertEquals("Please set at least one artifact to retrieve", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckNames(new ArrayList<>()).getMessage());
    }

    @Test
    public void descriptorDoCheckStageTest() throws Exception {
        List<Name> names = new ArrayList<>();
        Name name = new Name();
        name.setName("test-artifact");
        names.add(name);

        RetrieveArtifactsBuildStep publisher = new RetrieveArtifactsBuildStep(null, names, "Development", null);
        assertEquals("OK", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage("Development").kind.name());
        assertEquals("ERROR", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage("").kind.name());
        assertEquals("ERROR", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage(null).kind.name());
        assertEquals("Please set the deployment stage", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage("").getMessage());
    }

    static {
        SingletonStore.test(new TestServiceClientFactory());
    }
}
