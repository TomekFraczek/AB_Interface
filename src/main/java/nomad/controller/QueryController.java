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
    @RequestMapping("/queryTest")
    public String doTestQuery(HttpSession session) {
        logger.debug("Performing test of query (getting all invoices)");
        return this.doQuery(session, "Invoice", "").toString();
    }

    /**
     *  Main method to preform and execute Queries
     * @param condition SQL syntax condition (what comes after 'where' in an SQL statement
     * */
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

        String table = tableName.toLowerCase();  //Enforce all lowercase

        // If the given condition is not empty, then append a 'where' and the URL encoding of the condition to the URI
        String fullCondition = "";
        try {
            if (!condition.equals("")) {
                fullCondition = "%20where%20" + URLEncoder.encode(condition, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            // This should never happen, since the encoding is hardcoded in
            System.out.println("UnsupportedEncodingException in QueryController. Executing query w/o condition");
            fullCondition = "";
        }

        // Assemble the query section of the endpoint
        String query = "query?query=select%20%2a%20from%20" + table + fullCondition + queryEnd;

        // Put together and return the endpoint from the above data
        return String.format("%s/v3/company/%s/%s", oAuth2Configuration.getAccountingAPIHost(), realmId, query);
    }

}
