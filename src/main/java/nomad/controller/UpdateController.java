package nomad.controller;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class UpdateController extends PostController{

    private static final Logger logger = Logger.getLogger(UpdateController.class);

    @ResponseBody
    @RequestMapping("/updateTest")
    public String updateTest(HttpSession session){
        logger.debug("Executing a sparse update test");
        JSONObject json = new JSONObject();
        json.put("Id", "16");
        json.put("PrintStatus", "PRINT STATUS HAS BEEN UPDATED!");
        return this.doSparseUpdate(session, "invoice", 16, json).toString();
    }

    /**
     * The main update method requires that the updateData be the full data of the object to be updated
     * @param updateData the updateData must contain all of the data of the object to be updated
     */
    public JSONObject doUpdate(HttpSession session, String tableName, JSONObject updateData) {

        String endpoint = getEndpoint(session, tableName);
        return doPostRequest(session, endpoint, updateData);
    }

    /**
     * Performs a sparse update of a single data object
     * @param tableName the name of the table containing the objects to be updated (letters must be capitalized)
     * @param id the unique id of the object to be updated
     * @param updateData JSON containing the key value pairs to be updated for this object
     * @return JSON Object containing the response data of the update operation
     */
    public JSONObject doSparseUpdate(HttpSession session, String tableName, int id, JSONObject updateData) {

        // Add to the updateData so quick books knows to perform a sparse update on the object
        updateData.put("Id", String.valueOf(id));
        updateData.put("sparse", true);

        // Only add the sync token if it is nor already included (Saves a request if token is known)
        if (!updateData.has("SyncToken")) {
            // Get the current SyncToken
            ReadController readController = new ReadController();
            JSONObject data = readController.doRead(session, tableName, id);
            String key = tableName + ".SyncToken";
            String token = "0";
            try {
                token = data.get(key).toString();
            } catch (JSONException e) {
                // If this attempt to get the sync token fails, give up
                logger.error("Could not find " + key + "in the " + tableName + " with id=" + id, e);
            }
            // Add the sync token to the updateData
            updateData.put("SyncToken", token);
        }

        // Now that the body data is ready, execute the update request
        String endpoint = getEndpoint(session, tableName);
        return doPostRequest(session, endpoint, updateData);
    }

    private String getEndpoint(HttpSession session, String tableName) {

        String realmId = getRealmID(session);
        String urlBegin = oAuth2Configuration.getAccountingAPIHost();
        String table = tableName.toLowerCase();  //Enforce all lowercase

        return String.format("%s/v3/company/%s/%s?operation=update&minorversion=4", urlBegin, realmId, table);
    }
}
