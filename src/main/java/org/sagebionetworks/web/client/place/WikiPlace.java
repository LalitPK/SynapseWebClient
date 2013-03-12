package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class WikiPlace extends Place {
	
	private String token;	
	
	public WikiPlace(String token) {
		this.token = token;
	}

	public String toToken() {
		return token;
	}
	
	@Prefix("!WikiPlace")
	public static class Tokenizer implements PlaceTokenizer<WikiPlace> {
        @Override
        public String getToken(WikiPlace place) {
            return place.toToken();
        }

        @Override
        public WikiPlace getPlace(String token) {
            return new WikiPlace(token);
        }
    }

}

