package io.reliza.plugins.reliza;

import java.io.IOException;

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
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import jenkins.tasks.SimpleBuildStep;
import reliza.java.client.Flags;
import reliza.java.client.Flags.FlagsBuilder;
import reliza.java.client.Library;
import reliza.java.client.responses.ReleaseData;

/**
 * Uses the prdata method from reliza library within the reliza wrapper to send release details to reliza hub.
 */

/**
 * Method that denotes that we are sending data for a pull request. <p>
 * Method itself does not require parameters but requires that the Flags class passed during library initialization contains these parameters. <p>
 * - apiKeyId (required) - flag for instance api id. <br>
 * - apiKey (required) - flag for instance api key. <br>
 * - branch (required) - flag to denote name of the base branch for the pull request. <br>
 * - targetBranch (required) - flag to denote name of the target branch for the pull request. <br>
 * - state (required) - flag to denote state of the pull request. <br>
 * - endpoint (required) - flag to denote HTML endpoint of the pull request. <br>
 * - title (required) - flag to denote title of the pull request. <br>
 * - number (required) - flag to denote number of the pull request. <br>
 * - commits (required) - flag to denote comma seprated commit shas on this pull request (Optional when single commit flag is used). <br>
 * - commits (optional) - flag to denote current commit sha of this pull request. <br>
 * - createdDate (required) - flag to denote datetime when the pull request was created. <br>
 * - closedDate (optional) - flag to denote datetime when the pull request was closed. <br>
 * - mergedDate (optional) - flag to denote datetime when the pull request was merged. <br>
 * - projectId (optional) - flag to denote project uuid. Required if organization-wide read-write key is used, ignored if project specific api key is used. <br>
 * @return returns true if successful API call and null otherwise.
 */
public class RelizaPR extends Builder implements SimpleBuildStep {
	
	String targetBranch;
	String baseBranch;
    String state;
    String endpoint;
    String title;
    String number;
    String commits;
    String commit;
    String createdDate;
    String closedDate;
    String mergedDate;
	String uri;
	String projectId;
	String envSuffix;
	
	/**
	 * Builder initialization with no required parameters.
	 */
	@DataBoundConstructor
	public RelizaPR() {}
	
    /**
	 * Optional parameter for builder initialization.
	 * @param targetBranch - Used set the targetBranch.
	 */
	@DataBoundSetter public void setTargetBranch(String targetBranch) {
		this.targetBranch = targetBranch;
	}

    /**
	 * Optional parameter for builder initialization.
	 * @param baseBranch - Used set the baseBranch.
	 */
	@DataBoundSetter public void setbaseBranch(String baseBranch) {
		this.baseBranch = baseBranch;
	}


	/**
	 * Optional parameter for builder initialization.
	 * @param state - Used to override given state if needed.
	 */
	@DataBoundSetter public void setState(String state) {
		this.state = state;
	}

	
	
	/**
	 * Optional parameter for builder initialization.
	 * @param endpoint - Used to set the HTML endpoint of the PullRequest
	 */
	@DataBoundSetter public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	/**
	 * Optional parameter for builder initialization.
	 * @param title - Used to set the title of the PullRequest
	 */
	@DataBoundSetter public void settitle(String title) {
		this.title = title;
	}
	
	/**
	 * Optional parameter for builder initialization.
	 * @param number - Used to set the title of the PullRequest
	 */
	@DataBoundSetter public void setNumber(String number) {
		this.number = number;
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
	 * Optional parameter for builder initialization.
	 * @param mergedDate - Used to set the datetime when the pull request was merged
	 */
	@DataBoundSetter public void setMergedDate(String mergedDate) {
		this.mergedDate = mergedDate;
	}

	/**
	 * Optional parameter for builder initialization.
	 * @param closedDate - Used to set the datetime when the pull request was closed
	 */
	@DataBoundSetter public void setClosedDate(String closedDate) {
		this.closedDate = closedDate;
	}

	/**
	 * Optional parameter for builder initialization.
	 * @param createdDate - Used to set the datetime when the pull request was created
	 */
	@DataBoundSetter public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	
	/**
	 * Sets up optional parameters from buildwrapper initialization.
	 * @param commits - comma seprated commit shas on the pull request
	 */
	@DataBoundSetter public void setUseCommitList(String commits) {
		this.commits = commits;
	}

	/**
	 * Sets up optional parameters from buildwrapper initialization.
	 * @param commit - current commit sha of the pull request
	 */
	@DataBoundSetter public void setCommit(String commit) {
		this.commit = commit;
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
			.projectId(toUUID(resolveEnvVar("PROJECT_ID", envSuffix, envVars), listener));
		
		
		// Cannot pass null uri as it has a default set already
		if (resolveEnvVar("URI", envSuffix, envVars) != null) {
			flagsBuilder.baseUrl(resolveEnvVar("URI", envSuffix, envVars));
		}
		
		// variables passed through the function override environment variables
    
		if (uri != null) flagsBuilder.baseUrl(uri);
		if (state != null) flagsBuilder.state(state);
		if (endpoint != null) flagsBuilder.endPoint(endpoint);
		if (title != null) flagsBuilder.title(title);
		if (number != null) flagsBuilder.number(number);
		if (commits != null) flagsBuilder.commits(commits);
		if (commit != null) flagsBuilder.commitHash(commit);
		if (createdDate != null) flagsBuilder.createdDate(createdDate);
		if (closedDate != null) flagsBuilder.closedDate(closedDate);
		if (mergedDate != null) flagsBuilder.mergedDate(mergedDate);
		if (targetBranch != null) flagsBuilder.targetBranch(targetBranch);
		if (baseBranch != null) flagsBuilder.branch(baseBranch);
		
		if (projectId != null) flagsBuilder.projectId(toUUID(projectId, listener));
		
		Flags flags = flagsBuilder.build();
		Library library = new Library(flags);
		library.prData();
	}
	
	@Symbol("submitPrData")
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
