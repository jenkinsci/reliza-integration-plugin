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
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.tasks.SimpleBuildWrapper;
import reliza.java.client.Flags;
import reliza.java.client.Flags.FlagsBuilder;
import reliza.java.client.Library;
import reliza.java.client.responses.FullRelease;
import reliza.java.client.responses.ProjectVersion;

/**
 * An extension of {@link SimpleBuildWrapper} which sets up the reliza wrapper to perform api calls to reliza hub.
 */
public class RelizaBuildWrapper extends SimpleBuildWrapper {
	private String projectId;
	private String uri;
	private Boolean metadata = false;
	private String customMetadata;
	private String modifier;
	private Boolean onlyVersion = false;
	private Boolean getVersion = true;
	private String envSuffix = "";
	
	/**
	 * Buildwrapper initialization with no required parameters.
	 */
	@DataBoundConstructor
	public RelizaBuildWrapper() {
		super();
	}
	
	/**
	 * Sets up optional parameters from buildwrapper initialization.
	 * @param projectId - Project UUID obtainable from reliza hub.
	 */
	@DataBoundSetter public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	
	/**
	 * Sets up optional parameters from buildwrapper initialization.
	 * @param uri - Base uri of api call, default set to "https://app.relizahub.com".
	 */
	@DataBoundSetter public void setUri(String uri) {
		this.uri = uri;
	}
	
	/**
	 * Sets up optional parameters from buildwrapper initialization.
	 * @param metadata - if true, will set metadata flag on getversion call to Jenkins.
	 */
	@DataBoundSetter public void setJenkinsVersionMeta(String metadata) {
		if (metadata.toLowerCase().equals("true")) {
			this.metadata = true;
		}
	}
	
	/**
	 * Sets up optional parameters from buildwrapper initialization.
	 * @param customMetadata - Sets custom version metadata and will override useJenkinsVersionMeta.
	 */
	@DataBoundSetter public void setCustomVersionMeta(String customMetadata) {
		this.customMetadata = customMetadata;
	}
	
	/**
	 * Sets up optional parameters from buildwrapper initialization.
	 * @param modifier - will set modifier flag on getversion call
	 */
	@DataBoundSetter public void setCustomVersionModifier(String modifier) {
		this.modifier = modifier;
	}
	
	/**
	 * Sets up optional parameters from buildwrapper initialization.
	 * @param onlyVersion - Flag to skip creation of the release.
	 */
	@DataBoundSetter public void setOnlyVersion(String onlyVersion) {
		if (onlyVersion.equalsIgnoreCase("true")) {
			this.onlyVersion = true;
		}
	}
	
	/**
	 * Sets up optional parameters from buildwrapper initialization.
	 * @param getVersion - Flag to determine whether version information will be gotten from Reliza Hub or not.
	 */
	@DataBoundSetter public void setGetVersion(String getVersion) {
		if (getVersion.equalsIgnoreCase("false")) {
			this.getVersion = false;
		}
	}
	
	/**
	 * Sets up optional parameters from buildwrapper initialization.
	 * @param envSuffix - Flag which adds a suffix to all environment variables to differentiate from other addRelizaRelease calls.
	 */
	@DataBoundSetter public void setEnvSuffix(String envSuffix) {
		this.envSuffix = "_" + envSuffix;
	}
	
	
	/**
	 * {@inheritDoc} <p>
	 * Retrieves preset credentials and parameters to perform getVersion api call and then sets
	 * received information as environment variables to pass to subsequent addRelease call.
	 */
	@Override
	public void setUp(Context context, Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
		// If this does not require a workspace, defer to the version that does not take a workspace and launcher.
		if (!this.requiresWorkspace()) {
			this.setUp(context, build, listener, initialEnvironment);
			return;
		}
		listener.getLogger().println("setting up reliza context wrapper");
		
		// As a backup, use this call to determine build start time
		context.env("BUILD_START_TIME", Instant.now().toString());
		FlagsBuilder flagsBuilder = Flags.builder().apiKeyId(initialEnvironment.get("RELIZA_API_USR"))
			.apiKey(initialEnvironment.get("RELIZA_API_PSW"))
			.projectId(RelizaBuilder.toUUID(projectId, listener))
			.branch(RelizaBuilder.resolveEnvVar("GIT_BRANCH", envSuffix, initialEnvironment))
			.commitMessage(RelizaBuilder.resolveEnvVar("COMMIT_MESSAGE", envSuffix, initialEnvironment))
			.commitList(RelizaBuilder.resolveEnvVar("COMMIT_LIST", envSuffix, initialEnvironment))
			.modifier(modifier)
			.onlyVersion(onlyVersion);
		
		// Uri has a default value so cannot use null
		if (uri != null) flagsBuilder.baseUrl(uri);
		if (customMetadata != null) {
			flagsBuilder.metadata(customMetadata);
		} else if (metadata) {
			flagsBuilder.metadata(RelizaBuilder.resolveEnvVar("BUILD_NUMBER", envSuffix, initialEnvironment));
		}
		
		Flags flags = flagsBuilder.build();
		Library library = new Library(flags);
		
		if (getVersion) {
			ProjectVersion projectVersion = library.getVersion();
			if (projectVersion == null) {
				throw new RuntimeException("Version could not be retrieved");
			}
			listener.getLogger().println("Version is: " + projectVersion.getVersion());
			context.env("VERSION" + envSuffix, projectVersion.getVersion());
			context.env("DOCKER_VERSION" + envSuffix, projectVersion.getDockerTagSafeVersion());
		}
		
		FullRelease fullRelease = library.getLatestRelease();
		if (fullRelease != null && fullRelease.getSourceCodeEntryDetails() != null) {
			context.env("LATEST_COMMIT" + envSuffix, fullRelease.getSourceCodeEntryDetails().getCommit());
		}
		context.env("URI" + envSuffix, uri);
		context.env("PROJECT_ID" + envSuffix, projectId);
	}
	
	@Symbol("withReliza")
	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor {
		
		public DescriptorImpl() {
			super(RelizaBuildWrapper.class);
			load();
		}
		
		@Override
		public boolean isApplicable(final AbstractProject<?, ?> item) {
			return true;
		}
	}
}
