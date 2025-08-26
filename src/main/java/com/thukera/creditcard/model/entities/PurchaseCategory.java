package com.thukera.creditcard.model.entities;

import org.hibernate.annotations.NaturalId;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_purchase_category")
public class PurchaseCategory {

	@Id
	@SequenceGenerator(name = "purchase_category_seq", sequenceName = "seq_purchase_category", allocationSize = 1)
	@Column(name = "purchase_category_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchase_category_seq")
    private Long purchaseClassId;
	
	@Column(name = "@NotBlank", length = 50, nullable = false, unique = true)  
    private String name;
    
    @Column
    private boolean editable;

	public PurchaseCategory() {
		super();
	}

	public PurchaseCategory(String name, boolean editable) {
		super();
		this.name = name;
		this.editable = editable;
	}
    
    
}
