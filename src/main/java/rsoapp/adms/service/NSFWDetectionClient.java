package rsoapp.adms.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.kraken.client.KrakenIoClient;
import io.kraken.client.impl.DefaultKrakenIoClient;
import io.kraken.client.model.RGBA;
import io.kraken.client.model.request.DirectUploadRequest;
import io.kraken.client.model.resize.FillResize;
import io.kraken.client.model.response.SuccessfulUploadResponse;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rsoapp.adms.config.ApplicationVariables;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

@Service
public class NSFWDetectionClient {

    private final double nsfwPropThr = 0.1;
    private ApplicationVariables applicationVariables;

    public NSFWDetectionClient(ApplicationVariables applicationVariables) {
        this.applicationVariables = applicationVariables;
    }

    public boolean isNSFW(MultipartFile imageFile) {

        if (applicationVariables.getNsfwDetection().equals("false")) return false;

        String imageUrl = uploadImage(imageFile);

        if (imageUrl == null) return false;

        try {
            HttpResponse<String> response = Unirest.post("https://nsfw-image-classification1.p.rapidapi.com/img/nsfw")
                    .header("content-type", "application/json")
                    .header("x-rapidapi-key", "800901fca2mshdc960a62b1af6acp14c7ebjsna04d57ceae2c")
                    .header("x-rapidapi-host", "nsfw-image-classification1.p.rapidapi.com")
                    .body("{\n    \"url\": \"" + imageUrl + "\"\n}")
                    .asString();

            JSONObject responseJson = new JSONObject(response.getBody());
            double nsfwProb = responseJson.getDouble("NSFW_Prob");
            return nsfwProb > nsfwPropThr;
        } catch (UnirestException e) {
            // we could not check if it is nsfw -> nsfw to be sure
            return true;
        }
    }

    private String uploadImage(MultipartFile imageFile) {
        KrakenIoClient krakenIoClient = new DefaultKrakenIoClient("6b0ffec90bc0c226545a38af8f7dd45b", "3f5d5f6ae7e5cf5549961e71839b27b6710f1664");

        // InputStream direct file upload
        try {
            FillResize fillResize = new FillResize(150, 150, new RGBA(100, 100, 100, BigDecimal.ONE));
            DirectUploadRequest directUploadRequest = DirectUploadRequest.builder(
                    new ByteArrayInputStream(imageFile.getBytes())
            ).withResize(fillResize).build();

            SuccessfulUploadResponse successfulUploadResponse = krakenIoClient.directUpload(directUploadRequest);
            return successfulUploadResponse.getKrakedUrl();
        } catch (Exception e) {
            return null;
        }
    }
}
