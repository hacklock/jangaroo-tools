package net.jangaroo.exml.parser;

import net.jangaroo.exml.ExmlConstants;
import net.jangaroo.exml.ExmlParseException;
import net.jangaroo.exml.model.ConfigAttribute;
import net.jangaroo.exml.model.ConfigClass;
import net.jangaroo.utils.CharacterRecordingHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Generates an internal representation of all metadata of the component described by the given EXML.
 */
public class ExmlMetadataHandler extends CharacterRecordingHandler {
  private ConfigClass configClass;

  private Deque<QName> elementPath = new LinkedList<QName>();

  public ExmlMetadataHandler(ConfigClass configClass) {
    this.configClass = configClass;
  }

  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    if (ExmlConstants.isExmlNamespace(uri)) {
      if (ExmlConstants.EXML_CFG_NODE_NAME.equals(localName)) {
        //handle config elements
        configClass.addCfg(new ConfigAttribute(atts.getValue(ExmlConstants.EXML_CFG_NAME_ATTRIBUTE), atts.getValue(ExmlConstants.EXML_CFG_TYPE_ATTRIBUTE), null));
      } else if (ExmlConstants.EXML_DESCRIPTION_NODE_NAME.equals(localName)) {
        if (isLastInPathComponent() || isLastInPathConfig()) {
          // start recording characters of the description:
          startRecordingCharacters();
        }
      }
    } else if (elementPath.size() == 1) {
      if (configClass.getSuperClassName() != null) {
        // todo: line number
        throw new ExmlParseException("root node of EXML contained more than one component definition");
      }

      String thePackage = ExmlConstants.parsePackageFromNamespace(uri);
      if (thePackage == null) {
        throw new ExmlParseException("namespace '" + uri + "' of superclass element in EXML file does not denote a config package");
      }
      configClass.setSuperClassName(thePackage + "." + localName);
    }
    elementPath.push(new QName(uri, localName));
  }

  private boolean isLastInPathComponent() {
    QName parent = elementPath.peek();
    return ExmlConstants.isExmlNamespace(parent.getNamespaceURI()) && ExmlConstants.EXML_COMPONENT_NODE_NAME.equals(parent.getLocalPart());
  }

  private boolean isLastInPathConfig() {
    QName parent = elementPath.peek();
    return ExmlConstants.isExmlNamespace(parent.getNamespaceURI()) && ExmlConstants.EXML_CFG_NODE_NAME.equals(parent.getLocalPart());
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (ExmlConstants.isExmlNamespace(uri)) {
      elementPath.pop();
      if (ExmlConstants.EXML_DESCRIPTION_NODE_NAME.equals(localName)) {
        String characters = popRecordedCharacters();
        if (characters != null) {
          if (isLastInPathConfig()) {
            configClass.getCfgs().get(configClass.getCfgs().size() - 1).setDescription(characters.trim());
          } else if (isLastInPathComponent()) {
            configClass.setDescription(characters.trim());
          }
        }
      }
    }
  }
}