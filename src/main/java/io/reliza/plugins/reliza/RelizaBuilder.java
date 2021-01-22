package io.reliza.plugins.reliza;

import java.io.IOException;

import javax.servlet.ServletException;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.reliza.plugins.sample.Messages;
import jenkins.tasks.SimpleBuildStep;
import reliza.java.client.Flags;
import reliza.java.client.Library;

public class RelizaBuilder extends Builder implements SimpleBuildStep {

    private final String name;
    private boolean useFrench;

    @DataBoundConstructor
    public RelizaBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isUseFrench() {
        return useFrench;
    }

    @DataBoundSetter
    public void setUseFrench(boolean useFrench) {
        this.useFrench = useFrench;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
    	if (useFrench) {
            listener.getLogger().println("Bonjour, " + name + "!");
        } else {
            listener.getLogger().println("Hello, " + name + "!");
        }
        Flags flags = Flags.builder().apiKeyId("PROJECT__6ba5691c-05e3-4ecd-a45a-18b382419f40")
                .apiKey("0828b0fabf663fc17a604b527992965ee2abeb4831319125f1692d9ec111ea078dcc8261ed0b9aaf353ce2d003b823b7")
                .branch("ho")
                .version(envVars.get("VERSION"))
                .baseUrl("https://test.relizahub.com").build();
        Library library = new Library(flags);
    	listener.getLogger().println(library.addRelease());
    }

    @Symbol("relizagreet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value, @QueryParameter boolean useFrench)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingName());
            if (value.length() < 4)
                return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_tooShort());
            if (!useFrench && value.matches(".*[éáàç].*")) {
                return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_reallyFrench());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.HelloWorldBuilder_DescriptorImpl_DisplayName();
        }

    }
}
