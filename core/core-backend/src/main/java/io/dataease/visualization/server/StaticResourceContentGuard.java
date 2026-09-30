package io.dataease.visualization.server;

import org.w3c.dom.*;

import javax.imageio.ImageIO;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/** Shared allowlist for uploaded images, template resources and snapshots. */
public final class StaticResourceContentGuard {
    public static final int MAX_BYTES = 15 * 1024 * 1024;
    private static final String SVG = "http://www.w3.org/2000/svg";
    private static final Set<String> SVG_TAGS = Set.of("svg", "g", "defs", "symbol", "use", "path",
            "rect", "circle", "ellipse", "line", "polyline", "polygon", "text", "tspan", "textPath",
            "title", "desc", "linearGradient", "radialGradient", "stop", "pattern", "clipPath", "mask",
            "marker", "filter", "feBlend", "feColorMatrix", "feComponentTransfer", "feComposite",
            "feConvolveMatrix", "feDiffuseLighting", "feDisplacementMap", "feDistantLight", "feDropShadow",
            "feFlood", "feFuncA", "feFuncB", "feFuncG", "feFuncR", "feGaussianBlur", "feMerge",
            "feMergeNode", "feMorphology", "feOffset", "fePointLight", "feSpecularLighting", "feSpotLight",
            "feTile", "feTurbulence");

    private StaticResourceContentGuard() { }

    public static boolean allowedExtension(String filename) {
        return filename != null && filename.toLowerCase(Locale.ROOT).matches(".*\\.(png|jpe?g|gif|svg)");
    }

    public static void validate(String filename, byte[] content) throws Exception {
        if (!allowedExtension(filename) || content.length == 0 || content.length > MAX_BYTES) {
            throw new IOException("Invalid static resource type or size");
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (ext.equals("svg")) {
            validateSvg(content);
            return;
        }
        try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("Invalid image");
            var reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!(ext.equals(format) || ((ext.equals("jpg") || ext.equals("jpeg")) && format.equals("jpeg")))) {
                    throw new IOException("Image extension does not match content");
                }
                reader.setInput(input, true, true);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels <= 0 || pixels > 40_000_000 || reader.read(0) == null) {
                    throw new IOException("Invalid image dimensions");
                }
            } finally {
                reader.dispose();
            }
        }
    }

    private static void validateSvg(byte[] bytes) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        var document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
        Element root = document.getDocumentElement();
        if (!"svg".equals(root.getLocalName()) || !SVG.equals(root.getNamespaceURI())) {
            throw new IOException("Invalid SVG root");
        }
        // Processing instructions can load stylesheets, including document-level instructions.
        var stack = new java.util.ArrayDeque<Node>();
        stack.push(document);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) throw new IOException("SVG processing instruction");
            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) stack.push(child);
            if (!(node instanceof Element element)) continue;
            if (!SVG.equals(element.getNamespaceURI()) || !SVG_TAGS.contains(element.getLocalName())) {
                throw new IOException("Unsupported active SVG element");
            }
            NamedNodeMap attrs = element.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                String name = attr.getLocalName().toLowerCase(Locale.ROOT);
                String ns = attr.getNamespaceURI();
                if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(ns)) continue;
                if (name.startsWith("on") || (ns != null && !XMLConstants.XML_NS_URI.equals(ns)
                        && !("http://www.w3.org/1999/xlink".equals(ns) && name.equals("href")))) {
                    throw new IOException("Unsupported SVG attribute");
                }
                String value = attr.getValue().trim().toLowerCase(Locale.ROOT);
                var urls = java.util.regex.Pattern.compile("url\\(([^)]*)\\)").matcher(value);
                while (urls.find()) {
                    if (!urls.group(1).trim().matches("#[a-z0-9_.:-]+")) {
                        throw new IOException("External SVG URL");
                    }
                }
                if ((name.equals("href") && !value.startsWith("#")) || name.equals("base")
                        || value.contains("javascript:") || value.contains("vbscript:") || value.contains("data:")
                        || value.contains("@import") || value.contains("\\")
                        || (value.contains("url(") && !value.matches(".*url\\(\\s*#[a-z0-9_.:-]+\\s*\\).*"))) {
                    throw new IOException("External or active SVG reference");
                }
            }
        }
    }
}
