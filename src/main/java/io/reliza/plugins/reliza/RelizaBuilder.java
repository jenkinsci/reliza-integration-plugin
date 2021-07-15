package io.reliza.plugins.reliza;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;

import reliza.java.client.Flags;
import reliza.java.client.Library;

/**
 * Uses the addRelease method from reliza library within the reliza wrapper to send release details to reliza hub.
 */
public class RelizaBuilder extends Builder implements SimpleBuildStep {
	String status;
	String artId;
	String artType;
	
	/**
	 * Builder initialization with no required parameters.
	 */
	@DataBoundConstructor
	public RelizaBuilder() {}
	
	/**
	 * Optional parameter for builder initialization.
	 * @param status - Used to override given status if needed.
	 */
	@DataBoundSetter public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Optional parameter for builder initialization.
	 * @param artId - Id of created artifact, required only if building an artifact.
	 */
	@DataBoundSetter public void setArtId(String artId) {
		this.artId = artId;
	}
	
	/**
	 * Optional parameter for builder initialization.
	 * @param artType - Type of created artifact.
	 */
	@DataBoundSetter public void setArtType(String artType) {
		this.artType = artType;
	}
	
	/**
	 * Extracts project details from environment variables to send release metadata to reliza hub.
	 */
	@Override
	public void perform(Run<?, ?> run, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
		listener.getLogger().println("sending release metadata");
		Flags flags = Flags.builder().apiKeyId(envVars.get("RELIZA_API_USR"))
			.apiKey(envVars.get("RELIZA_API_PSW"))
			.branch(envVars.get("GIT_BRANCH"))
			.version(envVars.get("VERSION"))
			.status(envVars.get("STATUS"))
			.projectId(RelizaBuildWrapper.UUID(envVars.get("PROJECT_ID"), listener))
			.commitMessage(envVars.get("COMMIT_MESSAGE"))
			.commitHash(envVars.get("GIT_COMMIT"))
			.commitList(envVars.get("COMMIT_LIST"))
			.vcsType("Git")
			.vcsUri(envVars.get("GIT_URL"))
			.dateActual(envVars.get("COMMIT_TIME"))
			.artBuildId(envVars.get("BUILD_NUMBER"))
			.artBuildUri(envVars.get("RUN_DISPLAY_URL"))
			.artCiMeta("Jenkins")
			.artType(artType)
			.dateStart(envVars.get("BUILD_START_TIME"))
			.dateEnd(Instant.now().toString())
			.artDigests(envVars.get("SHA_256"))
			.build();
		
		if (envVars.get("URI") != null) {
			flags.setBaseUrl(envVars.get("URI"));
		}
		if (artId != null) {
			flags.setArtId(Arrays.asList(artId));
		}
		if (status != null) {
			flags.setStatus(status);
		}
		
		Library library = new Library(flags);
		library.addRelease();
	}
	
	@Symbol("addRelizaRelease")
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}
	}
}
