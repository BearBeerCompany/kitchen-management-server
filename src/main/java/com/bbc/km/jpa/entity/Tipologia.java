package com.bbc.km.jpa.entity;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "tipologie")
public class Tipologia {
    @Id
    private Integer id;
    private String descrizione;
    private Integer posizione;
    private Boolean visibile;
    private Integer sfondo;

    @OneToMany(mappedBy = "tipologia", fetch = FetchType.LAZY)
    private Set<Articolo> articoloSet;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Integer getPosizione() {
        return posizione;
    }

    public void setPosizione(Integer posizione) {
        this.posizione = posizione;
    }

    public Boolean getVisibile() {
        return visibile;
    }

    public void setVisibile(Boolean visibile) {
        this.visibile = visibile;
    }

    public Integer getSfondo() {
        return sfondo;
    }

    public void setSfondo(Integer sfondo) {
        this.sfondo = sfondo;
    }

    public Set<Articolo> getArticoloSet() {
        return articoloSet;
    }

    public void setArticoloSet(Set<Articolo> articoloSet) {
        this.articoloSet = articoloSet;
    }
}
