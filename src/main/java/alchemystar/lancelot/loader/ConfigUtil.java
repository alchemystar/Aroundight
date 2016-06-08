
package alchemystar.lancelot.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author lizhuyang
 */
public class ConfigUtil {

    public static String filter(String text) {
        return filter(text, System.getProperties());
    }

    public static String filter(String text, Properties properties) {
        StringBuilder s = new StringBuilder();
        int cur = 0;
        int textLen = text.length();
        int propStart = -1;
        int propStop = -1;
        String propName = null;
        String propValue = null;
        for (; cur < textLen; cur = propStop + 1) {
            propStart = text.indexOf("${", cur);
            if (propStart < 0) {
                break;
            }
            s.append(text.substring(cur, propStart));
            propStop = text.indexOf("}", propStart);
            if (propStop < 0) {
                throw new RuntimeException("Unterminated property: " + text.substring(propStart));
            }
            propName = text.substring(propStart + 2, propStop);
            propValue = properties.getProperty(propName);
            if (propValue == null) {
                s.append("${").append(propName).append('}');
            } else {
                s.append(propValue);
            }
        }
        return s.append(text.substring(cur)).toString();
    }

    public static Document getDocument(InputStream xml) throws ParserConfigurationException,
            SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        //不需要dtd校验
        //        builder.setEntityResolver(new EntityResolver() {
        //            @Override
        //            public InputSource resolveEntity(String publicId, String systemId) {
        //                return new InputSource(dtd);
        //            }
        //        });
        //        builder.setErrorHandler(new ErrorHandler() {
        //            @Override
        //            public void warning(SAXParseException e) {
        //            }
        //
        //            @Override
        //            public void error(SAXParseException e) throws SAXException {
        //                throw e;
        //            }
        //
        //            @Override
        //            public void fatalError(SAXParseException e) throws SAXException {
        //                throw e;
        //            }
        //        });
        return builder.parse(xml);
    }

}
