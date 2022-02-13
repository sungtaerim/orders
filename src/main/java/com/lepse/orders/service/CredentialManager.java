package com.lepse.orders.service;

import com.teamcenter.schemas.soa._2006_03.exceptions.InvalidCredentialsException;
import com.teamcenter.schemas.soa._2006_03.exceptions.InvalidUserException;
import com.teamcenter.soa.exceptions.CanceledOperationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class CredentialManager implements com.teamcenter.soa.client.CredentialManager {

    private String name;
    private String password;
    private final String tcServer;
    private String group = ""; // default group
    private String role = ""; // default role
    private String discriminator = "SoaAppX"; // always connect same user

    public CredentialManager(String name, String password, String tcServer) {
        this.name = name;
        this.password = password;
        this.tcServer = tcServer;
    }

    /**
     * Return the tc server address
     */
    public String getTcServer() {
        return tcServer;
    }

    /**
     * Return the type of credentials this implementation provides,
     * standard (user/password) or Single-Sign-On. In this case
     * Standard credentials are returned.
     */
    public int getCredentialType() {
        return com.teamcenter.soa.client.CredentialManager.CLIENT_CREDENTIAL_TYPE_STD;
    }

    /**
     * Prompt's the user for credentials.
     * This method will only be called by the framework when a login attempt has
     * failed.
     */
    public String[] getCredentials(InvalidCredentialsException e) throws CanceledOperationException {
        System.out.println(e.getMessage());
        return promptForCredentials();
    }

    /**
     * Return the cached credentials.
     * This method will be called when a service request is sent without a valid
     * session ( session has expired on the server).
     */
    public String[] getCredentials(InvalidUserException e) throws CanceledOperationException {
        // Have not logged in yet, should not happen but just in case
        if (name == null) return promptForCredentials();

        // Return cached credentials
        String[] tokens = { name, password, group, role, discriminator };
        return tokens;
    }

    /**
     * Cache the group and role
     * This is called after the SessionService.setSessionGroupMember service
     * operation is called.
     */
    public void setGroupRole(String group, String role) {
        this.group = group;
        this.role = role;
    }

    /**
     * Cache the User and Password
     * This is called after the SessionService.login service operation is called.
     */
    public void setUserPassword(String user, String password, String discriminator) {
        this.name = user;
        this.password = password;
        this.discriminator = discriminator;
    }

    public String[] promptForCredentials() throws CanceledOperationException {
        try
        {
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(System.in));
            System.out.println("Please enter user credentials (return to quit):");
            System.out.print("User Name: ");
            name = reader.readLine();

            if (name.length() == 0)
                throw new CanceledOperationException("");

            System.out.print("Password:  ");
            password = reader.readLine();
        }
        catch (IOException e)
        {
            String message = "Failed to get the name and password.\n" + e.getMessage();
            System.out.println(message);
            throw new CanceledOperationException(message);
        }

        String[] tokens = { name, password, group, role, discriminator };
        return tokens;
    }
}
