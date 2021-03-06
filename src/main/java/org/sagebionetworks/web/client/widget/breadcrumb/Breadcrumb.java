package org.sagebionetworks.web.client.widget.breadcrumb;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.IconSize;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.place.shared.Place;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Breadcrumb implements BreadcrumbView.Presenter,
		SynapseWidgetPresenter {

	private BreadcrumbView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private NodeModelCreator nodeModelCreator;
	private IconsImageBundle iconsImageBundle;

	@Inject
	public Breadcrumb(BreadcrumbView view, SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			NodeModelCreator nodeModelCreator,
			IconsImageBundle iconsImageBundle) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.nodeModelCreator = nodeModelCreator;
		this.iconsImageBundle = iconsImageBundle;
		view.setPresenter(this);
	}

	/**
	 * Create Breadcrumbs for an Entity
	 * 
	 * @param entity
	 * @return
	 */
	public Widget asWidget(EntityPath entityPath, EntityArea optionalArea, boolean showHome) {
		view.setPresenter(this);
		// exchange root for home
		List<LinkData> links = new ArrayList<LinkData>();
		if(showHome) links.add(new LinkData("Home", new Home(ClientProperties.DEFAULT_PLACE_TOKEN)));
		if (entityPath != null) {
			List<EntityHeader> path = entityPath.getPath();
			if (path != null) {
				// create link data for each path element except for the first
				// (root) and last (current)
				for (int i = 1; i < path.size(); i++) {
					EntityHeader element = path.get(i);
					String name = element.getName();
					Synapse place = new Synapse(element.getId());
					ImageResource icon = DisplayUtils.getSynapseIconForEntityClassName(element.getType(), IconSize.PX16, iconsImageBundle);
					if(optionalArea == EntityArea.FILES && Project.class.getName().equals(element.getType())) {
						// show files as root
						name = DisplayConstants.FILES;
						place.setArea(EntityArea.FILES);					
					} else  if(optionalArea == EntityArea.TABLES && Project.class.getName().equals(element.getType())) {
						// show tables as root
						name = DisplayConstants.TABLES;
						place.setArea(EntityArea.TABLES);					
					}
					links.add(new LinkData(name, icon, place));
				}
			}
		}
		view.setLinksList(links);
		return view.asWidget();
	}
	/**
	 * Use when the only page link is back to the Home page 
	 * @param currentPageName
	 * @return
	 */
	public Widget asWidget(String currentPageName){
		List<LinkData> links = new ArrayList<LinkData>();
		links.add(new LinkData("Home", new Home(
				ClientProperties.DEFAULT_PLACE_TOKEN)));
		return asWidget(links, currentPageName);
	}
	
	/**
	 * Create Breadcrumbs for an arbitrary set of link data, ending in the current page name
	 * @param links
	 * @param currentPageName
	 * @return
	 */
	public Widget asWidget(List<LinkData> links, String currentPageName){
		view.setPresenter(this);
		view.setLinksList(links, currentPageName);
		return view.asWidget();
	}
	
	/**
	 * Not used
	 */
	@Override
	public Widget asWidget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}

}
