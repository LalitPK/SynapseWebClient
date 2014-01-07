package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;

import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesViewImpl extends FlowPanel implements WikiSubpagesView {

	private Presenter presenter;
	private GlobalApplicationState globalAppState;
	
	@Inject
	public WikiSubpagesViewImpl(GlobalApplicationState globalAppState) {
		this.globalAppState = globalAppState;
	}
	
	@Override
	public void configure(TocItem root, final FlowPanel parentContainer, HTMLPanel markdownContainer) {
		clear();
		//this widget shows nothing if it doesn't have any pages!
		TocItem mainPage = (TocItem) root.getChild(0);
		if (mainPage.getChildCount() == 0)
			return;
		addStyleName("bs-sidebar");
		if (parentContainer != null)
			parentContainer.addStyleName("col-xs-12 col-md-3 col-md-push-9");	
		if (markdownContainer != null)
			markdownContainer.addStyleName("col-md-9 col-md-pull-3");	
		
		//only show the tree if the root has children
		if (mainPage.getChildCount() > 0) {
			//traverse the tree, and create anchors
			UnorderedListPanel ul = new UnorderedListPanel();
			ul.addStyleName("notopmargin nav bs-sidenav");
			addTreeItemsRecursive(ul, root.getChildren());
			add(new HTML("<h3>Pages</h3>"));
			add(ul);
		}
	}	
	
	private void addTreeItemsRecursive(UnorderedListPanel ul, List<ModelData> children) {
		for (ModelData modelData : children) {
			TocItem treeItem = (TocItem)modelData;
			String styleName = treeItem.isCurrentPage() ? "active" : "";
			ul.add(getListItem(treeItem), styleName);
			if (treeItem.getChildCount() > 0){
				UnorderedListPanel subList = new UnorderedListPanel();
				subList.addStyleName("nav");
				subList.setVisible(true);
				ul.add(subList);
				addTreeItemsRecursive(subList, treeItem.getChildren());
			}
		}
	}
	
	private Widget getListItem(final TocItem treeItem) {
		Anchor l = new Anchor(treeItem.getText());
		l.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				globalAppState.getPlaceChanger().goTo(treeItem.getTargetPlace());
			}
		});
		return l;
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
}
