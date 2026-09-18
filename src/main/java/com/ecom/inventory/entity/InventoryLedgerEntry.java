package com.ecom.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "inventory_ledger")
@Getter
@Setter
@Immutable // org.hibernate.annotations.Immutable — never save() this entity, writes go through InventoryRepository
public class InventoryLedgerEntry {
    @Id
    private Long id;
    @Column(name = "product_id")
    private Long productId;
}


