package de.uniwue.db.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.List;

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

    @JsonManagedReference
    @OneToMany(mappedBy="tour", fetch = FetchType.EAGER)
    public List<NormalSlide> normalSlides;

    @Transient
    public boolean hasCompletedOnce = false;
}
