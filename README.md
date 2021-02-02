# reliza-jenkins-plugin

## Introduction

Plugin will release new version and add release details to Reliza Hub when performing a push to GitHub, requires a Jenkins instance to use.

//TODO: Will only work once reliza library is pushed to maven central and plugin is official.

## 1. Getting started (Integration with GitHub):

Install docker and pipeline plugins, will need to restart server for installation to finish. <p>
https://plugins.jenkins.io/workflow-aggregator/  
https://plugins.jenkins.io/docker-build-publish/  
https://plugins.jenkins.io/docker-plugin/  
https://plugins.jenkins.io/docker-workflow/

## 2. Setting up GitHub webhook:

Go to the selected GitHub repository with which you wish to integrate with reliza hub.

### 2.1 Create new webhook:

Settings -> Webhooks -> Add webhook

### 2.2 Webhook configurations:

In payload URL, put in the base url of your Jenkins instance appended by "/github-webhook/" <p>

Set content-type to application/json <p>

Choose specific events for when you want to release details to reliza hub. Current plugins only support pushes, branches, and pull requests.

## 3. Setting up reliza hub configurations:

### 3.1 Acquiring api key and id:

Project Id: Go to reliza hub -> project -> chosen project -> click on padlock -> record given api key and id <p>

OR Org Id (will require project UUID): Go to reliza hub -> settings -> set org-wide read-write api key -> record given api key and id <p>

### 3.2 Storing in Jenkins:

Go to Jenkins -> Manage Jenkins -> Manage Credentials -> Domains: (global) -> Add Credentials <p>

Kind should be set to Username with password and scope should be set to global. <p>

Input your api key id into username and api key into password, then set identifying ID to "RELIZA_API". 

## 4. Setting up Jenkins:

You will have 2 options for configuring your pipeline, the first will be to directly input a pipeline script into the pipeline configurations and the second will be to create a Jenkinsfile in your project's root directory for Jenkins to read from. <p>

Creating a Jenkinsfile allows you to update your pipeline without having to go to your Jenkins instance to reconfigure it.

### 4.1 Pipeline configurations:

Go to Jenkins -> New Item -> Pipeline <p>

Check GitHub project and input your GitHub repository URL. <p>

Under build triggers, check GitHub hook trigger for GITScm polling. <p>

### 4.2 Directly input pipeline script:

Under pipeline, select pipeline script and simply put in the pipeline script you want to be run.

### 4.3 Create Jenkinsfile:

Under pipeline, select pipeline script from SCM and put in your GitHub repository URL, if your repository is private you will need to put in credentials. <p>

Branches to build default is set to master and set script path to Jenkinsfile. <p>

The Jenkinsfile you create should contain only the pipeline script.

## 5. Example pipeline:

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
        stage('Build Image') {
            steps {
                script {
                    env.BUILD_START_TIME = sh(script: 'date -Iseconds', returnStdout: true).trim() 
                    env.COMMIT_TIME = sh(script: 'git log -1 --date=iso-strict --pretty="%ad"', returnStdout: true).trim()
                }
                container('dind') {
                    sh '''
                        docker build -t relizatest/throw .
                        docker login -u relizatest -p 9557ef8b-3ac3-4a2e-b351-a412d52d88d9
                        docker push relizatest/throw
                        DOCKER_SHA_256=$(docker images --no-trunc --quiet relizatest/throw:latest)
                    '''
                    script {
                        env.DOCKER_SHA_256 = sh(script: 'docker images --no-trunc --quiet relizatest/throw:latest', returnStdout: true)
                        env.BUILD_END_TIME = sh(script: 'date -Iseconds', returnStdout: true).trim()
                    }
                }
            }
        }
    }
    post {
        failure {
            container('dind') {
                script {
                    env.STATUS = 'rejected'
                }
            }
        }
        always {
            container('dind') {
                reliza(uri: 'https://test.relizahub.com') {
                    echo "Version is ${env.VERSION}"
                    addRelease("Xenogents/mafia-vue")
                }
            }
        }
    }
}
```

Credentials that were set beforehand are set as environment variables to be used later. In this case I chose the identifying ID in 3.2 as RELIZA_API. The image is built and the reliza wrapper calls Reliza Hub to get version details in order to submit build information to Reliza Hub.

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

## Contributing

TODO review the default [CONTRIBUTING](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md) file and make sure it is appropriate for your plugin, if not then add your own one adapted from the base file

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

