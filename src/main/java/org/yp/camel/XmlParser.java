package org.yp.camel;

import java.io.IOException;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class XmlParser implements Processor {
    public void process(Exchange exchange)
            throws NoSuchAlgorithmException, ParserConfigurationException, SAXException, IOException {
        String xmlStr = exchange.getIn().getBody(String.class);

        String filter = exchange.getIn().getHeader("ProvinceToGet", String.class);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xmlStr));
        Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("site");

        ArrayList <Site> siteList = new ArrayList<Site>();
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String pr = eElement
                    .getElementsByTagName("provinceCode")
                    .item(0)
                    .getTextContent();
                if (pr.equals(filter)) {
                    siteList.add(new Site(
                        eElement.getAttribute("code")
                        , pr
                        , eElement
                            .getElementsByTagName("nameEn")
                            .item(0)
                            .getTextContent()
                        , eElement
                            .getElementsByTagName("nameFr")
                            .item(0)
                            .getTextContent()
                    ));
                }
            }
        }

        exchange.getIn().setBody(siteList);
    }
}
