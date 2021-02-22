package io.iktech.jenkins.plugin.artifactor;

import io.artifactz.client.ServiceClientBuilder;
import io.iktech.jenkins.plugins.artifactor.Name;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServiceClientBuilder.class})
@PowerMockIgnore({"org.apache.http.conn.ssl.*", "javax.net.ssl.*" , "javax.crypto.*" })
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
