
package org.imsglobal.lti2.objects;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.imsglobal.lti2.LTI2Config;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "@id",
    "service_provider_name",
    "description",
    "timestamp",
    "support"
})
public class Service_provider {

    @JsonProperty("@id")
    private String _id;
    @JsonProperty("service_provider_name")
    private Service_provider_name service_provider_name;
    @JsonProperty("description")
    private Description description;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("support")
    private Support support;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Service_provider(LTI2Config cnf) {
        this._id = cnf.getService_provider_id();
        this.service_provider_name = new Service_provider_name(cnf.getService_provider_provider_name());
        this.description = new Description(cnf.getService_provider_description());
        this.support = new Support(cnf.getService_provider_support_email());
    }

    @JsonProperty("@id")
    public String get_id() {
        return _id;
    }

    @JsonProperty("@id")
    public void set_id(String _id) {
        this._id = _id;
    }

    @JsonProperty("service_provider_name")
    public Service_provider_name getService_provider_name() {
        return service_provider_name;
    }

    @JsonProperty("service_provider_name")
    public void setService_name(Service_provider_name service_provider_name) {
        this.service_provider_name = service_provider_name;
    }

    @JsonProperty("description")
    public Description getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(Description description) {
        this.description = description;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("support")
    public Support getSupport() {
        return support;
    }

    @JsonProperty("support")
    public void setSupport(Support support) {
        this.support = support;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
