package com.microsoft.alm.java.git_credential_helper.helpers;

import com.microsoft.alm.java.git_credential_helper.authentication.Credential;
import com.microsoft.alm.java.git_credential_helper.authentication.Token;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class InsecureStore implements ISecureStore
{
    public Map<String, Token> Tokens = new HashMap<String, Token>();
    public Map<String, Credential> Credentials = new HashMap<String, Credential>();

    public InsecureStore()
    {

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

    }

    @Override
    public Credential readCredentials(final String targetName)
    {
        return null;
    }

    @Override
    public Token readToken(final String targetName)
    {
        return null;
    }

    @Override
    public void writeCredential(final String targetName, final Credential credentials)
    {

    }

    @Override
    public void writeToken(final String targetName, final Token token)
    {

    }
}
