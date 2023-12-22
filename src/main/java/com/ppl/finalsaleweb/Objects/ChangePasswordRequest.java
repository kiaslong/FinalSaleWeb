package com.ppl.finalsaleweb.Objects;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String newPassword;
    private boolean passwordChange;


}
