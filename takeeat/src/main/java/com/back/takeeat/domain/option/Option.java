package com.back.takeeat.domain.option;

import jakarta.persistence.*;

@Entity
@Table(name = "options")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long id;

    private String optionName;

    private int optionPrice;

    private String optionSelect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_category_id")
    private OptionCategory optionCategory;

}