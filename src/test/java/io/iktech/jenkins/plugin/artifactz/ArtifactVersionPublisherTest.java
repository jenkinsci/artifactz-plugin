package io.iktech.jenkins.plugin.artifactz;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.util.ListBoxModel;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.exception.ClientException;
import io.iktech.jenkins.plugins.artifactz.ArtifactVersionPublisher;
import io.iktech.jenkins.plugins.artifactz.Configuration;
import io.iktech.jenkins.plugins.artifactz.SingletonStore;
import io.jenkins.cli.shaded.org.apache.commons.io.FileUtils;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactVersionPublisherTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() throws IOException {
        TestHelper.addCredential(j, "test");
    }

    @Test
    public void publishArtifactSuccessTest() throws Exception {
        // Reset mock factory
        ((TestServiceClientFactory)SingletonStore.getInstance()).getServiceClient();

        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");

        ArtifactVersionPublisher step = new ArtifactVersionPublisher(null, null, null, null, null, null, null, null, null);
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
        // Reset mock factory
        ((TestServiceClientFactory)SingletonStore.getInstance()).getServiceClient();

        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");
        ArtifactVersionPublisher publisher = new ArtifactVersionPublisher("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
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
        ServiceClient serviceClient = ((TestServiceClientFactory)SingletonStore.getInstance()).getServiceClient();
        doThrow(new ClientException("Test error message")).when(serviceClient).publishArtifact(any(), any(), any(), any(), any(), any(), any(), any(), any());

        FreeStyleProject project = j.createFreeStyleProject();
        Configuration configuration = Configuration.get();
        configuration.setServerUrl("http://localhost:5002");
        configuration.doFillCredentialsIdItems(Jenkins.get(), null, "test");
        configuration.setCredentialsId("test");
        ArtifactVersionPublisher step = new ArtifactVersionPublisher("test-artifact", null, "JAR", "io.iktech.test", "test.artifact", "Development", null, null, "1.0.0");
        project.getBuildersList().add(step);
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName() + " completed");
        // TODO: change this to use HtmlUnit
        String s = FileUtils.readFileToString(build.getLogFile());
        assertThat(s, containsString("FATAL: Error while publishing artifact version: Test error message"));
    }

    @Test
    public void descriptorDisplayNameTest() throws Exception {
        ArtifactVersionPublisher publisher = new ArtifactVersionPublisher("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("Send Artifact Version To Artifactor Web Service Deprecated", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).getDisplayName());
    }

    @Test
    public void descriptorDoCheckNameTest() throws Exception {
        ArtifactVersionPublisher publisher = new ArtifactVersionPublisher("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("OK", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckName("test").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckName("").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckName(null).kind.name());
        assertEquals("Please set an artifact name", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckName("").getMessage());
    }

    @Test
    public void descriptorDoCheckStageTest() throws Exception {
        ArtifactVersionPublisher publisher = new ArtifactVersionPublisher("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("OK", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckStage("test").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckStage("").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckStage(null).kind.name());
        assertEquals("Please set the deployment stage", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckStage("").getMessage());
    }

    @Test
    public void descriptorDoCheckTypeTest() throws Exception {
        ArtifactVersionPublisher publisher = new ArtifactVersionPublisher("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("OK", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckType("JAR").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckType("").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckType(null).kind.name());
        assertEquals("Please select artifact type", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckType("").getMessage());
    }

    @Test
    public void descriptorDoCheckGroupIdTest() throws Exception {
        ArtifactVersionPublisher publisher = new ArtifactVersionPublisher("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("OK", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckGroupId("io.iktech", "JAR").kind.name());
        assertEquals("OK", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckGroupId("", "DockerImage").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckGroupId("", "JAR").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckGroupId(null, "JAR").kind.name());
        assertEquals("Group Id is mandatory for the Java Artifacts", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckGroupId("", "JAR").getMessage());
    }

    @Test
    public void descriptorDoCheckArtifactIdTest() throws Exception {
        ArtifactVersionPublisher publisher = new ArtifactVersionPublisher("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("OK", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckArtifactId("test", "JAR").kind.name());
        assertEquals("OK", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckArtifactId("", "DockerImage").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckArtifactId("", "JAR").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckArtifactId(null, "JAR").kind.name());
        assertEquals("Group Id is mandatory for the Java Artifacts", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckArtifactId("", "JAR").getMessage());
    }

    @Test
    public void descriptorDoCheckVersionTest() throws Exception {
        ArtifactVersionPublisher publisher = new ArtifactVersionPublisher("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        assertEquals("OK", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckVersion("1.0").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckVersion("").kind.name());
        assertEquals("ERROR", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckVersion(null).kind.name());
        assertEquals("Please enter the artifact version", ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doCheckVersion("").getMessage());
    }

    @Test
    public void descriptorDoFillItemsTest() throws Exception {
        ArtifactVersionPublisher publisher = new ArtifactVersionPublisher("test-artifact", "Test Artifact", "JAR", "io.iktech.test", "test.artifact", "Development", "Defalt", "Development Stage", "1.0.0");
        ListBoxModel m = ((ArtifactVersionPublisher.DescriptorImpl)publisher.getDescriptor()).doFillTypeItems(null);
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

    static {
        SingletonStore.test(new TestServiceClientFactory());
    }
}
