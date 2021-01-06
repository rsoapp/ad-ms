package rsoapp.adms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rsoapp.adms.model.dto.AdDto;
import rsoapp.adms.model.dto.AdImagesDto;
import rsoapp.adms.model.dto.UserAds;
import rsoapp.adms.model.entity.Ad;
import rsoapp.adms.service.AdService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/ads/")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    @GetMapping("{adId}")
    public ResponseEntity<AdDto> getAdById(@PathVariable Integer adId) {
        try {
            AdDto ad = adService.getAdById(adId);
            try {
                ad.setAdImagesDto(adService.getAdImages(ad.getId()));
            } catch (Exception e) {
                ad.setAdImagesDto(new AdImagesDto());
            }

            try {
                adService.getContactData(ad, ad.getUserId());
            } catch (Exception ignored) {

            }
            return new ResponseEntity<>(ad, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("user/{userId}")
    public ResponseEntity<UserAds> getUserAds(@PathVariable Integer userId) {
        try {
            List<AdDto> userAds = adService.getUserAds(userId);
            return new ResponseEntity<>(new UserAds(userAds), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @PostMapping("search")
    public ResponseEntity<List<AdDto>> searchAds(@RequestParam("keyword") String keyword) {
        try {
            return new ResponseEntity<>(adService.searchAds(keyword), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("user/{userId}")
    public ResponseEntity<AdDto> saveAd(
            @PathVariable Integer userId,
            @RequestParam(required = false) List<MultipartFile> images,
            @RequestParam("title") String title,
            @RequestParam("price") Integer price,
            @RequestParam("description") String description,
            @RequestParam("cond") String condition,
            @RequestParam("category") String category
    ) {
        try {
            Ad ad = new Ad(userId, title, price, description, condition, category);
            return adService.saveAd(ad, images);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @PutMapping("{adId}")
    public ResponseEntity<AdDto> updateAd(
            @PathVariable Integer adId,
            @RequestParam(required = false) List<MultipartFile> images,
            @RequestParam("title") String title,
            @RequestParam("price") Integer price,
            @RequestParam("description") String description,
            @RequestParam("cond") String condition,
            @RequestParam("category") String category
    ) {
        try {
            return adService.updateAdById(adId, title, price, description, condition, category, images);
        } catch (Exception e) {
            return new ResponseEntity<>(new AdDto(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @DeleteMapping("{adId}")
    public ResponseEntity<Void> deleteAd(@PathVariable Integer adId) {
        try {
            adService.deleteAdById(adId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
