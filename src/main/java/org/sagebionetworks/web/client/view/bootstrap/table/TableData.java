package org.sagebionetworks.web.client.view.bootstrap.table;

import org.gwtbootstrap3.client.ui.base.ComplexWidget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;

/**
 * Simple <td>
 * @author jmhill
 *
 */
public class TableData extends ComplexWidget{

	public TableData() {
        setElement(Document.get().createTDElement());
    }
    
}
