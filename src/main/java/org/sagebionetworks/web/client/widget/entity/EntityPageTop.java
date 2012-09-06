package org.sagebionetworks.web.client.widget.entity;

import java.util.Arrays;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.GenericData;
import org.sagebionetworks.repo.model.GenotypeData;
import org.sagebionetworks.repo.model.Locationable;
import org.sagebionetworks.repo.model.PhenotypeData;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.IconSize;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.EntityType;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityPageTop implements EntityPageTopView.Presenter, SynapseWidgetPresenter  {
		
	private EntityPageTopView view;
	private NodeServiceAsync nodeService;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private EntitySchemaCache schemaCache;	
	private JSONObjectAdapter jsonObjectAdapter;
	private EntityTypeProvider entityTypeProvider;
	private IconsImageBundle iconsImageBundle;
	
	private EntityBundle bundle;
	private boolean readOnly;
	private String entityTypeDisplay; 
	private EventBus bus;
	private String rStudioUrl;
	
	@Inject
	public EntityPageTop(EntityPageTopView view, NodeServiceAsync service,
			SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			EntitySchemaCache schemaCache,
			JSONObjectAdapter jsonObjectAdapter,
			EntityTypeProvider entityTypeProvider,
			IconsImageBundle iconsImageBundle,
			EventBus bus) {
		this.view = view;
		this.nodeService = service;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.schemaCache = schemaCache;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.entityTypeProvider = entityTypeProvider;
		this.iconsImageBundle = iconsImageBundle;
		this.bus = bus;
		view.setPresenter(this);
	}	

    /**
     * Update the bundle attached to this EntityPageTop. Consider calling refresh()
     * to notify an attached view.
     * 
     * @param bundle
     */
    public void setBundle(EntityBundle bundle, boolean readOnly) {
    	this.bundle = bundle;
    	this.readOnly = readOnly;
	}

	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
		// remove handlers
		this.bundle = null;		
	}
    
	@Override
	public Widget asWidget() {
		if(bundle != null) {
			return asWidget(bundle);
		} 
		return null;
	}	
	
	public Widget asWidget(EntityBundle bundle) {
		view.setPresenter(this);
		return view.asWidget();
	}
	
	@Override
	public void refresh() {
		sendDetailsToView(bundle.getPermissions().getCanChangePermissions(), bundle.getPermissions().getCanEdit());
	}	

	@Override
	public void fireEntityUpdatedEvent() {
		bus.fireEvent(new EntityUpdatedEvent());
	}
	
	public void addEntityUpdatedHandler(EntityUpdatedHandler handler) {		
		bus.addHandler(EntityUpdatedEvent.getType(), handler);
	}

	@Override
	public boolean isLocationable() {
		if(bundle.getEntity() instanceof Locationable) {
			return true;
		}
		return false;
	}
	
	@Override 
	public boolean isLoggedIn() {
		return authenticationController.getLoggedInUser() != null;
	}
	
	@Override
	public String createEntityLink(String id, String version, String display) {
		return DisplayUtils.createEntityLink(id, version, display);		
	}
	
	@Override
	public ImageResource getIconForType(String typeString) {		
		EntityType type = entityTypeProvider.getEntityTypeForString(typeString);		
		// try class name as some references are short names, some class names
		if(type == null) 
			type = entityTypeProvider.getEntityTypeForClassName(typeString);
		if(type == null) {
			return DisplayUtils.getSynapseIconForEntity(null, IconSize.PX16, iconsImageBundle);
		}
		return DisplayUtils.getSynapseIconForEntityType(type, IconSize.PX16, iconsImageBundle);
	}	

	@Override
	public void loadShortcuts(int offset, int limit, final AsyncCallback<PaginatedResults<EntityHeader>> callback) {
		PaginatedResults<EntityHeader> references = null;
		if(offset == 0) {
			 callback.onSuccess(bundle.getReferencedBy());			 
		} else {
			synapseClient.getEntityReferencedBy(bundle.getEntity().getId(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					PaginatedResults<EntityHeader> paginatedResults = nodeModelCreator.createPaginatedResults(result, EntityHeader.class);
					callback.onSuccess(paginatedResults);
				}
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		}		
	}	
	
	/*
	 * Private Methods
	 */
	private void sendDetailsToView(boolean isAdmin, boolean canEdit) {
		ObjectSchema schema = schemaCache.getSchemaEntity(bundle.getEntity());
		entityTypeDisplay = DisplayUtils.getEntityTypeDisplay(schema);
		UserSessionData sessionData = authenticationController.getLoggedInUser();
		UserProfile userProfile = (sessionData==null ? null : sessionData.getProfile());
		view.setEntityBundle(bundle, userProfile, entityTypeDisplay, isAdmin, canEdit, readOnly);
	}
	
	private UserProfile getUserProfile() {
		UserSessionData sessionData = authenticationController.getLoggedInUser();
		return (sessionData==null ? null : sessionData.getProfile());
		
	}

	@Override
	public boolean isAnonymous() {
		return getUserProfile()==null;
	}

	@Override
	public String getJiraFlagUrl() {
		UserProfile userProfile = getUserProfile();
		if (userProfile==null) throw new IllegalStateException("UserProfile is null");
		return JiraURLHelper.createFlagIssue(userProfile.getUserName(), userProfile.getDisplayName(), bundle.getEntity().getId());
	}

	@Override
	public String getJiraRestrictionUrl() {
		UserProfile userProfile = getUserProfile();
		if (userProfile==null) throw new IllegalStateException("UserProfile is null");
		return JiraURLHelper.createAccessRestrictionIssue(userProfile.getUserName(), userProfile.getDisplayName(), bundle.getEntity().getId());
	}

	@Override
	public boolean hasAdministrativeAccess() {
		return bundle.getPermissions().getCanChangePermissions();
	}

	@Override
	public boolean isRestrictedData() {
		return bundle.getAccessRequirements().getTotalNumberOfResults()>0L;
	}

	@Override
	public boolean hasFulfilledAccessRequirements() {
		return bundle.getUnmetAccessRequirements().getTotalNumberOfResults()==0L;
	}

	private static final String[] dataObjectEntityTypes = {
		Data.class.getName(),
		GenericData.class.getName(),
		GenotypeData.class.getName(),
		PhenotypeData.class.getName()
	};
	
	@Override
	public boolean includeRestrictionWidget() {
		// restriction widget is to be included iff Entity is a data object
		return Arrays.asList(dataObjectEntityTypes).contains(
				bundle.getEntity().getEntityType()
		);
	}

	@Override
	public String accessRequirementText() {
		if (!isRestrictedData()) throw new IllegalStateException("There is no access requirement.");
		AccessRequirement ar = bundle.getAccessRequirements().getResults().get(0);
		if (ar instanceof TermsOfUseAccessRequirement) {
			return ((TermsOfUseAccessRequirement)ar).getTermsOfUse();
		} else if (ar instanceof ACTAccessRequirement) {
			return ((ACTAccessRequirement)ar).getActContactInfo();			
		} else {
			throw new IllegalStateException("Unexpected access requirement type "+ar.getClass());
		}
	}
	
	private AccessRequirement getAccessRequirement() {
		return bundle.getAccessRequirements().getResults().get(0);
	}

	@Override
	public boolean isTermsOfUseAccessRequirement() {
		if (!isRestrictedData()) throw new IllegalStateException("There is no access requirement.");
		AccessRequirement ar = getAccessRequirement();
		if (ar instanceof TermsOfUseAccessRequirement) {
			return true;		
		} else {
			return false;
		}
	}

	@Override
	public Callback accessRequirementCallback() {
		if (!isTermsOfUseAccessRequirement()) throw new IllegalStateException("not a TOU Access Requirement");
		AccessRequirement ar = getAccessRequirement();
		TermsOfUseAccessRequirement tou = (TermsOfUseAccessRequirement)ar;
		return new Callback() {
			@Override
			public void invoke() {
				// TODO Auto-generated method stub
			}
		};
	}

	@Override
	public Callback getDataLockDownCallback() {
		return new Callback() {
			@Override
			public void invoke() {
				// TODO Auto-generated method stub
			}
		};
	}


}
