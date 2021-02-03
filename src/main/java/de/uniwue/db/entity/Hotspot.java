package de.uniwue.db.entity;

import javax.persistence.*;

@Entity
@Table(name = "hotspot")
public class Hotspot {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "attachto")
    private String selectorToAttach;

    @Column(name = "leftvalue")
    private String leftValue;

    @Transient()
    private boolean isHidden = false;

    public void setIsHidden(boolean hidden) {
        isHidden = hidden;
    }

    // start getters
    public String getSelectorToAttach() {
        return selectorToAttach;
    }

    public String getLeftValue() {
        return leftValue;
    }

    public boolean getIsHidden() {
        return isHidden;
    }
}
