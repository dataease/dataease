package io.dataease.visualization.utils;

import io.dataease.exception.DEException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Validates the static SVG image subset before preserving the original XML bytes. */
public final class SvgResourceValidator {

    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
    private static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";
    private static final Set<String> ELEMENTS = Set.of(
            "svg", "g", "defs", "desc", "title", "symbol", "use", "image", "switch",
            "path", "rect", "circle", "ellipse", "line", "polyline", "polygon",
            "text", "tspan", "textPath", "linearGradient", "radialGradient", "stop",
            "pattern", "clipPath", "mask", "marker", "filter", "feBlend", "feColorMatrix",
            "feComponentTransfer", "feComposite", "feConvolveMatrix", "feDiffuseLighting",
            "feDisplacementMap", "feDistantLight", "feDropShadow", "feFlood", "feFuncA",
            "feFuncB", "feFuncG", "feFuncR", "feGaussianBlur", "feImage", "feMerge",
            "feMergeNode", "feMorphology", "feOffset", "fePointLight", "feSpecularLighting",
            "feSpotLight", "feTile", "feTurbulence"
    );
    private static final Set<String> ATTRIBUTES = Set.of(
            "id", "class", "version", "baseProfile", "viewBox", "preserveAspectRatio",
            "x", "y", "x1", "y1", "x2", "y2", "dx", "dy", "width", "height",
            "cx", "cy", "r", "rx", "ry", "d", "points", "pathLength", "transform",
            "textLength", "lengthAdjust", "rotate", "startOffset", "method", "spacing",
            "gradientUnits", "gradientTransform", "spreadMethod", "fx", "fy", "fr", "offset",
            "patternUnits", "patternContentUnits", "patternTransform", "clipPathUnits",
            "maskUnits", "maskContentUnits", "markerUnits", "markerWidth", "markerHeight",
            "refX", "refY", "orient", "filterUnits", "primitiveUnits", "in", "in2", "result",
            "mode", "type", "values", "operator", "k1", "k2", "k3", "k4", "order",
            "kernelMatrix", "divisor", "bias", "targetX", "targetY", "edgeMode",
            "kernelUnitLength", "preserveAlpha", "surfaceScale", "diffuseConstant",
            "specularConstant", "specularExponent", "limitingConeAngle", "scale",
            "xChannelSelector", "yChannelSelector", "azimuth", "elevation", "stdDeviation",
            "tableValues", "slope", "intercept", "amplitude", "exponent", "radius",
            "z", "pointsAtX", "pointsAtY", "pointsAtZ", "baseFrequency", "numOctaves",
            "seed", "stitchTiles", "requiredFeatures", "requiredExtensions", "systemLanguage",
            "role", "focusable", "tabindex", "p-id", "t"
    );
    private static final Set<String> PRESENTATION_ATTRIBUTES = Set.of(
            "alignment-baseline", "baseline-shift", "clip-path", "clip-rule", "color",
            "color-interpolation", "color-interpolation-filters", "color-rendering", "direction",
            "display", "dominant-baseline", "enable-background", "fill", "fill-opacity",
            "fill-rule", "filter", "flood-color", "flood-opacity", "font-family", "font-size",
            "font-size-adjust", "font-stretch", "font-style", "font-variant", "font-weight",
            "image-rendering", "letter-spacing", "lighting-color", "marker-start", "marker-mid",
            "marker-end", "mask", "mask-type", "opacity", "overflow", "paint-order", "pointer-events",
            "shape-rendering", "stop-color", "stop-opacity", "stroke", "stroke-dasharray",
            "stroke-dashoffset", "stroke-linecap", "stroke-linejoin", "stroke-miterlimit",
            "stroke-opacity", "stroke-width", "text-anchor", "text-decoration", "text-rendering",
            "unicode-bidi", "vector-effect", "vertical-align", "visibility", "word-spacing", "writing-mode"
    );
    private static final Set<String> REFERENCE_ELEMENTS = Set.of(
            "use", "image", "textPath", "linearGradient", "radialGradient", "pattern", "feImage"
    );
    private static final Pattern FRAGMENT = Pattern.compile("#[\\p{L}\\p{N}_.:-]+");
    private static final Pattern LOCAL_URL = Pattern.compile(
            "url\\(\\s*(['\"]?)#[\\p{L}\\p{N}_.:-]+\\1\\s*\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_VALUE = Pattern.compile("[\\p{L}\\p{N}\\s_#.,%+\\-\"'():]*");
    private static final Set<String> COLOR_FUNCTIONS = Set.of("rgb", "rgba", "hsl", "hsla");

    private SvgResourceValidator() {
    }

    public static void validate(byte[] content) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try (ByteArrayInputStream input = new ByteArrayInputStream(content)) {
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(input);
            Element root = document.getDocumentElement();
            if (root == null || !SVG_NAMESPACE.equals(root.getNamespaceURI()) || !"svg".equals(root.getLocalName())) {
                reject();
            }

            // Include document-level processing instructions, not just descendants of <svg>.
            Deque<Node> nodes = new ArrayDeque<>();
            nodes.push(document);
            while (!nodes.isEmpty()) {
                Node node = nodes.pop();
                switch (node.getNodeType()) {
                    case Node.ELEMENT_NODE -> validateElement((Element) node);
                    case Node.DOCUMENT_NODE, Node.TEXT_NODE, Node.CDATA_SECTION_NODE, Node.COMMENT_NODE -> {
                    }
                    default -> reject();
                }
                for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                    nodes.push(child);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            DEException.throwException("SVG解析失败或包含不允许的XML声明");
        }
    }

    private static void validateElement(Element element) {
        if (!SVG_NAMESPACE.equals(element.getNamespaceURI()) || !ELEMENTS.contains(element.getLocalName())) {
            reject();
        }
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attribute = (Attr) attributes.item(i);
            String namespace = attribute.getNamespaceURI();
            String name = attribute.getLocalName();
            String value = attribute.getValue();
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespace)) {
                continue;
            }
            if (XMLConstants.XML_NS_URI.equals(namespace) && ("lang".equals(name) || "space".equals(name))) {
                continue;
            }
            if ("href".equals(name) && (namespace == null || XLINK_NAMESPACE.equals(namespace))) {
                if (!REFERENCE_ELEMENTS.contains(element.getLocalName()) || !FRAGMENT.matcher(value).matches()) {
                    reject();
                }
            } else if (namespace != null) {
                reject();
            } else if ("style".equals(name)) {
                validateStyle(value);
            } else if (PRESENTATION_ATTRIBUTES.contains(name)) {
                validateCssValue(value);
            } else if (!ATTRIBUTES.contains(name) && !name.startsWith("data-") && !name.startsWith("aria-")) {
                reject();
            }
        }
    }

    private static void validateStyle(String style) {
        for (String declaration : style.split(";")) {
            if (declaration.isBlank()) {
                continue;
            }
            int colon = declaration.indexOf(':');
            if (colon <= 0 || !PRESENTATION_ATTRIBUTES.contains(declaration.substring(0, colon).trim().toLowerCase(Locale.ROOT))) {
                reject();
            }
            validateCssValue(declaration.substring(colon + 1).trim());
        }
    }

    private static void validateCssValue(String value) {
        // Escapes/comments cannot be used to disguise CSS functions; URLs may only target this document.
        String withoutUrls = LOCAL_URL.matcher(value).replaceAll("");
        if (!CSS_VALUE.matcher(withoutUrls).matches()) {
            reject();
        }
        // Scan once: an unanchored identifier regex can take quadratic time on long font names.
        for (int i = 0; i < withoutUrls.length(); i++) {
            if (withoutUrls.charAt(i) != '(') {
                continue;
            }
            int end = i;
            while (end > 0 && Character.isWhitespace(withoutUrls.charAt(end - 1))) {
                end--;
            }
            int start = end;
            while (start > 0 && (Character.isLetter(withoutUrls.charAt(start - 1)) || withoutUrls.charAt(start - 1) == '-')) {
                start--;
            }
            if (!COLOR_FUNCTIONS.contains(withoutUrls.substring(start, end).toLowerCase(Locale.ROOT))) {
                reject();
            }
        }
    }

    private static void reject() {
        DEException.throwException("SVG包含不允许的元素、属性或外部引用");
    }
}
