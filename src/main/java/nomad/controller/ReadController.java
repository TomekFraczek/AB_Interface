package nomad.controller;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

public class ReadController extends GetController {

    @ResponseBody
    @RequestMapping("/queryTest")
    public String doTestRead(HttpSession session) {
        return doRead(session, "Invoice",16).toString();
    }

    /** Get a single object with the given id from the given table */
    public JSONObject doRead(HttpSession session, String tableName, int id) {


        String realmId = getRealmID(session);
        String urlBegin = oAuth2Configuration.getAccountingAPIHost();
        String endpoint =  String.format("%s/v3/company/%s/%s/%s?minorversion=4", urlBegin, realmId, tableName, id);

        // Leaving this out of return statement for readability and convenience
        JSONObject result = doGetRequest(session, endpoint);

        return result;
    }
}
