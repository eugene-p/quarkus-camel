package org.yp.camel.models;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

@XmlRootElement(name = "siteList")
public class SiteList {
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Site> site;

    public List<Site> getSites() {
        return this.site;
    }
}
