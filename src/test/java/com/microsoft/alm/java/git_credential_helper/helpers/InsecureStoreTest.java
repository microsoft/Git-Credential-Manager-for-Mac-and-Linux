package com.microsoft.alm.java.git_credential_helper.helpers;

import com.microsoft.alm.java.git_credential_helper.authentication.Token;
import com.microsoft.alm.java.git_credential_helper.authentication.TokenType;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class InsecureStoreTest
{
    @Test public void serialization_instanceToXmlToInstance()
    {
        final InsecureStore input = new InsecureStore();
        final Token inputBravo = new Token("42", TokenType.Test);
        input.Tokens.put("alpha", null);
        input.Tokens.put("bravo", inputBravo);
        input.Credentials.put("charlie", null);

        final InsecureStore actual = clone(input);

        Assert.assertEquals(2, actual.Tokens.size());
        Assert.assertTrue(actual.Tokens.containsKey("alpha"));
        final Token actualBravo = actual.Tokens.get("bravo");
        Assert.assertEquals("42", actualBravo.Value);
        Assert.assertFalse(actual.Tokens.containsKey("charlie"));

        Assert.assertEquals(1, actual.Credentials.size());
        Assert.assertTrue(actual.Credentials.containsKey("charlie"));
    }

    @Test public void reload_emptyFile() throws IOException
    {
        File tempFile = null;
        try
        {
            tempFile = File.createTempFile(this.getClass().getSimpleName(), null);
            Assert.assertEquals(0L, tempFile.length());

            final InsecureStore cut = new InsecureStore(tempFile);

            Assert.assertEquals(0, cut.Tokens.size());
            Assert.assertEquals(0, cut.Credentials.size());
        }
        finally
        {
            if (tempFile != null)
                tempFile.delete();
        }
    }

    static InsecureStore clone(InsecureStore inputStore)
    {
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        try
        {
            baos = new ByteArrayOutputStream();
            final PrintStream ps = new PrintStream(baos);

            inputStore.toXml(ps);
            ps.flush();

            final String xmlString = baos.toString();

            bais = new ByteArrayInputStream(xmlString.getBytes());
            final InsecureStore result = InsecureStore.fromXml(bais);

            return result;
        }
        finally
        {
            IOUtils.closeQuietly(baos);
            IOUtils.closeQuietly(bais);
        }
    }
}
