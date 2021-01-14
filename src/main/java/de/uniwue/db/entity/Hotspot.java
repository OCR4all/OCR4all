package de.uniwue.db.entity;

import javax.persistence.*;

@Entity
@Table(name="hotspot")
public class Hotspot {
    @Id
    @Column(name="id")
    private int id;

    @Column(name="attachto")
    public String selectorToAttach;

    @Column(name="xoffsetinpx")
    public Integer xSelectorOffsetInPx;

    @Column(name="attachfallback")
    @Enumerated(EnumType.STRING)
    public PositionFallback attachFallback;
}
