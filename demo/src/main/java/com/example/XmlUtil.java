package com.example;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XmlUtil {

    public static Document loadDocument(String fileName) {
        try {
            File file = new File(fileName); 
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); // Ottiene un'istanza di DocumentBuilderFactory
            DocumentBuilder db = dbf.newDocumentBuilder(); // Crea un DocumentBuilder usando la DocumentBuilderFactory
            return db.parse(file); // Parsea il file e ritorna l'oggetto Document che contiene  l'albero DOM del file XML
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getElementValue(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName); // Ottiene una lista di nodi con il tagName specificato
        if (nodeList != null && nodeList.getLength() > 0) { // Verifica che la lista di nodi non sia null e contenga almeno un elemento
            NodeList subList = nodeList.item(0).getChildNodes(); // Ottiene i nodi figli del primo elemento nella lista di nodi
            if (subList != null && subList.getLength() > 0) { // Verifica che la lista di nodi figli non sia null e contenga almeno un elemento
                return subList.item(0).getNodeValue(); //Ritorna il valore del primo nodo nella lista di nodi figli
            }
        }
        return null;
    }
}
