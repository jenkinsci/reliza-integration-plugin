package io.reliza.plugins.sample;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.EnvVars;
import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
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
		envVars.put("RELIZA_API_USR", "PROJECT__6d0418e2-ab77-4de4-b616-c4c817e9b716");
		envVars.put("RELIZA_API_PSW", "5d5ad1359d7926cd6971c809fbbf3c2c8e690c7298cfc451487845c15b602243467a43392481337a721faf86627c385a");
		envVars.put("GIT_BRANCH", "master");
		relizaBuildWrapper.setUp(context, null, null, null, listener, envVars);
		
		envVars.put("VERSION", context.getEnv().get("VERSION"));
		envVars.put("URI", "https://test.relizahub.com");
		RelizaBuilder relizaBuilder = new RelizaBuilder();
		relizaBuilder.perform(null, null, envVars, null, listener);
	}
}
