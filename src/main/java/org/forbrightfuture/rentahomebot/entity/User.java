package org.forbrightfuture.rentahomebot.entity;


import lombok.Data;
import jakarta.persistence.*;
import java.util.Date;


@Entity
@Table(name="USER")
@Data
public class User {

    @Id
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "IS_BOT")
    private Boolean isBot;

    @Column(name = "FIRST_NAME", length = 150)
    private String firstName;

    @Column(name = "LAST_NAME", length = 150)
    private String lastName;

    @Column(name = "USERNAME", length = 150)
    private String username;

    @Column(name = "LANGUAGE_CODE", length = 3)
    private String languageCode;

    @Column(name = "INSERT_TIME")
    private Date insertTime;

}
