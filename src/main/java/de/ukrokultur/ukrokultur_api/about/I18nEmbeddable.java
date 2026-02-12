package de.ukrokultur.ukrokultur_api.about;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class I18nEmbeddable {

    @Column(name = "en", nullable = false, columnDefinition = "text")
    private String en;

    @Column(name = "de", nullable = false, columnDefinition = "text")
    private String de;

    @Column(name = "uk", nullable = false, columnDefinition = "text")
    private String uk;

    public I18nEmbeddable() {}

    public I18nEmbeddable(String en, String de, String uk) {
        this.en = en;
        this.de = de;
        this.uk = uk;
    }

    public String getEn() { return en; }
    public void setEn(String en) { this.en = en; }

    public String getDe() { return de; }
    public void setDe(String de) { this.de = de; }

    public String getUk() { return uk; }
    public void setUk(String uk) { this.uk = uk; }
}
