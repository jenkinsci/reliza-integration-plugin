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
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.tasks.SimpleBuildWrapper;
import reliza.java.client.Flags;
import reliza.java.client.Library;
import reliza.java.client.responses.ProjectVersion;

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
	        Flags flags = Flags.builder().apiKeyId("PROJECT__6ba5691c-05e3-4ecd-a45a-18b382419f40")
	                .apiKey("0828b0fabf663fc17a604b527992965ee2abeb4831319125f1692d9ec111ea078dcc8261ed0b9aaf353ce2d003b823b7")
	                .branch("ho").baseUrl("https://test.relizahub.com").build();
            Library library = new Library(flags);
            ProjectVersion projectVersion = library.getVersion();
	        context.env("VERSION", projectVersion.getVersion());
	        
	        
	        // throw new AbstractMethodError("Unless a build wrapper is marked as not requiring a workspace context, you must implement the overload of the setUp() method that takes both a workspace and a launcher.");
	    }
	
	@Symbol("reliza")
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
