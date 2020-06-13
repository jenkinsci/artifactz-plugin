package io.iktech.jenkins.plugins.artifactor.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Stage implements Serializable {
    private static final long serialVersionUID = 5665727389410186012L;


    private String stage;

    private List<Version> artifacts;

    public Stage(@JsonProperty("stage") String stage,
                 @JsonProperty("artifacts") List<Version> artifacts) {
        this.stage = stage;
        this.artifacts = artifacts;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public List<Version> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Version> artifacts) {
        this.artifacts = artifacts;
    }
}
