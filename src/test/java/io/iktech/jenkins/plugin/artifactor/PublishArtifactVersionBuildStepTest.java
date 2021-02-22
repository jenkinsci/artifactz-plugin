package io.iktech.jenkins.plugin.artifactor;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.util.ListBoxModel;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.ServiceClientBuilder;
import io.artifactz.client.exception.ClientException;
import io.iktech.jenkins.plugins.artifactor.Configuration;
import io.iktech.jenkins.plugins.artifactor.PublishArtifactVersionBuildStep;
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
public class PublishArtifactVersionBuildStepTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws IOException {
        TestHelper.addCredential(j, "test");
    }

    @Test
    public void publishArtifactSuccessTest() throws Exception {
        TestHelper.setupClient();
        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");
        PublishArtifactVersionBuildStep step = new PublishArtifactVersionBuildStep(null, null, null, null, null, null, null, null, null);
        step.setName("test-artifact");
        step.setStage("Development");
        step.setStageDescription("Development stage");
        step.setDescription("Test Artifact");
        step.setType("JAR");
        step.setFlow("Default");
        step.setGroupId("io.iktech.test");
        step.setArtifactId("test.artifact");
        step.setVersion("1.0.0");
        project.getBuildersList().add(step);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Successfully patched artifact version"));
    }

    @Test
    public void publishArtifactSuccessTestAllFields() throws Exception {
        TestHelper.setupClient();
        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");
        PublishArtifactVersionBuildStep publisher = new PublishArtifactVersionBuildStep("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        publisher.setFlow("Default");
        publisher.setType("WAR");
        project.getBuildersList().add(publisher);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("Successfully patched artifact version"));
    }

    @Test
    public void publishArtifactNotSuccessTest() throws Exception {
        ServiceClient serviceClient = TestHelper.setupClient();

        doThrow(new ClientException("Test error message")).when(serviceClient).publishArtifact(any(), any(), any(), any(), any(), any(), any(), any(), any());

        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");
        project.getBuildersList().add(new PublishArtifactVersionBuildStep("test-artifact", null, "JAR", "io.iktech.test", "test.artifact", "Development", null, null, "1.0.0"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("FATAL: Test error message"));
    }

    @Test
    public void descriptorDisplayNameTest() throws Exception {
        PublishArtifactVersionBuildStep publisher = new PublishArtifactVersionBuildStep("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("Send Artifact Version To Artifactor", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).getDisplayName());
    }

    @Test
    public void descriptorDoCheckNameTest() throws Exception {
        PublishArtifactVersionBuildStep publisher = new PublishArtifactVersionBuildStep("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("OK", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckName("test").kind.name());
        assertEquals("ERROR", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckName("").kind.name());
        assertEquals("ERROR", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckName(null).kind.name());
        assertEquals("Please set an artifact name", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckName("").getMessage());
    }

    @Test
    public void descriptorDoCheckStageTest() throws Exception {
        PublishArtifactVersionBuildStep publisher = new PublishArtifactVersionBuildStep("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("OK", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage("test").kind.name());
        assertEquals("ERROR", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage("").kind.name());
        assertEquals("ERROR", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage(null).kind.name());
        assertEquals("Please set the deployment stage", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckStage("").getMessage());
    }

    @Test
    public void descriptorDoCheckTypeTest() throws Exception {
        PublishArtifactVersionBuildStep publisher = new PublishArtifactVersionBuildStep("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("OK", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckType("JAR").kind.name());
        assertEquals("ERROR", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckType("").kind.name());
        assertEquals("ERROR", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckType(null).kind.name());
        assertEquals("Please select artifact type", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckType("").getMessage());
    }

    @Test
    public void descriptorDoCheckGroupIdTest() throws Exception {
        PublishArtifactVersionBuildStep publisher = new PublishArtifactVersionBuildStep("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("OK", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckGroupId("io.iktech", "JAR").kind.name());
        assertEquals("OK", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckGroupId("", "DockerImage").kind.name());
        assertEquals("ERROR", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckGroupId("", "JAR").kind.name());
        assertEquals("ERROR", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckGroupId(null, "JAR").kind.name());
        assertEquals("Group Id is mandatory for the Java Artifacts", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckGroupId("", "JAR").getMessage());
    }

    @Test
    public void descriptorDoCheckArtifactIdTest() throws Exception {
        PublishArtifactVersionBuildStep publisher = new PublishArtifactVersionBuildStep("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("OK", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckArtifactId("test", "JAR").kind.name());
        assertEquals("OK", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckArtifactId("", "DockerImage").kind.name());
        assertEquals("ERROR", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckArtifactId("", "JAR").kind.name());
        assertEquals("ERROR", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckArtifactId(null, "JAR").kind.name());
        assertEquals("Group Id is mandatory for the Java Artifacts", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckArtifactId("", "JAR").getMessage());
    }

    @Test
    public void descriptorDoCheckVersionTest() throws Exception {
        PublishArtifactVersionBuildStep publisher = new PublishArtifactVersionBuildStep("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("OK", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckVersion("1.0").kind.name());
        assertEquals("ERROR", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckVersion("").kind.name());
        assertEquals("ERROR", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckVersion(null).kind.name());
        assertEquals("Please enter the artifact version", ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doCheckVersion("").getMessage());
    }

    @Test
    public void descriptorDoFillItemsTest() throws Exception {
        PublishArtifactVersionBuildStep publisher = new PublishArtifactVersionBuildStep("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        ListBoxModel m = ((PublishArtifactVersionBuildStep.DescriptorImpl)publisher.getDescriptor()).doFillTypeItems(null);
        assertEquals(5, m.size());
        assertEquals("- none -", m.get(0).name);
        assertEquals("", m.get(0).value);
        assertEquals("JAR Archive", m.get(1).name);
        assertEquals("JAR", m.get(1).value);
        assertEquals("WAR Archive", m.get(2).name);
        assertEquals("WAR", m.get(2).value);
        assertEquals("EAR Archive", m.get(3).name);
        assertEquals("EAR", m.get(3).value);
        assertEquals("Docker Image", m.get(4).name);
        assertEquals("DockerImage", m.get(4).value);
    }
}
