package de.uniwue.db.entity;

import javax.persistence.*;

@Entity
@Table(name="tour")
public class Tour {

    @Id
    @Column(name="id")
    public int id;

    @Column(name="relativeurl")
    public String relativeUrl;

    @Column(name="topic")
    public String topic;

    @Column(name="additionalhelpurl")
    public String additionalHelpUrl;

    @OneToOne()
    @JoinColumn(name = "hotspot_id", referencedColumnName = "id")
    public Hotspot hotspot;

    @OneToOne()
    @JoinColumn(name = "overview_slide_id", referencedColumnName = "id")
    public OverviewSlide overviewSlide;

    @Transient
    public boolean hasCompletedOnce = false;
}
