package io.reliza.plugins.reliza;

import java.io.IOException;
import java.time.Instant;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

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

/**
 * Defines the addRelease function to be used within the reliza wrapper to make an api call to reliza hub.
 */
public class RelizaBuilder extends Builder implements SimpleBuildStep {
    String artId;
    
    /**
     * Sets up the required parameters from buildwrapper initialization.
     * @param artId - Artifact identifier for labelling what your artifact should be called.
     */
    @DataBoundConstructor
    public RelizaBuilder(String artId) {
        this.artId = artId;
    }

    /**
     * Extracts project details from environment variables to send release metadata to reliza hub.
     */
    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        FlagsBuilder flagsBuilder = Flags.builder().apiKeyId(envVars.get("RELIZA_API_USR"))
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
                .artDigests(envVars.get("DOCKER_SHA_256"));
        if (envVars.get("URI") != null) {flagsBuilder.baseUrl(envVars.get("URI"));}
        Flags flags = flagsBuilder.build();
        Library library = new Library(flags);
        listener.getLogger().println("sending release metadata"); 
        library.addRelease();
    }

    @Symbol("addRelease")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }
    }
}
