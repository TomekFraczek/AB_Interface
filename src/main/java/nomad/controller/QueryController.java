package nomad.controller;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import nomad.domain.BearerTokenResponse;
import nomad.domain.OAuth2Configuration;
import nomad.helper.HttpHelper;
import nomad.service.RefreshTokenService;
import nomad.JSONWriter;

/**
 * @author dderose
 *
 */
@RestController
public class QueryController {
    
    @Autowired
    public OAuth2Configuration oAuth2Configuration;
    
    @Autowired
    public HttpHelper httpHelper;
    
    @Autowired
    public RefreshTokenService refreshTokenService;
    
    private static final HttpClient CLIENT = HttpClientBuilder.create().build();
    private static final Logger logger = Logger.getLogger(QueryController.class);
    
    /**
     * Sample QBO API call using OAuth2 tokens
     * 
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("/getCompanyInfo")
    public String callQBOCompanyInfo(HttpSession session) {
        
        //Ideally you would fetch the realmId and the accessToken from the data store based on the user account here.
        String realmId = (String)session.getAttribute("realmId");
        if (StringUtils.isEmpty(realmId)) {
            return new JSONObject().put("response","No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
        }

        String queryEndpoint = String.format("%s/v3/company/%s/%s", oAuth2Configuration.getAccountingAPIHost(), realmId, "query?query=select%20%2a%20from%20Invoice&minorversion=4");

        String failureMsg="Failed";

        // Create the HTTP GET request to be executed
        HttpGet queryRequest = new HttpGet(queryEndpoint);
        queryRequest.setHeader("Accept", "application/json");
        String accessToken = (String)session.getAttribute("access_token");
        queryRequest.setHeader("Authorization","Bearer " + accessToken);

        try {
            HttpResponse response = CLIENT.execute(queryRequest);

            logger.info("Response Code : "+ response.getStatusLine().getStatusCode());
            
            /*
             * Handle 401 status code - 
             * If a 401 response is received, refresh tokens should be used to get a new access token,
             * and the API call should be tried again.
             */
            if (response.getStatusLine().getStatusCode() == 401) {
                StringBuffer result = httpHelper.getResult(response);
                logger.debug("raw result for 401 companyInfo= " + result);
                
                //refresh tokens
                logger.info("received 401 during companyinfo call, refreshing tokens now");
                BearerTokenResponse bearerTokenResponse = refreshTokenService.refresh(session);
                session.setAttribute("access_token", bearerTokenResponse.getAccessToken());
                session.setAttribute("refresh_token", bearerTokenResponse.getRefreshToken());
                
                //call company info again using new tokens
                logger.info("calling companyinfo using new tokens");
                queryRequest.setHeader("Authorization","Bearer " + bearerTokenResponse.getAccessToken());
                response = CLIENT.execute(queryRequest);
            }  
            
            // Pass up a serious error (200 type) so it can be displayed
            if (response.getStatusLine().getStatusCode() != 200){
                logger.info("failed getting companyInfo");
                return new JSONObject().put("response",failureMsg).toString();
            }

            // Extract (and log) the query result from the HTTP response
            StringBuffer result = httpHelper.getResult(response);
            logger.debug("raw result for companyInfo= " + result);


            // We've hijacked the above request to get the shipments data. Now we write it out to a file for later work
            String filename = "Shipments";
            JSONWriter writer = new JSONWriter();
            writer.datedWrite(filename, result);

            return result.toString();

        // Catch any unexpected exceptions and pass them up to the log and for display
        } catch (Exception ex) {
            logger.error("Exception while getting company info ", ex);
            return new JSONObject().put("response",failureMsg).toString();
        }
    }

}
