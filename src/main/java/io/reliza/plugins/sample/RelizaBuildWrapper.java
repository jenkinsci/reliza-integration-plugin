package io.reliza.plugins.sample;

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
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.tasks.SimpleBuildWrapper;

public class RelizaBuildWrapper extends SimpleBuildWrapper {
	
    @DataBoundConstructor
    public RelizaBuildWrapper() {
    	super();
    }
	
	@Override
	 public void setUp(Context context, Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
	        // If this does not require a workspace, defer to the version that does not take a workspace and launcher.
	        if (!this.requiresWorkspace()) {
	            this.setUp(context, build, listener, initialEnvironment);
	            return;
	        }
	        listener.getLogger().println("setting up reliza context wrapper \n");
	        context.env("PAVEL_VAR", "pavel var from context");
	        
	        
	        // throw new AbstractMethodError("Unless a build wrapper is marked as not requiring a workspace context, you must implement the overload of the setUp() method that takes both a workspace and a launcher.");
	    }
	
	@Symbol("rwrap")
    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {
		
		public DescriptorImpl() {
            super(RelizaBuildWrapper.class);
            load();
        }
		
        @Override
        public String getDisplayName() {
            return "reliza wrapper";
        }

        @Override
        public boolean isApplicable(final AbstractProject<?, ?> item) {
          return true;
        }
    }
}
