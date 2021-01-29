package de.uniwue.db.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.util.Comparator;

@Entity
@Table(name = "normal_slide")
public class NormalSlide {
    @Id
    @Column(name = "id")
    private int id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Column(name = "ordinalposition")
    private Integer ordinalPosition;

    @Column(name = "attachto")
    public String attachTo;

    @Column(name = "hideif")
    public String hideIf;

    @Column(name = "endif")
    public String endIf;

    @Column(name = "endeventhint")
    public String endIfHint;

    @Column(name = "textcontent")
    public String textContent;

    @Column(name = "mediatype")
    @Enumerated(EnumType.STRING)
    public MediaType mediaType;

    @Column(name = "mediaurl")
    public String mediaUrl;

    @Column(name = "mediaplacement")
    @Enumerated(EnumType.STRING)
    public MediaPlacement mediaPlacement;

    @Transient
    boolean hasMedia = false;

    public Integer getOrdinalPosition() {
        return this.ordinalPosition;
    }

    @Transient
    public static Comparator<NormalSlide> normalSlideComparator = Comparator.comparing(NormalSlide::getOrdinalPosition);
}
