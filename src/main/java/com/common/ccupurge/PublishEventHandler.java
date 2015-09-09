package com.common.ccupurge;

import com.day.cq.replication.ReplicationAction;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.event.EventUtil;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.io.BufferedWriter;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * AkamaiEventHandler - Listen to repository replication notification and log all files to be invalidated
 *
 * @author Amit Periyapatna
 */
@Component(label = "Akamai Publish Logger", description = "Listen to repository replication notification and log all files to be invalidated", metatype = false, immediate = true)
@Service
@org.apache.felix.scr.annotations.Properties(value = {
	@Property(name = EventConstants.EVENT_TOPIC, value = ReplicationAction.EVENT_TOPIC, label = "Event topic"),
	@Property(name = "pathsHandled", value = "/content", label = "Handled paths", description = "Multiple paths to be comma (,) saperated"),
        @Property(name = "rootSiteUrl", value = "https://m.webex.com,http://m.webex.com", label = "Root site url", description = "URL to be appended with the path. Split the http and https URLs with a comma (,)")
})
public class PublishEventHandler implements EventHandler {
	private final Logger LOG = LoggerFactory.getLogger(PublishEventHandler.class);
     
        public static final String PATHS = "paths";
        
        private String pathsHandled;
        
        private String rootSiteUrl;

	@Override
	public void handleEvent(Event event) {
            if (EventUtil.isLocal(event)) {
                    LOG.debug("Start handling event to add Akamai job");
                    String[] paths = PropertiesUtil.toStringArray(event.getProperty(PATHS));
                    Set<String> validPaths = filterValidPath(paths);

                    if (!validPaths.isEmpty()) {				
                            Set<String> absoluteUrls = prependPathWithRootUrls(validPaths);                                
                            logUrls(absoluteUrls);  
                            boolean response = addToFile(absoluteUrls);
                            if(response){
                                LOG.debug("Added to files succesfully");
                            }	
                    } else {
                            LOG.debug("{} has no valid path(s) to purge. No Job added");
                    }
                    LOG.debug("Akamai Event Finished");
            }
	}
        /**
	 * Log all the paths that are to be purged
	 * @param objects the list of objects
	 * @return void
	 */
        private void logUrls(Set<String> pathsToPurge) {
		if (LOG.isInfoEnabled()) {
			LOG.info("Path(s) to purge:");
			for (String path : pathsToPurge) {
				LOG.info(path);
			}
		}
	}
         /**
	 * Check if the path published is in the list of paths to be purged
	 * @param objects the list of objects
	 * @return a ordered set of objects
	 */
	private Set<String> filterValidPath(String[] paths) {
		Set<String> validPaths = new HashSet<>();
                String[] pathHandled = pathsHandled.trim().split(",");
		for (String path : paths) {
			if (!path.isEmpty()) {
				for (String validPath : pathHandled) {
					if (path.startsWith(validPath)) {
						validPaths.add(path);
					}
				}
			}
		}
		return validPaths;
                
	}        
         /**
	 * Prepend the paths with the URL
	 * @return object prepended with rootSiteURL
	 */
        public Set<String> prependPathWithRootUrls(Set<String> paths) {
            if (rootSiteUrl.isEmpty()) {
                    return paths;
            }
            String[] rootSiteUrls = rootSiteUrl.trim().split(",");
            Set<String> urls = new HashSet<String>(paths.size());
            for (String siteUrl : rootSiteUrls){
                for (String path : paths) {
                    if (!path.startsWith(siteUrl)) {
                            urls.add(siteUrl.concat(path));
                    }
                }
            }
            return urls;
	}
         /**
	 * Add all the URLs to be purged into a file
	 * @param objects the list of objects
	 * @return void
	 */
        private boolean addToFile(Set<String> Objects){
            Collection<String> uniqueObjects = removeDuplicate(Objects);
            boolean status = true;
            try{
                File file = new File("/tmp/filelist.txt");//Change this if a windows system.
                if(!file.exists()){
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
                BufferedWriter bw = new BufferedWriter(fw);
                for(String eachItem : Objects){
                    bw.write(eachItem);
                    bw.newLine();
                }
                bw.close();
            }
            catch(IOException ex){
                LOG.error("Error Writing into file {}",ex.getMessage());
                status = false;
            }
            return status;
        }
        /**
	 * Remove null and duplicates but keep given ordering
	 * @param objects the list of objects
	 * @return a ordered set of objects
	 */
	private Collection<String> removeDuplicate(Collection<String> objects) {
            objects.removeAll(Collections.singleton(null));
            List<String> list2 = new ArrayList<String>();
            HashSet<String> lookup = new HashSet<String>();
            for (String item : objects) {
                if (lookup.add(item)) {
                    // Set.add returns false if item is already in the set
                    list2.add(item);
                }
            }
            objects = list2;
            return objects;
	}
        
	
	protected void activate(ComponentContext context) {
            pathsHandled = PropertiesUtil.toString(context.getProperties().get("pathsHandled"),"");
            rootSiteUrl = PropertiesUtil.toString(context.getProperties().get("rootSiteUrl"), "");
	}

	
	protected void deactivate() {
		
	}
}
