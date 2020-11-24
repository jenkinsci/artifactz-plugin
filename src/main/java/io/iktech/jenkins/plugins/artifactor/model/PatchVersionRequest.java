package io.iktech.jenkins.plugins.artifactor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatchVersionRequest implements Serializable {
    private static final long serialVersionUID = 2207419267657211214L;

    @JsonProperty("stage")
    private String stage;
    @JsonProperty("flow")
    private String flow;
    @JsonProperty("stage_description")
    private String stageDescription;
    @JsonProperty("artifact_name")
    private String name;
    @JsonProperty("artifact_description")
    private String description;
    @JsonProperty("type")
    private String type;
    @JsonProperty("group_id")
    private String groupId;
    @JsonProperty("artifact_id")
    private String artifactId;
    @JsonProperty("version")
    private String version;

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public String getStageDescription() {
        return stageDescription;
    }

    public void setStageDescription(String stageDescription) {
        this.stageDescription = stageDescription;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

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
