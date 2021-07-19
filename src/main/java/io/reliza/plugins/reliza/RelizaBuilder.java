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
	String envSuffix;
	
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
	 * @param version - Custom version of release to create, required if not using withReliza wrapper.
	 */
	@DataBoundSetter public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * Optional parameter for builder initialization.
	 * @param projectId - UUID of project, only used when supplying custom version.
	 */
	@DataBoundSetter public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	
	/**
	 * Optional parameter for builder initialization.
	 * @param uri - Base uri of api call, only used when supplying custom version. Default set to "https://app.relizahub.com".
	 */
	@DataBoundSetter public void setUri(String uri) {
		this.uri = uri;
	}
	
	/**
	 * Sets up optional parameters from buildwrapper initialization.
	 * @param envSuffix - Flag which adds a suffix to all environment variables to differentiate from other withReliza calls.
	 */
	@DataBoundSetter public void setEnvSuffix(String envSuffix) {
		this.envSuffix = "_" + envSuffix;
	}
	
	/**
	 * Extracts project details from environment variables to send release metadata to reliza hub.
	 */
	@Override
	public void perform(Run<?, ?> run, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
		listener.getLogger().println("sending release metadata");
		
		// Use env variable with envSuffix if it exists, otherwise use env variable without envSuffix. If neither exist null is passed through.
		FlagsBuilder flagsBuilder = Flags.builder().apiKeyId(envVars.get("RELIZA_API_USR"))
			.apiKey(envVars.get("RELIZA_API_PSW"))
			.branch(envVars.containsKey("GIT_BRANCH" + envSuffix) ?
					envVars.get("GIT_BRANCH" + envSuffix) : envVars.get("GIT_BRANCH"))
			.version(envVars.containsKey("VERSION" + envSuffix) ?
					envVars.get("VERSION" + envSuffix) : envVars.get("VERSION"))
			.status(envVars.containsKey("STATUS" + envSuffix) ?
					envVars.get("STATUS" + envSuffix) : envVars.get("STATUS"))
			.projectId(envVars.containsKey("PROJECT_ID" + envSuffix) ?
					RelizaBuildWrapper.toUUID(envVars.get("PROJECT_ID" + envSuffix), listener) : RelizaBuildWrapper.toUUID(envVars.get("PROJECT_ID"), listener))
			.commitMessage(envVars.containsKey("COMMIT_MESSAGE" + envSuffix) ?
					envVars.get("COMMIT_MESSAGE" + envSuffix) : envVars.get("COMMIT_MESSAGE"))
			.commitHash(envVars.containsKey("GIT_COMMIT" + envSuffix) ?
					envVars.get("GIT_COMMIT" + envSuffix) : envVars.get("GIT_COMMIT"))
			.commitList(envVars.containsKey("COMMIT_LIST" + envSuffix) ?
					envVars.get("COMMIT_LIST" + envSuffix) : envVars.get("COMMIT_LIST"))
			.vcsType("Git")
			.vcsUri(envVars.containsKey("GIT_URL" + envSuffix) ?
					envVars.get("GIT_URL" + envSuffix) : envVars.get("GIT_URL"))
			.dateActual(envVars.containsKey("COMMIT_TIME" + envSuffix) ?
					envVars.get("COMMIT_TIME" + envSuffix) : envVars.get("COMMIT_TIME"))
			.dateStart(envVars.containsKey("BUILD_START_TIME" + envSuffix) ?
					envVars.get("BUILD_START_TIME" + envSuffix) : envVars.get("BUILD_START_TIME"));
		
		if (envVars.containsKey("BUILD_END_TIME" + envSuffix)) {
			flagsBuilder.dateEnd(envVars.get("BUILD_END_TIME" + envSuffix));
		} else if (envVars.containsKey("BUILD_END_TIME")) {
			flagsBuilder.dateEnd(envVars.get("BUILD_END_TIME"));
		} else {
			// Last resort use current time as build end time
			flagsBuilder.dateEnd(Instant.now().toString());
		}
		
		// To create an artifact both the id and build sha256 need to exist
		if (artId != null && envVars.get("SHA_256") != null) {
			flagsBuilder.artId(artId)
			.artBuildId(envVars.containsKey("BUILD_NUMBER" + envSuffix) ?
					envVars.get("BUILD_NUMBER" + envSuffix) : envVars.get("BUILD_NUMBER"))
			.artBuildUri(envVars.containsKey("RUN_DISPLAY_URL" + envSuffix) ?
					envVars.get("RUN_DISPLAY_URL" + envSuffix) : envVars.get("RUN_DISPLAY_URL"))
			.artCiMeta("Jenkins")
			.artType(artType)
			.artDigests(envVars.containsKey("SHA_256" + envSuffix) ?
					envVars.get("SHA_256" + envSuffix) : envVars.get("SHA_256"));
		}
		
		// Cannot pass null uri as it has a default set already
		if (envVars.containsKey("URI" + envSuffix)) {
			flagsBuilder.baseUrl(envVars.get("URI" + envSuffix));
		} else if (envVars.containsKey("URI")) {
			flagsBuilder.baseUrl(envVars.get("URI"));
		}
		
		// variables passed through the function override environment variables
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
