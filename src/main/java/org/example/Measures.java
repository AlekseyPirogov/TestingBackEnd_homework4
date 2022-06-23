package org.example;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "original",
        "metric",
        "us"
})
@Generated("jsonschema2pojo")
public class Measures {

    @JsonProperty("original")
    private Original original;
    @JsonProperty("metric")
    private Metric metric;
    @JsonProperty("us")
    private Us us;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("original")
    public Original getOriginal() {
        return original;
    }

    @JsonProperty("original")
    public void setOriginal(Original original) {
        this.original = original;
    }

    @JsonProperty("metric")
    public Metric getMetric() {
        return metric;
    }

    @JsonProperty("metric")
    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    @JsonProperty("us")
    public Us getUs() {
        return us;
    }

    @JsonProperty("us")
    public void setUs(Us us) {
        this.us = us;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
