package com.thukera.creditcard.model.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thukera.user.model.entities.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

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

    @Min(1)
    @Max(31)
    @Column(name = "billing_period_start")
    private Integer billingPeriodStart;

    @Min(1)
    @Max(31)
    @Column(name = "billing_period_end")
    private Integer billingPeriodEnd;

    @Column(name = "used_limit", precision = 16, scale = 2)
    private BigDecimal usedLimit = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_limit", precision = 16, scale = 2)
    private BigDecimal totalLimit;
    


    @Column(name = "data_cadastro", nullable = false)
    private LocalDate dataCadastro;

    @PrePersist
    public void prePersist() {
        if (usedLimit == null) usedLimit = BigDecimal.ZERO;
        if (dataCadastro == null) dataCadastro = LocalDate.now();
    }

	@OneToMany(mappedBy = "creditCard", fetch = FetchType.LAZY)
	@JoinTable(name = "tb_invoice", joinColumns = @JoinColumn(name = "card_id"), inverseJoinColumns = @JoinColumn(name = "invoice_id"))
	private List<Invoice> invoices = new ArrayList<>();

	public CreditCard() {
		super();
	}

	public CreditCard(User user, @NotBlank @Size(max = 50) String bank, @NotBlank @Size(max = 4) String endnumbers,
			@NotBlank int billingPeriodStart, @NotBlank int billingPeriodEnd, BigDecimal usedLimit,
			@NotBlank BigDecimal totalLimit, @NotBlank LocalDate dataCadastro) {
		super();
		this.user = user;
		this.bank = bank;
		this.endnumbers = endnumbers;
		this.billingPeriodStart = billingPeriodStart;
		this.billingPeriodEnd = billingPeriodEnd;
		this.usedLimit = usedLimit;
		this.totalLimit = totalLimit;
		this.dataCadastro = dataCadastro;
	}

	public CreditCard(Long cardId, User user, @NotBlank @Size(max = 50) String bank,
			@NotBlank @Size(max = 4) String endnumbers, @NotBlank int billingPeriodStart,
			@NotBlank int billingPeriodEnd, BigDecimal usedLimit, @NotBlank BigDecimal totalLimit,
			LocalDate dataCadastro) {
		super();
		this.cardId = cardId;
		this.user = user;
		this.bank = bank;
		this.endnumbers = endnumbers;
		this.billingPeriodStart = billingPeriodStart;
		this.billingPeriodEnd = billingPeriodEnd;
		this.usedLimit = usedLimit;
		this.totalLimit = totalLimit;
		this.dataCadastro = dataCadastro;
	}

}
