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

@Data
@Entity
@Builder
@Table(name = "tb_installment")
public class Installment {

	@Id
	@SequenceGenerator(name = "installment_seq", sequenceName = "seq_installment", allocationSize = 1)
	@Column(name = "installment_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "installment_seq")
    private Long installmentId;
	
	@ManyToOne
	@JoinColumn(name = "purchase_id")
	private CreditPurchase purchase;

	@Column(name = "installment_number")
	private int number;

	@Column(name = "due_date")
	private LocalDate dueDate;

	@Column(name = "amount", precision = 16, scale = 2)
	private BigDecimal amount;
	
}
