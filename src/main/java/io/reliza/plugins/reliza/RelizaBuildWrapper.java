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
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.tasks.SimpleBuildWrapper;
import reliza.java.client.Flags;
import reliza.java.client.Library;
import reliza.java.client.responses.ProjectVersion;

/**
 * An extension of {@link SimpleBuildWrapper} which sets up the reliza wrapper to perform
 * the api call to reliza hub.
 */
public class RelizaBuildWrapper extends SimpleBuildWrapper {
    private String projectId;
    private String uri;
    
    /**
     * Sets up required parameters from buildwrapper initialization (currently no required parameters).
     */
    @DataBoundConstructor
    public RelizaBuildWrapper() {
        super();
    }
    
    /**
     * Sets up optional parameters from buildwrapper initialization.
     * @param projectId - Project UUID obtainable from reliza hub.
     */
    @DataBoundSetter public void setProjectId(String projectId) {this.projectId = projectId;}
    
    /**
     * Sets up optional parameters from buildwrapper initialization.
     * @param uri - Base uri of api call, default set to "https://app.relizahub.com".
     */
    @DataBoundSetter public void setUri(String uri) {this.uri = uri;}
    
    /**
     * {@inheritDoc} <p>
     * Retrieves preset credentials and parameters to perform getVersion api call and then sets
     * them as environment variables to perform subsequent addRelease call
     */
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
                    .branch("ho").build();
            if (uri != null) {flags.setBaseUrl(uri);}  
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
    
    /**
     * String to UUID converter which handles conversion errors.
     * @param projectId - Project UUID.
     * @param listener - TaskListener to log specific error.
     * @return Corresponding UUID if conversion succeeded and null otherwise.
     */
    public static UUID UUID(String projectId, TaskListener listener) {
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
}
