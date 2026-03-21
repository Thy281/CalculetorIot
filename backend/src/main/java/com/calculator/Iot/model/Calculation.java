package com.calculator.Iot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "calculations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String value1;

    @Column(nullable = false)
    private int base1;

    @Column(nullable = false)
    private String value2;

    @Column(nullable = false)
    private int base2;

    @Column(nullable = false)
    private String operator;

    @Column(nullable = false)
    private long result;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}

