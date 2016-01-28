// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialmanager;

import com.microsoft.alm.authentication.Credential;
import com.microsoft.alm.authentication.ISecureStore;
import com.microsoft.alm.authentication.Token;
import com.microsoft.alm.helpers.IOHelper;
import com.microsoft.alm.helpers.Trace;
import com.microsoft.alm.helpers.XmlHelper;
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

public class InsecureStore implements ISecureStore
{
    static final String MIGRATION_SUFFIX = ".old";
    private final File backingFile;

    final Map<String, Token> Tokens = new HashMap<String, Token>();
    final Map<String, Credential> Credentials = new HashMap<String, Credential>();

    private boolean isEnabled = true;

    /**
     * Creates an instance that only keeps the values in memory, never touching a file.
     *
     * @deprecated If you need an in-memory store, use SecretCache.
     */
    @Deprecated
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
            // TODO: 449510: consider creating a backup of the file, if it exists, before overwriting it
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
            if (!backingFile.setReadable(false, false)
                || !backingFile.setWritable(false, false)
                || !backingFile.setExecutable(false, false))
            {
                Trace.writeLine("Unable to remove file permissions for everybody: " + backingFile);
            }
            if (!backingFile.setReadable(true, true)
                || !backingFile.setWritable(true, true)
                || !backingFile.setExecutable(false, true))
            {
                Trace.writeLine("Unable to set file permissions for owner: " + backingFile);
            }
        }
    }

    static InsecureStore fromXml(final InputStream source)
    {
        try
        {
            final InsecureStore result = new InsecureStore(null);
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
                key = XmlHelper.getText(keyOrValueNode);
            }
            else if ("value".equals(keyOrValueName))
            {
                value = Credential.fromXml(keyOrValueNode);
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
                key = XmlHelper.getText(keyOrValueNode);
            }
            else if ("value".equals(keyOrValueName))
            {
                value = Token.fromXml(keyOrValueNode);
            }
        }
        result.Tokens.put(key, value);
    }

    void toXml(final OutputStream destination)
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
                final Element valueNode = value.toXml(document);

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
                final Element valueNode = value.toXml(document);

                entryNode.appendChild(valueNode);
            }
            credentialsNode.appendChild(entryNode);
        }
        return credentialsNode;
    }

    private void ensureEnabled()
    {
        if (!isEnabled)
        {
            throw new IllegalStateException("This InsecureStore has been disabled.");
        }
    }

    @Override
    public synchronized void delete(final String targetName)
    {
        ensureEnabled();

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
    public synchronized Credential readCredentials(final String targetName)
    {
        ensureEnabled();

        return Credentials.get(targetName);
    }

    @Override
    public synchronized Token readToken(final String targetName)
    {
        ensureEnabled();

        return Tokens.get(targetName);
    }

    @Override
    public synchronized void writeCredential(final String targetName, final Credential credentials)
    {
        ensureEnabled();

        Credentials.put(targetName, credentials);
        save();
    }

    @Override
    public synchronized void writeToken(final String targetName, final Token token)
    {
        ensureEnabled();

        Tokens.put(targetName, token);
        save();
    }

    /**
     * Exports all tokens and credentials to the specified {@link ISecureStore}
     * and then the polite thing to do is to rename the backing file
     * and prevent further use via this instance.
     * This will still make a downgrade possible
     * and the archival of the old file will be the user's responsibility.
     *
     * @param destination the {@link ISecureStore} that will replace this {@link InsecureStore}
     */
    public synchronized void migrateAndDisable(final ISecureStore destination)
    {
        ensureEnabled();

        for (final Map.Entry<String, Token> pair : Tokens.entrySet())
        {
            final String targetName = pair.getKey();
            final Token token = pair.getValue();
            destination.writeToken(targetName, token);
        }

        for (final Map.Entry<String, Credential> pair : Credentials.entrySet())
        {
            final String targetName = pair.getKey();
            final Credential credential = pair.getValue();
            destination.writeCredential(targetName, credential);
        }

        if (backingFile != null)
        {
            final File disabledBackingFile = new File(backingFile.getAbsolutePath() + MIGRATION_SUFFIX);
            final boolean wasRenamed = backingFile.renameTo(disabledBackingFile);
            if (!wasRenamed)
            {
                final String renameFailureTemplate = "Unable to rename '%1$s' to '%2$s' after migrating its contents.";
                final String renameFailureMessage = String.format(renameFailureTemplate, backingFile.getAbsolutePath(), disabledBackingFile.getAbsolutePath());
                throw new Error(renameFailureMessage);
            }
        }

        isEnabled = false;
    }
}
