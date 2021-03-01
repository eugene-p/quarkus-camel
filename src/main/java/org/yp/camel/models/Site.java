package org.yp.camel.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement
public class Site {
    @JacksonXmlProperty(isAttribute = true)
    private String code;
    @JsonProperty()
    private String nameEn;
    @JsonProperty()
    private String nameFr;
    @JsonProperty()
    private String provinceCode;

    Site() {super();}
    Site(String code, String nameEn, String nameFr, String provinceCode) {
        this.code = code;
        this.nameEn = nameEn;
        this.nameFr = nameFr;
        this.provinceCode = provinceCode;
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