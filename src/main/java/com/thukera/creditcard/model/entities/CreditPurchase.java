package com.thukera.creditcard.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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


@Data
@Entity
@Builder
@Table(name = "tb_credit_purchase")
public class CreditPurchase {
	
	@Id
	@Column(name = "purchase_id")
	@SequenceGenerator(name = "purchase_seq", sequenceName = "seq_purchase", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchase_seq")
    private Long purchaseId;

	@ManyToOne
	@JoinColumn(name = "invoice_id")
	private Invoice invoice;
	
	@Column
	private Integer totalInstallments;

	@OneToMany(mappedBy = "purchase", fetch = FetchType.LAZY)
	private List<Installment> installments;

	@ManyToOne
	@JoinColumn(name = "tb_purchase_class")
	private PurchaseClass category;
	
	@Column(name = "value", precision = 16, scale = 2)
	private BigDecimal value;
	
	@Column(name = "data_cadastro")
	private LocalDateTime dataCadastro;
	
	@PrePersist
	public void prePersist() {
		setDataCadastro(LocalDateTime.now());
	}
	
}
