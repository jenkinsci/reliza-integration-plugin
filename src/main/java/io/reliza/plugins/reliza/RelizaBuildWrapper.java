package io.reliza.plugins.reliza;

import java.io.IOException;
import java.util.UUID;

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
    private final String projectId;
    private final String uri;
    
    @DataBoundConstructor
    public RelizaBuildWrapper(String projectId, String uri) {
        super();
        this.projectId = projectId;
        this.uri = uri;
    }
    
    @Override
     public void setUp(Context context, Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
            // If this does not require a workspace, defer to the version that does not take a workspace and launcher.
            if (!this.requiresWorkspace()) {
                this.setUp(context, build, listener, initialEnvironment);
                return;
            }
            listener.getLogger().println("setting up reliza context wrapper \n");
            String apiKeyId;
            String apiKey;
            if (initialEnvironment.get("PROJECT_API_ID") != null && initialEnvironment.get("PROJECT_API_KEY") != null) {
                apiKeyId = initialEnvironment.get("PROJECT_API_ID");
                apiKey = initialEnvironment.get("PROJECT_API_KEY");
            } else {
                apiKeyId = initialEnvironment.get("ORG_API_ID");
                apiKey = initialEnvironment.get("ORG_API_KEY"); 
            }
            
            Flags flags = Flags.builder().apiKeyId(apiKeyId)
                    .apiKey(apiKey)
                    .projectId(UUID(projectId, listener))
                    .baseUrl(uri)
                    .branch("ho").build();
            Library library = new Library(flags);
            ProjectVersion projectVersion = library.getVersion();
            context.env("VERSION", projectVersion.getVersion());
            context.env("URI", uri);
            context.env("PROJECT_ID", projectId);
            context.env("API_KEY_ID", apiKeyId);
            context.env("API_KEY", apiKey);
            
            
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
    
    public static UUID UUID(String projectId, TaskListener listener) {
        try {
            if (projectId == null || projectId.isBlank()) {
                return null;
            } else {
                return UUID.fromString(projectId);
            }
        } catch (IllegalArgumentException e) {
            listener.getLogger().println(e);
            return null;
        }
    }
}
