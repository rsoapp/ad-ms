package rsoapp.adms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdDto {

    private Integer id;
    private Integer userId;
    private String title;
    private Integer price;
    private String description;
    private String condition;
    private String category;
    private AdImagesDto adImagesDto;
//    private Instant created;
    private String location;
    private String phoneNumber;
    private String email;
}
