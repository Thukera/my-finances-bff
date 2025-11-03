package com.thukera.panel.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.thukera.user.model.entities.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PanelUserDTO {
	private Long id;
	private String username;
	private String email;
	private String profilePicturePath;

	private List<PanelCreditCardDTOFromUser> creditcards;

	public static PanelUserDTO fromEntity(User user) {
		return new PanelUserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getProfilePicturePath(),
				user.getCreditcards().stream().map(PanelCreditCardDTOFromUser::fromEntity).collect(Collectors.toList()));
	}

}
