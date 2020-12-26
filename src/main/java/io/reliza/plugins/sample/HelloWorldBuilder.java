package io.reliza.plugins.sample;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;

import org.jenkinsci.Symbol;
import org.jenkinsci.lib.envinject.EnvInjectException;
import org.jenkinsci.plugins.envinject.service.EnvInjectActionSetter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;

public class HelloWorldBuilder extends Builder implements SimpleBuildStep {

    private final String name;
    private boolean useFrench;

    @DataBoundConstructor
    public HelloWorldBuilder(String name) {
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
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
    	if (useFrench) {
            listener.getLogger().println("Bonjour, " + name + "!");
        } else {
            listener.getLogger().println("Hello, " + name + "!");
        }
    	EnvVars envVars = new EnvVars();
    	envVars = run.getEnvironment(listener);
    	envVars.entrySet().forEach(entry -> {
    		listener.getLogger().print(entry.getKey() + " = " + entry.getValue() + "\n");
    	});
    	envVars.put("PAVEL_VAR", "Hi From Reliza!!!");
    	run.getAllActions().forEach(a -> {
    		listener.getLogger().println(a.toString() + "\n");
    	});
    	//run.getAction(EnvInjectPluginAction.class);
    	//run.
        EnvInjectActionSetter envInjectActionSetter = new EnvInjectActionSetter(getNodeRootPath());
        try {
            envInjectActionSetter.addEnvVarsToRun(run, envVars);
        } catch (EnvInjectException | IOException | InterruptedException e) {
            // logger.error("SEVERE ERROR occurs: " + e.getMessage());
            throw new Run.RunnerAbortedException();
        }
    	listener.getLogger().println("First entry = " + envVars.firstEntry().getKey() + ", " + envVars.firstEntry().getValue());
    	listener.getLogger().println("Env var = " + run.getEnvironment(listener).get("PAVEL_VAR"));
    }

    @Symbol("greet")
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
    
    @CheckForNull
    private Node getNode() {
        Computer computer = Computer.currentComputer();
        if (computer == null) {
            return null;
        }
        return computer.getNode();
    }
    
    @CheckForNull
    private FilePath getNodeRootPath() {
        Node node = getNode();
        if (node != null) {
            return node.getRootPath();
        }
        return null;
    }

}
