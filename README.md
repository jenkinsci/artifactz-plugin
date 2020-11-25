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
flow | The Flow name | The name of the flow if any, which the above stage is associated with. Bear in mind that stage can be associated with the number of flows or it could be used without association with the flow.
version | A version | The version of the artifact

Any parameters can include variables.

For example:
```
   step([$class: 'ArtifactVersionPublisher',
                        name: 'document-manager-ui',
                        type: 'DockerImage',
                        stage: 'uat',
                        flow: 'standard',
                        version: "1.0.0.${BUILD_NUMBER}"])
```
To push artifact through the flow use the following step:
```
   step([$class: 'ArtifactVersionPusher',
                        name: '<artifact name>',
                        stage: '<stage>',
                        version: "<version>"])
```
Parameter | Description | Notes
---|---|---
name | The name of the artifact to push | e.g. artifactor-plugin
stage | The SDLC stage | The stage in the process from where the version will be pushed
version | Artifact version | The artifact version to push

For example:
```
   step([$class: 'ArtifactVersionPusher',
                        name: 'document-manager-ui',
                        stage: 'uat',
                        version: "1.0.0.${BUILD_NUMBER}"])
```

In order to use the artifact retrieval function of the plugin the following step can be used:
```
    def result = retrieveArtifacts stage: '<stage>', names: ['<artifact name>']
```
Parameter | Description | Notes
---|---|---
stage | The SDLC stage | The stage in the process where the version in question is being deployed
names | The array of the artifact names | e.g. artifactor-plugin

For example:
```
    def result = retrieveArtifacts stage: 'uat', names: ['document-manager-ui']
```

## Testing
To start test Jenkins environment run the following command `mvn hpi:run`.
Once Jenkins is up and running it can be accessed at http://localhost:8080/jenkins 