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
		envVars.put("RELIZA_API_USR", "PROJECT__25f669d5-1dc9-40a2-ba13-43dabe8945c5");
		envVars.put("RELIZA_API_PSW", "1627c4214d917b2be39dc1d984500173952bfb3ca8c995329770e554a4655e4f7ccd559ffd0922bb0e59d2fec5b07c3e");
		envVars.put("GIT_BRANCH", "master");
		relizaBuildWrapper.setUp(context, null, null, null, listener, envVars);
		
		envVars.put("VERSION", context.getEnv().get("VERSION"));
		envVars.put("URI", "https://test.relizahub.com");
		RelizaBuilder relizaBuilder = new RelizaBuilder();
		relizaBuilder.perform(null, null, envVars, null, listener);
	}
}
