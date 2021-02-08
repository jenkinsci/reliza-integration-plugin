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
import reliza.java.client.Library;

/**
 * Uses the addRelease method from reliza library within the reliza wrapper to send release details to reliza hub.
 */
public class RelizaBuilder extends Builder implements SimpleBuildStep {
    String artId;
    
    /**
     * Builder initialization with no required parameters.
     */
    @DataBoundConstructor
    public RelizaBuilder() {}
    
    /**
     * Optional parameters for buildwrapper initialization.
     * @param uri - Base uri of api call, default set to "https://app.relizahub.com".
     */
    @DataBoundSetter public void setArtId(String artId) {this.artId = artId;}
    
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
            .commitHash(envVars.get("GIT_COMMIT"))
            .vcsType("git")
            .vcsUri(envVars.get("GIT_URL"))
            .dateActual(envVars.get("COMMIT_TIME"))
            .artId(artId)
            .artBuildId(envVars.get("BUILD_NUMBER"))
            .artCiMeta("Jenkins " + envVars.get("BUILD_URL"))
            .artType("Docker")
            .dateStart(envVars.get("BUILD_START_TIME"))
            .dateEnd(Instant.now().toString())
            .artDigests(envVars.get("DOCKER_SHA_256"))
            .build();
        if (envVars.get("URI") != null) {flags.setBaseUrl(envVars.get("URI"));}
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
