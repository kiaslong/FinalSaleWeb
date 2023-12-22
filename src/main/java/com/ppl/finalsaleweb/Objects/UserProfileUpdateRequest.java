package com.ppl.finalsaleweb.Objects;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
@Getter
@Setter
public class UserProfileUpdateRequest {
    private String name;
    private String email;
    private MultipartFile profileImage;

}
