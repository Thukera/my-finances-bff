package com.thukera.user.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.thukera.creditcard.model.dto.CreditCardDTOFromUser;
import com.thukera.user.model.entities.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String profilePicturePath;

    private List<CreditCardDTOFromUser> creditcards;

    public static UserDTO fromEntity(User user) {
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getProfilePicturePath(),
            user.getCreditcards().stream()
                .map(CreditCardDTOFromUser::fromEntity)
                .collect(Collectors.toList())
        );
    }
}


