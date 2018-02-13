package nomad.controller;

import nomad.domain.OAuth2Configuration;
import nomad.helper.HttpHelper;
import nomad.service.RefreshTokenService;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;

public class RequestController {

    @Autowired
    public OAuth2Configuration oAuth2Configuration;

    @Autowired
    public HttpHelper httpHelper;

    @Autowired
    public RefreshTokenService refreshTokenService;

    protected static final HttpClient CLIENT = HttpClientBuilder.create().build();

    /** Extract the realmID from the current HttpSession */
    protected String getRealmID(HttpSession session) {

        //Ideally you would fetch the realmId and the accessToken from the data store based on the user account here.
        String realmId = (String)session.getAttribute("realmId");
        if (StringUtils.isEmpty(realmId)) {
            return new JSONObject().put("response","No realm ID.  QBO calls only work if the accounting scope was passed!").toString();
        }
        return realmId;

    }
}
