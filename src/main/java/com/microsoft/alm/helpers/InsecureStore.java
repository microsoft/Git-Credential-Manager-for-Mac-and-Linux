// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import com.microsoft.alm.authentication.Credential;
import com.microsoft.alm.authentication.ISecureStore;
import com.microsoft.alm.authentication.Token;
import com.microsoft.alm.authentication.TokenType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InsecureStore implements ISecureStore
{
    private final File backingFile;

    final Map<String, Token> Tokens = new HashMap<String, Token>();
    final Map<String, Credential> Credentials = new HashMap<String, Credential>();

    /**
     * Creates an instance that only keeps the values in memory, never touching a file.
     */
    public InsecureStore()
    {
        this(null);
    }

    /**
     * Creates an instance that reads from and writes to the specified backingFile.
     *
     * @param backingFile the file to read from and write to.  Does not need to exist first.
     */
    public InsecureStore(final File backingFile)
    {
        this.backingFile = backingFile;
        reload();
    }

    void reload()
    {
        if (backingFile != null && backingFile.isFile() && backingFile.length() > 0)
        {
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(backingFile);
                final InsecureStore clone = fromXml(fis);
                if (clone != null)
                {
                    this.Tokens.clear();
                    this.Tokens.putAll(clone.Tokens);

                    this.Credentials.clear();
                    this.Credentials.putAll(clone.Credentials);
                }
            }
            catch (FileNotFoundException e)
            {
                Trace.writeLine("backingFile '" + backingFile.getAbsolutePath() + "' did not exist.");
            }
            finally
            {
                IOHelper.closeQuietly(fis);
            }
        }
    }

    void save()
    {
        if (backingFile != null)
        {
            // TODO: consider creating a backup of the file, if it exists, before overwriting it
            FileOutputStream fos = null;
            try
            {
                fos = new FileOutputStream(backingFile);
                toXml(fos);
            }
            catch (final FileNotFoundException e)
            {
                throw new Error("Error during save()", e);
            }
            finally
            {
                IOHelper.closeQuietly(fos);
            }
        }
    }

    public static InsecureStore fromXml(final InputStream source)
    {
        try
        {
            final InsecureStore result = new InsecureStore();
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            final Document document = builder.parse(source);
            final Element insecureStoreElement = document.getDocumentElement();

            final NodeList tokensOrCredentialsList = insecureStoreElement.getChildNodes();
            for (int toc = 0; toc < tokensOrCredentialsList.getLength(); toc++)
            {
                final Node tokensOrCredentials = tokensOrCredentialsList.item(toc);
                if (tokensOrCredentials.getNodeType() != Node.ELEMENT_NODE)
                    continue;
                if ("Tokens".equals(tokensOrCredentials.getNodeName()))
                {
                    result.Tokens.clear();
                }
                else if ("Credentials".equals(tokensOrCredentials.getNodeName()))
                {
                    result.Credentials.clear();
                }
                else continue;
                final NodeList entryList = tokensOrCredentials.getChildNodes();
                for (int e = 0; e < entryList.getLength(); e++)
                {
                    final Node entryNode = entryList.item(e);
                    if (entryNode.getNodeType() != Node.ELEMENT_NODE || !"entry".equals(entryNode.getNodeName())) continue;
                    if ("Tokens".equals(tokensOrCredentials.getNodeName()))
                    {
                        loadToken(result, entryNode);
                    }
                    else if ("Credentials".equals(tokensOrCredentials.getNodeName()))
                    {
                        loadCredential(result, entryNode);
                    }
                }
            }
            return result;
        }
        catch (final Exception e)
        {
            Trace.writeLine("Warning: unable to deserialize InsecureStore. Is the file corrupted?");
            return null;
        }
    }

    private static void loadCredential(final InsecureStore result, final Node entryNode)
    {
        String key = null;
        Credential value = null;
        final NodeList keyOrValueList = entryNode.getChildNodes();
        for (int kov = 0; kov < keyOrValueList.getLength(); kov++)
        {
            final Node keyOrValueNode = keyOrValueList.item(kov);
            if (keyOrValueNode.getNodeType() != Node.ELEMENT_NODE) continue;

            final String keyOrValueName = keyOrValueNode.getNodeName();
            if ("key".equals(keyOrValueName))
            {
                key = getText(keyOrValueNode);
            }
            else if ("value".equals(keyOrValueName))
            {
                String password = null;
                String username = null;

                final NodeList valueNodes = keyOrValueNode.getChildNodes();
                for (int v = 0; v < valueNodes.getLength(); v++)
                {
                    final Node valueNode = valueNodes.item(v);
                    if (valueNode.getNodeType() != Node.ELEMENT_NODE) continue;

                    final String attributeName = valueNode.getNodeName();
                    if ("Password".equals(attributeName))
                    {
                        password = getText(valueNode);
                    }
                    else if ("Username".equals(attributeName))
                    {
                        username = getText(valueNode);
                    }
                }
                value = new Credential(username, password);
            }
        }
        result.Credentials.put(key, value);
    }

    private static void loadToken(final InsecureStore result, final Node entryNode)
    {
        String key = null;
        Token value = null;
        final NodeList keyOrValueList = entryNode.getChildNodes();
        for (int kov = 0; kov < keyOrValueList.getLength(); kov++)
        {
            final Node keyOrValueNode = keyOrValueList.item(kov);
            if (keyOrValueNode.getNodeType() != Node.ELEMENT_NODE) continue;
            final String keyOrValueName = keyOrValueNode.getNodeName();
            if ("key".equals(keyOrValueName))
            {
                key = getText(keyOrValueNode);
            }
            else if ("value".equals(keyOrValueName))
            {
                String tokenValue = null;
                TokenType tokenType = null;
                UUID targetIdentity = Guid.Empty;

                final NodeList valueNodes = keyOrValueNode.getChildNodes();
                for (int v = 0; v < valueNodes.getLength(); v++)
                {
                    final Node valueNode = valueNodes.item(v);
                    final String attributeName = valueNode.getNodeName();
                    if ("Type".equals(attributeName))
                    {
                        tokenType = TokenType.valueOf(TokenType.class, getText(valueNode));
                    }
                    else if ("Value".equals(attributeName))
                    {
                        tokenValue = getText(valueNode);
                    }
                    else if ("targetIdentity".equals(attributeName))
                    {
                        targetIdentity = UUID.fromString(getText(valueNode));
                    }
                }
                value = new Token(tokenValue, tokenType);
                value.setTargetIdentity(targetIdentity);
            }
        }
        result.Tokens.put(key, value);
    }

    // Adapted from http://docs.oracle.com/javase/tutorial/jaxp/dom/readingXML.html
    private static String getText(final Node node) {
        final StringBuilder result = new StringBuilder();
        if (! node.hasChildNodes()) return "";

        final NodeList list = node.getChildNodes();
        for (int i=0; i < list.getLength(); i++) {
            Node subnode = list.item(i);
            if (subnode.getNodeType() == Node.TEXT_NODE) {
                result.append(subnode.getNodeValue());
            }
            else if (subnode.getNodeType() == Node.CDATA_SECTION_NODE) {
                result.append(subnode.getNodeValue());
            }
            else if (subnode.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
                // Recurse into the subtree for text
                // (and ignore comments)
                result.append(getText(subnode));
            }
        }

        return result.toString();
    }

    public void toXml(final OutputStream destination)
    {
        try
        {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            final Document document = builder.newDocument();

            final Element insecureStoreNode = document.createElement("insecureStore");
            insecureStoreNode.appendChild(createTokensNode(document));
            insecureStoreNode.appendChild(createCredentialsNode(document));
            document.appendChild(insecureStoreNode);

            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            //http://johnsonsolutions.blogspot.ca/2007/08/xml-transformer-indent-doesnt-work-with.html
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(document), new StreamResult(destination));
        }
        catch (final Exception e)
        {
            throw new Error(e);
        }
    }

    private Element createTokensNode(final Document document)
    {
        final Element tokensNode = document.createElement("Tokens");
        for (final Map.Entry<String, Token> entry : Tokens.entrySet())
        {
            final Element entryNode = document.createElement("entry");

            final Element keyNode = document.createElement("key");
            final Text keyValue = document.createTextNode(entry.getKey());
            keyNode.appendChild(keyValue);
            entryNode.appendChild(keyNode);

            final Token value = entry.getValue();
            if (value != null)
            {
                final Element valueNode = document.createElement("value");

                final Element typeNode = document.createElement("Type");
                final Text typeValue = document.createTextNode(value.Type.toString());
                typeNode.appendChild(typeValue);
                valueNode.appendChild(typeNode);

                final Element tokenValueNode = document.createElement("Value");
                final Text valueValue = document.createTextNode(value.Value);
                tokenValueNode.appendChild(valueValue);
                valueNode.appendChild(tokenValueNode);

                if (!Guid.Empty.equals(value.getTargetIdentity()))
                {
                    final Element targetIdentityNode = document.createElement("targetIdentity");
                    final Text targetIdentityValue = document.createTextNode(value.getTargetIdentity().toString());
                    targetIdentityNode.appendChild(targetIdentityValue);
                    valueNode.appendChild(targetIdentityNode);
                }

                entryNode.appendChild(valueNode);
            }

            tokensNode.appendChild(entryNode);
        }
        return tokensNode;
    }

    private Element createCredentialsNode(final Document document)
    {
        final Element credentialsNode = document.createElement("Credentials");
        for (final Map.Entry<String, Credential> entry : Credentials.entrySet())
        {
            final Element entryNode = document.createElement("entry");

            final Element keyNode = document.createElement("key");
            final Text keyValue = document.createTextNode(entry.getKey());
            keyNode.appendChild(keyValue);
            entryNode.appendChild(keyNode);

            final Credential value = entry.getValue();
            if (value != null)
            {
                final Element valueNode = document.createElement("value");

                final Element passwordNode = document.createElement("Password");
                final Text passwordValue = document.createTextNode(value.Password);
                passwordNode.appendChild(passwordValue);
                valueNode.appendChild(passwordNode);

                final Element usernameNode = document.createElement("Username");
                final Text usernameValue = document.createTextNode(value.Username);
                usernameNode.appendChild(usernameValue);
                valueNode.appendChild(usernameNode);

                entryNode.appendChild(valueNode);
            }
            credentialsNode.appendChild(entryNode);
        }
        return credentialsNode;
    }

    @Override
    public void delete(final String targetName)
    {
        if (Tokens.containsKey(targetName))
        {
            Tokens.remove(targetName);
            save();
        }
        else if (Credentials.containsKey(targetName))
        {
            Credentials.remove(targetName);
            save();
        }
    }

    @Override
    public Credential readCredentials(final String targetName)
    {
        return Credentials.get(targetName);
    }

    @Override
    public Token readToken(final String targetName)
    {
        return Tokens.get(targetName);
    }

    @Override
    public void writeCredential(final String targetName, final Credential credentials)
    {
        Credentials.put(targetName, credentials);
        save();
    }

    @Override
    public void writeToken(final String targetName, final Token token)
    {
        Tokens.put(targetName, token);
        save();
    }
}
