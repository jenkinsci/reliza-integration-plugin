package io.reliza.plugins.sample;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleProject;
import hudson.EnvVars;
import io.reliza.plugins.reliza.RelizaBuildWrapper;

/**
 * Class for testing reliza build wrapper.
 */
public class RelizaBuildWrapperTest {
    
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    
    /**
     * Simple test to make sure wrapper can perform api calls to reliza hub.
     */
    @Test
    public void testRelizaWrapper() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        RelizaBuildWrapper relizaBuildWrapper = new RelizaBuildWrapper();
        relizaBuildWrapper.setUri("https://test.relizahub.com");
        relizaBuildWrapper.setProjectId(null);
        project.getBuildWrappersList().add(relizaBuildWrapper);
        
        EnvVars envVars = new hudson.slaves.EnvironmentVariablesNodeProperty().getEnvVars();
        envVars.put("RELIZA_API_USR", "PROJECT__b4d6324f-4985-49a8-afba-bdcffec32b03");
        envVars.put("RELIZA_API_PSW", "1367ff3aa5a38ef626607a7b38613e4913ac732775444a26db4bc6d8344ad626aab1f7801ff9b51a8cd76a2dd61d8dc4");
        envVars.put("GIT_BRANCH", "master");
        relizaBuildWrapper.setUp(relizaBuildWrapper.createContext(), null, null, null, jenkins.createTaskListener(), envVars);
    }
}
