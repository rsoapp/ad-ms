package rsoapp.adms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rsoapp.adms.model.dto.AdDto;
import rsoapp.adms.model.entity.Ad;
import rsoapp.adms.service.AdService;

import java.util.List;

@RestController
@RequestMapping("/v1/")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    @GetMapping("ads/{adId}")
    public ResponseEntity<AdDto> getAdById(@PathVariable Integer adId) {
        try {
            AdDto ad = adService.getAdById(adId);
            return new ResponseEntity<>(ad, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}/ads")
    public ResponseEntity<List<AdDto>> getUserAds(@PathVariable Integer userId) {
        try {
            List<AdDto> userAds = adService.getUserAds(userId);
            return new ResponseEntity<>(userAds, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/user/{userId}/ads")
    public ResponseEntity<AdDto> saveAd(
            @PathVariable Integer userId,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam("title") String title,
            @RequestParam("price") Integer price,
            @RequestParam("description") String description,
            @RequestParam("condition") String condition,
            @RequestParam("category") String category,
            @RequestParam("location") String location,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("email") String email) {
        try {
            Ad ad = new Ad(userId, title, price, description, condition, category, location, phoneNumber, email);
            return adService.saveAd(ad, images);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/user/{userId}/ads/{adId}")
    public ResponseEntity<AdDto> updateAd(
            @PathVariable Integer userId,
            @PathVariable Integer adId,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam("title") String title,
            @RequestParam("price") Integer price,
            @RequestParam("description") String description,
            @RequestParam("condition") String condition,
            @RequestParam("category") String category,
            @RequestParam("location") String location,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("email") String email) {
        try {
            return adService.updateAdById(userId, adId, title, price, description, condition, category, location, phoneNumber, email, images);
        } catch (Exception e) {
            return new ResponseEntity<>(new AdDto(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("ad/{adId}")
    public ResponseEntity<Void> deleteAd(@PathVariable Integer adId) {
        try {
            adService.deleteAdById(adId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
