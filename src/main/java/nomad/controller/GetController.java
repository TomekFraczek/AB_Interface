package nomad.controller;

import nomad.domain.BearerTokenResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.http.HttpSession;

public class GetController extends RequestController {


    private static final Logger logger = Logger.getLogger(GetController.class);

    /** Performs a GET request to execute a query, and returns the result as a JSONObject */
    protected JSONObject doGetRequest(HttpSession session, String queryEndpoint) {

        String failureMsg="Failed";

        // Turn the endpoint into a valid HTTP GET request
        HttpGet getRequest = new HttpGet(queryEndpoint);
        getRequest.setHeader("Accept", "application/json");
        String accessToken = (String)session.getAttribute("access_token");
        getRequest.setHeader("Authorization","Bearer " + accessToken);

        try {
            HttpResponse response = CLIENT.execute(getRequest);

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
                getRequest.setHeader("Authorization","Bearer " + bearerTokenResponse.getAccessToken());
                response = CLIENT.execute(getRequest);
            }

            // Pass up a serious error (200 type) so it can be displayed
            if (response.getStatusLine().getStatusCode() != 200){
                logger.info("failed getting companyInfo");
                return new JSONObject().put("response",failureMsg);
            }

            // Extract (and log) the query result from the HTTP response
            StringBuffer result = httpHelper.getResult(response);
            logger.debug("raw result for query= " + result);
            // Apparently trying to build a JSONObject directly from a StringBuffer results in an empty object
            return new JSONObject(new String(result));

            // Catch any unexpected exceptions and pass them up to the log and for display
        } catch (Exception ex) {
            logger.error("Exception while getting company info ", ex);
            return new JSONObject().put("response", failureMsg);
        }
    }

}
