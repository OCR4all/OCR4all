package de.uniwue.db.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "tour")
public class Tour {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "relativeurl")
    private String relativeUrl;

    @Column(name = "topic")
    private String topic;

    @Column(name = "additionalhelpurl")
    private String additionalHelpUrl;

    @OneToOne()
    @JoinColumn(name = "hotspot_id", referencedColumnName = "id")
    private Hotspot hotspot;

    @OneToOne()
    @JoinColumn(name = "overview_slide_id", referencedColumnName = "id")
    private OverviewSlide overviewSlide;

    @JsonManagedReference
    @OneToMany(mappedBy = "tour", fetch = FetchType.EAGER)
    private List<NormalSlide> normalSlides;

    @Transient
    private boolean hasCompletedOnce = false;

    public void setHasCompletedOnce(boolean hasCompletedOnce) {
        this.hasCompletedOnce = hasCompletedOnce;
    }

    // start getters
    public int getId() {
        return id;
    }

    public String getRelativeUrl() {
        return relativeUrl;
    }

    public String getTopic() {
        return topic;
    }

    public String getAdditionalHelpUrl() {
        return additionalHelpUrl;
    }

    public Hotspot getHotspot() {
        return hotspot;
    }

    public OverviewSlide getOverviewSlide() {
        return overviewSlide;
    }

    public List<NormalSlide> getNormalSlides() {
        return normalSlides;
    }

    public boolean getHasCompletedOnce() {
        return hasCompletedOnce;
    }
}
