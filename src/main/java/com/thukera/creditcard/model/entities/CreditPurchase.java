package com.thukera.creditcard.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringExclude;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "tb_credit_purchase")
public class CreditPurchase {

	@Id
	@Column(name = "purchase_id")
	@SequenceGenerator(name = "purchase_seq", sequenceName = "seq_purchase", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchase_seq")
	private Long purchaseId;

	@Column(name = "descricao", length = 255)
	private String descricao;

	@Column
	private Integer totalInstallments;

	@ToString.Exclude
	@OneToMany(mappedBy = "purchase", fetch = FetchType.LAZY)
	private List<Installment> installments;

	@ManyToOne
	@JoinColumn(name = "tb_purchase_category")
	private PurchaseCategory category;

	@Column(name = "value", precision = 16, scale = 2)
	private BigDecimal value;

	@Column(name = "purchase_date_time")
	private LocalDateTime purchaseDateTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "invoice_id")
	@ToString.Exclude
	private Invoice invoice; // null until invoice is generated

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "card_id", nullable = false)
	@ToString.Exclude
	private CreditCard creditCard;

	@PrePersist
	public void prePersist() {
		setPurchaseDateTime(LocalDateTime.now());
	}

	public CreditPurchase() {
		this.purchaseDateTime = LocalDateTime.now();
	}

}
