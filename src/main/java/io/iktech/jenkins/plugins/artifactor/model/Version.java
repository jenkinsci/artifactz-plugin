package io.iktech.jenkins.plugins.artifactor.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Version implements Serializable {
    private static final long serialVersionUID = -4840017712293958874L;

    private String artifactName;
    private String artifactDescription;
    private String type;
    private String groupId;
    private String artifactId;
    private String version;

    public Version(@JsonProperty("artifact_name") String artifactName,
                   @JsonProperty("artifact_description") String artifactDescription,
                   @JsonProperty("type") String type,
                   @JsonProperty("group_id") String groupId,
                   @JsonProperty("artifact_id") String artifactId,
                   @JsonProperty("version") String version) {
        this.artifactName = artifactName;
        this.artifactDescription = artifactDescription;
        this.type = type;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @JsonGetter("artifact_name")
    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    @JsonGetter("artifact_description")
    public String getArtifactDescription() {
        return artifactDescription;
    }

    public void setArtifactDescription(String artifactDescription) {
        this.artifactDescription = artifactDescription;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonGetter("group_id")
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @JsonGetter("artifact_id")
    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
