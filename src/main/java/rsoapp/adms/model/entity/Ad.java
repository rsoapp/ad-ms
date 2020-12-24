package rsoapp.adms.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ad")
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private Integer price;

    @Column(name = "description")
    private String description;

    @Column(name = "condition")
    private String condition;

    @Column(name = "category")
    private String category;

    @Column(name = "created")
    private Instant created;

    @Column(name = "location")
    private String location;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    public Ad(Integer userId, String title, Integer price, String description, String condition, String category, String location, String phoneNumber, String email) {
        this.userId = userId;
        this.title = title;
        this.price = price;
        this.description = description;
        this.condition = condition;
        this.category = category;
        this.created = created;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }
}
