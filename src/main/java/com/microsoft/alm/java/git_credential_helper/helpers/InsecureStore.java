package com.microsoft.alm.java.git_credential_helper.helpers;

import com.microsoft.alm.java.git_credential_helper.authentication.Credential;
import com.microsoft.alm.java.git_credential_helper.authentication.Token;
import org.apache.commons.io.IOUtils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class InsecureStore implements ISecureStore
{
    private final File backingFile;

    public Map<String, Token> Tokens = new HashMap<String, Token>();
    public Map<String, Credential> Credentials = new HashMap<String, Credential>();

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
                this.Tokens = clone.Tokens;
                this.Credentials = clone.Credentials;
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
            FileOutputStream fos = null;
            try
            {
                fos = new FileOutputStream(backingFile);
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
        final XMLDecoder decoder = new XMLDecoder(source);
        final InsecureStore result = (InsecureStore) decoder.readObject();
        decoder.close();
        return result;
    }

    public void toXml(final PrintStream destination)
    {
        final XMLEncoder encoder = new XMLEncoder(destination);
        encoder.writeObject(this);
        encoder.close();
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
