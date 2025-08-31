package com.thukera.creditcard.model.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "tb_installment")
public class Installment {

	@Id
	@SequenceGenerator(name = "installment_seq", sequenceName = "seq_installment", allocationSize = 1)
	@Column(name = "installment_id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "installment_seq")
	private Long installmentId;

	@Column(name = "installment_number")
	private int currentInstallment;

	@Column(name = "installment_total")
	private int totalInstallment;

	@Column(name = "amount", precision = 16, scale = 2)
	private BigDecimal value;

	@ToString.Exclude
	@ManyToOne
	@JoinColumn(name = "credit_purchase_id")
	private CreditPurchase purchase;

	public Installment() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Installment(int currentInstallment, int totalInstallment, BigDecimal value, CreditPurchase purchase) {
		super();
		this.currentInstallment = currentInstallment;
		this.totalInstallment = totalInstallment;
		this.value = value;
		this.purchase = purchase;
	}
	
	

}
