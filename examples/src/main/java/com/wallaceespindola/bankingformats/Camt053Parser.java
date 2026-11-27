package com.wallaceespindola.bankingformats;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Reads the first statement of a camt.053 file with the JDK's DOM parser, ignoring the version namespace. */
public final class Camt053Parser {

    public Statement parse(InputStream xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // no XXE
        Document doc = factory.newDocumentBuilder().parse(xml);
        Element stmt = first(doc.getDocumentElement(), "Stmt");

        Element opening = balance(stmt, "OPBD");
        Element closing = balance(stmt, "CLBD");
        List<Statement.Entry> entries = new ArrayList<>();
        for (Element ntry : children(stmt, "Ntry")) {
            BigDecimal amount = signed(ntry);
            String counterparty = amount.signum() < 0 ? "Cdtr" : "Dbtr"; // the other side of the entry
            entries.add(new Statement.Entry(
                    LocalDate.parse(text(first(ntry, "BookgDt"), "Dt")),
                    amount,
                    text(first(ntry, counterparty), "Nm"),
                    text(first(ntry, "CdtrRefInf"), "Ref")));
        }

        return new Statement(
                text(first(stmt, "Acct"), "IBAN"),
                first(opening, "Amt").getAttribute("Ccy"),
                signed(opening),
                LocalDate.parse(text(first(opening, "Dt"), "Dt")),
                signed(closing),
                LocalDate.parse(text(first(closing, "Dt"), "Dt")),
                List.copyOf(entries));
    }

    /** Balance types: OPBD opening booked, CLBD closing booked, ITBD interim, CLAV closing available. */
    private static Element balance(Element stmt, String code) {
        return children(stmt, "Bal").stream()
                .filter(bal -> code.equals(text(bal, "Cd")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No balance " + code));
    }

    /** Amount and sign are direct children of Bal and Ntry; transaction details below repeat them. */
    private static BigDecimal signed(Element amountOwner) {
        BigDecimal value = new BigDecimal(children(amountOwner, "Amt").getFirst().getTextContent().strip())
                .setScale(2);
        String indicator = children(amountOwner, "CdtDbtInd").getFirst().getTextContent().strip();
        return "DBIT".equals(indicator) ? value.negate() : value;
    }

    private static Element first(Element parent, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", localName);
        return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
    }

    private static String text(Element parent, String localName) {
        Element element = parent == null ? null : first(parent, localName);
        return element == null ? null : element.getTextContent().strip();
    }

    private static List<Element> children(Element parent, String localName) {
        List<Element> result = new ArrayList<>();
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element e && localName.equals(e.getLocalName())) {
                result.add(e);
            }
        }
        return result;
    }
}
