package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;
import com.microsoft.alm.java.git_credential_helper.helpers.ScopeSet;
import com.microsoft.alm.java.git_credential_helper.helpers.StringHelper;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class VsoTokenScope extends TokenScope
{
    public static final VsoTokenScope None = new VsoTokenScope(StringHelper.Empty);
    /**
     * Grants the ability to access build artifacts, including build results, definitions, and
     * requests, and the ability to receive notifications about build events via service hooks.
     */
    public static final VsoTokenScope BuildAccess = new VsoTokenScope("vso.build");
    /**
     * Grants the ability to access build artifacts, including build results, definitions, and
     * requests, and the ability to queue a build, update build properties, and the ability to
     * receive notifications about build events via service hooks.
     */
    public static final VsoTokenScope BuildExecute = new VsoTokenScope("vso.build_execute");
    /**
     * Grants the ability to access rooms and view, post, and update messages. Also grants the
     * ability to manage rooms and users and to receive notifications about new messages via
     * service hooks.
     */
    public static final VsoTokenScope ChatManage = new VsoTokenScope("vso.chat_manage");
    /**
     * Grants the ability to access rooms and view, post, and update messages. Also grants the
     * ability to receive notifications about new messages via service hooks.
     */
    public static final VsoTokenScope ChatWrite = new VsoTokenScope("vso.chat_write");
    /**
     * Grants the ability to read, update, and delete source code, access metadata about
     * commits, changesets, branches, and other version control artifacts. Also grants the
     * ability to create and manage code repositories, create and manage pull requests and
     * code reviews, and to receive notifications about version control events via service
     * hooks.
     */
    public static final VsoTokenScope CodeManage = new VsoTokenScope("vso.code_manage");
    /**
     * Grants the ability to read source code and metadata about commits, changesets, branches,
     * and other version control artifacts. Also grants the ability to get notified about
     * version control events via service hooks.
     */
    public static final VsoTokenScope CodeRead = new VsoTokenScope("vso.code");
    /**
     * Grants the ability to read, update, and delete source code, access metadata about
     * commits, changesets, branches, and other version control artifacts. Also grants the
     * ability to create and manage pull requests and code reviews and to receive
     * notifications about version control events via service hooks.
     */
    public static final VsoTokenScope CodeWrite = new VsoTokenScope("vso.code_write");
    /**
     * Grants the ability to read, write, and delete feeds and packages.
     */
    public static final VsoTokenScope PackagingManage = new VsoTokenScope("vso.packaging_manage");
    /**
     * Grants the ability to list feeds and read packages in those feeds.
     */
    public static final VsoTokenScope PackagingRead = new VsoTokenScope("vso.packaging");
    /**
     * Grants the ability to list feeds and read, write, and delete packages in those feeds.
     */
    public static final VsoTokenScope PackagingWrite = new VsoTokenScope("vso.packaging_write");
    /**
     * Grants the ability to read your profile, accounts, collections, projects, teams, and
     * other top-level organizational artifacts.
     */
    public static final VsoTokenScope ProfileRead = new VsoTokenScope("vso.profile");
    /**
     * Grants the ability to read service hook subscriptions and metadata, including supported
     * events, consumers, and actions.
     */
    public static final VsoTokenScope ServiceHookRead = new VsoTokenScope("vso.hooks");
    /**
     * Grants the ability to create and update service hook subscriptions and read metadata,
     * including supported events, consumers, and actions."
     */
    public static final VsoTokenScope ServiceHookWrite = new VsoTokenScope("vso.hooks_write");
    /**
     * Grants the ability to read test plans, cases, results and other test management related
     * artifacts.
     */
    public static final VsoTokenScope TestRead = new VsoTokenScope("vso.test");
    /**
     * Grants the ability to read, create, and update test plans, cases, results and other
     * test management related artifacts.
     */
    public static final VsoTokenScope TestWrite = new VsoTokenScope("vso.test_write");
    /**
     * Grants the ability to read work items, queries, boards, area and iterations paths, and
     * other work item tracking related metadata. Also grants the ability to execute queries
     * and to receive notifications about work item events via service hooks.
     */
    public static final VsoTokenScope WorkRead = new VsoTokenScope("vso.work");
    /**
     * Grants the ability to read, create, and update work items and queries, update board
     * metadata, read area and iterations paths other work item tracking related metadata,
     * execute queries, and to receive notifications about work item events via service hooks.
     */
    public static final VsoTokenScope WorkWrite = new VsoTokenScope("vso.work_write");

    private VsoTokenScope(final String value)
    {
        super(value);
    }

    private VsoTokenScope(final String[] values)
    {
        super(values);
    }

    private VsoTokenScope(final ScopeSet set)
    {
        super(set);
    }

    private static final List<VsoTokenScope> values = Arrays.asList
    (
        BuildAccess,
        BuildExecute,
        ChatManage,
        ChatWrite,
        CodeManage,
        CodeRead,
        CodeWrite,
        PackagingManage,
        PackagingRead,
        PackagingWrite,
        ProfileRead,
        ServiceHookRead,
        ServiceHookWrite,
        TestRead,
        TestWrite,
        WorkRead,
        WorkWrite
    );

    public static Iterator<VsoTokenScope> enumerateValues()
    {
        return values.iterator();
    }

    // TODO: [MethodImpl(MethodImplOptions.AggressiveInlining)]
    public static VsoTokenScope operatorPlus(final VsoTokenScope scope1, final VsoTokenScope scope2)
    {
        throw new NotImplementedException();
    }
    // TODO: [MethodImpl(MethodImplOptions.AggressiveInlining)]
    public static VsoTokenScope operatorMinus(final VsoTokenScope scope1, final VsoTokenScope scope2)
    {
        throw new NotImplementedException();
    }
    // TODO: [MethodImpl(MethodImplOptions.AggressiveInlining)]
    public static VsoTokenScope operatorOr(final VsoTokenScope scope1, final VsoTokenScope scope2)
    {
        throw new NotImplementedException();
    }
    // TODO: [MethodImpl(MethodImplOptions.AggressiveInlining)]
    public static VsoTokenScope operatorAnd(final VsoTokenScope scope1, final VsoTokenScope scope2)
    {
        throw new NotImplementedException();
    }
    // TODO: [MethodImpl(MethodImplOptions.AggressiveInlining)]
    public static VsoTokenScope operatorXor(final VsoTokenScope scope1, final VsoTokenScope scope2)
    {
        throw new NotImplementedException();
    }
}
