version = "1.1.0"
projectName = "artifactor-plugin"

podTemplate(label: 'jpod', cloud: 'kubernetes', serviceAccount: 'jenkins',
    containers: [
    containerTemplate(name: 'java', image: 'maven:3.6.3-openjdk-8', resourceRequestMemory: '2048Mi', resourceLimitMemory: '2048Mi', resourceRequestCpu: '1000m', resourceLimitCpu: '1200m', ttyEnabled: true, command: 'cat'),
    containerTemplate(name: 'sonarqube', image: 'iktech/sonarqube-scanner', ttyEnabled: true, command: 'cat'),
    ],
    volumes: [
       secretVolume(mountPath: '/etc/.ssh', secretName: 'ssh-home'),
    ]) {
    node('jpod') {
        stage('Prepare') {
            checkout scm
            // Set up private key to access BitBucket
            sh "cat /etc/.ssh/id_rsa > ~/.ssh/id_rsa"
            sh "chmod 400 ~/.ssh/id_rsa"
        }

        stage('Build Java Code') {
            container('java') {
                try {
                    sh "mvn versions:set -DnewVersion=${version}.${BUILD_NUMBER}"
                    sh 'mvn compile test jacoco:report hpi:hpi'
                    step([$class: 'ArtifactArchiver', artifacts: 'target/*.hpi', fingerprint: true])
                } catch (error) {
                    currentBuild.result = 'FAILURE'
                    step([$class: 'Mailer',
                        notifyEveryUnstableBuild: true,
                        recipients: emailextrecipients([[$class: 'CulpritsRecipientProvider'],
                                                        [$class: 'DevelopersRecipientProvider']]),
                        sendToIndividuals: true])
                    throw error
                } finally {
                   step([$class: 'JUnitResultArchiver', testResults: 'target/surefire-reports/**/*.xml'])
                   step([$class: 'JacocoPublisher'])
                }
            }
        }

        container('sonarqube') {
            lock(resource: "${projectName}-sonarqube") {
                stage('SonarQube Analysis') {
                    try {
                        def scannerHome = tool 'sonarqube-scanner';
                        withSonarQubeEnv('Sonarqube') {
                            sh "${scannerHome}/bin/sonar-scanner"
                        }
                    } catch (error) {
                        currentBuild.result = 'FAILURE'
                        step([$class: 'Mailer',
                            notifyEveryUnstableBuild: true,
                            recipients: emailextrecipients([[$class: 'CulpritsRecipientProvider'],
                                                            [$class: 'RequesterRecipientProvider']]),
                            sendToIndividuals: true])
                        throw error
                    }
                }
            }
        }

        stage("Quality Gate") {
	          milestone(1)
	          lock(resource: "${projectName}-sonarqube") {
                  timeout(time: 1, unit: 'HOURS') { // Just in case something goes wrong, pipeline will be killed after a timeout
                    def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
                    if (qg.status != 'OK') {
                        step([$class: 'Mailer',
                            notifyEveryUnstableBuild: true,
                            recipients: emailextrecipients([[$class: 'CulpritsRecipientProvider'],
                                                            [$class: 'RequesterRecipientProvider']]),
                            sendToIndividuals: true])
                        error "Pipeline aborted due to quality gate failure: ${qg.status}"
                    }
                  }
		          milestone(2)
             }
        }

        stage('Tag Source Code') {
            def repositoryCommitterEmail = "jenkins@iktech.io"
            def repositoryCommitterUsername = "jenkinsCI"
            values = version.tokenize(".")

            sh "git config user.email ${repositoryCommitterEmail}"
            sh "git config user.name '${repositoryCommitterUsername}'"
            sh "git tag -d v${values[0]} || true"
            sh "git push origin :refs/tags/v${values[0]}"
            sh "git tag -d v${values[0]}.${values[1]} || true"
            sh "git push origin :refs/tags/v${values[0]}.${values[1]}"
            sh "git tag -d v${version} || true"
            sh "git push origin :refs/tags/v${version}"

            sh "git tag -fa v${values[0]} -m \"passed CI\""
            sh "git tag -fa v${values[0]}.${values[1]} -m \"passed CI\""
            sh "git tag -fa v${version} -m \"passed CI\""
            sh "git tag -a v${version}.${env.BUILD_NUMBER} -m \"passed CI\""
            sh "git push -f --tags"

            milestone(3)
        }
    }
}


properties([[
    $class: 'BuildDiscarderProperty',
    strategy: [
        $class: 'LogRotator',
        artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10']
    ]
]);
