package io.iktech.jenkins.plugin.artifactor;

import io.iktech.jenkins.plugins.artifactor.InjectVariable;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class InjectVariableTest {
    @Test
    public void testInjectVariableProperties() {
        InjectVariable test = new InjectVariable("key", "value");
        assertNull(test.getIconFileName());
        assertNull(test.getDisplayName());
        assertNull(test.getUrlName());
    }
}
