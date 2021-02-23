package io.iktech.jenkins.plugins.artifactor;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Extension;
import hudson.Util;
import hudson.model.ItemGroup;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.artifactz.client.ServiceClient;
import io.artifactz.client.exception.ClientException;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.CheckForNull;
import java.util.Collections;

@Extension
public class Configuration extends GlobalConfiguration {

    /** @return the singleton instance */
    public static io.iktech.jenkins.plugins.artifactor.Configuration get() {
        return GlobalConfiguration.all().get(io.iktech.jenkins.plugins.artifactor.Configuration.class);
    }

    @CheckForNull
    private String serverUrl;

    @CheckForNull
    private String credentialsId;

    private String sender;

    private String proxy;

    private String proxyCredentialsId;

    public Configuration() {
        load();
    }

    @CheckForNull
    public String getServerUrl() {
        return this.serverUrl;
    }

    @DataBoundSetter
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        save();
    }

    public String getSender() {
        return this.sender;
    }

    @DataBoundSetter
    public void setSender(String sender) {
        this.sender = sender;
        save();
    }

    @CheckForNull
    public String getCredentialsId() {
        return this.credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = Util.fixEmpty(credentialsId);
        save();
    }

    public String getProxy() {
        return this.proxy;
    }

    @DataBoundSetter
    public void setProxy(String proxy) {
        this.proxy = proxy;
        save();
    }

    public String getProxyCredentialsId() {
        return this.proxyCredentialsId;
    }

    @DataBoundSetter
    public void setProxyCredentialsId(String proxyCredentialsId) {
        this.proxyCredentialsId = proxyCredentialsId;
        save();
    }

    public FormValidation doCheckCredentialsId(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please select Artifactor Service credentials.");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckServerUrl(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify an Artifactor Service URL.");
        }
        return FormValidation.ok();
    }

    @RequirePOST
    @SuppressWarnings("unused") // used by jelly
    public FormValidation doTestConnection(@QueryParameter String serverUrl,
                                           @QueryParameter String credentialsId,
                                           @QueryParameter String proxy,
                                           @QueryParameter String proxyCredentialsId) throws Exception {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        if (StringUtils.isBlank(serverUrl)) {
            return FormValidation.error("name is required");
        }

        StringCredentials credentials = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        StringCredentials.class,
                        Jenkins.get(),
                        ACL.SYSTEM,
                        Collections.emptyList()
                ), CredentialsMatchers.withId(credentialsId));
        if (credentials != null && credentials.getSecret() != null) {
            try {
                ServiceClient client = ServiceHelper.getClient(credentials.getSecret().getPlainText());
                client.validateConnection();
                return FormValidation.ok("Connection test successful");
            } catch (ClientException e) {
                return FormValidation.error("Connection failed : " + e.getMessage());
            }
        } else {
            return FormValidation.error("Cannot validate connection without proper credentials");
        }
    }

    @RequirePOST
    @SuppressWarnings("unused") // used by jelly
    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath ItemGroup context, @QueryParameter String serverUrl, String credentialId) {
        if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
            return new StandardListBoxModel().includeCurrentValue(credentialId);
        }

        StandardListBoxModel result = new StandardListBoxModel();
        result.includeEmptyValue();
        result.includeMatchingAs(
                ACL.SYSTEM,
                context,
                StandardCredentials.class,
                serverUrl != null ? URIRequirementBuilder.fromUri(serverUrl).build()
                        : Collections.EMPTY_LIST,
                new ArtifactorCredentialsMatcher());

        return result;
    }

    @RequirePOST
    @SuppressWarnings("unused") // used by jelly
    public ListBoxModel doFillProxyCredentialsIdItems(@AncestorInPath ItemGroup context, @QueryParameter String serverUrl, String credentialId) {
        if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
            return new StandardListBoxModel().includeCurrentValue(credentialId);
        }

        StandardListBoxModel result = new StandardListBoxModel();
        result.includeEmptyValue();
        result.includeMatchingAs(
                ACL.SYSTEM,
                context,
                StandardCredentials.class,
                serverUrl != null ? URIRequirementBuilder.fromUri(serverUrl).build()
                        : Collections.EMPTY_LIST,
                new ProxyCredentialsMatcher());

        return result;
    }
}
