package com.thukera.creditcard.model.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thukera.user.model.entities.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "tb_credit_card")
public class CreditCard {

    @Id
    @Column(name = "card_id")
    @SequenceGenerator(name = "credit_card_seq", sequenceName = "seq_credit_card", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "credit_card_seq")
    private Long cardId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "bank", length = 50, nullable = false)
    private String bank;

    @NotBlank
    @Column(name = "end_numbers", length = 4)
    @Size(max = 4, min = 4)  // exactly 4 digits
    private String endnumbers;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;

    @Min(1)
    @Max(31)
    @Column(name = "billing_period_start")
    private Integer billingPeriodStart;

    @Min(1)
    @Max(31)
    @Column(name = "billing_period_end")
    private Integer billingPeriodEnd;
    
    @Min(1)
    @Max(31)
    @Column(name = "billing_due_date")
    private Integer dueDate;

    @Column(name = "used_limit", precision = 16, scale = 2)
    private BigDecimal usedLimit = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_limit", precision = 16, scale = 2)
    private BigDecimal totalLimit;
    
    @Column(name = "estimate_limit", precision = 16, scale = 2)
    private BigDecimal estimateLimit;


    @Column(name = "data_cadastro", nullable = false)
    private LocalDate dataCadastro;

    @PrePersist
    public void prePersist() {
        if (usedLimit == null) usedLimit = BigDecimal.ZERO;
        if (dataCadastro == null) dataCadastro = LocalDate.now();
    }

    @OrderBy("dueDate ASC")
    @ToString.Exclude
    @OneToMany(mappedBy = "creditCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invoice> invoices = new ArrayList<>();
    
    @ToString.Exclude
    @OneToMany(mappedBy = "creditCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CreditPurchase> purchases = new ArrayList<>();
    

	public CreditCard() {
		super();
	}

	public CreditCard(User user, @NotBlank @Size(max = 50) String bank, @NotBlank @Size(max = 4) String endnumbers, @Size(max = 50) String nickname,
			@NotBlank int billingPeriodStart, @NotBlank int billingPeriodEnd, BigDecimal usedLimit,
			@NotBlank BigDecimal totalLimit, BigDecimal estimateLimit,@NotBlank LocalDate dataCadastro) {
		super();
		this.user = user;
		this.bank = bank;
		this.endnumbers = endnumbers;
		this.nickname = nickname;
		this.billingPeriodStart = billingPeriodStart;
		this.billingPeriodEnd = billingPeriodEnd;
		this.usedLimit = usedLimit;
		this.totalLimit = totalLimit;
		this.estimateLimit = estimateLimit;
		this.dataCadastro = dataCadastro;
	}

	public CreditCard(Long cardId, User user, @NotBlank @Size(max = 50) String bank, String nickname,
			@NotBlank @Size(max = 4) String endnumbers, @NotBlank int billingPeriodStart,
			@NotBlank int billingPeriodEnd, BigDecimal usedLimit, @NotBlank BigDecimal totalLimit,BigDecimal estimateLimit,
			LocalDate dataCadastro) {
		super();
		this.cardId = cardId;
		this.user = user;
		this.bank = bank;
		this.nickname = nickname;
		this.endnumbers = endnumbers;
		this.billingPeriodStart = billingPeriodStart;
		this.billingPeriodEnd = billingPeriodEnd;
		this.usedLimit = usedLimit;
		this.totalLimit = totalLimit;
		this.estimateLimit = estimateLimit;
		this.dataCadastro = dataCadastro;
	}
	

}
