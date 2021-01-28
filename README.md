# reliza-jenkins-plugin

## Introduction

Plugin will release new version and add release details to Reliza Hub when performing a push to GitHub, requires a Jenkins instance to use.

//TODO: Will only work once reliza library is pushed to maven central and plugin is official.

## 1. Getting started (Integration with GitHub):

Install pipeline plugin, will need to restart server for installation to finish.
https://plugins.jenkins.io/workflow-aggregator/

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

Input your api key id into username and api key into password, then choose identifying ID.

## 4. Setting up Jenkins:

You will have 2 options for configuring your pipeline, the first will be to directly input a pipeline script into the pipeline configurations and the second will be to create a Jenkinsfile in your project's root directory for Jenkins to read from. <p>

Creating a Jenkinsfile allows you to update your pipeline without having to reconfigure it in your Jenkins instance.

### 4.1 Pipeline configurations:

Go to Jenkins -> New Item -> Pipeline <p>

Check GitHub project and input your GitHub repository URL. <p>

Under build triggers, check GitHub hook trigger for GITScm polling. <p>

### 4.2 Directly input pipeline script:

Under pipeline, select pipeline script and simply put in the pipeline script you want to be run.

### 4.3 Create Jenkinsfile:

Under pipeline, select pipeline script from SCM and put in your GitHub repository URL, if your repository is private you will need to put in credentials. <p>

Branches to build default is set to master and set script path to Jenkinsfile. <p>

The Jenkinsfile you create will contain only the pipeline script.

##5. Example pipeline:

```
pipeline {
    agent any
    
    stages {
        stage('setEnvironment') {
            steps {
                script {
                    try {
                        withCredentials([usernamePassword(credentialsId: 'PROJECT_API', usernameVariable: 'PROJECT_API_ID', passwordVariable: 'PROJECT_API_KEY')]){
                            env.PROJECT_API_ID = "${PROJECT_API_ID}"
                            env.PROJECT_API_KEY = "${PROJECT_API_KEY}"  
                        }
                    } catch (Exception e) {}
                    try{
                        withCredentials([usernamePassword(credentialsId: "ORG_API", usernameVariable: "ORG_API_ID", passwordVariable: "ORG_API_KEY")]) {
                            env.ORG_API_ID = "${ORG_API_ID}"
                            env.ORG_API_KEY = "${ORG_API_KEY}"
                        }
                    } catch (Exception e) {}
                }
            }
        }
        stage('addRelease') {
            steps {
                reliza(uri: "https://test.relizahub.com", projectId: "6ba5691c-05e3-4ecd-a45a-18b382419f40") {
                    getProjectMetadata()
                    echo "Version is ${env.VERSION}"
                }
            }
        }
    }
}
```

Credentials that were set beforehand are set as environment variables to be used later. In this case I chose the identifying ID in 3.2 as PROJECT_API and ORG_API. The wrapper calls Reliza Hub to get version details and then propagates those version details onto enclosed Reliza steps to submit build information to Reliza Hub.

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

