package com.microsoft.alm.java.git_credential_helper.cli;

/**
 * Level of interactivity allowed and enabled.
 */
enum Interactivity
{
    /**
     * Present an interactive logon prompt when necessary, otherwise use cached credentials
     */
    Auto,
    /**
     * Always present an interactive logon prompt regardless if cached credentials exist
     */
    Always,
    /**
     * Never present an present an interactive logon prompt, fail without cached credentials
     */
    Never
}
