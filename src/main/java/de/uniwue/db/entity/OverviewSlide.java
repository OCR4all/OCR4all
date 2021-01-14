package de.uniwue.db.entity;

import javax.persistence.*;

@Entity
@Table(name="overview_slide")
public class OverviewSlide {
    @Id
    @Column(name="id")
    private int id;

    @Column(name="textcontent")
    public String textContent;
}
