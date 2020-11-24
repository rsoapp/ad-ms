package rsoapp.adms.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rsoapp.adms.model.dto.AdDto;
import rsoapp.adms.model.dto.AdImagesDto;
import rsoapp.adms.model.dto.ImageDto;
import rsoapp.adms.model.entity.Ad;
import rsoapp.adms.repository.AdRepository;

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
        return adToAdDto(query.get());
    }

    public List<AdDto> getUserAds(Integer userId) {
        List<Ad> userAds = adRepository.getAllByUserId(userId);
        List<AdDto> userAdsDto = new ArrayList<>(userAds.size());

        for (Ad userAd : userAds) {
            userAdsDto.add(adToAdDto(userAd));
        }

        return userAdsDto;
    }

    public Integer saveAd(AdDto adDto) {
        try {
            Ad savedAd = adRepository.save(adDtoToAd(adDto));

            // we save images to msimage
            for (ImageDto image : adDto.getAdImagesDto().getImages()) {
                image.setAdId(savedAd.getId());
                restTemplate.postForObject("http://localhost:8081/image", image, Integer.class);
            }

            return savedAd.getId();
        } catch (Exception e) {
            return -1;
        }
    }

    public void deleteAdById(Integer adId) {
        adRepository.deleteById(adId);
        restTemplate.delete("http://localhost:8081/ad/" + adId.toString() + "/images");
    }

    public void updateAdById(Integer adId, AdDto adData) {
        Optional<Ad> query = adRepository.findById(adId);

        if(query.isEmpty()) {
            return;
        }

        Ad ad = query.get();

        ad.setId(adData.getId());
        ad.setUserId(adData.getUserId());
        ad.setTitle(adData.getTitle());
        ad.setPrice(adData.getPrice());
        ad.setDescription(adData.getDescription());
        ad.setCondition(adData.getCondition());
        ad.setCategory(adData.getCategory());
//        ad.setCreated(adData.getCreated());
        ad.setLocation(adData.getLocation());
        ad.setPhoneNumber(adData.getPhoneNumber());
        ad.setEmail(adData.getEmail());

        // Update images
        restTemplate.put("http://localhost:8081/ad/" + adId.toString() + "/images", adData.getAdImagesDto(), AdImagesDto.class);

        adRepository.save(ad);
    }




    // converts Ad to AdDto
    private AdDto adToAdDto(Ad ad) {

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
        adDto.setAdImagesDto(getAdImages(ad.getId()));

        return adDto;
    }

    // converts AdDto to Ad
    private Ad adDtoToAd(AdDto adDto) {

        if (adDto == null) {
            return null;
        }

        Ad ad = new Ad();

        ad.setUserId(adDto.getUserId());
        ad.setTitle(adDto.getTitle());
        ad.setPrice(adDto.getPrice());
        ad.setDescription(adDto.getDescription());
        ad.setCondition(adDto.getCondition());
        ad.setCategory(adDto.getCategory());
//        ad.setCreated(adDto.getCreated());
        ad.setLocation(adDto.getLocation());
        ad.setPhoneNumber(adDto.getPhoneNumber());
        ad.setEmail(adDto.getEmail());

        return ad;
    }

    // gets images from msimage
    private AdImagesDto getAdImages(Integer adId) {
        try {
            return restTemplate.getForObject("http://localhost:8081/ad/" + adId.toString() + "/images", AdImagesDto.class);
        } catch (Exception e) {
            return null;
        }
    }
}
