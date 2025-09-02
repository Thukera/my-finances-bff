package com.thukera.creditcard.model.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thukera.creditcard.model.enums.InvoiceStatus;
import com.thukera.user.model.entities.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "tb_invoice")
public class Invoice {

	@Id
	@Column(name = "invoice_id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_seq")
	private Long invoiceId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "card_id", nullable = false)
	@ToString.Exclude
	private CreditCard creditCard;

	@Column(name = "total_amount", precision = 16, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private InvoiceStatus status;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "tb_invoice_purchase", joinColumns = @JoinColumn(name = "invoice_id"), inverseJoinColumns = @JoinColumn(name = "purchase_id"))
	private List<CreditPurchase> purchases = new ArrayList<>();
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "tb_installment", joinColumns = @JoinColumn(name = "invoice_id"), inverseJoinColumns = @JoinColumn(name = "installment_id"))
	private List<Installment> installments = new ArrayList<>();

	public Invoice() {
		super();
		// TODO Auto-generated constructor stub
	}

}
