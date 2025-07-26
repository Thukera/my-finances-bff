package com.thukera.model.creditcard.entities;

import java.math.BigDecimal;
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
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Builder
@Table(name = "tb_invoice")
public class Invoice {
	
	@Id
	@Column(name = "invoice_id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_seq")
    private Long invoiceId;
	
	@ManyToOne
	@JoinColumn(name = "card_id", insertable = false, updatable = false)
	private CreditCard creditCard;
	
	@OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
	private List<CreditPurchase> purchases;
	
	@Column(name = "total_amount", precision = 16, scale = 2)
	private BigDecimal totalAmount;
	
	
	
}
