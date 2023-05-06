package io.iktech.jenkins.plugin.artifactz;

import io.iktech.jenkins.plugins.artifactz.Name;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;

public class NameTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void descriptorDoCheckVersionTest() throws Exception {
        Name publisher = new Name();
        assertEquals("OK", ((Name.DescriptorImpl)publisher.getDescriptor()).doCheckName("test").kind.name());
        assertEquals("ERROR", ((Name.DescriptorImpl)publisher.getDescriptor()).doCheckName("").kind.name());
        assertEquals("ERROR", ((Name.DescriptorImpl)publisher.getDescriptor()).doCheckName(null).kind.name());
        assertEquals("Please set at least one artifact to retrieve", ((Name.DescriptorImpl)publisher.getDescriptor()).doCheckName("").getMessage());
    }
}
