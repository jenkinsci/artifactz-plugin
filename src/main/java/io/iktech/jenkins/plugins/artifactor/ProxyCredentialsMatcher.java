package io.iktech.jenkins.plugins.artifactor;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;

public class ProxyCredentialsMatcher implements CredentialsMatcher {

    @Override
    public boolean matches(@NonNull Credentials credentials) {
        try {
            return credentials instanceof StandardUsernamePasswordCredentials;
        } catch (Throwable e) {
            return false;
        }    }
}
