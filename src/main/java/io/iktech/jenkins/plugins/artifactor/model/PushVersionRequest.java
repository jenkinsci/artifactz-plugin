package io.iktech.jenkins.plugins.artifactor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushVersionRequest implements Serializable {
    private static final long serialVersionUID = 1719249389867475917L;
    @JsonProperty("stage_name")
    private String stage;
    @JsonProperty("artifact_name")
    private String name;
    @JsonProperty("version")
    private String version;

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
