package com.thukera.model.creditcard.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Builder
@Table(name = "tb_purchase_class")
public class PurchaseClass {

	@Id
	@SequenceGenerator(name = "purchase_class_seq", sequenceName = "seq_purchase_class", allocationSize = 1)
	@Column(name = "purchase_class_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "purchase_class_seq")
    private Long purchaseClassId;
	
    @Column
    private String name;
    
    @Column
    private boolean editable;
}
