package io.reliza.plugins.sample;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.envinject.EnvInjectPluginAction;
import org.jenkinsci.plugins.envinject.util.RunHelper;
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
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars envVars, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
    	if (useFrench) {
            listener.getLogger().println("Bonjour, " + name + "!");
        } else {
            listener.getLogger().println("Hello, " + name + "!");
        }
    	// envVars = run.getEnvironment(listener);
 //   	envVars.entrySet().forEach(entry -> {
 //   		listener.getLogger().print(entry.getKey() + " = " + entry.getValue() + "\n");
 //   	});

    	// Node n = run.getExecutor().getOwner().getNode();
    	// listener.getLogger().println("node = " + n.getDisplayName() + "\n");
    	// Computer c = n.toComputer();
    	
    	// run.getExecutor().getOwner().
    	envVars.put("PAVEL_VAR", "Hi From Reliza!!!");
    	
    	printEnvVars(envVars, listener, "default from perfrom signature");
    	Computer c = run.getExecutor().getOwner();
    	envVars = c.getEnvironment();
    	printEnvVars(envVars, listener, "computer with get env");
    	envVars = c.buildEnvironment(listener);
    	printEnvVars(envVars, listener, "computer with build env");
    	envVars = run.getEnvironment(listener);
    	printEnvVars(envVars, listener, "run from listener");
    	
    	
    	// envVars = c.getEnvironment();

    	envVars.put("PAVEL_VAR", "Hi From Reliza!!!");
    	// EnvInjectPluginAction envInjectAction = new EnvInjectPluginAction(envVars);
    	// c.addOrReplaceAction(envInjectAction);
    	EnvInjectPluginAction envInjectAction = run.getAction(EnvInjectPluginAction.class);
    	if (envInjectAction == null) {
    		listener.getLogger().println("null envinjectaction \n");
    		envInjectAction = new EnvInjectPluginAction(envVars);
    		run.addAction(envInjectAction);
    	}
    	envInjectAction.overrideAll(RunHelper.getSensitiveBuildVariables(run), envVars);
    	
    	envVars = run.getEnvironment(listener);
    	EnvVars env = run.getEnvironment(listener);
    	env.put("PAVEL_VAR", "Hi From Reliza!!!");
    	envVars.overrideAll(env);
    	// n.toComputer().addOrReplaceAction(a)
//    	EnvInjectActionSetter envInjectActionSetter = new EnvInjectActionSetter(n.getRootPath());
//    	try {
//			envInjectActionSetter.addEnvVarsToRun(run, envVars);
//		} catch (EnvInjectException | IOException | InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
 //   	run.getAllActions().forEach(a -> {
 //   		listener.getLogger().println(a.toString() + "\n");
 //   	});
    	
    	/*
    	//run.getAction(EnvInjectPluginAction.class);
    	//run.
        // EnvInjectActionSetter envInjectActionSetter = new EnvInjectActionSetter(getNodeRootPath());
        //  envInjectActionSetter.addEnvVarsToRun(run, envVars);
        	 EnvInjectPluginAction envInjectAction = run.getAction(EnvInjectPluginAction.class);
        	 // 
        	 if (null == envInjectAction) {
        		 listener.getLogger().println("Env Inject Action is null");
                 envInjectAction = new EnvInjectPluginAction(envVars);
                 run.addOrReplaceAction(envInjectAction);
                 // run.getAllActions().forEach(a -> {
                //	 listener.getLogger().println(a.toString() + "\n");
                 // });
                // run.getParent().getAllActions().forEach(a -> {
                //	 listener.getLogger().println(a.toString() + "\n");
                // });
                 // Node n = getNode();
                 Node n = run.getExecutor().getOwner().getNode();
                 listener.getLogger().println("node = " + n.getDisplayName() + "\n");
                 
                 envInjectAction = run.getAction(EnvInjectPluginAction.class);
                 envInjectAction.overrideAll(RunHelper.getSensitiveBuildVariables(run), envVars);
                 
        	 } else {
        		 envInjectAction.overrideAll(RunHelper.getSensitiveBuildVariables(run), envVars);
        	 }
        */	 
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
    
    private void printEnvVars(EnvVars envVars, TaskListener listener, String type) {
    	listener.getLogger().println("--------------- Printing env vars for " + type + "! \n");
    	envVars.entrySet().forEach(entry -> {
    		listener.getLogger().print(entry.getKey() + " = " + entry.getValue() + "\n");
    	});
    	listener.getLogger().println("--------------- End of env vars! \n");
    }

}
