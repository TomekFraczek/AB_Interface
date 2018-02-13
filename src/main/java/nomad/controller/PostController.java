package nomad.controller;

import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.http.HttpSession;

public class PostController  extends RequestController {

    private static final Logger logger = Logger.getLogger(PostController.class);

    /** Performs a GET request to execute a query, and returns the result as a JSONObject */
    protected JSONObject doPostRequest(HttpSession session, String postEndpoint) {

        // Turn the endpoint into a valid HTTP GET request
        HttpPost postRequest = new HttpPost(postEndpoint);
        postRequest.setHeader("Accept", "application/json");
        String accessToken = (String)session.getAttribute("access_token");
        postRequest.setHeader("Authorization","Bearer " + accessToken);

        return doRequest(session, postRequest);
    }
}
