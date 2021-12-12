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
		envVars.put("RELIZA_API_USR", "PROJECT__24625ac0-0256-4638-99d2-f245cc56ff8f");
		envVars.put("RELIZA_API_PSW", "5953d4e03ce1bbb6183665bf3b7db41ab713e87af47c5dbfd77bead8d339c71366566db15aef87a7a5ced6231ea433fb");
		envVars.put("GIT_BRANCH", "master");
		relizaBuildWrapper.setUp(context, null, null, null, listener, envVars);
		
		envVars.put("VERSION", context.getEnv().get("VERSION"));
		envVars.put("URI", "https://test.relizahub.com");
		RelizaBuilder relizaBuilder = new RelizaBuilder();
		relizaBuilder.perform(null, null, envVars, null, listener);
	}
}
