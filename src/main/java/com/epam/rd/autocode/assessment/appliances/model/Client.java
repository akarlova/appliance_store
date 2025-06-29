package com.epam.rd.autocode.assessment.appliances.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "client")
@EqualsAndHashCode(callSuper = true)
@Getter @Setter
@NoArgsConstructor

public class Client extends User {
    private String card;

    public Client(Long id,
                  String name,
                  String email,
                  String password,
                  String card) {
        super(id, name, email, password);
        this.card = card;
    }
}
