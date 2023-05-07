package io.iktech.jenkins.plugin.artifactz;

import com.google.inject.Guice;
import com.google.inject.Injector;
import groovy.lang.Singleton;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.exception.ClientException;
import io.artifactz.client.model.Stage;
import io.artifactz.client.model.Version;
import io.iktech.jenkins.plugins.artifactz.Configuration;
import io.iktech.jenkins.plugins.artifactz.PushArtifactStep;
import io.iktech.jenkins.plugins.artifactz.SingletonStore;
import io.iktech.jenkins.plugins.artifactz.client.ServiceClientFactory;
import io.iktech.jenkins.plugins.artifactz.client.impl.ServiceClientFactoryImpl;
import io.jenkins.cli.shaded.org.apache.commons.io.FileUtils;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class PushArtifactStepTest {
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
        PushArtifactStep test = new PushArtifactStep(null, null, null, null, null);
        test.setToken("54156708-00ea-418d-b125-b5c3bf1a0e0a");
        test.setStage("Development");
        test.setName("test-artifact");
        test.setVersion("1.0.0");
        assertEquals("54156708-00ea-418d-b125-b5c3bf1a0e0a", test.getToken());
        assertEquals("Development", test.getStage());
        assertEquals("test-artifact", test.getName());
        assertEquals("1.0.0", test.getVersion());
    }

    @Test
    public void pushArtifactSuccessTest() throws Exception {
        List<Version> artifacts = new ArrayList<>();

        WorkflowJob project = j.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("" +
                "node {" +
                "  def version = pushArtifact stage: 'Development', name: 'test-artifact'\n" +
                "  echo \"Version: ${version}\"\n" +
                "}", true));

        ServiceClient client = ((TestServiceClientFactory)SingletonStore.getInstance()).getServiceClient();
        when(client.pushArtifact(eq("Development"), eq("test-artifact"), any())).thenReturn("1.0.0");

        WorkflowRun build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Finished: SUCCESS"));
    }

    @Test
    public void pushArtifactWithTokenSuccessTest() throws Exception {
        List<Version> artifacts = new ArrayList<>();

        ServiceClient client = ((TestServiceClientFactory)SingletonStore.getInstance()).getServiceClient();
        when(client.pushArtifact(eq("Development"), eq("test-artifact"), any())).thenReturn("1.0.0");

        WorkflowJob project = j.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("" +
                "node {" +
                "  def version = pushArtifact token: '9c9696fa-129c-49c3-93a8-2db0e6657f5e', stage: 'Development', name: 'test-artifact'\n" +
                "  echo \"Version: ${version}\"\n" +
                "}", true));

        WorkflowRun build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Finished: SUCCESS"));
    }
    @Test
    public void retrieveArtifactFailureTest() throws Exception {
        Stage stage = new Stage();
        stage.setStage("Development");

        ServiceClient client = ((TestServiceClientFactory)SingletonStore.getInstance()).getServiceClient();
        when(client.pushArtifact(eq("Development"), eq("test-artifact"), any())).thenThrow(new ClientException("test exception"));

        WorkflowJob project = j.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("" +
                "node {" +
                "  def version = pushArtifact stage: 'Development', name: 'test-artifact'\n" +
                "  echo \"Version: ${version}\"\n" +
                "}", true));

        WorkflowRun build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Error while pushing artifact version: test exception"));
    }

    static {
        SingletonStore.test(new TestServiceClientFactory());
    }
}
