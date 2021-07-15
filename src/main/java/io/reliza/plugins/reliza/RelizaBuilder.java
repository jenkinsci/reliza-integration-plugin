package io.reliza.plugins.reliza;

import java.io.IOException;
import java.time.Instant;

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
import reliza.java.client.Flags.FlagsBuilder;
import reliza.java.client.Library;
import reliza.java.client.responses.ReleaseData;

/**
 * Uses the addRelease method from reliza library within the reliza wrapper to send release details to reliza hub.
 */
public class RelizaBuilder extends Builder implements SimpleBuildStep {
	String status;
	String artId;
	String artType;
	String version;
	String uri;
	String projectId;
	
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
	 * Optional parameter for builder initialization.
	 * @param artType - Type of created artifact.
	 */
	@DataBoundSetter public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * Optional parameter for builder initialization.
	 * @param artType - Type of created artifact.
	 */
	@DataBoundSetter public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	
	/**
	 * Optional parameter for builder initialization.
	 * @param artType - Type of created artifact.
	 */
	@DataBoundSetter public void setUri(String uri) {
		this.uri = uri;
	}
	
	/**
	 * Extracts project details from environment variables to send release metadata to reliza hub.
	 */
	@Override
	public void perform(Run<?, ?> run, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
		listener.getLogger().println("sending release metadata");
		FlagsBuilder flagsBuilder = Flags.builder().apiKeyId(envVars.get("RELIZA_API_USR"))
			.apiKey(envVars.get("RELIZA_API_PSW"))
			.branch(envVars.get("GIT_BRANCH"))
			.version(envVars.get("VERSION"))
			.status(envVars.get("STATUS"))
			.projectId(RelizaBuildWrapper.toUUID(envVars.get("PROJECT_ID"), listener))
			.commitMessage(envVars.get("COMMIT_MESSAGE"))
			.commitHash(envVars.get("GIT_COMMIT"))
			.commitList(envVars.get("COMMIT_LIST"))
			.vcsType("Git")
			.vcsUri(envVars.get("GIT_URL"))
			.dateActual(envVars.get("COMMIT_TIME"))
			.dateStart(envVars.get("BUILD_START_TIME"))
			.dateEnd(Instant.now().toString())
			.artBuildId(envVars.get("BUILD_NUMBER"))
			.artBuildUri(envVars.get("RUN_DISPLAY_URL"))
			.artCiMeta("Jenkins")
			.artType(artType)
			.artDigests(envVars.get("SHA_256"));
		
		if (artId != null) {
			flagsBuilder.artId(artId);
		}
		
		// variables passed through the function override environment variables
		if (envVars.get("URI") != null) flagsBuilder.baseUrl(envVars.get("URI"));
		if (uri != null) flagsBuilder.baseUrl(uri);
		if (status != null) flagsBuilder.status(status);
		if (version != null) flagsBuilder.version(version);
		if (projectId != null) flagsBuilder.projectId(RelizaBuildWrapper.toUUID(projectId, listener));
		
		Flags flags = flagsBuilder.build();
		Library library = new Library(flags);
		ReleaseData releaseData = library.addRelease();
		if (releaseData == null) {
			throw new RuntimeException("Failed to create new release");
		}
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
