package com.ppl.finalsaleweb.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Document(collection = "users")
@Getter
@Setter
public class User {

    @Id
    private String id;

    @NotBlank
    @Field("username")
    private String username;

    @NotBlank
    @Field("fullname")
    private String fullname;

    @NotBlank
    @Email(message = "Please enter a valid email address.")
    @Field("email")
    private String email;

    @NotBlank
    @Size(min = 6)
    @Field("password")
    private String password;

    @Field("token")
    private String token = null;

    @Field("passwordChangeRequired")
    private boolean passwordChangeRequired = true;

    @Field("profilePhotoURL")
    private String profilePhotoURL = "";

    @Field("isLock")
    private boolean isLock = false;



}