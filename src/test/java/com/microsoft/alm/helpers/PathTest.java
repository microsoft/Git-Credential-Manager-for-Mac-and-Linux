package com.microsoft.alm.helpers;

import org.junit.Assert;
import org.junit.Test;

public class PathTest
{
    @Test public void changeExtension_single()
    {
        final String goodFileName = "C:\\mydir\\myfile.com";

        final String actual = Path.changeExtension(goodFileName, ".old");

        Assert.assertEquals("C:\\mydir\\myfile.old", actual);
    }

    @Test public void changeExtension_singleWithoutLeadingPeriod()
    {
        final String goodFileName = "C:\\mydir\\myfile.com";

        final String actual = Path.changeExtension(goodFileName, "old");

        Assert.assertEquals("C:\\mydir\\myfile.old", actual);
    }

    @Test public void changeExtension_multiple()
    {
        final String goodFileName = "C:\\mydir\\myfile.com.extension";

        final String actual = Path.changeExtension(goodFileName, ".old");

        Assert.assertEquals("C:\\mydir\\myfile.com.old", actual);
    }

    @Test public void changeExtension_badFileName()
    {
        final String badFileName = "C:\\mydir\\";

        final String actual = Path.changeExtension(badFileName, ".old");

        Assert.assertEquals("C:\\mydir\\.old", actual);
    }

    // If extension is null, the returned string contains the contents of path
    // with the last period and all characters following it removed.
    @Test public void changeExtension_nullExtensionRemovesIt()
    {
        final String goodFileName = "C:\\mydir\\myfile.com.extension";

        final String actual = Path.changeExtension(goodFileName, null);

        Assert.assertEquals("C:\\mydir\\myfile.com", actual);
    }

    // If extension is an empty string, the returned path string contains the contents of path
    // with any characters following the last period removed.
    @Test public void changeExtension_emptyExtensionRemovesIt()
    {
        final String goodFileName = "C:\\mydir\\myfile.com.extension";

        final String actual = Path.changeExtension(goodFileName, StringHelper.Empty);

        Assert.assertEquals("C:\\mydir\\myfile.com.", actual);
    }
}
