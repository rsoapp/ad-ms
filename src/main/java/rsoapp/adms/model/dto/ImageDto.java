package rsoapp.adms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageDto {

    private Integer id;
    private Integer adId;
    private Integer consecutiveNumber;
    private Integer height;
    private Integer width;
    private Byte[] image;

    public ImageDto(Integer consecutiveNumber, Integer height, Integer width, Byte[] image) {
        this.consecutiveNumber = consecutiveNumber;
        this.height = height;
        this.width = width;
        this.image = image;
    }
}
