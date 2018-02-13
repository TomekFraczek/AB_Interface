package nomad.controller;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

public class CreateController extends PostController {

    private static final Logger logger = Logger.getLogger(CreateController.class);

    @ResponseBody
    @RequestMapping("/createTest")
    public String doTestCreate(HttpSession session) {
        logger.debug("Performing create test (creating the basic minimal invoice)");
        JSONObject json = new JSONObject( "{\"Line\": [ { \"Amount\": 100.00, \"DetailType\": \"SalesItemLineDetail\", \"SalesItemLineDetail\": { \"ItemRef\": { \"value\": \"1\",\"name\": \"Services\" } } } ], \"CustomerRef\": { \"value\": \"1\" }}");
        return doCreate(session, "Invoice", json).toString();
    }

    /** Create an object containing the newData in the specified table */
    public JSONObject doCreate(HttpSession session, String tableName, JSONObject newData) {

        String realmId = getRealmID(session);
        String urlBegin = oAuth2Configuration.getAccountingAPIHost();
        String endpoint =  String.format("%s/v3/company/%s/%s?minorversion=4", urlBegin, realmId, tableName);

        return doPostRequest(session, endpoint, newData);
    }

}
