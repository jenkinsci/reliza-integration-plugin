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

## Contributing

TODO review the default [CONTRIBUTING](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md) file and make sure it is appropriate for your plugin, if not then add your own one adapted from the base file

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

