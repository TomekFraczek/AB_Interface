package nomad.controller;

import org.json.JSONObject;

import javax.servlet.http.HttpSession;

public class CreateController extends PostController {

    /** Create an object containing the newData in the specified table */
    public JSONObject doCreate(HttpSession session, String tableName, JSONObject newData) {

        String realmId = getRealmID(session);
        String urlBegin = oAuth2Configuration.getAccountingAPIHost();
        String endpoint =  String.format("%s/v3/company/%s/%s?minorversion=4", urlBegin, realmId, tableName);

        return doPostRequest(session, endpoint, newData);
    }

}
