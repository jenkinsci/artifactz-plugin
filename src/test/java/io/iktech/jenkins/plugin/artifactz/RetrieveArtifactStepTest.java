package io.iktech.jenkins.plugin.artifactz;

import io.artifactz.client.ServiceClient;
import io.artifactz.client.ServiceClientBuilder;
import io.artifactz.client.exception.ClientException;
import io.artifactz.client.model.Stage;
import io.artifactz.client.model.Version;
import io.iktech.jenkins.plugins.artifactz.Configuration;
import io.iktech.jenkins.plugins.artifactz.RetrieveArtifactsStep;
import io.jenkins.cli.shaded.org.apache.commons.io.FileUtils;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
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
public class RetrieveArtifactStepTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws IOException {
        TestHelper.addCredential(j, "test");
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");
    }

    @Test
    public void gettersAndSettersTest() throws Exception {
        RetrieveArtifactsStep test = new RetrieveArtifactsStep(null, null, null);
        test.setToken("25b311dd-4fde-4fd0-9aa7-8508cf59a969");
        test.setStage("Development");
        List<String> names = new ArrayList<>();
        names.add("test-artifact");
        test.setNames(names);
        assertEquals("25b311dd-4fde-4fd0-9aa7-8508cf59a969", test.getToken());
        assertEquals("Development", test.getStage());
        assertNotNull(test.getNames());
        assertEquals(1, test.getNames().size());
        assertEquals("test-artifact", test.getNames().get(0));
    }

    @Test
    public void retrieveArtifactSuccessTest() throws Exception {
        ServiceClient client = TestHelper.setupClient();

        List<Version> artifacts = new ArrayList<>();

        Version version = new Version("test-artifact", "Test Artifact", "DockerImage", null, null, "1.0.0");
        artifacts.add(version);

        Stage stage = new Stage("Development", artifacts);

        when(client.retrieveVersions(eq("Development"), eq("test-artifact"))).thenReturn(stage);

        WorkflowJob project = j.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("" +
                "node {" +
                "  def result = retrieveArtifacts stage: 'Development', names: ['test-artifact']\n" +
                "  def version = result['test-artifact']\n" +
                "  echo \"Version: ${version}\"\n" +
                "}", true));

        WorkflowRun build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Successfully retrieved artifact versions"));
        assertThat(s, containsString("Version: 1.0.0"));
    }

    @Test
    public void retrieveArtifactWithTokenSuccessTest() throws Exception {
        ServiceClient client = TestHelper.setupClient();

        List<Version> artifacts = new ArrayList<>();

        Version version = new Version("test-artifact", "Test Artifact", "DockerImage", null, null, "1.0.0");
        artifacts.add(version);

        Stage stage = new Stage("Development", artifacts);

        when(client.retrieveVersions(eq("Development"), eq("test-artifact"))).thenReturn(stage);

        WorkflowJob project = j.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("" +
                "node {" +
                "  def result = retrieveArtifacts token: '2801c4ac-8a9f-4692-ad17-b820b56e7e3b', stage: 'Development', names: ['test-artifact']\n" +
                "  def version = result['test-artifact']\n" +
                "  echo \"Version: ${version}\"\n" +
                "}", true));

        WorkflowRun build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Successfully retrieved artifact versions"));
        assertThat(s, containsString("Version: 1.0.0"));
    }

    @Test
    public void retrieveArtifactSuccessEmptyDataSetTest() throws Exception {
        ServiceClient client = TestHelper.setupClient();

        Stage stage = new Stage();
        stage.setStage("Development");

        when(client.retrieveVersions(eq("Development"), eq("test-artifact"))).thenReturn(stage);

        WorkflowJob project = j.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("" +
                "node {" +
                "  def result = retrieveArtifacts stage: 'Development', names: ['test-artifact']\n" +
                "  def version = result['test-artifact']\n" +
                "  echo \"Version: ${version}\"\n" +
                "}", true));

        WorkflowRun build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("No artifacts data in the response"));
    }

    @Test
    public void retrieveArtifactFailureTest() throws Exception {
        ServiceClient client = TestHelper.setupClient();

        Stage stage = new Stage();
        stage.setStage("Development");

        when(client.retrieveVersions(eq("Development"), eq("test-artifact"))).thenThrow(new ClientException("test exception"));

        WorkflowJob project = j.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("" +
                "node {" +
                "  def result = retrieveArtifacts stage: 'Development', names: ['test-artifact']\n" +
                "  def version = result['test-artifact']\n" +
                "  echo \"Version: ${version}\"\n" +
                "}", true));

        WorkflowRun build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Error while retrieving artifact versions: test exception"));
    }
}
