package pe.intercorp.ValidateDad.repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pe.intercorp.ValidateDad.entity.Authorization;

@Repository
public class RepoApi {
    @Value("${api.url}")
    public String api_url;

    @Value("${api.username}")
    public String api_username;

    @Value("${api.password}")
    public String api_password;

    // private final Logger log = LoggerFactory.getLogger(RepoApi.class);

    public String loginDAD() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        String stringBody = String.format("{\r\n  \"username\": \"%s\",\r\n  \"password\": \"%s\"\r\n}", api_username,
                api_password);
        RequestBody body = RequestBody.create(stringBody, mediaType);
        Request request = new Request.Builder()
                .url("https://login." + api_url + "/auth/login")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // System.out.printf("Code Response : %s\n", response.code());

        Authorization authorization = objectMapper.readValue(response.body().string(),
                new TypeReference<Authorization>() {
                });
        // System.out.printf("Conected using %s@%s\n", api_username, api_url);

        // System.out.printf("Return token : %s\n", authorization.getAccess_token());
        return authorization.getAccess_token();
    }

    public String getProductDAD(String sku, String token) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url("https://products." + api_url + "/products/search/" + sku)
                .method("GET", null)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 401) {
            return "401";
        } else {
            return response.body().string();
        }
    }

}
