package nomad.controller;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class UpdateController extends PostController{

    @ResponseBody
    @RequestMapping("/updateTest")
    public String updateTest(HttpSession session){
        JSONObject json = new JSONObject();
        json.put("Id", "16");
        json.put("PrintStatus", "PRINT STATUS HAS BEEN UPDATED!");
        return this.doUpdate(session, "invoice", json).toString();
    }

    // The main update method
    public JSONObject doUpdate(HttpSession session, String tableName, JSONObject updateData) {

        String realmId = getRealmID(session);
        String urlBegin = oAuth2Configuration.getAccountingAPIHost();
        String table = tableName.toLowerCase();  //Enforce all lowercase
        String endpoint =  String.format("%s/v3/company/%s/%s?operation=update&minorversion=4", urlBegin, realmId, table);

        return doPostRequest(session, endpoint, updateData);

    }
}
