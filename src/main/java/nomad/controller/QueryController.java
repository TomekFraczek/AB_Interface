package nomad.controller;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author dderose
 *
 */
@RestController
public class QueryController  extends GetController {

    private static final Logger logger = Logger.getLogger(QueryController.class);

    @ResponseBody
    @RequestMapping("/getCompanyInfo")
    public String doTestQuery(HttpSession session) {
        return this.doQuery(session, "Invoice", "").toString();
    }

    /** Main method to preform and execute Queries */
    public JSONObject doQuery(HttpSession session, String tableName, String condition) {

        String realmId = getRealmID(session);

        // Prepare the URL for the get request
        String queryEndpoint = getEndpoint(realmId, tableName, condition);

        // Get the result of the query request
        JSONObject result = doGetRequest(session, queryEndpoint);

        return result;
    }

    private String getEndpoint(String realmId, String tableName, String condition){
        // All query endpoints end with this version specification
        String queryEnd = "&minorversion=4";

        // Encode the condition into a valid url segment
        String urlCondition;
        try {
            urlCondition = URLEncoder.encode(condition, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // This should never happen, since the encoding is hardcoded in
            System.out.println("UnsupportedEncodingException in QueryController. Executing query w/o condition");
            urlCondition = "";
        }

        // Assemble the query section of the endpoint
        String query = "query?query=select%20%2a%20from%20" + tableName + urlCondition + queryEnd;

        // Put together and return the endpoint from the above data
        return String.format("%s/v3/company/%s/%s", oAuth2Configuration.getAccountingAPIHost(), realmId, query);
    }

}
