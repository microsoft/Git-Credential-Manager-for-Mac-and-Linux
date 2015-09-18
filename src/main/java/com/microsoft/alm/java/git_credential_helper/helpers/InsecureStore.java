package com.microsoft.alm.java.git_credential_helper.helpers;

import com.microsoft.alm.java.git_credential_helper.authentication.Credential;
import com.microsoft.alm.java.git_credential_helper.authentication.ISecureStore;
import com.microsoft.alm.java.git_credential_helper.authentication.Token;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
public class InsecureStore implements ISecureStore
{
    private final File backingFile;

    @XmlElement final Map<String, Token> Tokens = new HashMap<String, Token>();
    @XmlElement final Map<String, Credential> Credentials = new HashMap<String, Credential>();

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
        if (backingFile != null)
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
                IOUtils.closeQuietly(fis);
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
                IOUtils.closeQuietly(fos);
            }
        }
    }

    public static InsecureStore fromXml(final InputStream source)
    {
        try
        {
            final JAXBContext context = JAXBContext.newInstance(InsecureStore.class);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            final InsecureStore result = (InsecureStore) unmarshaller.unmarshal(source);
            return result;
        }
        catch (final JAXBException e)
        {
            Trace.writeLine("Warning: unable to deserialize InsecureStore. Is the file corrupted?");
            return null;
        }
    }

    public void toXml(final OutputStream destination)
    {
        try
        {
            final JAXBContext context = JAXBContext.newInstance(InsecureStore.class);
            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(this, destination);
        }
        catch (final JAXBException e)
        {
            throw new Error(e);
        }
    }

    @Override
    public void delete(final String targetName)
    {
        if (Tokens.containsKey(targetName))
        {
            Tokens.remove(targetName);
        }
        else if (Credentials.containsKey(targetName))
        {
            Credentials.remove(targetName);
        }
        else
        {
            throw new IllegalArgumentException("targetName '" + targetName + "' is neither a token nor a credential.");
        }
        save();
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
