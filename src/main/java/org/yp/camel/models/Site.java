package org.yp.camel.models;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Site {
    @XmlAttribute(name = "code")
    private String code;
    @XmlElement
    private String nameEn;
    @XmlElement
    private String nameFr;
    @XmlElement
    private String provinceCode;

    Site() {super();}
    Site(String code, String nameEn, String nameFr, String provinceCode) {
        this.code = code;
        this.nameEn = nameEn;
        this.nameFr = nameFr;
        this.provinceCode = provinceCode;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public void setNameFr(String nameFr) {
        this.nameFr= nameFr;
    }

    public String getProvinceCode(){
        return this.provinceCode;
    }

    public String getCode(){
        return this.code;
    }

    public String getNameEn() {
        return this.nameEn;
    }

    public String getNameFr() {
        return this.nameFr;
    }

    @Override
    public String toString() {
        return "Site{" +
                "code='" + code + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", nameFr='" + nameFr + '\'' +
                ", provinceCode='" + provinceCode + '\'' +
                '}';
    }
}