package com.thukera.user.model.forms;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thukera.user.model.entities.User;

import jakarta.validation.constraints.*;

import lombok.Data;


@Data
public class SignUpForm {
	
	@Autowired
	PasswordEncoder encoder;
	
	private Long id;

	@NotBlank
    @Size(min = 11, max = 15)
    private String doc;

    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(max = 60)
    @Email
    private String email;

    private Set<String> role;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotNull
    private Boolean status;
    
    @JsonFormat(pattern = "dd/MM/yyyy")
	private LocalDate cadastro;
    
	public User toModel() {
		return new User(doc, name, username,email, encoder.encode(password), status);
	}
	
//	public static ClienteFormRequest fromModel(Cliente cliente) {
//		return new ClienteFormRequest(cliente.getId(),cliente.getNome(),cliente.getCpf(),cliente.getNascimento(),cliente.getEndereco(), cliente.getEmail(),cliente.getTelefone(),cliente.getDataCadastro());
//	}


}