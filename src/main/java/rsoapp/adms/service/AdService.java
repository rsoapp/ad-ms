package rsoapp.adms.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import rsoapp.adms.model.dto.AdDto;
import rsoapp.adms.model.dto.AdImagesDto;
import rsoapp.adms.model.dto.ImageDto;
import rsoapp.adms.model.entity.Ad;
import rsoapp.adms.repository.AdRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
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

    public AdDto saveAd(Ad ad, List<MultipartFile> images) {
        try {
            Ad savedAd = adRepository.save(ad);

            List<ImageDto> savedImages = new ArrayList<>();
            int consecutiveNumber = 0;

            // we save images to msimage
            for (MultipartFile imageFile : images) {
                ImageDto image = new ImageDto();
                image.setAdId(savedAd.getId());
                BufferedImage bimg = ImageIO.read(imageFile.getInputStream());
                image.setHeight(bimg.getHeight());
                image.setWidth(bimg.getWidth());
                image.setConsecutiveNumber(consecutiveNumber);
                System.out.println(Base64.getEncoder().encodeToString(imageFile.getBytes()).length());
                image.setImage(Base64.getEncoder().encodeToString(imageFile.getBytes()));
                savedImages.add(restTemplate.postForObject("http://localhost:8080/v1/image", image, ImageDto.class));
                consecutiveNumber ++;
            }

            return adToAdDto(savedAd, new AdImagesDto(savedImages));

        } catch (Exception e) {
            return null;
        }
    }

    public void deleteAdById(Integer adId) {
        adRepository.deleteById(adId);
        restTemplate.delete("http://localhost:8080/v1/ads/" + adId.toString() + "/images");
    }

    public AdDto updateAdById(Integer userId, Integer adId, String title, Integer price, String description, String condition, String category, String location, String phoneNumber, String email, List<MultipartFile> images) {
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

        List<ImageDto> imagesToUpdate = new ArrayList<>();

        // Update images
        try {
            int consecutiveNumber = 0;
            for (MultipartFile imageFile : images) {
                imagesToUpdate.add(multipartFileToImageDto(adId, consecutiveNumber, imageFile));
                consecutiveNumber ++;
            }
            restTemplate.put("http://localhost:8080/v1/image/" + adId.toString() + "/images", new AdImagesDto(imagesToUpdate), AdImagesDto.class);
        } catch (Exception e) {
            return adToAdDto(ad, new AdImagesDto(imagesToUpdate));
        }

        return adToAdDto(adRepository.save(ad), new AdImagesDto(imagesToUpdate));
    }



    // gets images from msimage
    private AdImagesDto getAdImages(Integer adId) {
        try {
            return restTemplate.getForObject("http://localhost:8080/v1/ads/" + adId.toString() + "/images", AdImagesDto.class);
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

    // converts multipart file image to ImageDto
    private ImageDto multipartFileToImageDto(Integer adId, Integer consecutiveNumber, MultipartFile imageFile) {
        try {
            ImageDto image = new ImageDto();
            image.setAdId(adId);
            BufferedImage bimg = ImageIO.read(imageFile.getInputStream());
            image.setHeight(bimg.getHeight());
            image.setWidth(bimg.getWidth());
            image.setConsecutiveNumber(consecutiveNumber);
            image.setImage(Base64.getEncoder().encodeToString(imageFile.getBytes()));
            return image;
        } catch (IOException e) {
            return new ImageDto();
        }
    }
}
