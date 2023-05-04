package io.iktech.jenkins.plugin.artifactz;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.ServiceClientBuilder;
import io.artifactz.client.exception.ClientException;
import io.iktech.jenkins.plugins.artifactz.PushArtifactVersionBuildStep;
import io.iktech.jenkins.plugins.artifactz.Configuration;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceClientBuilder.class})
@PowerMockIgnore({"org.apache.http.conn.ssl.*", "javax.net.ssl.*" , "javax.crypto.*" })
public class PushArtifactVersionBuildStepTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws IOException {
        TestHelper.addCredential(j, "test");
    }

//    @Test
//    public void testConfigRoundTrip() throws Exception {
//        FreeStyleProject project = j.createFreeStyleProject();
//        project.getBuildersList().add(new PushArtifactVersionBuildStep("test-artifact", "Development", "1.0.0"));
//        project = j.configRoundtrip(project);
//        j.assertEqualDataBoundBeans(new PushArtifactVersionBuildStep("test-artifact", "Development", "1.0.0"), project.getBuildersList().get(0));
//    }
//
    @Test
    public void pushArtifactSuccessTest() throws Exception {
        TestHelper.setupClient();
        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.doFillProxyCredentialsIdItems(Jenkins.get(), null, "proxy-test");
        configuration.setCredentialsId("test");
        configuration.setProxy("http://proxy.iktech.io:3128");
        configuration.setProxyCredentialsId("proxy-test");
        PushArtifactVersionBuildStep step = new PushArtifactVersionBuildStep(null, null, null, null, null);
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
    public void pushArtifactWithTokenSuccessTest() throws Exception {
        TestHelper.setupClient();
        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.doFillProxyCredentialsIdItems(Jenkins.get(), null, "proxy-test");
        configuration.setCredentialsId("test");
        configuration.setProxy("http://proxy.iktech.io:3128");
        configuration.setProxyCredentialsId("proxy-test");
        PushArtifactVersionBuildStep step = new PushArtifactVersionBuildStep(null, null, null, null, null);
        step.setToken("191c06b9-8c01-47e9-996c-5f1c58851ca3");
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
        ServiceClient serviceClient = TestHelper.setupClient();

        doThrow(new ClientException("Test error message")).when(serviceClient).pushArtifact(any(), any(), any());

        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");
        configuration.setProxy("http://proxy.iktech.io:3128");
        configuration.setProxyCredentialsId("proxy-test");
        project.getBuildersList().add(new PushArtifactVersionBuildStep(null, "test-artifact", "Development", "1.0.0", null));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("FATAL: Error while pushing artifact version: Test error message"));
    }

    @Test
    public void descriptorDisplayNameTest() throws Exception {
        PushArtifactVersionBuildStep publisher = new PushArtifactVersionBuildStep("test", "test-artifact", "Development", "1.0.0", null);
        assertEquals("Push Artifact Version to the next stage in the flow", ((PushArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).getDisplayName());
    }

    @Test
    public void descriptorDoCheckNameTest() throws Exception {
        PushArtifactVersionBuildStep publisher = new PushArtifactVersionBuildStep(null, "test-artifact", "Development", "1.0.0", null);
        assertEquals("OK", ((PushArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckName("test").kind.name());
        assertEquals("ERROR", ((PushArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckName("").kind.name());
        assertEquals("ERROR", ((PushArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckName(null).kind.name());
        assertEquals("Please set an artifact name", ((PushArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckName("").getMessage());
    }

    @Test
    public void descriptorDoCheckStageTest() throws Exception {
        PushArtifactVersionBuildStep publisher = new PushArtifactVersionBuildStep(null, "test-artifact", "Development", "1.0.0", null);
        assertEquals("OK", ((PushArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage("test").kind.name());
        assertEquals("ERROR", ((PushArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage("").kind.name());
        assertEquals("ERROR", ((PushArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage(null).kind.name());
        assertEquals("Please set the deployment stage", ((PushArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage("").getMessage());
    }
}
