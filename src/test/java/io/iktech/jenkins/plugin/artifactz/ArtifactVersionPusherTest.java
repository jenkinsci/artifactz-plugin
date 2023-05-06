package io.iktech.jenkins.plugin.artifactz;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.exception.ClientException;
import io.iktech.jenkins.plugins.artifactz.ArtifactVersionPusher;
import io.iktech.jenkins.plugins.artifactz.Configuration;
import io.iktech.jenkins.plugins.artifactz.SingletonStore;
import io.jenkins.cli.shaded.org.apache.commons.io.FileUtils;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public class ArtifactVersionPusherTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws IOException {
        TestHelper.addCredential(j, "test");
    }

//    @Test
//    public void testConfigRoundTrip() throws Exception {
//        FreeStyleProject project = j.createFreeStyleProject();
//        project.getBuildersList().add(new ArtifactVersionPusher("test-artifact", "Development", "1.0.0"));
//        project = j.configRoundtrip(project);
//        j.assertEqualDataBoundBeans(new ArtifactVersionPusher("test-artifact", "Development", "1.0.0"), project.getBuildersList().get(0));
//    }
//
    @Test
    public void pushArtifactSuccessTest() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.doFillProxyCredentialsIdItems(Jenkins.get(), null, "proxy-test");
        configuration.setCredentialsId("test");
        configuration.setProxy("http://proxy.iktech.io:3128");
        configuration.setProxyCredentialsId("proxy-test");
        ArtifactVersionPusher step = new ArtifactVersionPusher(null, null, null, null);
        step.setName("test-artifact");
        step.setStage("Development");
        step.setVersion("1.0.0");
        step.setVariableName("VERSION");
        project.getBuildersList().add(step);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Successfully pushed artifact version"));
    }

    @Test
    public void pushArtifactSuccessFailureTest() throws Exception {
        ServiceClient serviceClient = ((TestServiceClientFactory)SingletonStore.getInstance()).getServiceClient();
        doThrow(new ClientException("Test error message")).when(serviceClient).pushArtifact(any(), any(), any());

        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");
        configuration.setProxy("http://proxy.iktech.io:3128");
        configuration.setProxyCredentialsId("proxy-test");
        ArtifactVersionPusher step = new ArtifactVersionPusher("test-artifact", "Development", "1.0.0", null);
        project.getBuildersList().add(step);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("FATAL: Error while pushing artifact version: Test error message"));
    }

    @Test
    public void descriptorDisplayNameTest() throws Exception {
        ArtifactVersionPusher publisher = new ArtifactVersionPusher("test-artifact", "Development", "1.0.0", null);
        assertEquals("Push Artifact Version to the next stage in the flow Deprecated", ((ArtifactVersionPusher.DescriptorImpl)publisher.getDescriptor()).getDisplayName());
    }

    @Test
    public void descriptorDoCheckNameTest() throws Exception {
        ArtifactVersionPusher publisher = new ArtifactVersionPusher("test-artifact", "Development", "1.0.0", null);
        assertEquals("OK", ((ArtifactVersionPusher.DescriptorImpl)publisher.getDescriptor()).doCheckName("test").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPusher.DescriptorImpl)publisher.getDescriptor()).doCheckName("").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPusher.DescriptorImpl)publisher.getDescriptor()).doCheckName(null).kind.name());
        assertEquals("Please set an artifact name", ((ArtifactVersionPusher.DescriptorImpl)publisher.getDescriptor()).doCheckName("").getMessage());
    }

    @Test
    public void descriptorDoCheckStageTest() throws Exception {
        ArtifactVersionPusher publisher = new ArtifactVersionPusher("test-artifact", "Development", "1.0.0", null);
        assertEquals("OK", ((ArtifactVersionPusher.DescriptorImpl)publisher.getDescriptor()).doCheckStage("test").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPusher.DescriptorImpl)publisher.getDescriptor()).doCheckStage("").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPusher.DescriptorImpl)publisher.getDescriptor()).doCheckStage(null).kind.name());
        assertEquals("Please set the deployment stage", ((ArtifactVersionPusher.DescriptorImpl)publisher.getDescriptor()).doCheckStage("").getMessage());
    }

    static {
        SingletonStore.test(new TestServiceClientFactory());
    }
}
