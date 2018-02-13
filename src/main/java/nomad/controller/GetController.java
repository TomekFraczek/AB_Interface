package nomad.controller;


import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.http.HttpSession;

public class GetController extends RequestController {


    private static final Logger logger = Logger.getLogger(GetController.class);

    /** Performs a GET request to execute a query, and returns the result as a JSONObject */
    protected JSONObject doGetRequest(HttpSession session, String getEndpoint) {

        // Turn the endpoint into a valid HTTP GET request
        HttpGet getRequest = new HttpGet(getEndpoint);

        getRequest.setHeader("Accept", "application/json");
        String accessToken = (String)session.getAttribute("access_token");
        getRequest.setHeader("Authorization","Bearer " + accessToken);

        return doRequest(session, getRequest);
    }

}
