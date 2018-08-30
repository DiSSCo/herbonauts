package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;
import java.util.Map;

public class Cart {

    public class CartQuery {

        public Long hits;
        public Boolean inCart;
        public List<String> selected;
        public Boolean selectedAll;
        public Map<String, String> query;
        public Boolean loaded;
        public Boolean modified;

    }

    @JsonDeserialize(contentAs = CartQuery.class)
    public List<CartQuery> queries;

}
