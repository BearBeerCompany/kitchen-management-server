package com.bbc.km.jpa.entity;

import javax.persistence.*;

@Entity
@Table(name = "articoli")
public class Articolo {
    @Id
    private Integer id;
    private String descrizione;
    @Column(name = "descrizionebreve")
    private String descrizioneBreve;
    private Integer posizione;
    private Integer sfondo;

    @ManyToOne
    @JoinColumn(name = "id_tipologia", nullable = false)
    private Tipologia tipologia;

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

    public String getDescrizioneBreve() {
        return descrizioneBreve;
    }

    public void setDescrizioneBreve(String descrizioneBreve) {
        this.descrizioneBreve = descrizioneBreve;
    }

    public Integer getPosizione() {
        return posizione;
    }

    public void setPosizione(Integer posizione) {
        this.posizione = posizione;
    }

    public Integer getSfondo() {
        return sfondo;
    }

    public void setSfondo(Integer sfondo) {
        this.sfondo = sfondo;
    }

    public Tipologia getTipologia() {
        return tipologia;
    }

    public void setTipologia(Tipologia tipologia) {
        this.tipologia = tipologia;
    }
}
