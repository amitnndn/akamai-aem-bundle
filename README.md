AEM Project Bundle for Akamai Purge using Akamai Purge API. 

Steps to set up: 
1. Make sure you have maven and java installed in your system. 
2. Execute the command "mvn clean install"
3. The target directory is created with the executable jar file in it. 
4. Install the OSGI bundle in your OSGI console @ http://{aem-author-url}:{port-number}/system/console/
5. After installing the bundle, install "json-simple-1.1.1.jar" file through the same console. 
6. Enter the required content in the configuration tab of the bundle.