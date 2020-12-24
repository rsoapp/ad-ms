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
    private Integer height;
    private Integer width;
    private byte[] imageBytes;

    public ImageDto(Integer height, Integer width, byte[] imageBytes) {
        this.height = height;
        this.width = width;
        this.imageBytes = imageBytes;
    }
}
