package io.iktech.jenkins.plugins.artifactor;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

public class ArtifactorCredentialsMatcher implements CredentialsMatcher {
    private static final long serialVersionUID = 8486859513374906795L;

    @Override
    public boolean matches(@NonNull Credentials credentials) {
        try {
            return credentials instanceof StringCredentials;
        } catch (Throwable e) {
            return false;
        }    }
}
