/*
 * #%L
 * ACS AEM Samples
 * %%
 * Copyright (C) 2015 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package com.common.ccupurge;

import java.io.IOException;
import java.util.ArrayList;
import java.io.File;
import org.apache.felix.scr.annotations.*;
import org.json.simple.parser.ParseException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
    label = "Akamai CCU Purge",
    description = "Akamai Purge Cron Job Details",
    immediate = true, // Load immediately
    metatype=true
)
@org.apache.felix.scr.annotations.Properties(value = {
    @Property(
            label = "Cron expression defining when this Scheduled Service will run",
            description = "[every minute = 0 * * * * ?] Visit www.cronmaker.com to generate cron expressions.",
            name = "scheduler.expression",
            value = "0 * * * * ?"
    ),
    @Property(
            label = "Allow concurrent executions",
            description = "Allow concurrent executions of this Scheduled Service. This is almost always false.",
            name = "scheduler.concurrent",
            propertyPrivate = true,
            boolValue = false
    )
})

@Service
public class PurgeCronJob implements Runnable {
    private final Logger log = LoggerFactory.getLogger(PurgeCronJob.class);

    @Property(name="userName",label="Username",description="Akamai CCU User Name")
    private String userName;
    @Property(name="password",label="Password",description="Akamai CCU Password")
    private String password;
    @Property(name = "purgeDomain",label = "Environment",description = "Mention environment for the purge request",value = "staging")
    private PurgeDomain purgeDomain;
    @Property(name = "eMail", label = "E-Mail", description = "Mail will be triggered to this E-Mail after successful purge")
    private String eMail;
    
    
    @Override
    public void run() {
        String fileName = "/tmp/filelist.txt";//change this file name for windows
        String fileNameDone = "/tmp/filelist_done.txt";  //change this file name for windows
        String toMail = eMail;
        String fromMail = "admin@somemail.com";
        if(fromMail.isEmpty() || fromMail == null){
            fromMail = "admin@somemail.com";
        }
        ArrayList<String> fileLists = null;
        SendRequest request = new SendRequest();
        File purgeList = new File(fileName);
        fileLists = request.getFiles(fileName);
        if(fileLists.isEmpty()){
            log.info("Nothing to purge");
        }
        else{
            log.info("PurgeDomain {}",purgeDomain.toString());
            log.info("UserName {}",userName);
            log.info("Password {}",password);
            String result = request.executeCommand(fileLists,purgeDomain.toString().toLowerCase(),userName,password);            
            log.info(result);
            PurgeResponse response = null;
            log.info("here");
            try {
                response = new PurgeResponse(result);
                log.info("response.isSuccess() {}",response.isSuccess());
                if(response.isSuccess()){
                    log.info("The response object was successfully created");
                    boolean emailStatus = sendSuccessEmail(toMail,fromMail,response,null,purgeList);
                    if(!emailStatus)
                        log.info("Email not sent.");
                    if(request.putToFile(fileLists,fileNameDone)){
                        if(request.removeList(fileName)){
                            log.info("DONE!");
                        }
                    }
                }
            } catch (ParseException ex) {
                log.error("JSON Parse error {}",ex.getMessage());
                request.sendFailureMail(ex.getMessage());
            } catch (IOException ex) {
                log.error("IO Exception: {}", ex);
                request.sendFailureMail(ex.getMessage());
            }            
        }
    }
    @Activate
    public void activate(ComponentContext context){
        setUserName((String) context.getProperties().get("userName"));
        setPassword((String) context.getProperties().get("password"));
        setPurgeDomain((String) context.getProperties().get("purgeDomain"));
        setEMail((String) context.getProperties().get("eMail"));
    }
    
    private void setPurgeDomain(String purgeDomain){
        this.purgeDomain = PurgeDomain.valueOf(purgeDomain.toUpperCase());
    }
    
    private void setUserName(String userName){
        this.userName = userName;
    }
    
    private void setPassword(String password){
        this.password = password;
    }
    
    private void setEMail(String eMail){
        this.eMail = eMail;
    }
    public boolean sendSuccessEmail(String emailTo, String emailFrom, PurgeResponse response,String response1, File purgeList) {
       /*
		Send success mail here.
	*/
        return true;
    }
}