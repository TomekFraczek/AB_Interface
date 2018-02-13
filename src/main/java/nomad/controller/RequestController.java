package nomad.controller;

import nomad.domain.OAuth2Configuration;
import nomad.helper.HttpHelper;
import nomad.service.RefreshTokenService;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class RequestController {

    @Autowired
    public OAuth2Configuration oAuth2Configuration;

    @Autowired
    public HttpHelper httpHelper;

    @Autowired
    public RefreshTokenService refreshTokenService;

    protected static final HttpClient CLIENT = HttpClientBuilder.create().build();

}
