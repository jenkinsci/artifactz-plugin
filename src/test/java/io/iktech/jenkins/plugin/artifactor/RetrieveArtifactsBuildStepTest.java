package io.iktech.jenkins.plugin.artifactor;

import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.ServiceClientBuilder;
import io.artifactz.client.exception.ClientException;
import io.artifactz.client.model.Stage;
import io.artifactz.client.model.Version;
import io.iktech.jenkins.plugins.artifactor.Configuration;
import io.iktech.jenkins.plugins.artifactor.Name;
import io.iktech.jenkins.plugins.artifactor.PublishArtifactVersionBuildStep;
import io.iktech.jenkins.plugins.artifactor.RetrieveArtifactsBuildStep;
import io.jenkins.cli.shaded.org.apache.commons.io.FileUtils;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceClientBuilder.class})
@PowerMockIgnore({"org.apache.http.conn.ssl.*", "javax.net.ssl.*" , "javax.crypto.*" })
public class RetrieveArtifactsBuildStepTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws IOException {
        TestHelper.addCredential(j, "test");
    }

    @Test
    public void retrieveArtifactSuccessTest() throws Exception {
        ServiceClient client = TestHelper.setupClient();

        List<Version> artifacts = new ArrayList<>();

        Version version = new Version("test-artifact", "Test Artifact", "DockerImage", null, null, "1.0.0");
        artifacts.add(version);

        Stage stage = new Stage("Development", artifacts);

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

        RetrieveArtifactsBuildStep step = new RetrieveArtifactsBuildStep(names, "Development", null);
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
        ServiceClient client = TestHelper.setupClient();

        List<Version> artifacts = new ArrayList<>();

        Version version = new Version("test-artifact", "Test Artifact", "DockerImage", null, null, "1.0.0");
        artifacts.add(version);

        Stage stage = new Stage("Development", artifacts);

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

        project.getBuildersList().add(new RetrieveArtifactsBuildStep(names, "Development", null));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Successfully retrieved artifact versions"));
        EnvVars vars = build.getEnvironment();
        assertNotNull(vars);
        assertEquals("{\"test-artifact\":\"1.0.0\"}", vars.get("ARTIFACTOR_VERSION_DATA"));
    }

    @Test
    public void retrieveArtifactEmptyDataSetTest() throws Exception {
        ServiceClient client = TestHelper.setupClient();

        Stage stage = new Stage();
        stage.setStage("Development");

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

        RetrieveArtifactsBuildStep step = new RetrieveArtifactsBuildStep(names, "Development", "ARTIFACT_VERSIONS");
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
        ServiceClient client = TestHelper.setupClient();

        Stage stage = new Stage();
        stage.setStage("Development");

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

        RetrieveArtifactsBuildStep step = new RetrieveArtifactsBuildStep(names, "Development", null);
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

        RetrieveArtifactsBuildStep publisher = new RetrieveArtifactsBuildStep(names, "Development", null);
        assertEquals("Retrieve artifact version for stage", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).getDisplayName());
    }

    @Test
    public void descriptorDoCheckNamesTest() throws Exception {
        List<Name> names = new ArrayList<>();
        Name name = new Name();
        name.setName("test-artifact");
        names.add(name);

        RetrieveArtifactsBuildStep publisher = new RetrieveArtifactsBuildStep(names, "Development", null);
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

        RetrieveArtifactsBuildStep publisher = new RetrieveArtifactsBuildStep(names, "Development", null);
        assertEquals("OK", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage("Development").kind.name());
        assertEquals("ERROR", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage("").kind.name());
        assertEquals("ERROR", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage(null).kind.name());
        assertEquals("Please set the deployment stage", ((RetrieveArtifactsBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage("").getMessage());
    }
}
