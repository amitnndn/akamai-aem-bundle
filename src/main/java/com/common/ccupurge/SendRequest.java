package com.common.ccupurge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SendRequest {
    private final Logger log = LoggerFactory.getLogger(SendRequest.class);
    private String username = "";
    private String password = "";
    public SendRequest(){
    }
    public String executeCommand(ArrayList<String> fileList,String purgeDomain, String userName, String password) {
        URL url;
        HttpURLConnection client = null;
        String response = null;
        String request = buildDataString(fileList,purgeDomain);
        String userPassword = userName + ":" + password;
        //String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
        Base64 base64 = new Base64();
        String encoding = new String(base64.encode(userPassword.getBytes()));
        try {                                                                               
           String destination = "https://api.ccu.akamai.com/ccu/v2/queues/default";
           url = new URL(destination);                                                     
           client = (HttpURLConnection) url.openConnection();                              
           client.setDoOutput(true);                                                       
           client.setDoInput(true);                                              
           client.setRequestProperty("Authorization", "Basic " + encoding);
           client.setRequestProperty("Content-Type", "application/json");   
           client.setRequestMethod("POST");                                                
           //client.setFixedLengthStreamingMode(request.toString().getBytes("UTF-8").length);
           client.connect();                                                               

           log.debug("doInBackground(Request) {}" + request);                           

           OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream());   
           String output = request;                                             
           writer.write(output);
           //Add latency to wait for response if no response then go to exception..
           writer.flush();                                                                 
           writer.close();   
           InputStream input;
           if(client.getResponseCode() == 200 || 201 == client.getResponseCode())
             input = client.getInputStream();                                    
           else {
               input = client.getErrorStream();
               //Close the connection and E-Mail
               
           }
               
           BufferedReader reader = new BufferedReader(new InputStreamReader(input));       
           StringBuilder result = new StringBuilder();                                     
           String line;                                                                    

           while ((line = reader.readLine()) != null) {                                    
               result.append(line);                                                        
           }                                                                               
           log.debug("doInBackground(Resp) {}", result.toString());  
           
           response = result.toString();                                   
       } catch (IOException e) {
           sendFailureMail(e.getMessage());
       } finally {                                                                         
           client.disconnect();                                                            
       }
        return response;

    }
    
    private String buildDataString(ArrayList<String> list,String purgeDomain){
        String fileList = null;
        JSONObject obj = new JSONObject();
        JSONArray objectList = new JSONArray();
        for(String eachItem : list){
            objectList.add(eachItem);
        }
        obj.put("objects",objectList);
        obj.put("domain",purgeDomain);
        fileList = obj.toJSONString();
        log.info("File list {}",fileList);
        return fileList;
        
    }
    public ArrayList<String> getFiles(String fileName){
        ArrayList<String> fileLists = null;
        try{
            BufferedReader in = new BufferedReader(new FileReader(fileName));//"C:/logs/filename.txt"
           
            String line;            
            fileLists = new ArrayList<>();      
            try{
                while((line = in.readLine()) != null)
                    {
                        if(!(line.isEmpty()) || !line.equals(""))
                            fileLists.add(line);
                    }
                    in.close();
            } catch (IOException ex) {
                log.error("Unable to read file {}",fileName);
                sendFailureMail(ex.getMessage());
            }
        }
        catch(Exception e){
            log.info(e.getMessage());
            sendFailureMail(e.getMessage());
        }
        return fileLists;
    }
    public boolean putToFile(ArrayList<String> list,String fileName) throws IOException{
        boolean status = true;
        try{
            File file = new File(fileName);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);
            for(String eachItem : list){
                bw.write(eachItem);
                bw.newLine();
            }
            bw.close();
        }
        catch(IOException ex){
            log.error("Error Writing into file {}",ex.getMessage());
            sendFailureMail(ex.getMessage());
            status = false;
        }
        return status;
    }
    public boolean removeList(String fileName){
        boolean status = true;
        try{
            File file = new File(fileName);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("");
            bw.close();
        }
        catch(IOException ex){
            log.error("Error Writing into file {}",ex.getMessage());
            sendFailureMail(ex.getMessage());
            status = false;
        }
        return status;
    }
    public void sendFailureMail(String response){
        /*
			Insert your send-mail content here.
		*/
    }
}