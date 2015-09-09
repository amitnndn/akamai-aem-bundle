package com.common.ccupurge;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * PurgeStatus -
 *
 * @author Sebastien Bernard
 */

public class PurgeResponse {
	public Long httpStatus;
	public String detail;
	public Long estimatedSeconds;
	public String purgeId;
	public String progressUri;
	public Long pingAfterSeconds;
	public String supportId;
        
        public PurgeResponse(String response) throws ParseException{
            JSONObject jsonResult = (JSONObject)new JSONParser().parse(response);
            
            
            this.httpStatus = (Long) jsonResult.get("httpStatus");
            this.detail = (String) jsonResult.get("detail");
            if(this.httpStatus == 201){
                this.estimatedSeconds = (Long) jsonResult.get("estimatedSeconds");
                this.purgeId = (String) jsonResult.get("purgeId");
                this.progressUri = (String) jsonResult.get("progressUri");
                this.pingAfterSeconds = (Long) jsonResult.get("pingAfterSeconds");
                this.supportId = (String) jsonResult.get("supportId");
            }
        }

	boolean isSuccess() {
		if(this.httpStatus == 201){
                    return true;
                }
                return false;
	}
}
