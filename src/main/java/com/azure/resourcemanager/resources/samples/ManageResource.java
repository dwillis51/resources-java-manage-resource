// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.Provider;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;

/**
 * Azure Resource sample for managing resources -
 * - Create a resource
 * - Update a resource
 * - Create another resource
 * - List resources
 * - Delete a resource.
 */

public final class ManageResource {

	public static boolean listAllResources(AzureResourceManager azureResourceManager) {
		try {
			//=============================================================
	        // List all resources.

	        System.out.println("Listing all generic resources: ");
	        int count = 0;
	        
	        for (GenericResource sRes : azureResourceManager.genericResources().list()) {
	        	count++;
	            System.out.println("Resource: " + sRes.name());
	        }
	        System.out.println("Total Resource Count: " + count);
	        
	        return true;        			
		} catch (Exception g) {
            g.printStackTrace();
        }
		return false;
	}
	
    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgRSMR", 24);
        final String resourceName1 = Utils.randomResourceName(azureResourceManager, "rn1", 24);
        final String resourceName2 = Utils.randomResourceName(azureResourceManager, "rn2", 24);
        try {
        	
            //=============================================================
            // Create resource group.

            System.out.println("Creating a resource group with name: " + rgName);

            azureResourceManager.resourceGroups().define(rgName)
                    .withRegion(Region.US_WEST)
                    .create();


            //=============================================================
            // Create storage account.

            System.out.println("Creating a storage account with name: " + resourceName1);

            StorageAccount storageAccount = azureResourceManager.storageAccounts().define(resourceName1)
                    .withRegion(Region.US_WEST)
                    .withExistingResourceGroup(rgName)
                    .create();

            System.out.println("Storage account created: " + storageAccount.id());


            //=============================================================
            // Update - set the sku name

            System.out.println("Updating the storage account with name: " + resourceName1);

            storageAccount.update()
                    .withSku(StorageAccountSkuType.STANDARD_RAGRS)
                    .apply();

            System.out.println("Updated the storage account with name: " + resourceName1);


            //=============================================================
            // Create another storage account.

            System.out.println("Creating another storage account with name: " + resourceName2);

            StorageAccount storageAccount2 = azureResourceManager.storageAccounts().define(resourceName2)
                    .withRegion(Region.US_WEST)
                    .withExistingResourceGroup(rgName)
                    .create();

            System.out.println("Storage account created: " + storageAccount2.id());


            //=============================================================
            // List storage accounts.

            System.out.println("Listing all storage accounts for resource group: " + rgName);

            for (StorageAccount sAccount : azureResourceManager.storageAccounts().list()) {
                System.out.println("Storage account: " + sAccount.name());
            }


            //=============================================================
            // Delete a storage accounts.

            System.out.println("Deleting storage account: " + resourceName2);

            azureResourceManager.storageAccounts().deleteById(storageAccount2.id());

            System.out.println("Deleted storage account: " + resourceName2);
            return true;
        } finally {

            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
        	
        	//Getting Authentication working was kind of tricky. I had to 
        	//1. Register an app in Azure AD
        	//2. Create a client secret in the Certificates and secrets section of my app registration
        	//3. Give that app contributor access to my subscription
        	
            //=================================================================
            // Authenticate
        	ClientSecretCredential credential = new ClientSecretCredentialBuilder()
        		    .clientId("<Application (client) ID>")
        		    .clientSecret("<Value From Your Client Secret>")
        		    .tenantId("<Your Tenant ID>")
        		    // authority host is optional
        		    //.authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
        		    .build();
        	        	
        	//If this will be Azure Gov, change the 3rd parameter to AzureEnvironment.AZURE_US_GOVERNMENT
        	AzureProfile profile = new AzureProfile("<Your Tenant ID>", "<Your Subscription ID>", AzureEnvironment.AZURE);

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            //this function will list out all resources, including hidden
            listAllResources(azureResourceManager);
            
            //the line below is the original code
            //runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
