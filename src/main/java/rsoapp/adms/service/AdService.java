package rsoapp.adms.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import rsoapp.adms.config.ApplicationVariables;
import rsoapp.adms.model.dto.AdDto;
import rsoapp.adms.model.dto.AdImagesDto;
import rsoapp.adms.model.dto.ImageDto;
import rsoapp.adms.model.dto.UserDto;
import rsoapp.adms.model.entity.Ad;
import rsoapp.adms.repository.AdRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AdService {

    private final AdRepository adRepository;
    private final RestTemplate restTemplate;
    private final NSFWDetectionClient nsfwDetectionClient;
    private ApplicationVariables applicationVariables;

    // just for testing
    private String msImageUrl;
    private String msUserUrl;

    public AdService(AdRepository adRepository, RestTemplate restTemplate, NSFWDetectionClient nsfwDetectionClient, ApplicationVariables applicationVariables) {
        this.adRepository = adRepository;
        this.restTemplate = restTemplate;
        this.nsfwDetectionClient = nsfwDetectionClient;
        this.applicationVariables = applicationVariables;

        if (applicationVariables.getEnvironmentType().equals("prod")) {
            msImageUrl = "http://image-ms:8080/v1/images/ad/";
            msUserUrl = "http://user-ms:8080/v1/user/";
        }
        else {
            msImageUrl = "http://localhost:8082/v1/images/ad/";
            msUserUrl = "http://localhost:8083/v1/user/";
        }
    }

    public AdDto getAdById(Integer adId) {
        Optional<Ad> query = adRepository.findById(adId);

        if (query.isPresent()) {
            AdImagesDto adImagesDto = getAdImages(query.get().getId());
            return adToAdDto(query.get(), adImagesDto);
        }
        else {
            return null;
        }
    }

    public List<AdDto> getUserAds(Integer userId) {
        List<Ad> userAds = adRepository.getAllByUserId(userId);
        List<AdDto> userAdsDto = new ArrayList<>(userAds.size());

        for (Ad userAd : userAds) {
            try {
                AdImagesDto adImagesDto = getAdImages(userAd.getId());
                userAdsDto.add(adToAdDto(userAd, adImagesDto));
            } catch (Exception e) {
                userAdsDto.add(adToAdDto(userAd, null));
            }
        }

        return userAdsDto;
    }

    public List<AdDto> searchAds(String keyword) {
        List<Ad> userAds = adRepository.searchAds(keyword.toLowerCase(Locale.ROOT));
        List<AdDto> userAdsDto = new ArrayList<>(userAds.size());

        for (Ad userAd : userAds) {
            userAdsDto.add(adToAdDto(userAd, null));
        }

        return userAdsDto;
    }

    public ResponseEntity<AdDto> saveAd(Ad ad, List<MultipartFile> images) {
        try {
            Ad savedAd = adRepository.save(ad);
            List<ImageDto> savedImages = new ArrayList<>();

            // save images to msimage
            for (MultipartFile imageFile : images) {
                if (!nsfwDetectionClient.isNSFW(imageFile)) {
                    ImageDto savedImage = sendImageToMsImage(imageFile, savedAd.getId());
                    savedImages.add(savedImage);
                }
            }

            return new ResponseEntity<>(adToAdDto(savedAd, new AdImagesDto(savedImages)), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteAdById(Integer adId) {
        adRepository.deleteById(adId);
        restTemplate.delete(msImageUrl + adId.toString());
    }

    public ResponseEntity<AdDto> updateAdById(Integer adId, String title, Integer price, String description, String condition, String category, List<MultipartFile> images) {
        Optional<Ad> query = adRepository.findById(adId);

        if(query.isEmpty()) {
            return null;
        }

        Ad ad = query.get();

        ad.setTitle(title);
        ad.setPrice(price);
        ad.setDescription(description);
        ad.setCond(condition);
        ad.setCategory(category);

        // Update images
        try {
            restTemplate.delete(msImageUrl + adId);

            List<ImageDto> savedImages = new ArrayList<>();

            // save images to msimage
            for (MultipartFile imageFile : images) {
                if (!nsfwDetectionClient.isNSFW(imageFile)) {
                    ImageDto savedImage = sendImageToMsImage(imageFile, adId);
                    savedImages.add(savedImage);
                }
            }

            return new ResponseEntity<>(adToAdDto(adRepository.save(ad), new AdImagesDto(savedImages)), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(adToAdDto(ad, new AdImagesDto()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // gets images from image microservice
    public AdImagesDto getAdImages(Integer adId) {
        try {
            return restTemplate.getForObject(msImageUrl + adId.toString(), AdImagesDto.class);
        } catch (Exception e) {
            return null;
        }
    }

    // get user data from user microservice
    public void getContactData(AdDto ad, Integer userId) {
        try {
            UserDto user = restTemplate.getForObject(msUserUrl + userId.toString(), UserDto.class);
            if (user != null) {
                ad.setEmail(user.getEmail());
                ad.setLocation(user.getAddress());
                ad.setPhoneNumber(user.getPhoneNumber());
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    // converts Ad to AdDto
    private AdDto adToAdDto(Ad ad, AdImagesDto adImagesDto) {

        if (ad == null) {
            return null;
        }

        AdDto adDto = new AdDto();

        adDto.setId(ad.getId());
        adDto.setUserId(ad.getUserId());
        adDto.setTitle(ad.getTitle());
        adDto.setPrice(ad.getPrice());
        adDto.setDescription(ad.getDescription());
        adDto.setCond(ad.getCond());
        adDto.setCategory(ad.getCategory());
        adDto.setAdImagesDto(adImagesDto);

        return adDto;
    }

    private ImageDto sendImageToMsImage(MultipartFile imageFile, Integer adId) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, String> pdfHeaderMap = new LinkedMultiValueMap<>();
        pdfHeaderMap.add("Content-disposition", "form-data; name=imageFile; filename=" + imageFile.getOriginalFilename());
        pdfHeaderMap.add("Content-type", "application/pdf");
        HttpEntity<byte[]> doc = new HttpEntity<>(imageFile.getBytes(), pdfHeaderMap);

        LinkedMultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add("imageFile", doc);

        HttpEntity<LinkedMultiValueMap<String, Object>> reqEntity = new HttpEntity<>(multipartReqMap, headers);
        ResponseEntity<ImageDto> resE = restTemplate.exchange(msImageUrl + adId.toString(), HttpMethod.POST, reqEntity, ImageDto.class);

        return resE.getBody();
    }
}
