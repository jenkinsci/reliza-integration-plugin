package io.reliza.plugins.reliza;

import java.io.IOException;

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

public class RelizaBuilder extends Builder implements SimpleBuildStep {

    @DataBoundConstructor
    public RelizaBuilder() {
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        FlagsBuilder flagsBuilder = Flags.builder().apiKeyId(envVars.get("API_KEY_ID"))
                .apiKey(envVars.get("API_KEY"))
                .version(envVars.get("VERSION"))
                .projectId(RelizaBuildWrapper.UUID(envVars.get("PROJECT_ID"), listener))
                .branch("ho");
        if (envVars.get("URI") != null) {flagsBuilder.baseUrl(envVars.get("URI"));}
        Flags flags = flagsBuilder.build();
        Library library = new Library(flags);
        listener.getLogger().println(library.addRelease());
    }

    @Symbol("getProjectMetadata")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }
    }
}
