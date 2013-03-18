package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.widget.APITableColumnConfig;
import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class APITableColumnRendererDate implements APITableColumnRenderer {

	private DateTimeFormat formatter;
	private SynapseJSNIUtils synapseJSNIUtils;
	private String outputColumnName;
	private Map<String, List<String>> outputColumnData;
	
	@Inject
	public APITableColumnRendererDate(SynapseJSNIUtils synapseJSNIUtils) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		formatter = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
	}
	
	@Override
	public void init(Map<String, List<String>> columnData,
			APITableColumnConfig config,
			AsyncCallback<APITableInitializedColumnRenderer> callback) {
		outputColumnName = APITableWidget.getSingleOutputColumnName(config);
		
		//and precompute the output values
		outputColumnData = new HashMap<String, List<String>>();
		List<String> colValues = columnData.get(APITableWidget.getSingleInputColumnName(config));
		List<String> outputValues = new ArrayList<String>();
		
		for (Iterator iterator2 = colValues.iterator(); iterator2
				.hasNext();) {
			String colValue = (String) iterator2.next();
			Date date = formatter.parse(colValue);
			outputValues.add(synapseJSNIUtils.convertDateToSmallString(date));
		}
		outputColumnData.put(outputColumnName, outputValues);
		
		callback.onSuccess(new APITableInitializedColumnRenderer() {
			
			@Override
			public Map<String, List<String>> getColumnData() {
				return outputColumnData;
			}
			
			@Override
			public List<String> getColumnNames() {
				return APITableWidget.wrap(outputColumnName);
			}
		});
	}
}
