# Artifactz.io plugin for Jenkins

Jenkins plugin to submit or retrieve artifact details to and from Artifactz.io server.

The Artifactz.io helps to track versions of the artifacts such as Jar(War/Ear) files or Docker Images through various stages of the SDLC.

## Usage
In order to call the plugin method add the following to the pipeline:
```
   publishArtifact name: '<artifact name>',
                   token: '<optional API token>',
                   description: '<artifact description>',
                   type: '<artifact type>',
                   flow: '<flow name>',
                   stage: '<stage>',
                   stageDescription: '<stage description>',
                   groupId: '<java artifact group Id>',
                   artifactId: '<java artifact name, i.e. artifact Id>',
                   version: "<version>"
                    
// or                     
   step([$class: 'PublishArtifactVersionBuildStep',
                        token: '<optional API token>',
                        name: '<artifact name>',
                        description: '<artifact description>',
                        type: '<artifact type>',
                        flow: '<flow name>',
                        stage: '<stage>',
                        stageDescription: '<stage description>',
                        groupId: '<java artifact group Id>',
                        artifactId: '<java artifact name, i.e. artifact Id>',
                        version: "<version>"])
```

| Parameter        | Description                | Notes                                                                                                                                                                                                      |
|------------------|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| token            | Artifactz.io API token     | The optional token with 'urn:artifactor:write' scope                                                                                                                                                       |
| stage            | The SDLC stage             | The stage in the process where the version in question is being deployed                                                                                                                                   |
| stageDescription | The SDLC stage description | The above stage description (optional)                                                                                                                                                                     |
| name             | Artifact Name              | A unique name that identifies an artifact, e.g. artifactor-plugin                                                                                                                                          |
| description      | Artifact Description       | An artifact description (optional)                                                                                                                                                                         |
| type             | An artifact type           | Allowed values 'JAR', 'WAR', 'EAR', 'DockerImage'                                                                                                                                                          |
| flow             | The Flow name              | The name of the flow if any, which the above stage is associated with. Bear in mind that stage can be associated with the number of flows or it could be used without association with the flow (optional) |
| groupId          | Java Group Id              | The maven group Id (mandatory for Java artifacts, optional for the others)                                                                                                                                 |
| artifactId       | Java Artifact Id           | The maven artifact name (mandatory for Java artifacts, optional for the others)                                                                                                                            |
| version          | A version                  | The version of the artifact                                                                                                                                                                                |

Any parameters can include variables.

For example:
```
   step([$class: 'PublishArtifactVersionBuildStep',
                        token: "${ARTIFACTZ_TOKEN}",
                        name: 'document-manager-ui',
                        type: 'DockerImage',
                        stage: 'uat',
                        flow: 'standard',
                        version: "1.0.0.${BUILD_NUMBER}"])
// or
   publishArtifact name: 'document-manager-ui',
                    type: 'DockerImage',
                    stage: 'uat',
                    flow: 'standard',
                    version: "1.0.0.${BUILD_NUMBER}"
```
To push artifact through the flow use the following step. If successful, the step will store the pushed version in the 
specified variable.
```
   step([$class: 'PushArtifactVersionBuildStep',
                        token: '<optional API token>', 
                        name: '<artifact name>',
                        stage: '<stage>',
                        version: "<version>",
                        variableName: '<name of the variable to store pushed version>'])
// or
   def version = pushArtifact token: '<optional API token>', 
                              name: '<artifact name>',
                              stage: '<stage>',
                              version: "<version>"
```

| Parameter    | Description            | Notes                                                                                               |
|--------------|------------------------|-----------------------------------------------------------------------------------------------------|
| token        | Artifactz.io API token | The optional token with 'urn:artifactor:write' scope                                                |
| name         | Artifact name          | The name of the artifact to push, e.g. artifactor-plugin                                            |
| stage        | The SDLC stage         | The stage in the process from where the version will be pushed                                      |
| version      | Artifact version       | The artifact version to push (optional, if omitted the current version at the stage will be pushed) |
| variableName | Variable Name          | The variable name where the pushed version will be stored, default ARTIFACTZ_VERSION                |

For example:
```
   step([$class: 'PushArtifactVersionBuildStep',
                        token: "${ARTIFACTZ_TOKEN}",                   
                        name: 'document-manager-ui',
                        stage: 'uat',
                        version: "1.0.0.${BUILD_NUMBER}"])
// or
   def version = pushVersion name: 'document-manager-ui',
                            stage: 'uat',
                            version: "1.0.0.${BUILD_NUMBER}"               
```

In order to use the artifact retrieval function of the plugin the following step can be used:
```
    def result = retrieveArtifacts token: '<optional API token>', stage: '<stage>', names: ['<artifact name>']
```
| Parameter | Description                     | Notes                                                                    |
|-----------|---------------------------------|--------------------------------------------------------------------------|
| token     | Artifactz.io API token          | The optional token with 'urn:artifactor:read' scope                      |
| stage     | The SDLC stage                  | The stage in the process where the version in question is being deployed |
| names     | The array of the artifact names | e.g. artifactor-plugin                                                   |

For example:
```
    def result = retrieveArtifacts token: "${ARTIFACTZ_TOKEN}", stage: 'uat', names: ['document-manager-ui']
```

## Testing
To start test Jenkins environment run the following command `mvn hpi:run`.
Once Jenkins is up and running it can be accessed at http://localhost:8080/jenkins 