package com.thukera.user.model.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.thukera.creditcard.model.entities.CreditCard;
import com.thukera.root.model.messages.JwtResponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;


@Entity
@Table(name = "tb_users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
            "username"
        }),
        @UniqueConstraint(columnNames = {
            "email"
        }),
        @UniqueConstraint(columnNames = {
            "doc"
        })
})
@Data
public class User{
	@Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
	@SequenceGenerator(name = "user_seq", sequenceName = "tb_users_id_seq", allocationSize = 1)
    private Long id;
	
	@NotBlank
	@Size(min = 11, max = 15)
    @Column(unique = true)
	private String doc;

    @NotBlank
    @Size(min=3, max = 50)
    private String name;

    @NotBlank
    @Size(min=3, max = 50)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(min=6, max = 100)
    private String password;

    @NotNull
    private Boolean status = false;

    @Transient
    private JwtResponse token;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tb_user_roles", 
        joinColumns = @JoinColumn(name = "user_id"), 
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    
    @Column(name = "data_cadastro")
	private LocalDate dataCadastro;
    
    @OneToMany
    private List<CreditCard> creditcards = new ArrayList<CreditCard>();

	@PrePersist
	public void prePersist() {
		setDataCadastro(LocalDate.now());
	}
	

    public User() {
    	super();
    }

    public User(String doc, String name, String username, String email, String password, Boolean status) {
        this.doc = doc;
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.status = status;
    }
    
    public User(long id,String doc, String name, String username, String email, String password, Boolean status,LocalDate dataCadastro) {
        this.doc = doc;
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.status = status;
        this.dataCadastro = dataCadastro;
    }
}