# Reliza integration with Jenkins

## Plugin use

Plugin integrates itself with Reliza Hub (https://app.relizahub.com), allowing you to automatically set new releases through your Jenkinsfile. More information on how to use Reliza Hub here https://www.youtube.com/watch?v=yDlf5fMBGuI

## Setting up instance

For the plugin to interact with Reliza Hub you will need to set up credentials on your Jenkins instance.

### Acquiring api key and id

Project API: Go to Reliza Hub -> project -> project you wish to integrate -> click on padlock -> record given api key and id

OR Org API (will require project ID): Go to Reliza Hub -> settings -> set org-wide read-write api key -> record given api key and id

Project ID (if using Org API): Go to Reliza Hub -> project -> project you wish to integrate -> click on wrench -> record UUID

### Storing in Jenkins

Go to your Jenkins instance -> Manage Jenkins -> Manage Credentials -> Domains: (global) -> Add Credentials

Kind should be set to Username with password and scope should be set to global.

Input your api key id into username and api key into password, then set identifying ID to "RELIZA_API", description can be anything.

### Blue Ocean

In order for the plugin to link your build URL on Reliza Hub, the base URL of your Jenkins instance needs to be preset.

Go to your Jenkins instance -> Manage Jenkins -> Configure System -> Jenkins URL -> put in URL

## Available features
* *withReliza*: Wrapper will call Reliza Hub to get new version to be released. Version and docker safe version can then be accessed using **env.VERSION** and **env.DOCKER_VERSION** from inside the wrapper
    * uri: Uri is defaulted to https://app.relizahub.com but this parameter can override it if necessary
    * projectId: Uuid of project required only if authenticating using an organization wide api
    * jenkinsVersionMeta: If set to true, will set the metadata flag to the Jenkins build id
    * customVersionMeta: Will set the metadata flag to a custom value and overrides jenkinsVersionMeta
    * customVersionModifier Will set modifier flag to a custom value
* *addRelizaRelease*: This method can only be called within the withReliza wrapper and will send release details to Reliza Hub
    * artId: Parameter to specify artifact id
    * artType: Parameter to specify artifact type
    * status: If needed, this parameter will set status and override previously set statuses
* *COMMIT_TIME* and *SHA_256* are parameters set within script which *addRelizaRelease* will read when called

## Example Jenkinsfile/Pipeline usage

```groovy
pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    run: jenkins-slave
spec:
  containers:
  - name: dind
    image: docker:19.03.12-dind
    command:
    - cat
    volumeMounts:
    - mountPath: /var/run/docker.sock
      name: dockersock
    tty: true
  volumes:
  - name: dockersock
    hostPath:
      path: /var/run/docker.sock
"""
        }
    }
    environment { RELIZA_API = credentials('RELIZA_API') }
    stages {
        stage('Build and Deploy') {
            steps {
                withReliza(jenkinsVersionMeta: 'true', customVersionModifier: 'GitHub') {
                    script {
                        try {
                            env.COMMIT_TIME = sh(script: 'git log -1 --date=iso-strict --pretty="%ad"', returnStdout: true).trim()
                            container('dind') {
                                sh '''
                                    docker build -t relizatest/throw .
                                    docker login -u USERNAME -p PASSWORD
                                    docker push relizatest/throw
                                '''
                                env.SHA_256 = sh(script: 'docker inspect -f \'{{range .RepoDigests}}{{.}}{{end}}\' relizatest/throw:latest | cut -f 2 -d\'@\'', returnStdout: true)
                            }
                        } catch (Exception e) {
                            env.STATUS = 'rejected'
                            echo 'FAILED BUILD: ' + e.toString()
                        }
                        addRelizaRelease(artId: "relizatest/throw", artType: "Docker")
                    }
                }
            }
        }
    }
}
```

## Resources on pipelines and writing plugins
https://www.jenkins.io/doc/book/pipeline/syntax/  
https://www.jenkins.io/doc/developer/guides/  
https://wiki.jenkins.io/display/JENKINS/Extend+Jenkins  
https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial  
https://softwareengineering.stackexchange.com/questions/64867/writing-jenkins-plugin-where-is-the-documentation/87913  
https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial#Plugintutorial-CreatingaNewPlugin  
https://www.jenkins.io/doc/developer/plugin-development/pipeline-integration/  
https://github.com/jenkinsci/workflow-step-api-plugin/blob/master/README.md  
https://github.com/jenkinsci/workflow-basic-steps-plugin/blob/master/CORE-STEPS.md  
https://blog.codecentric.de/en/2012/08/tutorial-create-a-jenkins-plugin-to-integrate-jenkins-and-nexus-repository/  
http://javaadventure.blogspot.com/2008/04/writing-hudson-plugin-part-6-parsing.html  

## Version History

See the [changelog](https://github.com/jenkinsci/reliza-integration-plugin/blob/main/CHANGELOG.md)
