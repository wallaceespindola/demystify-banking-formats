package com.wallaceespindola.bankingformats;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Translates the core fields of a pacs.008 into an MT103 text block and reports every piece of data
 * that does not fit. It is a teaching example of why MX to MT is lossy, not a CBPR+ translator.
 */
public final class Pacs008ToMt103 {

    private static final int LINE = 35;
    private static final DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");
    /** SWIFT "X" character set used by MT; anything else has to be replaced. */
    private static final Pattern NOT_X_CHARSET = Pattern.compile("[^A-Za-z0-9/\\-?:().,'+ ]");

    public record Result(String mt103, List<String> losses) {}

    public Result translate(InputStream xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document doc = factory.newDocumentBuilder().parse(xml);
        Element tx = first(doc.getDocumentElement(), "CdtTrfTxInf");
        List<String> losses = new ArrayList<>();

        Element amount = first(tx, "IntrBkSttlmAmt");
        String date = LocalDate.parse(text(tx, "IntrBkSttlmDt")).format(YYMMDD);
        Element debtor = first(tx, "Dbtr");
        Element creditor = first(tx, "Cdtr");

        StringBuilder mt = new StringBuilder();
        mt.append("{3:{121:").append(text(tx, "UETR")).append("}}{4:\n");
        mt.append(":20:").append(text(tx, "InstrId")).append('\n');
        mt.append(":23B:CRED\n");
        mt.append(":32A:").append(date).append(amount.getAttribute("Ccy"))
                .append(amount.getTextContent().strip().replace('.', ',')).append('\n');
        mt.append(":50K:/").append(text(first(tx, "DbtrAcct"), "IBAN")).append('\n');
        party(debtor, "50K", losses).forEach(l -> mt.append(l).append('\n'));
        mt.append(":59:/").append(text(first(tx, "CdtrAcct"), "Id")).append('\n');
        party(creditor, "59", losses).forEach(l -> mt.append(l).append('\n'));
        String remittance = text(tx, "Ustrd");
        if (remittance != null) {
            mt.append(":70:");
            lines(remittance, 4, "70", "remittance", losses).forEach(l -> mt.append(l).append('\n'));
        }
        mt.append(":71A:").append(charges(text(tx, "ChrgBr"))).append('\n');
        mt.append("-}");

        losses.add("EndToEndId " + text(tx, "EndToEndId") + " has no MT103 field (kept only if the bank maps it to :70:)");
        return new Result(mt.toString(), List.copyOf(losses));
    }

    /** Name plus structured address squeezed into at most 3 more lines of 35 characters. */
    private static List<String> party(Element party, String tag, List<String> losses) {
        String name = text(party, "Nm");
        List<String> out = new ArrayList<>(lines(name, 1, tag, "name", losses));
        Element adr = first(party, "PstlAdr");
        if (adr == null) {
            return out;
        }
        String street = join(text(adr, "StrtNm"), text(adr, "BldgNb"));
        String town = join(text(adr, "PstCd"), text(adr, "TwnNm"), text(adr, "CtrySubDvsn"), text(adr, "Ctry"));
        out.addAll(lines(street, 1, tag, "street", losses));
        out.addAll(lines(town, 1, tag, "town", losses));
        losses.add(":" + tag + ": structured address (StrtNm, BldgNb, PstCd, TwnNm, Ctry) flattened to free text");
        return out;
    }

    /** Cut text into at most maxLines lines of 35 characters, reporting what fell off. Like MT, it cuts mid-word. */
    private static List<String> lines(String text, int maxLines, String tag, String what, List<String> losses) {
        String clean = NOT_X_CHARSET.matcher(text).replaceAll(".");
        if (!clean.equals(text)) {
            losses.add(":" + tag + ": " + what + " had characters outside the SWIFT X set");
        }
        List<String> out = new ArrayList<>();
        for (int i = 0; i < clean.length() && out.size() < maxLines; i += LINE) {
            out.add(clean.substring(i, Math.min(clean.length(), i + LINE)));
        }
        int kept = out.stream().mapToInt(String::length).sum();
        if (kept < clean.length()) {
            losses.add(":" + tag + ": " + what + " truncated, lost \"" + clean.substring(kept) + "\"");
        }
        return out;
    }

    private static String charges(String chrgBr) {
        return switch (chrgBr) {
            case "SHAR" -> "SHA";
            case "DEBT" -> "OUR";
            case "CRED" -> "BEN";
            default -> throw new IllegalArgumentException("Unknown ChrgBr " + chrgBr);
        };
    }

    private static String join(String... parts) {
        return String.join(" ", java.util.Arrays.stream(parts).filter(p -> p != null && !p.isBlank()).toList());
    }

    private static Element first(Element parent, String localName) {
        var nodes = parent.getElementsByTagNameNS("*", localName);
        return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
    }

    private static String text(Element parent, String localName) {
        Element element = parent == null ? null : first(parent, localName);
        return element == null ? null : element.getTextContent().strip();
    }
}
