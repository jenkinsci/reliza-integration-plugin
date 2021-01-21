# reliza-jenkins-plugin

## Introduction

TODO Describe what your plugin does here

## Getting started

Run development mode with 

```
mvn hpi:run
```

Sample pipeline:

```
pipeline {
    agent any
    
    stages {
        stage('Hello From Reliza') {
            steps {
                reliza {
                    echo 'test'
                    relizagreet('Reliza')
                    echo "Pavel var is ${env.PAVEL_VAR}"
                }
                echo 'Hello World'
                echo "Pavel var is ${env.PAVEL_VAR}"
            }
            post {
                always {
                    echo 'post action'
                }
            }
        }
    }
}
```

This will set PAVEL_VAR env variable to "pavel var from context".


TODO:

The wrapper should call Reliza Hub and get version details, it should then propagate those version details onto enclosed Reliza steps to submit build information to Reliza Hub.

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

