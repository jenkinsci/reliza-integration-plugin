package io.reliza.plugins.sample;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
import hudson.EnvVars;
import io.reliza.plugins.reliza.RelizaBuildWrapper;
import io.reliza.plugins.reliza.RelizaBuilder;
import jenkins.tasks.SimpleBuildWrapper.Context;

/**
 * Class for testing reliza build wrapper and reliza builder.
 */
public class RelizaBuilderTest {
    
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
        Context context = relizaBuildWrapper.createContext();
        TaskListener listener = jenkins.createTaskListener();
        project.getBuildWrappersList().add(relizaBuildWrapper);
        
        EnvVars envVars = new hudson.slaves.EnvironmentVariablesNodeProperty().getEnvVars();
        envVars.put("RELIZA_API_USR", "PROJECT__314c0886-0f41-4f92-a4ef-59c2cbb0e3b0");
        envVars.put("RELIZA_API_PSW", "7dc2dcd7a581f5a8346d9499adef781334785a149de18d92c65b938723aed32c8861473b720f3eab0170c3b188e8d19b");
        envVars.put("GIT_BRANCH", "master");
        relizaBuildWrapper.setUp(context, null, null, null, listener, envVars);
        
        envVars.put("VERSION", context.getEnv().get("VERSION"));
        envVars.put("URI", "https://test.relizahub.com");
        RelizaBuilder relizaBuilder = new RelizaBuilder();
        relizaBuilder.perform(null, null, envVars, null, listener);
    }
}
