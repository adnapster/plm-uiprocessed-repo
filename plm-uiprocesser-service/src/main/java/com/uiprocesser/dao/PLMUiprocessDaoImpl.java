package com.uiprocesser.dao;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

@Repository
public class PLMUiprocessDaoImpl implements PLMUiprocessDao{
	private static final Logger LOG = LoggerFactory.getLogger(PLMUiprocessDaoImpl.class);
	
	@Value("${azure.storage.connectionstring}")
	private String connectionString;
	@Value("${azure.storage.blobname}")
	private String blobName;

	@Override
	public String readBlobXML(String ecnNumber) {
		if (LOG.isDebugEnabled()) {
			LOG.info("###### Starting PLMWebPortalDAOImpl.readBlobXML() #######");
		}
		String xmlOutput = "";
		try {
			CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			CloudBlobContainer blobContainer = blobClient.getContainerReference(blobName);
			boolean tableExistsOrNOt = true;
			if (blobContainer == null) {
				tableExistsOrNOt = blobContainer.createIfNotExists();
			}
			if (tableExistsOrNOt) {
				CloudBlockBlob blob = blobContainer.getBlockBlobReference(ecnNumber);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				blob.download(outputStream);
				xmlOutput = blob.downloadText();
				blob.downloadToFile("output.xml");
			}
		} catch  (URISyntaxException | StorageException e) {
			LOG.error("### URISyntaxException | StorageException in PLMUiprocessDaoImpl.readBlobXML() : ", e);
		} catch (Exception e) {
			LOG.error("### Encountered Generic Exception in PLMUiprocessDaoImpl.readBlobXML() : ", e);
		}
		if (LOG.isDebugEnabled()) {
			LOG.info("###### Ending PLMUiprocessDaoImpl.readBlobXML() #######");
		}
		return xmlOutput;
	}

}
