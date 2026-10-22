package io.dataease.utils;

import org.w3c.dom.*;

import javax.imageio.ImageIO;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/** Shared allowlist for uploaded images, template resources and snapshots. */
public final class ImageContentGuard {
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

    private ImageContentGuard() { }

    public static boolean allowedExtension(String filename) {
        return filename != null && filename.toLowerCase(Locale.ROOT).matches(".*\\.(png|jpe?g|gif|svg)");
    }

    public static void validate(String filename, byte[] content) throws Exception {
        validate(filename, content, false);
    }

    public static void validateMaterial(String filename, byte[] content) throws Exception {
        validate(filename, content, true);
    }

    private static void validate(String filename, byte[] content, boolean strict) throws Exception {
        if (!allowedExtension(filename) || content.length == 0 || content.length > MAX_BYTES) {
            throw new IOException("Invalid static resource type or size");
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (ext.equals("svg")) {
            validateSvg(content, strict);
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
                reader.setInput(input, false, true);
                int frames = ext.equals("gif") ? reader.getNumImages(true) : 1;
                if (frames < 1 || frames > 200) throw new IOException("Too many image frames");
                long decodedPixels = 0;
                for (int frame = 0; frame < frames; frame++) {
                    long pixels = (long) reader.getWidth(frame) * reader.getHeight(frame);
                    decodedPixels += pixels;
                    if (pixels <= 0 || pixels > 40_000_000 || decodedPixels > 80_000_000
                            || reader.read(frame) == null) throw new IOException("Invalid image dimensions");
                }
            } finally {
                reader.dispose();
            }
        }
    }

    private static void validateSvg(byte[] bytes, boolean strict) throws Exception {
        if (strict) {
            var streaming = javax.xml.stream.XMLInputFactory.newFactory();
            streaming.setProperty(javax.xml.stream.XMLInputFactory.SUPPORT_DTD, false);
            streaming.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
            var reader = streaming.createXMLStreamReader(new ByteArrayInputStream(bytes));
            int nodes = 0;
            int depth = 0;
            long characters = 0;
            try {
                while (reader.hasNext()) {
                    int event = reader.next();
                    if (event == javax.xml.stream.XMLStreamConstants.DTD) throw new IOException("SVG DTD");
                    if (event == javax.xml.stream.XMLStreamConstants.START_ELEMENT) {
                        if (++nodes > 4096 || ++depth > 64 || reader.getAttributeCount() > 64) throw new IOException("SVG too complex");
                        for (int i = 0; i < reader.getAttributeCount(); i++) characters += reader.getAttributeValue(i).length();
                    } else if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT) depth--;
                    else if (reader.hasText()) characters += reader.getTextLength();
                    if (characters > 1000000) throw new IOException("SVG too complex");
                }
            } finally { reader.close(); }
        }
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
        int nodes = 0;
        int attributes = 0;
        int characters = 0;
        while (!stack.isEmpty()) {
            if (++nodes > 4096) throw new IOException("SVG too complex");
            Node node = stack.pop();
            if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) throw new IOException("SVG processing instruction");
            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) stack.push(child);
            if (!(node instanceof Element element)) continue;
            if (!SVG.equals(element.getNamespaceURI()) || !SVG_TAGS.contains(element.getLocalName())
                    || (strict && (Set.of("use", "filter", "pattern", "feImage", "mask", "clipPath", "marker", "textPath").contains(element.getLocalName())
                    || element.getLocalName().startsWith("fe")))) {
                throw new IOException("Unsupported active SVG element");
            }
            NamedNodeMap attrs = element.getAttributes();
            attributes += attrs.getLength();
            if (attrs.getLength() > 64 || attributes > 16384) throw new IOException("SVG too complex");
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
                if (strict && (Set.of("href", "mask", "clip-path", "filter", "marker-start", "marker-mid", "marker-end").contains(name))) {
                    throw new IOException("Unsupported recursive SVG reference");
                }
                characters += value.length();
                if (value.length() > 200000 || characters > 1000000) throw new IOException("SVG too complex");
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
