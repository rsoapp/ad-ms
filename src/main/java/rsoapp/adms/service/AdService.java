package rsoapp.adms.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import rsoapp.adms.model.dto.AdDto;
import rsoapp.adms.model.dto.AdImagesDto;
import rsoapp.adms.model.dto.ImageDto;
import rsoapp.adms.model.entity.Ad;
import rsoapp.adms.repository.AdRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AdService {

    private final AdRepository adRepository;
    private final RestTemplate restTemplate;

    public AdService(AdRepository adRepository, RestTemplate restTemplate) {
        this.adRepository = adRepository;
        this.restTemplate = restTemplate;
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

    public ResponseEntity<AdDto> saveAd(Ad ad, List<MultipartFile> images) {
        try {
            Ad savedAd = adRepository.save(ad);
            List<ImageDto> savedImages = new ArrayList<>();

            // save images to msimage
            for (MultipartFile imageFile : images) {
                ImageDto savedImage = sendImageToMsImage(imageFile, savedAd.getId());
                savedImages.add(savedImage);
            }

            return new ResponseEntity<>(adToAdDto(savedAd, new AdImagesDto(savedImages)), HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.toString());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteAdById(Integer adId) {
        adRepository.deleteById(adId);
        restTemplate.delete("http://image-ms:8080/v1/ads/" + adId.toString() + "/images");
    }

    public ResponseEntity<AdDto> updateAdById(Integer userId, Integer adId, String title, Integer price, String description, String condition, String category, String location, String phoneNumber, String email, List<MultipartFile> images) {
        Optional<Ad> query = adRepository.findById(adId);

        if(query.isEmpty()) {
            return null;
        }

        Ad ad = query.get();

        ad.setUserId(userId);
        ad.setTitle(title);
        ad.setPrice(price);
        ad.setDescription(description);
        ad.setCondition(condition);
        ad.setCategory(category);
//        ad.setCreated(adData.getCreated());
        ad.setLocation(location);
        ad.setPhoneNumber(phoneNumber);
        ad.setEmail(email);

        // Update images
        try {
            restTemplate.delete("http://image-ms:8080/v1/ads/" + adId + "/images");

            List<ImageDto> savedImages = new ArrayList<>();

            // save images to msimage
            for (MultipartFile imageFile : images) {
                ImageDto savedImage = sendImageToMsImage(imageFile, adId);
                savedImages.add(savedImage);
            }

            return new ResponseEntity<>(adToAdDto(adRepository.save(ad), new AdImagesDto(savedImages)), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(adToAdDto(ad, new AdImagesDto()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // gets images from msimage
    private AdImagesDto getAdImages(Integer adId) {
        try {
            return restTemplate.getForObject("http://image-ms:8080/v1/ads/" + adId.toString() + "/images", AdImagesDto.class);
        } catch (Exception e) {
            return null;
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
        adDto.setCondition(ad.getCondition());
        adDto.setCategory(ad.getCategory());
//        adDto.setCreated(ad.getCreated());
        adDto.setLocation(ad.getLocation());
        adDto.setPhoneNumber(ad.getPhoneNumber());
        adDto.setEmail(ad.getEmail());
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
        ResponseEntity<ImageDto> resE = restTemplate.exchange("http://image-ms:8080/v1/ads/" + adId + "/images", HttpMethod.POST, reqEntity, ImageDto.class);

        return resE.getBody();
    }
}
