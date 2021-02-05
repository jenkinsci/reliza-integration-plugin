# Reliza integration with Jenkins

## Plugin use

Plugin integrates itself with Reliza Hub (https://app.relizahub.com), allowing you to automatically set new releases through your Jenkinsfile. More information on how to use Reliza Hub here https://www.youtube.com/watch?v=yDlf5fMBGuI

## Setting Reliza Hub credentials

For the plugin to interact with Reliza Hub you will need to set up credentials on your Jenkins instance.

### Acquiring api key and id

Project API: Go to reliza hub -> project -> project you wish to integrate -> click on padlock -> record given api key and id

OR Org API (will require project ID): Go to reliza hub -> settings -> set org-wide read-write api key -> record given api key and id

Project ID (if using Org API): Go to reliza hub -> project -> project you wish to integrate -> click on wrench -> record UUID

### Storing in Jenkins

Go to Jenkins instance -> Manage Jenkins -> Manage Credentials -> Domains: (global) -> Add Credentials

Kind should be set to Username with password and scope should be set to global.

Input your api key id into username and api key into password, then set identifying ID to "RELIZA_API", description can be anything.

## Example Jenkinsfile/Pipeline usage

```
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
        stage ('Build and Deploy') {
            steps {
                reliza (uri: 'https://test.relizahub.com') {
                    script {
                        try {
                            env.COMMIT_TIME = sh(script: 'git log -1 --date=iso-strict --pretty="%ad"', returnStdout: true).trim()
                            container('dind') {
                                sh '''
                                    docker build -t relizatest/throw .
                                    docker login -u USERNAME -p PASSWORD
                                    docker push relizatest/throw
                                    DOCKER_SHA_256=$(docker images --no-trunc --quiet relizatest/throw:latest)
                                '''
                                env.DOCKER_SHA_256 = sh(script: 'docker images --no-trunc --quiet relizatest/throw:latest', returnStdout: true)
                            }
                        } catch (Exception e) {
                            env.STATUS = 'rejected'
                            echo 'FAILED BUILD: ' + e.toString()
                        }
                        addRelease(artId: "relizatest/throw")
                    }
                }
            }
        }
    }
}
```

1. Credentials set beforehand in Jenkins instance are set as environment variables for plugin to read.

2. Reliza wrapper is called with 2 optional parameters uri and projectId. Base uri is preset to https://app.relizahub.com and projectId is only required if using ORG wide api. Wrapper calls Reliza Hub to get new version to be released.

3. Jenkinsfile reads from repository Dockerfile to build the image and push to Docker Hub, then sets certain values as environment variables for the plugin to read and send to Reliza Hub.

4. addRelease method can only be called within Reliza wrapper and will send release details to Reliza Hub. Method has 1 optional parameter artId (image name) which is only required when building an image.

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
