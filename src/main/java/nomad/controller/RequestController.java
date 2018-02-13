package nomad.controller;

import nomad.domain.BearerTokenResponse;
import nomad.domain.OAuth2Configuration;
import nomad.helper.HttpHelper;
import nomad.service.RefreshTokenService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;
import java.io.IOException;

public class RequestController {

    @Autowired
    public OAuth2Configuration oAuth2Configuration;

    @Autowired
    public HttpHelper httpHelper;

    @Autowired
    public RefreshTokenService refreshTokenService;

    protected static final HttpClient CLIENT = HttpClientBuilder.create().build();

    private static final Logger logger = Logger.getLogger(RequestController.class);

    /** Extract the realmID from the current HttpSession */
    protected String getRealmID(HttpSession session) {

        //Ideally you would fetch the realmId and the accessToken from the data store based on the user account here.
        String realmId = (String)session.getAttribute("realmId");
        if (StringUtils.isEmpty(realmId)) {
            return new JSONObject().put("response","No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
        }
        return realmId;

    }

    /** Perform the entire HttpRequest, returning a JSONObject of the response */
    protected JSONObject doRequest(HttpSession session, HttpRequestBase request) {
        try {
            // Perform the request and return the resultant response as a JSON object
            HttpResponse response = getResponse(session, request);
            return extractResult(response);

        } catch (Exception ex) {
            // Catch any unexpected exceptions and pass them up to the log and for display
            logger.error("Exception while getting company info ", ex);
            return new JSONObject().put("response", "Unexpected failure. See console");
        }
    }

    /** Execute the request, trying again with refreshed tokens if they expired. Return the received HttpResponse */
    protected HttpResponse getResponse(HttpSession session, HttpRequestBase request) throws Exception {

        HttpResponse response = CLIENT.execute(request);

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
            request.setHeader("Authorization","Bearer " + bearerTokenResponse.getAccessToken());
            response = CLIENT.execute(request);
        }

        return response;
    }

    /** Extract the response body as a JSONObject, catching a 200 type response */
    protected JSONObject extractResult(HttpResponse response) throws IOException {

        JSONObject result;

        // Pass up a serious error (200 type) so it can be displayed
        if (response.getStatusLine().getStatusCode() != 200){
            logger.info("failed getting companyInfo");
            result = new JSONObject().put("response", "Failed 200 type");
        } else {
            // Extract (and log) the query result from the HTTP response
            StringBuffer resultStr = httpHelper.getResult(response);
            logger.debug("raw result for query= " + resultStr);
            // Apparently trying to build a JSONObject directly from a StringBuffer results in an empty object
            result = new JSONObject(new String(resultStr));
        }

        return result;
    }

}
