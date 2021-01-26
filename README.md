# reliza-jenkins-plugin

## Introduction

TODO Describe what your plugin does here

## Getting started

Run development mode with 

```
mvn hpi:run
```

Install pipeline plugin  
https://plugins.jenkins.io/workflow-aggregator/

Sample pipeline:

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

Credentials should be set beforehand to be set as environment variables. The wrapper calls Reliza Hub to get version details and then
propagates those version details onto enclosed Reliza steps to submit build information to Reliza Hub.

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

