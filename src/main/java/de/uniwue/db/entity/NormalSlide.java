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
    private String attachTo;

    @Column(name = "showifclassonattachto")
    private String showIfClass;

    @Column(name = "hideifclassonattachto")
    private String hideIfClass;

    @Column(name = "endifeventonattachto")
    private String endIfEvent;

    @Column(name = "endifhint")
    private String endIfHint;

    @Column(name = "textcontent")
    private String textContent;

    @Column(name = "mediatype")
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @Column(name = "mediaurl")
    private String mediaUrl;

    @Column(name = "mediaplacement")
    @Enumerated(EnumType.STRING)
    private MediaPlacement mediaPlacement;

    @Transient
    public static Comparator<NormalSlide> normalSlideComparator = Comparator.comparing(normalSlide -> normalSlide.ordinalPosition);

    // start getters
    public String getAttachTo() {
        return attachTo;
    }

    public String getShowIfClass() {
        return showIfClass;
    }

    public String getHideIfClass() {
        return hideIfClass;
    }

    public String getEndIfEvent() {
        return endIfEvent;
    }

    public String getEndIfHint() {
        return endIfHint;
    }

    public String getTextContent() {
        return textContent;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public MediaPlacement getMediaPlacement() {
        return mediaPlacement;
    }

}
