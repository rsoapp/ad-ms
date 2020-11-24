package rsoapp.adms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rsoapp.adms.model.dto.AdDto;
import rsoapp.adms.service.AdService;

import java.util.List;

@RestController
@RequestMapping("/")
public class AdController {

    @Autowired
    private AdService adService;

    @GetMapping("ad/{adId}")
    public ResponseEntity<AdDto> getAdById(@PathVariable Integer adId) {
        try {
            AdDto ad = adService.getAdById(adId);
            return new ResponseEntity<>(ad, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("{userId}/ads")
    public ResponseEntity<List<AdDto>> getUserAds(@PathVariable Integer userId) {
        try {
            List<AdDto> userAds = adService.getUserAds(userId);
            return new ResponseEntity<>(userAds, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("ad")
    public ResponseEntity<Integer> saveAd(@RequestBody AdDto ad) {
        try {
            return new ResponseEntity<>(adService.saveAd(ad), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("ad/{adId}")
    public ResponseEntity<Void> updateAd(@PathVariable Integer adId, @RequestBody AdDto adDto) {
        try {
            adService.updateAdById(adId, adDto);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
