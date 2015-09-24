package com.microsoft.alm.java.git_credential_helper.authentication;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;

public class VsoTokenScopeTest
{
    @Test
    public void AddOperator()
    {
        VsoTokenScope val = VsoTokenScope.add(VsoTokenScope.BuildAccess, VsoTokenScope.TestRead);
        Assert.assertEquals(VsoTokenScope.BuildAccess.getValue() + " " + VsoTokenScope.TestRead.getValue(), val.getValue());

        val = VsoTokenScope.add(val, VsoTokenScope.ProfileRead);
        Assert.assertEquals(VsoTokenScope.BuildAccess.getValue() + " " + VsoTokenScope.ProfileRead.getValue() + " " + VsoTokenScope.TestRead.getValue(), val.getValue());
    }
    
    @Test
    public void AndOperator()
    {
        VsoTokenScope val = VsoTokenScope.and(VsoTokenScope.BuildAccess, VsoTokenScope.BuildAccess);
        Assert.assertEquals(VsoTokenScope.BuildAccess, val);

        val = VsoTokenScope.add(
                VsoTokenScope.add(VsoTokenScope.ProfileRead, VsoTokenScope.PackagingWrite),
                VsoTokenScope.BuildAccess);
        Assert.assertEquals(VsoTokenScope.ProfileRead, VsoTokenScope.and(val, VsoTokenScope.ProfileRead));
        Assert.assertEquals(VsoTokenScope.PackagingWrite, VsoTokenScope.and(val, VsoTokenScope.PackagingWrite));
        Assert.assertEquals(VsoTokenScope.BuildAccess, VsoTokenScope.and(val, VsoTokenScope.BuildAccess));
        Assert.assertEquals(VsoTokenScope.None, VsoTokenScope.and(val, VsoTokenScope.PackagingManage));
    }
    
    @Test
    public void Equality()
    {
        Assert.assertEquals(VsoTokenScope.CodeWrite, VsoTokenScope.CodeWrite);
        Assert.assertEquals(VsoTokenScope.None, VsoTokenScope.None);
        
        Assert.assertNotEquals(VsoTokenScope.BuildAccess, VsoTokenScope.CodeRead);
        Assert.assertNotEquals(VsoTokenScope.BuildAccess, VsoTokenScope.None);
        
        Assert.assertEquals(VsoTokenScope.or(VsoTokenScope.or(VsoTokenScope.PackagingManage, VsoTokenScope.PackagingRead), VsoTokenScope.PackagingWrite), VsoTokenScope.or(VsoTokenScope.or(VsoTokenScope.PackagingManage, VsoTokenScope.PackagingRead), VsoTokenScope.PackagingWrite));
        Assert.assertEquals(VsoTokenScope.or(VsoTokenScope.or(VsoTokenScope.PackagingWrite, VsoTokenScope.PackagingManage), VsoTokenScope.PackagingRead), VsoTokenScope.or(VsoTokenScope.or(VsoTokenScope.PackagingManage, VsoTokenScope.PackagingRead), VsoTokenScope.PackagingWrite));

        Assert.assertNotEquals(VsoTokenScope.or(VsoTokenScope.or(VsoTokenScope.PackagingManage, VsoTokenScope.ServiceHookRead), VsoTokenScope.PackagingWrite), VsoTokenScope.or(VsoTokenScope.or(VsoTokenScope.PackagingManage, VsoTokenScope.PackagingRead), VsoTokenScope.PackagingWrite));
        Assert.assertNotEquals(VsoTokenScope.or(VsoTokenScope.or(VsoTokenScope.PackagingManage, VsoTokenScope.PackagingRead), VsoTokenScope.PackagingWrite), VsoTokenScope.or(VsoTokenScope.PackagingManage, VsoTokenScope.PackagingRead));
    }

    @Test
    public void HashCode()
    {
        final HashSet<Integer> hashCodes = new HashSet<Integer>();

        final Iterator<VsoTokenScope> it = VsoTokenScope.enumerateValues();
        while (it.hasNext())
        {
            final VsoTokenScope item = it.next();
            Assert.assertTrue(hashCodes.add(item.hashCode()));
        }

        int loop1 = 0;
        final Iterator<VsoTokenScope> outerIt = VsoTokenScope.enumerateValues();
        while (outerIt.hasNext())
        {
            final VsoTokenScope item1 = outerIt.next();
            int loop2 = 0;

            final Iterator<VsoTokenScope> innerIt = VsoTokenScope.enumerateValues();
            while (innerIt.hasNext())
            {
                final VsoTokenScope item2 = innerIt.next();
                final VsoTokenScope orred = VsoTokenScope.or(item1, item2);
                if (loop1 < loop2)
                {
                    Assert.assertTrue(hashCodes.add(orred.hashCode()));
                }
                else
                {
                    Assert.assertFalse(hashCodes.add(orred.hashCode()));
                }

                loop2++;
            }

            loop1++;
        }
    }

    @Test
    public void OrOperator()
    {
        VsoTokenScope val1 = VsoTokenScope.or(VsoTokenScope.BuildAccess, VsoTokenScope.BuildAccess);
        Assert.assertEquals(VsoTokenScope.BuildAccess, val1);

        val1 = VsoTokenScope.add(
                VsoTokenScope.add(VsoTokenScope.ProfileRead, VsoTokenScope.PackagingWrite),
                VsoTokenScope.BuildAccess);
        VsoTokenScope val2 = VsoTokenScope.or(val1, VsoTokenScope.ProfileRead);
        Assert.assertEquals(val1, val2);

        val2 = VsoTokenScope.or(
                VsoTokenScope.or(VsoTokenScope.ProfileRead, VsoTokenScope.PackagingWrite),
                VsoTokenScope.BuildAccess);
        Assert.assertEquals(VsoTokenScope.ProfileRead, VsoTokenScope.and(val2, VsoTokenScope.ProfileRead));
        Assert.assertEquals(VsoTokenScope.PackagingWrite, VsoTokenScope.and(val2, VsoTokenScope.PackagingWrite));
        Assert.assertEquals(VsoTokenScope.BuildAccess, VsoTokenScope.and(val2, VsoTokenScope.BuildAccess));
        Assert.assertEquals(VsoTokenScope.None, VsoTokenScope.and(val2, VsoTokenScope.PackagingManage));
    }
}
