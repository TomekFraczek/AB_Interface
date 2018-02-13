package nomad.controller;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class ReadController extends GetController {

    private static final Logger logger = Logger.getLogger(ReadController.class);

    @ResponseBody
    @RequestMapping("/readTest")
    public String doTestRead(HttpSession session) {
        logger.debug("Performing test read of invoice with id=16");
        return doRead(session, "invoice",16).toString();
    }

    /** Get a single object with the given id from the given table */
    public JSONObject doRead(HttpSession session, String tableName, int id) {


        String realmId = getRealmID(session);
        String urlBegin = oAuth2Configuration.getAccountingAPIHost();
        String table = tableName.toLowerCase();     //Enforce all lowercase
        String endpoint =  String.format("%s/v3/company/%s/%s/%s?minorversion=4", urlBegin, realmId, table, id);

        // Leaving this out of return statement for readability and convenience
        JSONObject result = doGetRequest(session, endpoint);

        return result;
    }
}
