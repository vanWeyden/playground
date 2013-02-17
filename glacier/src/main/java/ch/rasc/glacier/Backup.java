package ch.rasc.glacier;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.CreateVaultRequest;
import com.amazonaws.services.glacier.model.CreateVaultResult;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;

public class Backup {

	public static void main(String[] args) {
		if (args.length == 4) {
			AWSCredentials credentials = new BasicAWSCredentials(args[0], args[1]);
			AmazonGlacierClient client = new AmazonGlacierClient(credentials);
			client.setEndpoint("https://glacier.us-east-1.amazonaws.com/");

			String fileToBackup = args[2];
			String vaultName = args[3];

			ListVaultsRequest lvRequest = new ListVaultsRequest();
			ListVaultsResult lvResult = client.listVaults(lvRequest);

			boolean found = false;
			for (DescribeVaultOutput v : lvResult.getVaultList()) {
				System.out.println(v.getVaultName());
				System.out.println(v.getVaultARN());

				if (vaultName.equals(v.getVaultName())) {
					found = true;
					// break;
				}
			}

			if (!found) {
				try {
					CreateVaultResult cvr = client.createVault(new CreateVaultRequest().withVaultName(vaultName));
					System.out.println("Created vault: " + cvr.getLocation());
				} catch (AmazonServiceException e) {
					e.printStackTrace();
				} catch (AmazonClientException e) {
					e.printStackTrace();
				}
			} else {
				System.out.printf("Vault %s exists\n", vaultName);
			}

			try {
				ArchiveTransferManager atm = new ArchiveTransferManager(client, credentials);

				UploadResult result = atm.upload(vaultName, fileToBackup, Paths.get(fileToBackup).toFile());
				System.out.println("Archive ID: " + result.getArchiveId());

			} catch (Exception e) {
				System.err.println(e);
			}

		} else {
			System.out.println("Backup <accessKey> <secretKey> <file> <vaultName>");
		}
		// AWSCredentials credentials = new PropertiesCredentials(
		// ArchiveUploadHighLevel.class.getResourceAsStream("AwsCredentials.properties"));
		// client = new AmazonGlacierClient(credentials);
		// client.setEndpoint("https://glacier.us-east-1.amazonaws.com/");
		//
		// try {
		// ArchiveTransferManager atm = new ArchiveTransferManager(client,
		// credentials);
		//
		// UploadResult result = atm.upload(vaultName, "my archive " + (new
		// Date()), new File(archiveToUpload));
		// System.out.println("Archive ID: " + result.getArchiveId());
		//
		// } catch (Exception e)
		// {
		// System.err.println(e);
		// }

	}

}
