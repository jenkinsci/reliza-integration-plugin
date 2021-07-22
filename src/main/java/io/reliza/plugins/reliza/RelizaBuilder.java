package io.reliza.plugins.reliza;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

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
	Boolean useCommitList = false;
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
	 * @param useCommitList - Flag which allows commit information from commit list to be prioritized over other commit parameters
	 */
	@DataBoundSetter public void setUseCommitList(String useCommitList) {
		if (useCommitList.equalsIgnoreCase("true")) {
			this.useCommitList = true;
		}
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
		
		FlagsBuilder flagsBuilder = Flags.builder().apiKeyId(envVars.get("RELIZA_API_USR"))
			.apiKey(envVars.get("RELIZA_API_PSW"))
			.branch(resolveEnvVar("GIT_BRANCH", envSuffix, envVars))
			.version(resolveEnvVar("VERSION", envSuffix, envVars))
			.status(resolveEnvVar("STATUS", envSuffix, envVars))
			.projectId(toUUID(resolveEnvVar("PROJECT_ID", envSuffix, envVars), listener))
			.commitList(resolveEnvVar("COMMIT_LIST", envSuffix, envVars))
			.vcsType("Git")
			.vcsUri(resolveEnvVar("GIT_URL", envSuffix, envVars))
			.dateStart(resolveEnvVar("BUILD_START_TIME", envSuffix, envVars));
		
		if (!useCommitList) {
			flagsBuilder.commitHash(resolveEnvVar("GIT_COMMIT", envSuffix, envVars))
			.commitMessage(resolveEnvVar("COMMIT_MESSAGE", envSuffix, envVars))
			.dateActual(resolveEnvVar("COMMIT_TIME", envSuffix, envVars));
		}
		
		if (resolveEnvVar("BUILD_END_TIME", envSuffix, envVars) == null) {
			flagsBuilder.dateEnd(resolveEnvVar("BUILD_END_TIME", envSuffix, envVars));
		} else {
			// Backup use current time as build end time
			flagsBuilder.dateEnd(Instant.now().toString());
		}
		
		// To create an artifact both the id and build sha256 need to exist
		if (artId != null && envVars.get("SHA_256") != null) {
			flagsBuilder.artId(artId)
			.artBuildId(resolveEnvVar("BUILD_NUMBER", envSuffix, envVars))
			.artBuildUri(resolveEnvVar("RUN_DISPLAY_URL", envSuffix, envVars))
			.artCiMeta("Jenkins")
			.artType(artType)
			.artDigests(resolveEnvVar("SHA_256", envSuffix, envVars));
		}
		
		// Cannot pass null uri as it has a default set already
		if (resolveEnvVar("URI", envSuffix, envVars) != null) {
			flagsBuilder.baseUrl(resolveEnvVar("URI", envSuffix, envVars));
		}
		
		// variables passed through the function override environment variables
		if (uri != null) flagsBuilder.baseUrl(uri);
		if (status != null) flagsBuilder.status(status);
		if (version != null) flagsBuilder.version(version);
		if (projectId != null) flagsBuilder.projectId(toUUID(projectId, listener));
		
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
	
	/**
	 * String to UUID converter which handles conversion errors.
	 * @param projectId - Project UUID.
	 * @param listener - TaskListener to log specific error.
	 * @return Corresponding UUID if conversion succeeded and null otherwise.
	 */
	public static UUID toUUID(String projectId, TaskListener listener) {
		try {
			if (projectId == null || projectId.isEmpty()) {
				return null;
			} else {
				return UUID.fromString(projectId);
			}
		} catch (IllegalArgumentException e) {
			listener.getLogger().println(e);
			return null;
		}
	}
	
	/**
	 * Obtains env variable with envSuffix if it exists, otherwise obtains env variable without envSuffix. If neither exist null is passed through.
	 * @param envVar - Key to resolve to a value using environment variables
	 * @param suffix - Search for environment variables with this suffix
	 * @param envVars - Map of all environment variables to search through
	 * @return Corresponding value from environment variable map
	 */
	public static String resolveEnvVar(String envVar, String suffix, EnvVars envVars) {
		String envVarValue = null;
		if (envVars.containsKey(envVar + suffix)) {
			envVarValue = envVars.get(envVar + suffix);
		} else if (envVars.containsKey(envVar)) {
			envVarValue = envVars.get(envVar);
		}
		return envVarValue;
	}
}
