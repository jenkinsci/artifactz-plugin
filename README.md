# Artifactor plugin for Jenkins

Jenkins plugin to submit or retrieve artifact details to and from Artifactor server.

The Artifactor helps to track versions of the artifacts such as Jar(War/Ear) files or Docker Images between various stages of the SDLC.

## Usage
In order to call the plugin method add the following to the pipeline:
```
   step([$class: 'ArtifactVersionPublisher',
                        name: '<artifact name>',
                        type: '<artifact type>',
                        stage: '<stage>',
                        version: "<version>"])
```
Parameter | Description | Notes
---|---|---
name | Artifact Name, a unique name that identifies an artifact | e.g. artifactor-plugin
type | An artifact type | Allowed values 'JAR', 'WAR', 'EAR', 'DockerImage'
stage | The SDLC stage | The stage in the process where the version in question is being deployed
version | A version | The version of the artifact

Any parameters can include variables.

For example:
```
   step([$class: 'ArtifactVersionPublisher',
                        name: 'document-manager-ui',
                        type: 'DockerImage',
                        stage: 'uat',
                        version: "1.0.0.${BUILD_NUMBER}"])
```

```
    def result = retrieveArtifacts stage: 'uat', names: ['document-manager-ui']
```