package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.client.CollectionSpaceClientUtils;
import org.collectionspace.services.client.LoanoutClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.relation.nuxeo.RelationConstants;
import org.collectionspace.services.loanout.nuxeo.LoanoutConstants;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.place.nuxeo.PlaceAuthorityConstants;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateVoucherBatchJob extends AbstractBatchJob {
	final Logger logger = LoggerFactory.getLogger(CreateVoucherBatchJob.class);

	public CreateVoucherBatchJob() {
		setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE));
	}

	public void run() {
		setCompletionStatus(STATUS_MIN_PROGRESS);

		try {
			String mode = getInvocationContext().getMode();

			if (!mode.equalsIgnoreCase(INVOCATION_MODE_SINGLE)) {
				throw new Exception("Unsupported invocation mode: " + mode);
			}

			String csid = getInvocationContext().getSingleCSID();

			if (StringUtils.isEmpty(csid)) {
				throw new Exception("Missing context csid");
			}

			String docType = getInvocationContext().getDocType();

			if (docType.equals(CollectionObjectConstants.NUXEO_DOCTYPE)) {
				setResults(createVoucherFromCataloging(csid));	
			}
			else if (docType.equals(MovementConstants.NUXEO_DOCTYPE)) {
				setResults(createVoucherFromCurrentLocation(csid));
			}
			else {
				throw new Exception("Unsupported docType: " + docType);
			}

			setCompletionStatus(STATUS_COMPLETE);
		}
		catch(Exception e) {
			setCompletionStatus(STATUS_ERROR);
			setErrorInfo(new InvocationError(INT_ERROR_STATUS, e.getMessage()));
		}
	}

	public InvocationResults createVoucherFromCataloging(String collectionObjectCsid) throws ResourceException, URISyntaxException, DocumentException {
		return createVoucherFromCataloging(collectionObjectCsid, null);
	}
	
	public InvocationResults createVoucherFromCataloging(String collectionObjectCsid, String movementCsid) throws ResourceException, URISyntaxException, DocumentException {
		InvocationResults results = new InvocationResults();
		Map<String, String> fields = new HashMap<String, String>();
		
		if (movementCsid == null) {
			movementCsid = findSingleRelatedMovement(collectionObjectCsid);
		}

		if (movementCsid != null) {
			PoxPayloadOut movementPayload = findMovementByCsid(movementCsid);
			
			if (movementPayload != null) {
				fields.put("gardenLocation", getFieldValue(movementPayload, MovementConstants.CURRENT_LOCATION_SCHEMA_NAME, MovementConstants.CURRENT_LOCATION_FIELD_NAME));
			}
		}
		
		PoxPayloadOut collectionObjectPayload = findCollectionObjectByCsid(collectionObjectCsid);
		
		fields.put("fieldCollectionNote", getFieldCollectionNote(collectionObjectPayload));
		fields.put("annotation", getAnnotation(collectionObjectPayload));
		fields.put("labelRequested", LoanoutConstants.LABEL_REQUESTED_NO_VALUE);
		
		String voucherCsid = createVoucher(fields);
		logger.debug("voucher created: voucherCsid=" + voucherCsid);
		
		String forwardRelationCsid = createRelation(voucherCsid, LoanoutConstants.NUXEO_DOCTYPE, collectionObjectCsid, CollectionObjectConstants.NUXEO_DOCTYPE, RelationConstants.AFFECTS_TYPE);
		String backwardRelationCsid = createRelation(collectionObjectCsid, CollectionObjectConstants.NUXEO_DOCTYPE, voucherCsid, LoanoutConstants.NUXEO_DOCTYPE, RelationConstants.AFFECTS_TYPE);
		logger.debug("relations created: forwardRelationCsid=" + forwardRelationCsid + " backwardRelationCsid=" + backwardRelationCsid);
		
		results.setNumAffected(1);
		results.setPrimaryURICreated("loanout.html?csid=" + voucherCsid);
		results.setUserNote("Voucher created");

		return results;
	}
	
	private String getFieldCollectionNote(PoxPayloadOut collectionObjectPayload) throws URISyntaxException, DocumentException {
		//String fieldCollectionPlace = getDisplayNameFromRefName(getFieldValue(collectionObjectPayload, CollectionObjectConstants.FIELD_COLLECTION_PLACE_SCHEMA_NAME, CollectionObjectConstants.FIELD_COLLECTION_PLACE_FIELD_NAME));		
		String fieldCollectionPlace = getReverseFieldCollectionPlace(collectionObjectPayload);		
		String comment = this.getFieldValue(collectionObjectPayload, CollectionObjectConstants.COMMENT_SCHEMA_NAME, CollectionObjectConstants.COMMENT_FIELD_NAME);
		String collectionNote;
		
		if (StringUtils.isNotBlank(fieldCollectionPlace) && StringUtils.isNotBlank(comment)) {
			collectionNote = fieldCollectionPlace + ": " + comment;
		}
		else if (StringUtils.isNotBlank(fieldCollectionPlace)) {
			collectionNote = fieldCollectionPlace;
		}
		else {
			collectionNote = comment;
		}			
		
		return collectionNote;
	}
	
	private String getReverseFieldCollectionPlace(PoxPayloadOut collectionObjectPayload) throws URISyntaxException, DocumentException {
		String fieldCollectionPlaceRefName = getFieldValue(collectionObjectPayload, CollectionObjectConstants.FIELD_COLLECTION_PLACE_SCHEMA_NAME, CollectionObjectConstants.FIELD_COLLECTION_PLACE_FIELD_NAME);		
		RefName.AuthorityItem item = RefName.AuthorityItem.parse(fieldCollectionPlaceRefName);
		
		String vocabularyShortId = item.getParentShortIdentifier();
		String itemShortId = item.getShortIdentifier();
		
		logger.debug("finding place: vocabularyShortId=" + vocabularyShortId + " itemShortId=" + itemShortId);
		
		PoxPayloadOut placePayload = this.findPlaceByShortId(vocabularyShortId, itemShortId);
		
		List<String> termTypes = this.getFieldValues(placePayload, PlaceAuthorityConstants.TERM_TYPE_SCHEMA_NAME, PlaceAuthorityConstants.TERM_TYPE_FIELD_NAME);
		List<String> displayNames = this.getFieldValues(placePayload, PlaceAuthorityConstants.DISPLAY_NAME_SCHEMA_NAME, PlaceAuthorityConstants.DISPLAY_NAME_FIELD_NAME);
		
		int index = termTypes.indexOf(PlaceAuthorityConstants.REVERSE_TERM_TYPE);
		String reverseDisplayName = null;
		
		if (index < 0) {
			// There's no reverse term. Just use the primary.
			
			if (displayNames.size() > 0) {
				reverseDisplayName = displayNames.get(0);
			}
		}
		else {
			reverseDisplayName = displayNames.get(index);
		}
		
		if (reverseDisplayName == null) {
			reverseDisplayName = "";
		}
		
		return reverseDisplayName;
	}
	
	private String getAnnotation(PoxPayloadOut collectionObjectPayload) {
		String determinationBy = getDisplayNameFromRefName(getFieldValue(collectionObjectPayload, CollectionObjectConstants.DETERMINATION_BY_SCHEMA_NAME, CollectionObjectConstants.DETERMINATION_BY_FIELD_NAME));
		String determinationInstitution = getDisplayNameFromRefName(getFieldValue(collectionObjectPayload, CollectionObjectConstants.DETERMINATION_INSTITUTION_SCHEMA_NAME, CollectionObjectConstants.DETERMINATION_INSTITUTION_FIELD_NAME));
		String determinationDate = getFieldValue(collectionObjectPayload, CollectionObjectConstants.DETERMINATION_DATE_SCHEMA_NAME, CollectionObjectConstants.DETERMINATION_DATE_FIELD_NAME);

		String annotation = "";
		
		if (StringUtils.isNotBlank(determinationBy)) {
			annotation += "det. by " + determinationBy;
			
			if (StringUtils.isNotBlank(determinationInstitution)) {
				annotation += ", " + determinationInstitution;
			}
			
			if (StringUtils.isNotBlank(determinationDate)) {
				annotation += ", " + determinationDate;
			}
		}
		
		return annotation;
	}
	
	public InvocationResults createVoucherFromCurrentLocation(String movementCsid) throws ResourceException, URISyntaxException, DocumentException {
		long numAffected = 0;
		String primaryUriCreated = null;
		
		List<String> collectionObjectCsids = findRelatedCollectionObjects(movementCsid);

		// There should only be one, but just in case...
			
		for (String collectionObjectCsid : collectionObjectCsids) {
			InvocationResults innerResults = createVoucherFromCataloging(collectionObjectCsid, movementCsid);
				
			numAffected = numAffected + innerResults.getNumAffected();
				
			if (primaryUriCreated == null) {
				primaryUriCreated = innerResults.getPrimaryURICreated();
			}
		}
		
		InvocationResults results = new InvocationResults();
		results.setNumAffected(numAffected);
		results.setPrimaryURICreated(primaryUriCreated);
		
		if (collectionObjectCsids.size() == 0) {
			results.setUserNote("No related cataloging record found");
		}
		else {
			results.setUserNote("Voucher created for " + numAffected + " cataloging " + (numAffected == 1 ? "record" : "records"));
		}
		
		return results;
	}

	private String createVoucher(Map<String, String> fields) throws ResourceException {
		String voucherCsid = null;

		String createVoucherPayload = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<document name=\"loansout\">" +
				"<ns2:loansout_botgarden xmlns:ns2=\"http://collectionspace.org/services/loanout/local/botgarden\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					getFieldXml(fields, "gardenLocation") +
					getFieldXml(fields, "fieldCollectionNote") +
					getFieldXml(fields, "fieldCollectionPlaceNote") +
					getFieldXml(fields, "annotation") +
					getFieldXml(fields, "labelRequested") +
				"</ns2:loansout_botgarden>" +
			"</document>";

		ResourceBase resource = getResourceMap().get(LoanoutClient.SERVICE_NAME);
		Response response = resource.create(getResourceMap(), null, createVoucherPayload);

		if (response.getStatus() == CREATED_STATUS) {
			voucherCsid = CollectionSpaceClientUtils.extractId(response);
		}
		else {
			throw new ResourceException(response, "Error creating voucher");
		}

		return voucherCsid;
	}
}