// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialmanager;

import com.microsoft.alm.authentication.Credential;
import com.microsoft.alm.authentication.ISecureStore;
import com.microsoft.alm.authentication.Token;
import com.microsoft.alm.authentication.TokenType;
import com.microsoft.alm.helpers.IOHelper;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class InsecureStoreTest
{
    /**
     * {@link InsecureStore#delete(String)} must not throw an exception for an invalid key,
     * because when entering incorrect credentials, git will issue an "erase" command on an entry
     * that may not actually be there, so we shouldn't panic and instead just calmly carry on.
     */
    @Test public void delete_noMatchingTokenOrCredential()
    {
        final InsecureStore cut = new InsecureStore(null);

        cut.delete("foo");
    }

    @Test public void fromXml()
    {
        ByteArrayInputStream bais = null;
        try
        {
            final String xmlString =
                    "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\n" +
                    "<insecureStore>\n" +
                    "    <Tokens/>\n" +
                    "    <Credentials>\n" +
                    "        <entry>\n" +
                    "            <key>git:https://server.example.com</key>\n" +
                    "            <value>\n" +
                    "                <Password>swordfish</Password>\n" +
                    "                <Username>j.travolta</Username>\n" +
                    "            </value>\n" +
                    "        </entry>\n" +
                    "    </Credentials>\n" +
                    "</insecureStore>";
            bais = new ByteArrayInputStream(xmlString.getBytes());

            final InsecureStore actual = InsecureStore.fromXml(bais);

            Assert.assertNotNull(actual);
            Assert.assertEquals(1, actual.Credentials.size());
            final Credential credential = actual.Credentials.get("git:https://server.example.com");
            Assert.assertEquals("swordfish", credential.Password);
            Assert.assertEquals("j.travolta", credential.Username);
        }
        finally
        {
            IOHelper.closeQuietly(bais);
        }
    }

    @Test public void serialization_instanceToXmlToInstance()
    {
        final InsecureStore input = new InsecureStore(null);
        initializeTestData(input);

        final InsecureStore actual = clone(input);

        verifyTestData(actual);
    }

    @Test public void migrateAndDisable_noBackingFile()
    {
        final InsecureStore input = new InsecureStore(null);
        initializeTestData(input);
        final InsecureStore actual = new InsecureStore(null);

        input.migrateAndDisable(actual);

        verifyTestData(actual);
    }

    @Test public void migrateAndDisable_withBackingFile() throws Exception
    {
        File tempFile = null;
        try
        {
            tempFile = File.createTempFile(this.getClass().getSimpleName(), null);
            Assert.assertEquals(0L, tempFile.length());

            final InsecureStore input = new InsecureStore(tempFile);
            initializeTestData(input);
            Assert.assertNotEquals(0L, tempFile.length());
            final InsecureStore actual = new InsecureStore(null);

            input.migrateAndDisable(actual);

            verifyTestData(actual);
            Assert.assertFalse(tempFile.isFile());
            final File migratedFile = new File(tempFile.getAbsolutePath() + InsecureStore.MIGRATION_SUFFIX);
            Assert.assertTrue(migratedFile.isFile());
            //noinspection ResultOfMethodCallIgnored
            migratedFile.delete();
        }
        finally
        {
            if (tempFile != null)
                //noinspection ResultOfMethodCallIgnored
                tempFile.delete();
        }
    }

    private static void initializeTestData(final ISecureStore input)
    {
        final Token inputBravo = new Token("42", TokenType.Test);
        input.writeToken("alpha", null);
        input.writeToken("bravo", inputBravo);
        input.writeCredential("charlie", null);
        final Credential inputDelta = new Credential("douglas.adams", "42");
        input.writeCredential("delta", inputDelta);
    }

    private void verifyTestData(final InsecureStore actual)
    {
        Assert.assertEquals(2, actual.Tokens.size());
        Assert.assertTrue(actual.Tokens.containsKey("alpha"));
        final Token actualBravo = actual.Tokens.get("bravo");
        Assert.assertEquals("42", actualBravo.Value);
        Assert.assertEquals(TokenType.Test, actualBravo.Type);
        Assert.assertFalse(actual.Tokens.containsKey("charlie"));

        Assert.assertEquals(2, actual.Credentials.size());
        Assert.assertTrue(actual.Credentials.containsKey("charlie"));
        final Credential actualDelta = actual.Credentials.get("delta");
        Assert.assertEquals("douglas.adams", actualDelta.Username);
        Assert.assertEquals("42", actualDelta.Password);
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

    @Test public void save_toFile() throws IOException
    {
        File tempFile = null;
        try
        {
            tempFile = File.createTempFile(this.getClass().getSimpleName(), null);
            final InsecureStore cut = new InsecureStore(tempFile);

            cut.save();

            Assert.assertTrue(tempFile.length() > 0);
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

            inputStore.toXml(baos);

            final String xmlString = baos.toString();

            bais = new ByteArrayInputStream(xmlString.getBytes());
            final InsecureStore result = InsecureStore.fromXml(bais);

            return result;
        }
        finally
        {
            IOHelper.closeQuietly(baos);
            IOHelper.closeQuietly(bais);
        }
    }
}
